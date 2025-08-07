package group5.backend.data;

import group5.backend.domain.user.Category;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class StoreNameGenerator {

    private static final Map<Category, List<String>> STORE_NAMES = Map.of(
            Category.CAFE, List.of(
                    "달빛카페", "소소한커피", "감성다방", "브루잉하우스", "커피곰", "하늘정원카페", "우주카페", "빈티지브루"
            ),
            Category.FOOD, List.of(
                    "홍대국밥", "연남식당", "신촌돈까스", "마포칼국수", "가마솥한끼", "서울라멘", "트러플덮밥집", "집밥상회"
            ),
            Category.SHOPPING, List.of(
                    "리빙101", "트렌드스토어", "비건마켓", "편집샵더블랙", "빈티지하우스", "오브젝트샵", "라이프셀렉트", "디자인문구"
            ),
            Category.ENTERTAINMENT, List.of(
                    "플레이존", "방탈출연구소", "보드게임스페이스", "홍대게임존", "스크린파크", "도심놀이터", "VR룸", "펀타운"
            ),
            Category.K_POP, List.of(
                    "케이팝스퀘어", "포카존", "팬플래닛", "굿즈마켓", "응원샵", "아이돌베이스", "뮤직앤굿즈", "컴백스팟"
            ),
            Category.CLUB, List.of(
                    "클럽블루", "EDM하우스", "파티존", "클럽드림", "더하우스", "비트클럽", "밤의정원", "디제이웨이브"
            ),
            Category.ETC, List.of(
                    "취미공작소", "창작의집", "메이커스페이스", "원데이랩", "작은책방", "소소살롱", "동네문화센터", "취향저격클래스"
            )
    );

    public static String getRandomName(Category category) {
        List<String> names = STORE_NAMES.getOrDefault(category, List.of("무명가게"));
        return names.get(new Random().nextInt(names.size()));
    }
}