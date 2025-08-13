package group5.backend.service.imagetranslate;

import group5.backend.dto.translate.menu.MenuDetectResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MenuBoardDetector {

    // 가격: 14,000 / 9000 / 1.2만 / 1만2천 / 1만 2천 / 1만2000 (+선택)원
    private static final Pattern PRICE = Pattern.compile("(?:\\d{1,3}(?:,\\d{3})+|\\d{4,6}|\\d+(?:\\.\\d+)?\\s?만|\\d+\\s?만\\s?\\d{1,4}\\s?천?)\\s?원?");
    // 수량/단위(한글+영문 혼용)
    private static final Pattern COUNT_UNIT = Pattern.compile("(\\d+\\s?(인|인분|개|잔|병|캔|pcs?|피스|그릇|접시|pc))", Pattern.CASE_INSENSITIVE);
    // 음료 용량/사이즈(ml/L/cl/oz, S/M/L 등)
    private static final Pattern VOLUME_SIZE = Pattern.compile("(?:(\\d{2,4})\\s?(?:ml|mL|㎖|L|ℓ|cl|oz))|\\b(S|M|L|XL)\\b|\\b(스몰|미디엄|라지)\\b|\\b더블샷\\b|\\b트리플샷\\b", Pattern.CASE_INSENSITIVE);
    // 알코올 도수: 4.5도 / 12% / ABV / proof
    private static final Pattern ABV = Pattern.compile("(?:\\d{1,2}(?:\\.\\d)?\\s?%|\\d{1,2}(?:\\.\\d)?\\s?도|\\bABV\\b|\\bproof\\b)", Pattern.CASE_INSENSITIVE);

    // 리소스에서 키워드 로드 (중복 자동 제거)
    private static final Set<String> MENU_KEYWORDS   = loadKeywords("static/menu_keywords/menu_keywords.txt");
    private static final Set<String> SECTION_HINTS   = loadKeywords("static/menu_keywords/section_hints.txt");

    private final VisionApiClient vision;

    private static Set<String> loadKeywords(String classpathLocation) {
        try (InputStream is = MenuBoardDetector.class.getClassLoader().getResourceAsStream(classpathLocation)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found: " + classpathLocation);
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                return br.lines()
                        .map(String::trim)
                        .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load keywords from " + classpathLocation, e);
        }
    }

    public MenuDetectResult detect(byte[] imageBytes) throws Exception {
        List<String> lines = vision.extractLines(imageBytes);
        if (lines.isEmpty()) {
            return new MenuDetectResult(false, 0.0, List.of("no_text"));
        }

        int n = lines.size();
        int priceHits = 0;
        int wonEnd = 0;
        int sectionHits = 0;
        int keywordHits = 0;      // 통합 키워드 매칭 수
        int countUnitHits = 0;
        int volumeSizeHits = 0;
        int abvHits = 0;
        int priceOnlyLines = 0;

        for (String raw : lines) {
            String s = raw == null ? "" : raw.trim();
            if (s.isEmpty()) continue;

            // 가격/원 말미
            if (PRICE.matcher(s).find()) priceHits++;
            if (s.endsWith("원") || s.matches(".*\\d\\s?만(?:\\s?\\d{1,4}\\s?천?)?$")) wonEnd++;

            // 섹션 헤더
            String up = s.toUpperCase();
            for (String sec : SECTION_HINTS) { if (up.contains(sec)) { sectionHits++; break; } }

            // 통합 키워드
            for (String k : MENU_KEYWORDS) { if (s.contains(k)) { keywordHits++; break; } }

            // 수량/용량/도수
            if (COUNT_UNIT.matcher(s).find()) countUnitHits++;
            if (VOLUME_SIZE.matcher(s).find()) volumeSizeHits++;
            if (ABV.matcher(s).find()) abvHits++;

            // 가격 전용 라인
            String noSpace = s.replaceAll("\\s+","");
            if (PRICE.matcher(noSpace).matches()) priceOnlyLines++;
        }

        double priceRatio  = priceHits / (double) n;
        double wonEndRatio = wonEnd   / (double) n;

        double sPrice      = clamp01(priceRatio);
        double sWonEnd     = clamp01(wonEndRatio);
        double sSection    = clamp01(Math.log1p(sectionHits)   / Math.log(11));
        double sKeyword    = clamp01(Math.log1p(keywordHits)   / Math.log(11));
        double sCnt        = clamp01(Math.log1p(countUnitHits) / Math.log(11));
        double sVol        = clamp01(Math.log1p(volumeSizeHits)/ Math.log(11));
        double sAbv        = clamp01(Math.log1p(abvHits)       / Math.log(11));
        double sPriceOnly  = clamp01(priceOnlyLines/(double)n);

        double score =
                0.40*sPrice   + 0.08*sWonEnd +
                        0.12*sSection + 0.14*sKeyword +
                        0.06*sCnt     + 0.05*sVol +
                        0.03*sAbv     + 0.12*sPriceOnly;

        boolean hardRule = (priceOnlyLines >= 4 && sectionHits >= 1) || (priceHits >= 6 && priceRatio >= 0.28);

        double threshold = 0.45;
        boolean isMenu = hardRule || score >= threshold;

        return new MenuDetectResult(
                isMenu,
                clamp01(score),
                List.of(
                        "priceHits=" + priceHits,
                        "priceRatio=" + String.format("%.2f", priceRatio),
                        "wonEndRatio=" + String.format("%.2f", wonEndRatio),
                        "sectionHits=" + sectionHits,
                        "keywordHits=" + keywordHits,
                        "countUnits=" + countUnitHits,
                        "volumeSizeHits=" + volumeSizeHits,
                        "abvHits=" + abvHits,
                        "priceOnlyLines=" + priceOnlyLines
                )
        );
    }

    private static double clamp01(double v) {
        return Math.max(0d, Math.min(1d, v));
    }
}
