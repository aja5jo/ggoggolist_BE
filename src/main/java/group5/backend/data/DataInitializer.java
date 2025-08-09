package group5.backend.data;
import com.github.javafaker.Faker;
import group5.backend.domain.user.Category;
import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.Role;
import group5.backend.domain.user.User;
import group5.backend.repository.EventRepository;
import group5.backend.repository.StoreRepository;
import group5.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Locale;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer{

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;
    @PostConstruct
    public void init() {
        try {
            log.info("🟡 PostConstruct 진입 시작");

            Faker faker = new Faker(new Locale("ko"));
            Category[] categories = Category.values();

            // ✅ 고정 일반 유저 생성
            User user = User.builder()
                    .email("user@test.com")
                    .password(passwordEncoder.encode("123456"))
                    .role(Role.USER)
                    .build();
            userRepository.save(user);
            log.info("👤 일반 사용자 저장 완료");

            // ✅ 상인 50명 + 가게 50개 + 이벤트 여러 개 생성
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

                Store store = Store.builder()
                        .owner(merchant)
                        .name(faker.company().name())
                        .address(address)
                        .number("010-" + faker.number().digits(4) + "-" + faker.number().digits(4))
                        .intro(faker.lorem().sentence())
                        .category(categories[i % categories.length])
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

                // ✅ 이벤트 2~4개 생성
                int eventCount = faker.number().numberBetween(2, 5);
                for (int j = 1; j <= eventCount; j++) {
                    boolean isPopup = (j % 4 == 0); // 가끔만 팝업

                    Event event = Event.builder()
                            .store(isPopup ? null : store) // 팝업 이벤트면 store null 허용
                            .name("이벤트 " + j + " - " + store.getName())
                            .description(faker.lorem().sentence())
                            .intro(faker.lorem().sentence())
                            .thumbnail("https://picsum.photos/seed/event" + i + "-" + j + "/300/200")
                            .images(List.of(
                                    "https://picsum.photos/seed/event" + i + "-" + j + "-1/300/200",
                                    "https://picsum.photos/seed/event" + i + "-" + j + "-2/300/200"
                            ))
                            .startDate(LocalDate.now().plusDays(faker.number().numberBetween(-5, 2)))
                            .endDate(LocalDate.now().plusDays(faker.number().numberBetween(3, 14)))
                            .startTime(LocalTime.of(11, 0))
                            .endTime(LocalTime.of(21, 0))
                            .isPopup(isPopup)
                            .likeCount(faker.number().numberBetween(0, 300))
                            .build();

                    eventRepository.save(event);
                }

                log.info("✅ merchant {} 저장 완료", i);
            }

            log.info("✅ 전체 더미 데이터 생성 완료 ✅");

        } catch (Exception e) {
            log.error("❌ 더미 데이터 생성 중 예외 발생", e);
        }
    }
}