package group5.backend.data;

import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.Category;
import group5.backend.domain.user.Role;
import group5.backend.domain.user.User;
import com.github.javafaker.Faker;
import group5.backend.repository.EventRepository;
import group5.backend.repository.PopupRepository;
import group5.backend.repository.StoreRepository;
import group5.backend.repository.UserRepository;
import group5.backend.service.ai.recomm.EmbeddingPreloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;
import java.util.Random;
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

    @PersistenceContext
    private EntityManager em;

    private final Random random = new Random();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Faker faker = new Faker(new Locale("ko"));
        Category[] categories = Category.values();

        // USER 1명
        User user = User.builder()
                .email("user@test.com")
                .password(passwordEncoder.encode("user1234"))
                .role(Role.USER)
                .build();
        userRepository.save(user);

        int batch = 0;

        // 상인 50명 + 가게 + 이벤트
        for (int i = 0; i < 50; i++) {
            User merchant = User.builder()
                    .email("merchant" + i + "@test.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.MERCHANT)
                    .build();
            userRepository.save(merchant);

            String address = (i % 2 == 0)
                    ? "서울 마포구 홍대입구로 " + i + "길"
                    : "서울 서대문구 신촌로 " + i + "길";

            Category storeCategory = categories[i % categories.length];
            String storeName = StoreNameGenerator.getRandomName(storeCategory);
            String storeIntro = StoreIntroGenerator.getRandomIntro(storeCategory);

            Store store = Store.builder()
                    .owner(merchant)
                    .name(storeName)
                    .address(address)
                    .number("010-" + faker.number().digits(4) + "-" + faker.number().digits(4))
                    .intro(storeIntro)
                    .category(storeCategory)
                    .thumbnail("https://picsum.photos/seed/store" + i + "/300/200")
                    .images(List.of(
                            "https://picsum.photos/seed/store" + i + "-1/300/200",
                            "https://picsum.photos/seed/store" + i + "-2/300/200"
                    ))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(22, 0))
                    .likeCount(faker.number().numberBetween(0, 300))
                    .build();
            storeRepository.save(store);

            int eventCount = faker.number().numberBetween(2, 5);
            for (int j = 1; j <= eventCount; j++) {
                LocalDate start = LocalDate.now().plusDays(faker.number().numberBetween(-5, 2));
                LocalDate end = start.plusDays(faker.number().numberBetween(3, 14));

                Event event = Event.builder()
                        .store(store)
                        .name("이벤트 " + j + " - " + store.getName())
                        .description(EventTextGenerator.getRandomDescription())
                        .intro(EventTextGenerator.getRandomIntro())
                        .thumbnail("https://picsum.photos/seed/event" + i + "-" + j + "/300/200")
                        .images(List.of(
                                "https://picsum.photos/seed/event" + i + "-" + j + "-1/300/200",
                                "https://picsum.photos/seed/event" + i + "-" + j + "-2/300/200"
                        ))
                        .startDate(start)
                        .endDate(end)
                        .startTime(LocalTime.of(11, 0))
                        .endTime(LocalTime.of(21, 0))
                        .likeCount(randomLikeCount(0, 200))
                        .build();

                eventRepository.save(event);
            }

            if (++batch % 25 == 0) {
                em.flush();
                em.clear();
            }
        }
        List<User> merchants = userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.MERCHANT)
                .limit(30) // 앞에서 30명만
                .toList();
        // 팝업 30개 (카테고리+제너레이터 적용)
        for (int k = 0; k < 30; k++) {
            User owner = merchants.get(k);

            // ✅ 카테고리 배정
            Category cat = categories[k % categories.length];

            LocalDate start = LocalDate.now().plusDays(faker.number().numberBetween(-3, 5));
            LocalDate end = start.plusDays(faker.number().numberBetween(2, 10));

            Popup popup = Popup.builder()
                    .user(owner)
                    .category(cat) // ✅ 저장
                    .name(PopupTextGenerator.name(cat))
                    .description(PopupTextGenerator.desc(cat))
                    .intro(PopupTextGenerator.intro(cat))
                    .thumbnail("https://picsum.photos/seed/popup" + k + "/300/200")
                    .images(List.of(
                            "https://picsum.photos/seed/popup" + k + "-1/300/200",
                            "https://picsum.photos/seed/popup" + k + "-2/300/200"
                    ))
                    .startDate(start)
                    .endDate(end)
                    .startTime(LocalTime.of(12, 0))
                    .endTime(LocalTime.of(20, 0))
                    .address("서울 마포구 임시로 " + (100 + k) + "번지")
                    .likeCount(randomLikeCount(0, 200))// ✅ 랜덤 likeCount
                    .build();

            popupRepository.save(popup);

            if (k % 20 == 19) {
                em.flush();
                em.clear();
            }
        }
        // 데이터 초기화 후 임베딩 미리 로드
        embeddingPreloadService.preloadData();

        em.flush();
        em.clear();
    }

    private User getRandomMerchant() {
        List<User> merchants = userRepository.findAll()
                .stream()
                .filter(u -> u.getRole() == Role.MERCHANT)
                .toList();
        return merchants.get(random.nextInt(merchants.size()));
    }

    // ✅ 랜덤 likeCount 생성 메서드
    private int randomLikeCount(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
