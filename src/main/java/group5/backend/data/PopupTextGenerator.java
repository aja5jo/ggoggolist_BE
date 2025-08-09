package group5.backend.data;

import group5.backend.domain.user.Category;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class PopupTextGenerator {

    private static final Random random = new Random();

    // 카테고리별 팝업 이름
    private static final Map<Category, List<String>> POPUP_NAMES = Map.of(
            Category.CAFE, List.of(
                    "한정판 원두 팝업카페", "여름 시즌 콜드브루 팝업", "빈티지 머그 전시 팝업",
                    "디저트&커피 페어링 팝업", "원데이 라떼아트 클래스"
            ),
            Category.FOOD, List.of(
                    "지역 맛집 페스티벌", "한정판 라멘 팝업", "이탈리안 파스타 팝업",
                    "매운맛 챌린지 팝업", "미쉐린 셰프 초청 팝업"
            ),
            Category.SHOPPING, List.of(
                    "빈티지 의류 팝업", "국내 신진 브랜드 팝업", "여름 악세서리 한정전",
                    "친환경 쇼핑 페어", "한정판 스니커즈 발매"
            ),
            Category.ENTERTAINMENT, List.of(
                    "보드게임 체험존", "인디영화 시사회", "뮤직 버스킹 팝업",
                    "VR 체험관 팝업", "마술 쇼 팝업"
            ),
            Category.K_POP, List.of(
                    "아이돌 굿즈 한정 팝업", "팬미팅 사인회", "앨범 발매 기념 팝업",
                    "포토카드 교환전", "뮤직비디오 촬영 소품전"
            ),
            Category.CLUB, List.of(
                    "DJ 파티 나이트", "EDM 스페셜 나잇", "레트로 디스코 팝업",
                    "하우스 뮤직 위크", "90's 힙합 파티"
            ),
            Category.ETC, List.of(
                    "지역 아트마켓", "플리마켓 팝업", "반려동물 용품 한정전",
                    "중고서적 교환전", "크래프트 클래스"
            )
    );

    // 카테고리별 인트로
    private static final Map<Category, List<String>> POPUP_INTROS = Map.of(
            Category.CAFE, List.of(
                    "향긋한 커피와 함께하는 단 1주의 특별한 시간.",
                    "커피 애호가들을 위한 한정판 경험.",
                    "커피 한 잔의 여유를 느껴보세요.",
                    "당신만을 위한 디저트와 커피 페어링.",
                    "스페셜티 원두의 매력을 만나다."
            ),
            Category.FOOD, List.of(
                    "전국 맛집이 한자리에 모였습니다.",
                    "셰프가 직접 준비한 특별 메뉴.",
                    "한정 수량으로 준비된 미식의 향연.",
                    "식도락가를 위한 최고의 선택.",
                    "먹는 즐거움을 배로 느껴보세요."
            ),
            Category.SHOPPING, List.of(
                    "단 한 번의 기회, 한정판 아이템!",
                    "지금만 만나볼 수 있는 브랜드.",
                    "쇼핑의 설렘을 그대로 담았습니다.",
                    "개성 있는 패션과 라이프스타일 제안.",
                    "친환경과 스타일을 동시에."
            ),
            Category.ENTERTAINMENT, List.of(
                    "오감이 즐거운 특별한 경험.",
                    "지루할 틈 없는 엔터테인먼트의 향연.",
                    "즐거움이 가득한 하루.",
                    "가족과 함께하기 좋은 체험 팝업.",
                    "모두가 즐길 수 있는 이색 이벤트."
            ),
            Category.K_POP, List.of(
                    "팬들을 위한 단 하나의 공간.",
                    "아이돌과 함께하는 특별한 추억.",
                    "굿즈부터 사인회까지 모두 준비!",
                    "K-POP 팬심을 채워드립니다.",
                    "뮤직과 팬심이 만나는 곳."
            ),
            Category.CLUB, List.of(
                    "밤을 뜨겁게 불태울 순간.",
                    "음악과 춤이 함께하는 파티.",
                    "DJ의 비트에 몸을 맡기세요.",
                    "화려한 조명 아래, 잊지 못할 밤.",
                    "하우스와 EDM의 환상적인 조화."
            ),
            Category.ETC, List.of(
                    "다양한 취향을 만족시키는 팝업.",
                    "소소하지만 확실한 행복.",
                    "누구나 즐길 수 있는 열린 공간.",
                    "재미와 감동이 함께하는 시간.",
                    "창작자와 소비자가 만나는 곳."
            )
    );

    // 카테고리별 상세 설명
    private static final Map<Category, List<String>> POPUP_DESCS = Map.of(
            Category.CAFE, List.of(
                    "원두의 산지부터 로스팅까지, 커피의 모든 것을 경험할 수 있는 팝업입니다.",
                    "전문 바리스타와 함께하는 라떼아트 클래스와 시음회.",
                    "한정판 디저트와 커피를 함께 즐길 수 있는 기회.",
                    "커피 애호가들이 기다려온 특별한 원두 발매 행사.",
                    "카페 문화를 사랑하는 이들을 위한 공간."
            ),
            Category.FOOD, List.of(
                    "전국 각지의 맛집이 모여 만든 미식의 향연.",
                    "셰프가 직접 준비한 시그니처 메뉴를 한자리에서.",
                    "한정 수량으로 준비된 프리미엄 요리를 즐기세요.",
                    "입맛을 사로잡는 다양한 음식과 디저트.",
                    "식도락가들을 위한 최고의 맛집 탐방."
            ),
            Category.SHOPPING, List.of(
                    "국내외 인기 브랜드의 한정판 아이템을 만나보세요.",
                    "친환경 소재와 독창적인 디자인의 조화.",
                    "신진 디자이너들의 작품을 직접 보고 구매.",
                    "개성을 표현할 수 있는 유니크한 패션 아이템.",
                    "쇼핑의 즐거움과 설렘을 한 번에."
            ),
            Category.ENTERTAINMENT, List.of(
                    "VR, 보드게임, 공연 등 다양한 즐길 거리 제공.",
                    "남녀노소 누구나 즐길 수 있는 체험형 팝업.",
                    "음악, 영화, 게임을 한 자리에서 경험.",
                    "이색적인 엔터테인먼트를 체험해보세요.",
                    "신나는 공연과 함께하는 하루."
            ),
            Category.K_POP, List.of(
                    "아이돌 굿즈, 사인회, 팬미팅이 한 곳에.",
                    "최신 앨범 발매를 기념하는 특별 행사.",
                    "팬과 아티스트가 직접 소통하는 공간.",
                    "포토카드 교환부터 한정판 굿즈까지.",
                    "K-POP 팬들을 위한 모든 것."
            ),
            Category.CLUB, List.of(
                    "국내외 유명 DJ들이 선사하는 밤.",
                    "EDM, 하우스, 힙합 등 다양한 장르의 음악.",
                    "밤새도록 즐길 수 있는 클럽 파티.",
                    "화려한 조명과 폭발적인 사운드.",
                    "잊지 못할 추억을 만드는 클럽 나잇."
            ),
            Category.ETC, List.of(
                    "지역 예술가와 창작자들이 모인 마켓.",
                    "반려동물과 함께 즐기는 팝업 이벤트.",
                    "중고서적과 빈티지 물품 교환전.",
                    "다양한 취미를 체험할 수 있는 클래스.",
                    "생활 속 작은 행복을 전하는 공간."
            )
    );

    public static String name(Category category) {
        return getRandom(POPUP_NAMES.get(category));
    }

    public static String intro(Category category) {
        return getRandom(POPUP_INTROS.get(category));
    }

    public static String desc(Category category) {
        return getRandom(POPUP_DESCS.get(category));
    }

    private static String getRandom(List<String> list) {
        return list.get(random.nextInt(list.size()));
    }
}
