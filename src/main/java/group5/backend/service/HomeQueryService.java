package group5.backend.service;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.common.event.response.EventSummaryResponse;
import group5.backend.dto.common.store.response.StoreEventResponse;
import group5.backend.dto.common.store.response.StoreSummaryResponse;
import group5.backend.repository.*;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeQueryService {

    private static final int LIMIT = 12; // 홈 섹션당 뿌릴 개수 (원하면 조절)

    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final FavoriteEventRepository favoriteEventRepository;
    private final UserRepository userRepository;

    public StoreEventResponse getHomeDetails(@Nullable Long userId) {
        return getHome(userId);
    }

    public StoreEventResponse getHome(@Nullable Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // likeCount desc, id desc 공통 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Pageable page = PageRequest.of(0, LIMIT, sort);

        List<Category> interests = Collections.emptyList();
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getCategories() != null) {
                interests = user.getCategories();
            }
        }

        // 1) 스토어 목록
        List<Store> stores = (userId != null && !interests.isEmpty())
                ? storeRepository.findByCategoryIn(interests, page).getContent()
                : storeRepository.findAll(page).getContent();

        // 2) 이벤트 목록 (진행 중만)
        List<Event> events = (userId != null && !interests.isEmpty())
                ? eventRepository
                .findByStore_CategoryInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        interests, today, today, page
                ).getContent()
                : eventRepository
                .findOngoing(today, page)
                .getContent();

        // 3) DTO 매핑 (+ liked 플래그는 로그인시에만 계산)
        boolean isLoggedIn = (userId != null);

        var storeDtos = stores.stream().map(s -> StoreSummaryResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .thumbnail(s.getThumbnail())
                .likeCount(s.getLikeCount())
                .liked(isLoggedIn && favoriteStoreRepository.existsByUserIdAndStoreId(userId, s.getId()))
                .build()
        ).toList();

        var eventDtos = events.stream().map(e -> EventSummaryResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .thumbnail(e.getThumbnail())
                .description(e.getDescription())
                .likeCount(e.getLikeCount())
                .liked(isLoggedIn && favoriteEventRepository.existsByUserIdAndEventId(userId, e.getId()))
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .build()
        ).toList();

        return StoreEventResponse.builder()
                .stores(storeDtos)
                .events(eventDtos)
                .build();
    }
}
