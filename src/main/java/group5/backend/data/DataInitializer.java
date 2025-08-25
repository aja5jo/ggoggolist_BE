package group5.backend.data;

import com.github.javafaker.Faker;
import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.Category;
import group5.backend.domain.user.Role;
import group5.backend.domain.user.User;
import group5.backend.repository.EventRepository;
import group5.backend.repository.PopupRepository;
import group5.backend.repository.StoreRepository;
import group5.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import group5.backend.service.ai.recomm.EmbeddingPreloadService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmbeddingPreloadService embeddingPreloadService;

    @PersistenceContext private EntityManager em;

    // ─────────────────────────────
    // SEED 스키마
    // ─────────────────────────────
    private record SeedData(
            String imageUrl,
            String storeName,
            String storeIntro,
            String contentTitle,
            String contentIntro,
            String contentDescription,
            String type,        // "event" 또는 "popup"
            Category category   // CAFE, FOOD, SHOPPING, ENTERTAINMENT, K_POP, CLUB, ETC
    ) {}

    // ─────────────────────────────
    // 34개 SEED 데이터 (요청한 그대로 포함)
    // ─────────────────────────────
    private static final List<SeedData> SEED_DATA = List.of(
            // 1) FOOD 피자 라인업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/bf40a532-e492-4488-b604-d7d595ef0105.jpg",
                    "몬스터 피자 하우스", "취향대로 즐기는 수제 피자 전문점",
                    "몬스터 피자 페스티벌", "데리치킨·미트러버·알로하까지 총출동",
                    "2판 세트 구성 시 10% 할인, 신메뉴 시식 쿠폰 제공",
                    "event", Category.FOOD
            ),

            // 2) FOOD 북아프리카 요리
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/43904457-6c5d-42c2-b18c-fa435a1712dd.jpg",
                    "마그레브 다이닝", "모로코·튀니지 정통 요리를 즐길 수 있는 레스토랑",
                    "모로칸 위크", "쿠스쿠스와 타진으로 즐기는 마그레브 한 상",
                    "2인 코스 주문 시 민트티 무료 제공",
                    "event", Category.FOOD
            ),

            // 3) FOOD 중식당 메뉴판
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/2ee0245d-7c35-4f19-a607-d0f03883892f.jpg",
                    "XO 차이니즈", "해산물과 소스를 활용한 모던 중식",
                    "XO 해산물 프로모션", "칠리새우와 전복볶음을 특별가에",
                    "2인 이상 주문 시 해산물 탕 서비스",
                    "event", Category.FOOD
            ),

            // 4) FOOD 타진 요리
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/7b22312f-5e41-4230-9e35-054fef654b29.jpg",
                    "지중해 하우스", "허브와 채소로 맛을 낸 모로칸 퓨전",
                    "허브 치킨 타진 데이", "지중해 허브와 채소로 끓인 정통 타진",
                    "저녁 타임 한정 20인분",
                    "event", Category.FOOD
            ),

            // 5) FOOD 고르곤졸라 피자
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/59f7342d-97f4-406e-9618-fd6077e3c2e3.jpg",
                    "라코타 피자", "치즈와 견과류가 어우러진 수제 화덕피자",
                    "고르곤졸라 허니 피자 스페셜", "견과 토핑과 꿀 페어링",
                    "샐러드 세트 주문 시 2천원 할인",
                    "event", Category.FOOD
            ),

            // 6) FOOD 짜장면
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/ea35634a-6ff6-47f9-b9da-a4aee9f99d70.jpg",
                    "차이나 팔레트", "직접 뽑은 생면으로 만드는 중화요리",
                    "수타면 짜장 프로젝트", "탱글한 생면과 춘장의 정석 조합",
                    "곱빼기 무료 업그레이드 타임 운영",
                    "event", Category.FOOD
            ),

            // 7) CAFE 아메리카노
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/004de6df-8af5-4b98-9611-970ea694ea6b.jpg",
                    "카페 도로시", "합리적인 가격대의 스페셜티 커피",
                    "더블 아메리카노 1+1", "산미 균형 잡힌 블렌드 아이스커피",
                    "오전 10–12시 테이크아웃 한정 1+1",
                    "event", Category.CAFE
            ),

            // 8) CAFE 메뉴판
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/29c6b1a9-8dee-4b1a-8f60-998b6214dcac.jpg",
                    "하우스 커피", "가성비 좋은 에스프레소 바",
                    "메뉴 리뉴얼 오픈", "합리적인 가격의 리뉴얼 오픈",
                    "시그니처 크림라떼/복숭아 아이스티 신메뉴 출시",
                    "event", Category.CAFE
            ),

            // 9) CAFE 파스텔 외관
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/7c78913a-a515-4b0a-a099-650b8e2ad245.jpg",
                    "코티지 가든", "파스텔톤 감성 외관과 정원",
                    "코티지 포토스팟", "파스텔 벽과 올리브 나무 감성",
                    "주말 한정 폴라로이드 이벤트",
                    "popup", Category.CAFE
            ),

            // 10) CAFE 야경 창가
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/8238625f-4b98-447f-bac5-b2e12b92a72e.jpg",
                    "윈도우 카페", "야경을 즐길 수 있는 도심 속 라운지",
                    "윈도우 나이트 라운지", "잔잔한 플레이리스트와 야경",
                    "19–22시 디저트 페어링 세트 할인",
                    "event", Category.CAFE
            ),

            // 11) CAFE 실내 플랜트
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/69a5f2bf-733f-4759-ac42-912540c303bd.jpg",
                    "플랜트 카페", "식물과 커피가 어우러진 공간",
                    "플랜트 인테리어 클래스", "화분과 커피 향이 공존",
                    "초보자도 쉽게 만드는 플랜트 클래스",
                    "popup", Category.CAFE
            ),

// 12) POPUP_etc_1 → 스탠리 물통 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/73434c86-0420-4921-98f6-404fe071072d.jpg",
                    "스탠리 하이드레이션 존", "스탠리 물통과 텀블러를 만나볼 수 있는 공간",
                    "스탠리 팝업 스토어", "신상 텀블러와 한정 컬러 전시",
                    "현장 구매 고객 한정 스티커팩 증정",
                    "popup", Category.ETC
            ),

// 13) POPUP_shopping_1 → 포켓몬 샵 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/cbbf227a-5683-466c-90b3-eb07cb7937dc.jpg",
                    "포켓몬 샵", "포켓몬 굿즈와 피규어 전문 공간",
                    "포켓몬 팝업 스토어", "인형·피규어·생활 소품 판매",
                    "특정 금액 이상 구매 시 포켓몬 한정 카드 증정",
                    "popup", Category.SHOPPING
            ),

// 14) POPUP_shopping_2 → 산리오 샵 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/c26f2330-cf96-4a43-aed1-6cd6df161c62.jpg",
                    "산리오 프렌즈 숍", "헬로키티·마이멜로디·쿠로미 굿즈 전문",
                    "산리오 팝업 스토어", "인형·문구·라이프스타일 굿즈",
                    "팝업 한정 굿즈 구매 시 캐릭터 포토카드 증정",
                    "popup", Category.SHOPPING
            ),

// 16) POPUP_food_1 → 와인 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/8c893423-0613-45f0-b265-4a2705de31c0.jpg",
                    "빈야드 와인 팝업", "세계 각국 와인을 시음할 수 있는 체험 공간",
                    "와인 셀렉션 팝업", "레드·화이트·스파클링 와인 시음",
                    "구매 고객 대상 소믈리에 추천 페어링 노트 제공",
                    "popup", Category.FOOD
            ),

// 17) POPUP_food_2 → 농심 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/ba2aa465-7cc0-4973-9b84-8830f7060a2b.jpg",
                    "농심 푸드 팝업", "라면·스낵 브랜드의 특별 체험존",
                    "농심 브랜드 위크", "신제품 시식과 한정 굿즈 전시",
                    "참여 고객 대상 미니 라면 증정 이벤트",
                    "popup", Category.FOOD
            ),

// 18) POPUP_kpop_1 → RIIZE 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/c9efbd8a-d6fe-4dfb-8cfd-eda223cf66de.jpg",
                    "RIIZE 팝업 스토어", "RIIZE 팬들을 위한 굿즈와 포토카드 체험",
                    "RIIZE 스페셜 팝업", "앨범·포토카드 DIY 체험",
                    "현장 구매 시 한정 포스터와 슬리브 증정",
                    "popup", Category.K_POP
            ),

// 19) POPUP_kpop_2 → NCT 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/01dcc25b-cafc-4011-80dd-95c4163d2b85.jpg",
                    "NCT 팝업 스토어", "네온사인과 응원봉을 테마로 한 공간",
                    "NCT 네온 팝업", "응원봉 커스터마이징 + 한정 굿즈 전시",
                    "현장 이벤트 참여 시 NCT 스페셜 키트 제공",
                    "popup", Category.K_POP
            ),

// 20) KPOP_1 → 앨범 존 (앨범 판매 이벤트)
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/6792cafe-7105-428b-a545-2b82453a4701.jpg",
                    "케이팝 앨범 존", "최신 앨범과 한정판 굿즈를 만날 수 있는 공간",
                    "케이팝 앨범 런칭 이벤트", "신보 발매 기념 앨범 전시와 판매",
                    "앨범 구매 시 랜덤 포토카드 증정",
                    "event", Category.K_POP
            ),

            // 21) KPOP_2
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/2d9459dd-cc98-4c5d-b6e6-05bcab3a1a2f.jpg",
                    "앨범 존", "한정판 앨범 전문 매장",
                    "케이팝 앨범 럭키드로우", "앨범 구매 시 럭키드로우 응모권",
                    "추첨으로 사인 폴라 증정",
                    "event", Category.K_POP
            ),

// 22) KPOP_3 → 앨범 판매 페어
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/346cd6dd-9d5a-4209-ad63-f05480b69d82.jpg",
                    "케이팝 레코드", "아이돌 앨범 전문 판매 존",
                    "앨범 페어", "다양한 아티스트의 신보와 명반 판매",
                    "현장 구매 고객 대상 사인회 응모권 제공",
                    "event", Category.K_POP
            ),

            // 23) ENT_1
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/0b3644a9-66d1-4d33-bfea-2c629ef62dcb.jpg",
                    "스크린 베이스볼 존", "실감 나는 스크린 야구 체험 공간",
                    "스크린 베이스볼 챌린지", "현장감 넘치는 스크린 타격 이벤트",
                    "타격 점수에 따라 경품 증정, 홈런왕에게 특별 굿즈 제공",
                    "event", Category.ENTERTAINMENT
            ),

            // 24) ENT_2
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/28a960b9-4520-404f-a060-33f0fb47ddc0.jpg",
                    "인디 스테이션", "인디 개발팀과 직접 만나는 자리",
                    "인디게임 페어", "최신작 시연과 개발자 만남",
                    "피드백 설문 작성 시 굿즈 추첨권",
                    "event", Category.ENTERTAINMENT
            ),

// 25) ENT_3 → 스크린 골프
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/96f97f61-66de-4bc8-bb75-b0787431575d.jpg",
                    "스크린 골프존", "실감 나는 스크린 골프 체험 공간",
                    "스크린 골프 챌린지", "최신 장비와 실시간 스코어링 시스템",
                    "참가자 전원 음료 제공, 우승자에겐 골프 굿즈 증정",
                    "event", Category.ENTERTAINMENT
            ),

            // 26) CLUB_1
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/b44b1120-9104-4dce-bc1f-460ba199714e.jpg",
                    "네온 클럽", "네온사인 디스코 파티",
                    "네온 디스코 나잇", "올드스쿨 디스코 파티",
                    "드레스코드 네온 시 웰컴 드링크 제공",
                    "event", Category.CLUB
            ),

            // 27) CLUB_2
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/17311362-73cb-4aad-8481-be22dd2e5379.jpg",
                    "베이스 클럽", "묵직한 베이스 EDM",
                    "베이스 EDM 나잇", "일렉트로닉 사운드 파티",
                    "새벽 2시까지 풀셋",
                    "event", Category.CLUB
            ),

// 28) CLUB_3 → Scoop 클럽
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/01154f57-8655-4048-9966-22cd244e6385.jpg",
                    "SCOOP 클럽", "힙합과 일렉트로닉을 동시에 즐길 수 있는 클럽",
                    "SCOOP 나잇", "최고의 DJ 라인업과 화려한 무대",
                    "프리패스 구매 고객 대상 웰컴 드링크 제공",
                    "event", Category.CLUB
            ),

// 29) ETC_2 → 식물 판매
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/87281d3b-b3c3-44b7-a7e9-8f4777f2aa75.jpg",
                    "그린 플랜트 마켓", "다양한 실내외 식물을 판매하는 공간",
                    "플랜트 스페셜 데이", "다육·허브·공기정화 식물 전시",
                    "구매 고객에게 미니 화분 증정",
                    "event", Category.ETC
            ),

// 30) SHOPPING → 백화점 이벤트
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/884e5fcb-65e0-4eb0-8592-7252f01b6083.jpg",
                    "프리미엄 백화점", "럭셔리 브랜드와 라이프스타일이 함께하는 공간",
                    "백화점 스페셜 위크", "의류·잡화·리빙 전 카테고리 할인전",
                    "구매 고객 대상 사은품 및 멤버십 포인트 더블 적립",
                    "event", Category.SHOPPING
            ),

            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/10250e6d-a8e0-4bfe-bd07-e3fbd08d8423.jpg",
                    "뷰티 존", "스킨케어와 메이크업 전문 스토어",
                    "뷰티 페어", "신상 화장품 시연 및 할인",
                    "특정 금액 이상 구매 시 뷰티 키트 증정",
                    "event", Category.SHOPPING
            ),

// 32) SHOPPING_3 → 화장품 판매점
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/1d1b2b37-c14c-4649-bc03-abf041bb17ba.jpg",
                    "코스메틱 스튜디오", "뷰티 아이템과 메이크업 전문점",
                    "코스메틱 페스티벌", "뷰티 클래스와 메이크업 시연",
                    "구매 고객 대상 립스틱 증정",
                    "event", Category.SHOPPING
            ),

// 33) SHOPPING_4 → 향수 판매점
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/events/2025-08-25/c44f18fc-1248-45a3-8580-1f32a499a80e.jpg",
                    "퍼퓸 하우스", "니치 향수와 스페셜 에디션 전문",
                    "향수 페어", "신상 니치 향수 라인업 공개",
                    "시향 고객 대상 샘플 증정",
                    "event", Category.SHOPPING
            ),
            // 34) FOOD_6 → 한식 비빔밥
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/0c107bb5-451d-480f-8e9c-0d86cbde0e8d.png",
                    "한식 비빔밥 하우스", "전통 나물과 매콤한 고추장 조합",
                    "비빔밥 페스티벌", "다양한 나물과 신선한 재료로 만든 비빔밥",
                    "비빔밥 주문 시 계란후라이 무료 추가",
                    "event", Category.FOOD
            ),

// 35) FOOD_7 → 브라질 슈하스코
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/4ef81623-5209-4a88-b85b-7497f694eaac.png",
                    "브라질 슈하스코 그릴", "숯불에 구운 정통 브라질 바비큐",
                    "슈하스코 무한리필 데이", "쇠고기·닭고기·소시지 풀코스",
                    "평일 런치 한정 무제한 제공",
                    "event", Category.FOOD
            ),

// 36) FOOD_8 → 멕시칸 타코
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/fa787f0d-2bd5-4ff6-8e22-049e100cbc4d.png",
                    "타코 하우스", "정통 멕시칸 타코 전문점",
                    "멕시칸 타코 나잇", "소고기·새우·아보카도 타코",
                    "3개 세트 주문 시 멕시칸 소다 무료",
                    "event", Category.FOOD
            ),

// 37) FOOD_9 → 인도 커리
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/6530e4a5-1811-4e6d-acfb-276b978467a8.png",
                    "인도 커리 하우스", "탄두리와 난을 곁들인 인도 전통 요리",
                    "커리&난 스페셜", "다양한 커리와 갓 구운 난",
                    "커리 주문 고객 난 1개 무제한 제공",
                    "event", Category.FOOD
            ),

// 38) FOOD_10 → 라멘
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/14b9faef-6e25-4885-9a37-ba68fe3653f0.png",
                    "라멘 전문점", "정통 일본식 라멘 하우스",
                    "라멘 위크", "돈코츠·쇼유·미소 라멘 풀 라인업",
                    "추가 토핑 무료 행사 진행",
                    "event", Category.FOOD
            ),

// 39) CAFE_4 → 디저트 카페
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/55b72874-1e2a-4b05-8888-49789d8a33bd.png",
                    "스윗 디저트 카페", "티라미수와 홈메이드 케이크 전문점",
                    "티라미수 스페셜", "직접 만든 홈메이드 티라미수",
                    "아메리카노와 세트 구매 시 할인",
                    "event", Category.CAFE
            ),

// 40) CAFE_5 → 루프탑 카페
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/af38e5b3-9977-4eac-925c-28452263d04e.png",
                    "루프탑 카페", "노을과 야경을 즐길 수 있는 공간",
                    "루프탑 선셋 이벤트", "노을 보며 즐기는 라떼",
                    "18–20시 방문 고객 와인 서비스",
                    "event", Category.CAFE
            ),

// 41) CAFE_6 → 북카페
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/98f13b7f-c052-48c4-8c51-c54c4b62f302.png",
                    "빈티지 북카페", "책과 커피가 함께하는 아늑한 공간",
                    "북카페 북토크", "독서 모임 & 라떼 클래스",
                    "참가자에게 도서 할인 쿠폰 제공",
                    "event", Category.CAFE
            ),

// 42) POPUP_9 → 레고 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/77956977-62a7-45b7-bdc4-2df998b59b71.png",
                    "레고 팝업 스토어", "레고 신제품 전시와 체험 공간",
                    "레고 플레이존", "레고 브릭 조립 체험",
                    "현장 구매 시 미니 피규어 증정",
                    "popup", Category.SHOPPING
            ),

// 43) POPUP_10 → 스타워즈 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/80ad80de-1988-447a-8f33-a12403900916.png",
                    "스타워즈 존", "스타워즈 굿즈와 포토존",
                    "스타워즈 스페셜 존", "라이트세이버 체험",
                    "굿즈 구매 시 스타워즈 포스터 증정",
                    "popup", Category.ENTERTAINMENT
            ),

// 44) POPUP_11 → 카카오프렌즈 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/cda7ca7e-0b21-4788-b4ee-d0ac49e48d21.png",
                    "카카오프렌즈 팝업", "라이언·어피치 한정판 굿즈",
                    "라이언 팝업 스토어", "캐릭터 인형·문구·소품",
                    "한정 굿즈 구매 시 캐릭터 키링 증정",
                    "popup", Category.ETC
            ),

// 45) POPUP_12 → 아웃도어 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/93e20075-b2f2-4987-b966-4a70310e92bb.png",
                    "캠핑 기어 팝업", "아웃도어 브랜드 체험 공간",
                    "캠핑 기어 팝업", "텐트·버너·체어 전시",
                    "현장 구매 고객 캠핑컵 제공",
                    "popup", Category.ETC
            ),

// 46) POPUP_13 → 아이폰 액세서리
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/aa720a7c-0a2a-4f42-ad3c-56b078f9404b.png",
                    "아이폰 액세서리 존", "최신 스마트폰 케이스 & 충전기",
                    "아이폰 케이스 페어", "다양한 액세서리 시리즈",
                    "케이스 2개 이상 구매 시 20% 할인",
                    "popup", Category.SHOPPING
            ),

// 47) KPOP_4 → IVE 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/e8da247f-c109-450d-b271-7fce82210a97.png",
                    "IVE 팬 존", "아이브 굿즈와 포토카드 체험",
                    "IVE 팝업 스토어", "앨범·포토카드·MD 전시",
                    "현장 이벤트 참여 시 랜덤 폴라 증정",
                    "popup", Category.K_POP
            ),

// 48) KPOP_5 → 세븐틴 팝업
            new SeedData(
                    "https://aja5jo-image.s3.ap-northeast-2.amazonaws.com/public/stores/2025-08-25/8a6af436-8a66-4d8e-b3ab-a44445b57286.png",
                    "SEVENTEEN 굿즈 존", "세븐틴 앨범과 응원봉 전시",
                    "SEVENTEEN 굿즈 페어", "응원봉 체험 & 이벤트",
                    "특정 금액 이상 구매 시 미공개 포토카드 증정",
                    "popup", Category.K_POP
            )

            );

    // ─────────────────────────────
    // 실행
    // ─────────────────────────────
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Faker faker = new Faker(new Locale("ko"));

        // 1) 일반 유저 3명
        for (int i = 1; i <= 3; i++) {
            userRepository.save(
                    User.builder()
                            .email("user" + i + "@test.com")
                            .password(passwordEncoder.encode("user1234"))
                            .role(Role.USER)
                            .build()
            );
        }

        // 2) 시드 1개당: 상인 1명 + 상점 1개 + (type에 따라 이벤트 or 팝업) 1개
        for (int idx = 0; idx < SEED_DATA.size(); idx++) {
            SeedData s = SEED_DATA.get(idx);

            // 상인 생성
            User merchant = userRepository.save(
                    User.builder()
                            .email("merchant" + idx + "@test.com")
                            .password(passwordEncoder.encode("123456"))
                            .role(Role.MERCHANT)
                            .build()
            );

            // 주소는 번갈아가며
            String address = (idx % 2 == 0)
                    ? "서울 마포구 홍대입구로 " + (10 + idx) + "길"
                    : "서울 서대문구 신촌로 "   + (20 + idx) + "길";

            // 상점 생성 (썸네일/이미지 = 시드 이미지)
            Store store = storeRepository.save(
                    Store.builder()
                            .owner(merchant)
                            .name(s.storeName)
                            .intro(s.storeIntro)
                            .address(address)
                            .number("010-" + rand4() + "-" + rand4())
                            .category(s.category)
                            .thumbnail(s.imageUrl)
                            .images(List.of(s.imageUrl))
                            .startTime(LocalTime.of(10, 0))
                            .endTime(LocalTime.of(22, 0))
                            .likeCount(rand(0, 300))
                            .build()
            );

            // 공통 기간/시간
            LocalDate start = LocalDate.now().plusDays(rand(-3, 2));
            LocalDate end   = start.plusDays(rand(3, 10));

            // type에 따라 이벤트 or 팝업 생성
            if ("popup".equalsIgnoreCase(s.type)) {
                popupRepository.save(
                        Popup.builder()
                                .user(merchant)
                                .name(s.contentTitle)
                                .intro(s.contentIntro)
                                .description(s.contentDescription)
                                .thumbnail(s.imageUrl)
                                .images(List.of(s.imageUrl))
                                .category(s.category)
                                .address(address + " 팝업홀 B" + (idx % 5 + 1))
                                .startDate(start)
                                .endDate(end)
                                .startTime(LocalTime.of(12, 0))
                                .endTime(LocalTime.of(20, 0))
                                .likeCount(rand(0, 200))
                                .build()
                );
            } else {
                eventRepository.save(
                        Event.builder()
                                .store(store)
                                .name(s.contentTitle)
                                .intro(s.contentIntro)
                                .description(s.contentDescription)
                                .thumbnail(s.imageUrl)
                                .images(List.of(s.imageUrl))
                                .startDate(start)
                                .endDate(end)
                                .startTime(LocalTime.of(11, 0))
                                .endTime(LocalTime.of(21, 0))
                                .likeCount(rand(0, 200))
                                .build()
                );
            }

            if (idx % 25 == 24) {
                em.flush();
                em.clear();
            }
        }

        // 필요 시 임베딩 프리로드
        embeddingPreloadService.preloadData();

        em.flush();
        em.clear();
    }


    // ─────────────────────────────
    // 유틸
    // ─────────────────────────────
    private static int rand(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
    private static String rand4() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
    }
}
