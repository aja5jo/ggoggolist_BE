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
@Profile("local") // 로컬에서만 동작
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

        // 2) 상인 15명 + 가게 15개 (콘텐츠 생성 없음)
        User[] merchants = new User[15];
        Store[] stores = new Store[15];

        for (int i = 0; i < 15; i++) {
            merchants[i] = userRepository.save(
                    User.builder()
                            .email("merchant" + i + "@test.com")
                            .password(passwordEncoder.encode("123456"))
                            .role(Role.MERCHANT)
                            .build()
            );

            SeedData seed = SEED_DATA.get(i); // 앞 15개로 가게 생성

            stores[i] = storeRepository.save(
                    Store.builder()
                            .owner(merchants[i])
                            .name(seed.storeName)
                            .intro(seed.storeIntro)
                            .address((i % 2 == 0)
                                    ? "서울 마포구 홍대입구로 " + (10 + i) + "길"
                                    : "서울 서대문구 신촌로 "   + (20 + i) + "길")
                            .number("010-" + rand4() + "-" + rand4())
                            .category(seed.category)
                            .thumbnail(seed.imageUrl)
                            .images(List.of(seed.imageUrl))
                            .startTime(LocalTime.of(10, 0))
                            .endTime(LocalTime.of(22, 0))
                            .likeCount(rand(0, 300))
                            .build()
            );

            if (i % 5 == 4) {
                em.flush(); em.clear();
            }
        }

        // 3) 나머지 19개(인덱스 15~33) → "type"에 따라 Popup/Event만 생성
        for (int k = 15; k < SEED_DATA.size(); k++) {
            SeedData c = SEED_DATA.get(k);
            Store target = stores[k % stores.length]; // 라운드로빈으로 스토어 배정

            LocalDate start = LocalDate.now().plusDays(rand(-3, 2));
            LocalDate end   = start.plusDays(rand(3, 10));

            if ("popup".equalsIgnoreCase(c.type)) {
                popupRepository.save(
                        Popup.builder()
                                .user(target.getOwner())
                                .name(c.contentTitle)
                                .intro(c.contentIntro)
                                .description(c.contentDescription)
                                .thumbnail(c.imageUrl)
                                .images(List.of(c.imageUrl))
                                .category(c.category)
                                .address("서울 임시로 " + (300 + k) + "번지")
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
                                .store(target)
                                .name(c.contentTitle)
                                .intro(c.contentIntro)
                                .description(c.contentDescription)
                                .thumbnail(c.imageUrl)
                                .images(List.of(c.imageUrl))
                                .startDate(start)
                                .endDate(end)
                                .startTime(LocalTime.of(11, 0))
                                .endTime(LocalTime.of(21, 0))
                                .likeCount(rand(0, 200))
                                .build()
                );
            }

            if ((k - 15) % 7 == 6) {
                em.flush(); em.clear();
            }
        }
        // 데이터 초기화 후 임베딩 미리 로드
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
