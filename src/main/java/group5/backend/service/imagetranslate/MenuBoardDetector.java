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
    // 수량/단위(한글+영문 혼용)
    private static final Pattern COUNT_UNIT = Pattern.compile("(\\d+\\s?(인|인분|개|잔|병|캔|pcs?|피스|그릇|접시|pc))", Pattern.CASE_INSENSITIVE);

    // 한글 접미사(음식 유형) - 중복 제거 확장판
    private static final Set<String> KO_SUFFIX = Set.of(
            // 면·국수류
            "면","국수","잔치국수","비빔국수","메밀국수","칼국수","수제비","쫄면",
            "라면","우동","소바","냉면","막국수","짜장면","짬뽕","콩국수","수타면","쌀국수",

            // 밥·식사 구성
            "밥","덮밥","볶음밥","비빔밥","쌈밥","솥밥","돌솥","주먹밥","국밥",
            "백반","정식","한정식","도시락","한상","모둠","세트","코스","특선","플래터","오마카세",

            // 탕·국·찌개·전골
            "탕","국","찌개","전골","해장국","설렁탕","곰탕","육개장","추어탕","매운탕","뼈해장국",
            "순두부","청국장","콩비지",

            // 조리 방식·한식 카테고리
            "구이","찜","조림","볶음","튀김","무침","강정","주물럭","샤브샤브","나베",
            "전","파전","부침개","전병","빈대떡","육전","꼬치",

            // 해산물·회류
            "회","물회","숙회","초회","게장",

            // 고기·부속·메인 카테고리
            "불고기","보쌈","수육","제육","닭갈비","찜닭","닭발","치킨",
            "삼겹살","갈비","등갈비","족발","막창","곱창","대창",

            // 분식·간식·사이드
            "만두","떡볶이","순대","어묵","오뎅","김밥","초밥","죽",
            "빙수","팥빙수","화채","나물","장아찌","젓갈","샐러드","쌈","카레"
    );


    // 섹션 헤더(한/영 혼용: 한국어 메뉴판에 영어 섹션 자주 등장)
    private static final Set<String> SECTION_HINTS = Set.of(
            // 한글
            "대표메뉴","한그릇메뉴","세트메뉴","점심특선","추천","인기",
            "추가","면추가","공기밥","사이드","토핑","주류","음료","디저트",
            "포장","포장가능","테이크아웃","모든 메뉴 포장 가능",
            // 영문
            "SUSHI","SASHIMI","DRINK","DRINKS","BEVERAGE","BEVERAGES",
            "SET","SET A","SET B","SET C","SET D","A SET","B SET","C SET","D SET",
            "LUNCH SET","DINNER SET","COMBO","COURSE"
    );

    private static final Set<String> INTL_FOOD_HINTS = Set.of(
            // 일본
            "스시","초밥","사시미","회","니기리","마키","군함","군함말이","우동","라멘","소바","돈부리",
            "가츠","가스","돈카츠","카레","덮밥","텐푸라","튀김","규동","오야코동","가이센동",
            "차완무시","오코노미야키","타코야키","야키토리","모찌","단고","미소","미소시루","츠케멘",
            "샤부샤부","스키야키","하야시라이스","에비후라이","카츠동","히야시츄카","아게다시도후",
            "니쿠자가","야키소바","고로케",

            // 중국
            "딤섬","군만두","물만두","완탕","바오","마파두부","짜장","짬뽕","탕수육","볶음밥","양꼬치",
            "깐풍기","팔보채","고추잡채","칠리새우","홍쇼로우","훠궈","라즈지","꿔바로우","멘보샤",
            "춘권","유린기","산라탕","마라탕","마라샹궈","차슈","단탄면","우육면","광동식바베큐",
            "북경오리","궁보계정","샤오롱바오","소롱포","지삼쓰","퉁수이","양장피","즈란양러우",

            // 이탈리아
            "파스타","스파게티","라자냐","피자","마르게리타","페퍼로니","리조또","뇨끼","토르텔리니",
            "푸실리","카르보나라","알리오올리오","볼로네제","미네스트로네","프로슈토","브루스케타",
            "카프레제","티라미수","젤라또","포카치아","아란치니","칼조네","리몬첼로","파르미자나",
            "카놀리","판나코타","스트라치아텔라","올리브","소프리토","바냐카우다","카포나타",

            // 프랑스
            "크루아상","바게트","퀴시","라따뚜이","부야베스","포토푀","크레프","에스카르고","파테",
            "그라탕","수플레","무슬","마카롱","브리","카망베르","퐁듀","가토","에끌레어","프티푸르",
            "마들렌","브리오슈","타르트","타르트 타탱","클라푸티","크렘브륄레","크렘카라멜",
            "오페라케이크","밀푀유","파리브레스트","콩피드카나르","카수레","코크오뱅","뵈프부르기뇽",
            "살라드니수아즈","프와그라","갈레트","팽오쇼콜라","바바오럼","크레페수제트","오니옹수프",
            "로크포르","몽도르","브루고뉴에스카르고","사블레","바스크케이크","바스크치킨","피카르디플라멩",

            // 멕시코
            "타코","부리토","퀘사디야","엔칠라다","나초","파히타","살사","과카몰리","토스타다",
            "타말레","치미창가","포솔레","카르네아사다","엘로테","세비체","소파피야","찰루파","몰레",
            "코치니타피빌","알람브레스","카르니타스","멘도","비리아","멕시칸라이스","프리홀레","멕시칸프라이즈",
            "피코데가요","케소푸디도","멘도","후에보스란체로스","소페","고르디타","펀차","멕시칸핫초코",
            "멕시칸콘","아도보","칠리콩카르네","칠레렐레노","칠레엔노가다","캄페차노","파스토르","트리파",
            "바하피시타코","알람브레스","카마로네스알아히요","칼도데레슬","피칸테소스","멘도포조",

            // 중동
            "타진","쿠스쿠스","후무스","파라펠","샤와르마","케밥","피타","라브네","바클라바","파티르",
            "만사프","샤크슈카","무탑발","코프타","사프란라이스","마크루드",

            // 베트남
            "쌀국수","퍼","반미","분짜","짜조","분보남보","고이꾸온","반쎄오","카페쓰어다","퍼가",

            // 태국
            "팟타이","쏨땀","똠얌꿍","카오팟","카오만까이","팟카파오","옐로커리","그린커리","레드커리","망고밥",

            // 인도
            "커리","마살라","탄두리","난","로티","사모사","비리야니","팔락파니르","치킨티카","달",
            "라씨","알루고비","바터치킨","마토르파니르","코르마",

            // 미국
            "햄버거","핫도그","바비큐","립","치즈버거","애플파이","브라우니","팬케이크","도넛","프라이드치킨",

            // 스페인
            "빠에야","감바스알아히요","또르티야데파타타","초리소","하몬","크로켓","가스파초","산그리아","추로스",

            // 독일
            "소시지","브라트부어스트","프레첼","슈니첼","사우어크라우트","아펠슈트루델","커리부어스트",
            "카르토펠살라트","슈바인스학센","비스마르크헤링",

            // 터키·그리스
            "기로스","수블라키","무사카","돌마","사츠지키","타브불레","메제",

            // 브라질
            "슈하스코","페이조아다","파스텔","브리가데이로","카이피리냐"
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
        int intlFoodHits = 0;
        int priceOnlyLines = 0;

        for (String raw : lines) {
            String s = raw.trim();
            if (PRICE.matcher(s).find()) priceHits++;
            if (s.endsWith("원") || s.matches(".*\\d\\s?만(?:\\s?\\d{1,4}\\s?천?)?$")) wonEnd++;

            for (String suf : KO_SUFFIX) { if (s.contains(suf)) { suffixHits++; break; } }
            String up = s.toUpperCase();
            for (String sec : SECTION_HINTS) { if (up.contains(sec)) { sectionHits++; break; } }
            for (String h : INTL_FOOD_HINTS) { if (s.contains(h)) { intlFoodHits++; break; } }
            if (COUNT_UNIT.matcher(s).find()) countUnitHits++;

            String noSpace = s.replaceAll("\\s+","");
            if (PRICE.matcher(noSpace).matches()) priceOnlyLines++;
        }

        double priceRatio   = priceHits / (double) n;
        double wonEndRatio  = wonEnd   / (double) n;

        double sPrice      = clamp01(priceRatio);                               // 핵심
        double sWonEnd     = clamp01(wonEndRatio);
        double sSuffix     = clamp01(Math.log1p(suffixHits)   / Math.log(11));
        double sSect       = clamp01(Math.log1p(sectionHits)  / Math.log(11));
        double sIntl       = clamp01(Math.log1p(intlFoodHits) / Math.log(11));
        double sCnt        = clamp01(Math.log1p(countUnitHits)/ Math.log(11));
        double sPriceOnly  = clamp01(priceOnlyLines/(double)n);

        // 한글 메뉴판 + 일/중/양식 혼합 대응 가중치
        double score = 0.45*sPrice + 0.10*sWonEnd + 0.12*sSect + 0.12*sIntl + 0.11*sPriceOnly + 0.10*sSuffix + 0.10*sCnt;

        // 하드룰: 가격 전용 라인 + 섹션 존재 OR 가격 비율 충분
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
                        "intlFoodHits=" + intlFoodHits,
                        "priceOnlyLines=" + priceOnlyLines,
                        "suffixHits=" + suffixHits,
                        "countUnits=" + countUnitHits
                )
        );
    }

    private static double clamp01(double v) {
        return Math.max(0d, Math.min(1d, v));
    }
}