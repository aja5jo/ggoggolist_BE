package group5.backend.service.imagetranslate;

import group5.backend.dto.translate.menu.MenuDetectResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
public class MenuBoardDetector {

    // 가격: 14,000 / 9000 / 1.2만 / 1만2천 / 1만 2천 / 1만2000 (+선택)원
    private static final Pattern PRICE = Pattern.compile("(?:\\d{1,3}(?:,\\d{3})+|\\d{4,6}|\\d+(?:\\.\\d+)?\\s?만|\\d+\\s?만\\s?\\d{1,4}\\s?천?)\\s?원?");
    // 한국형 수량/단위
    private static final Pattern COUNT_UNIT = Pattern.compile("(\\d+\\s?(인|인분|개|잔|병|캔|pcs|피스|그릇|접시))");

    // 한국어 접미사(음식 유형) 가점
    private static final Set<String> KO_SUFFIX = Set.of(
            "국수","칼국수","면","라면","우동","소바","냉면","막국수","수제비","비빔면",
            "덮밥","볶음밥","비빔밥","쌈밥","초밥","김밥","회","전","파전","부침개",
            "찌개","탕","국","전골","찜","구이","볶음","튀김","꼬치",
            "돈까스","돈카츠","만두","떡볶이","순대","보쌈","수육","감자탕",
            "해장국","설렁탕","곰탕","죽","제육","제육덮밥","카레"
    );

    // 섹션(메뉴판 헤더) 힌트
    private static final Set<String> SECTION_HINTS = Set.of(
            "대표메뉴","한그릇메뉴","세트메뉴","점심특선","추천","인기",
            "추가","면추가","공기밥","사이드","토핑","주류","음료","디저트",
            "포장","포장가능","테이크아웃","모든 메뉴 포장 가능"
    );

    private final VisionApiClient vision;

    public MenuDetectResult detect(byte[] imageBytes) throws Exception {
        List<String> lines = vision.extractLines(imageBytes);
        if (lines.isEmpty()) {
            return new MenuDetectResult(false, 0.0, List.of("no_text"));
        }

        int n = lines.size();
        int priceHits = 0;
        int wonEnd = 0;
        int suffixHits = 0;
        int sectionHits = 0;
        int countUnitHits = 0;

        for (String raw : lines) {
            String s = raw.trim();
            if (PRICE.matcher(s).find()) priceHits++;
            if (s.endsWith("원") || s.matches(".*\\d\\s?만(?:\\s?\\d{1,4}\\s?천?)?$")) wonEnd++;

            for (String suf : KO_SUFFIX) {
                if (s.contains(suf)) {
                    suffixHits++;
                    break;
                }
            }
            for (String sec : SECTION_HINTS) {
                if (s.contains(sec)) {
                    sectionHits++;
                    break;
                }
            }
            if (COUNT_UNIT.matcher(s).find()) countUnitHits++;
        }

        double priceRatio = priceHits / (double) n;
        double wonEndRatio = wonEnd / (double) n;

        double sPrice  = clamp01(priceRatio);                              // 가장 중요
        double sWonEnd = clamp01(wonEndRatio);
        double sSuffix = clamp01(Math.log1p(suffixHits)  / Math.log(11));
        double sSect   = clamp01(Math.log1p(sectionHits) / Math.log(11));
        double sCnt    = clamp01(Math.log1p(countUnitHits)/ Math.log(11));

        // 한국 메뉴판 가중치
        double score = 0.50*sPrice + 0.20*sWonEnd + 0.12*sSuffix + 0.10*sSect + 0.08*sCnt;

        // 하드룰: 가격 라인이 충분하면 무조건 메뉴판
        boolean hardRule = (priceHits >= 5 && priceRatio >= 0.28);
        double threshold = 0.45;

        boolean isMenu = hardRule || score >= threshold;

        return new MenuDetectResult(
                isMenu,
                clamp01(score),
                List.of(
                        "priceHits=" + priceHits,
                        "priceRatio=" + String.format("%.2f", priceRatio),
                        "wonEndRatio=" + String.format("%.2f", wonEndRatio),
                        "suffixHits=" + suffixHits,
                        "sectionHits=" + sectionHits,
                        "countUnits=" + countUnitHits
                )
        );
    }

    private static double clamp01(double v) {
        return Math.max(0d, Math.min(1d, v));
    }
}
