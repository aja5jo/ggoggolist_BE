package group5.backend.service.imagetranslate;
import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.dto.translate.menu.MenuItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MenuBoardTranslateService {

    // 가격(한 줄 버전)
    private static final Pattern PRICE = Pattern.compile("(?:\\d{1,3}(?:,\\d{3})+|\\d{4,6}|\\d+(?:\\.\\d+)?\\s?만|\\d+\\s?만\\s?\\d{1,4}\\s?천?)\\s?원?");
    // 수량/옵션
    private static final Pattern COUNT_UNIT = Pattern.compile("(\\d+\\s?(인|인분|개|잔|병|캔|pcs?|피스|그릇|접시|pc))", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAREN_OPT  = Pattern.compile("\\(([^)]+)\\)");

    private static final Set<String> OPTION_HINTS = Set.of(
            "곱빼기","대","소","라지","레귤러","추가","면추가","밥추가","세트","여름한정","한정","핫","아이스"
    );

    // 섹션 헤더(한/영 혼용)
    private static final Set<String> SECTION_HINTS = Set.of(
            "대표메뉴","한그릇메뉴","세트메뉴","점심특선","추천","인기",
            "추가","면추가","공기밥","사이드","토핑","주류","음료","디저트",
            "포장","포장가능","테이크아웃","모든 메뉴 포장 가능",
            "SUSHI","SASHIMI","DRINK","DRINKS","BEVERAGE","BEVERAGES",
            "SET","SET A","SET B","SET C","SET D","A SET","B SET","C SET","D SET",
            "LUNCH SET","DINNER SET","COMBO","COURSE"
    );

    // OCR 흔한 오인식 교정(필요 시 확장)
    private static final Map<String,String> CONFUSABLES = Map.of(
            "Λ","A","Η","H","Β","B","Ο","O","Ι","I","С","C","Т","T"
    );
    private static final Map<String,String> OCR_COMMON = Map.of(
            "징어","오징어", "주주리","주류", "막걸","막걸리"
    );

    private final VisionApiClient vision;
    private final TranslationApiClient translate;

    public List<MenuItem> translateMenuBoard(byte[] imageBytes, SupportedLanguage targetLang) throws Exception {
        List<String> lines = vision.extractLines(imageBytes);
        if (lines.isEmpty()) return List.of();

        List<RawItem> rawItems = parseLinesToItems(lines);

        List<String> names = new ArrayList<>(rawItems.size());
        for (RawItem it : rawItems) names.add(it.menuName);

        List<String> translated = translate.translateBatch(names, targetLang.code());

        List<MenuItem> out = new ArrayList<>(rawItems.size());
        for (int i = 0; i < rawItems.size(); i++) {
            RawItem r = rawItems.get(i);
            String tName = translated.get(i) == null ? "" : translated.get(i);
            MenuItem dto = MenuItem.builder()
                    .section(r.section.isBlank() ? null : r.section)
                    .originalName(r.menuName)
                    .translatedName(tName)
                    .price(r.price)
                    .option(r.option)
                    .build();
            out.add(dto);
        }
        return out;
    }

    // ---------------- parsing ----------------

    private List<RawItem> parseLinesToItems(List<String> rawLines) {
        List<RawItem> items = new ArrayList<>();
        String currentSection = "";

        for (int i = 0; i < rawLines.size(); i++) {
            String line = clean(rawLines.get(i));
            if (line.isBlank()) continue;

            // 섹션 헤더
            if (isSectionHeader(line)) {
                currentSection = stripIcons(line);
                continue;
            }

            // 노이즈 컷
            if (isNoise(line)) continue;

            // 같은 줄 가격
            String price = findPrice(line);
            String name = price.isEmpty() ? line : line.replace(price, "").trim();

            // 다음 줄 가격만 있으면 흡수
            if (price.isEmpty() && i + 1 < rawLines.size()) {
                String next = clean(rawLines.get(i + 1));
                if (looksLikePriceOnly(next)) {
                    price = findPrice(next);
                    i++; // 소비
                }
            }

            // 옵션 추출/제거
            String option = extractOptionAgg(name);
            name = removeOptionsFromName(name);

            if (name.length() < 2) continue;

            String normPrice = normalizePrice(price);
            items.add(new RawItem(currentSection, name, normPrice, option));
        }

        // 연속 중복 합치기(가격 최신값 우선)
        List<RawItem> merged = new ArrayList<>();
        String prevName = "";
        for (RawItem it : items) {
            if (!it.menuName.equals(prevName)) {
                merged.add(it);
                prevName = it.menuName;
            } else {
                RawItem last = merged.get(merged.size() - 1);
                if (last.price.isBlank() && !it.price.isBlank()) {
                    merged.set(merged.size() - 1, new RawItem(last.section, last.menuName, it.price, last.option));
                }
            }
        }
        return merged;
    }

    // ---------------- helpers ----------------

    private String clean(String s) {
        String t = s == null ? "" : s.trim();
        t = fixConfusables(t);
        t = fixCommonErrors(t);
        t = t.replaceAll("[.·•]{2,}", " ");     // 점선 제거
        t = t.replaceAll("\\s+", " ").trim();
        return t;
    }

    private boolean isSectionHeader(String s) {
        String up = s.toUpperCase();
        for (String h : SECTION_HINTS) if (up.contains(h)) return true;
        return false;
    }

    private boolean isNoise(String s) {
        String core = s.replaceAll("[\\p{Punct}\\p{S}\\s]+", "");
        if (core.isBlank()) return true;            // 기호뿐
        if (core.length() <= 1) return true;        // 한 글자
        if (s.matches("^[A-Za-z]$")) return true;   // 단일 영문
        if (s.matches("^\\(?\\)?$")) return true;   // () 만
        if (s.matches("^\\d{1,2}원$")) return true; // 2자리 이하는 노이즈
        return false;
    }

    private String stripIcons(String s) {
        return s.replaceAll("[※*•◆▶▷◀◁\\[\\]{}()]+", " ").replaceAll("\\s+", " ").trim();
    }

    private boolean looksLikePriceOnly(String s) {
        String t = s.replaceAll("\\s", "");
        Matcher m = PRICE.matcher(t);
        return m.matches();
    }

    private String findPrice(String s) {
        Matcher m = PRICE.matcher(s);
        return m.find() ? m.group().trim() : "";
    }

    private String extractOptionAgg(String s) {
        StringBuilder opt = new StringBuilder();
        Matcher m = PAREN_OPT.matcher(s);
        while (m.find()) {
            String in = m.group(1).trim();
            if (!in.isBlank()) opt.append(in).append(" ");
        }
        for (String h : OPTION_HINTS) {
            if (s.contains(h)) opt.append(h).append(" ");
        }
        if (COUNT_UNIT.matcher(s).find()) {
            opt.append("수량표기").append(" ");
        }
        return opt.toString().trim();
    }

    private String removeOptionsFromName(String s) {
        String out = s.replaceAll("\\([^)]*\\)", " ");
        for (String h : OPTION_HINTS) out = out.replace(h, " ");
        out = out.replaceAll("\\s+", " ").trim();
        return out;
    }

    private String normalizePrice(String p) {
        if (p == null || p.isBlank()) return "";
        String s = p.replaceAll("\\s+", "");
        // 1) 1.2만 / 1만 / 1만2천 / 1만2000
        if (s.matches("\\d+(?:\\.\\d+)?만(?:\\d{1,4}천?)?")) {
            long won = 0L;
            Matcher m1 = Pattern.compile("(\\d+(?:\\.\\d+)?)만").matcher(s);
            if (m1.find()) {
                double man = Double.parseDouble(m1.group(1));
                won += Math.round(man * 10000);
            }
            Matcher m2 = Pattern.compile("만(\\d{1,4})천?").matcher(s);
            if (m2.find()) {
                won += Integer.parseInt(m2.group(1)) * 1000L;
            }
            if (won > 0) return formatWon(won);
        }
        // 2) 숫자/콤마형
        String digits = s.replaceAll("[^0-9]", "");
        if (!digits.isBlank()) {
            try {
                long v = Long.parseLong(digits);
                if (v < 100) return ""; // 201원 같은 노이즈 컷
                return formatWon(v);
            } catch (NumberFormatException ignore) { }
        }
        return p.trim();
    }

    private String formatWon(long v) {
        String withComma = String.valueOf(v).replaceAll("(\\d)(?=(\\d{3})+$)", "$1,");
        return withComma + "원";
    }

    private String fixConfusables(String s) {
        String out = s;
        for (Map.Entry<String, String> e : CONFUSABLES.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }
        return out;
    }

    private String fixCommonErrors(String s) {
        String out = s;
        for (Map.Entry<String, String> e : OCR_COMMON.entrySet()) {
            out = out.replace(e.getKey(), e.getValue());
        }
        return out;
    }

    // 내부 구조체
    private static class RawItem {
        final String section;
        final String menuName;
        final String price;
        final String option;

        RawItem(String section, String menuName, String price, String option) {
            this.section = section == null ? "" : section;
            this.menuName = menuName == null ? "" : menuName;
            this.price = price == null ? "" : price;
            this.option = option == null ? "" : option;
        }
    }
}