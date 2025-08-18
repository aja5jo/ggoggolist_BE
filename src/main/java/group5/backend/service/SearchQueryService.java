package group5.backend.service;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import group5.backend.dto.common.home.response.StoreEventResponse;
import group5.backend.dto.common.home.response.EventSummaryResponse;
import group5.backend.dto.common.home.response.StoreSummaryResponse;
import group5.backend.repository.EventRepository;
import group5.backend.repository.FavoriteEventRepository;
import group5.backend.repository.FavoriteStoreRepository;
import group5.backend.repository.StoreRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchQueryService {

    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final FavoriteEventRepository favoriteEventRepository;

    public StoreEventResponse search(String rawKeyword, @Nullable Long userId) {
        if (rawKeyword == null || rawKeyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색어는 필수입니다.");
        }
        String keyword = rawKeyword.trim();
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 좋아요 ↓, id ↓ 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        // 1) 가게명 검색(부분 일치 + ignore case)
        List<Store> stores = storeRepository.findByNameContainingIgnoreCase(keyword, sort);

        // 2) 이벤트명 검색(부분 일치 + 진행중만)
        List<Event> events = eventRepository
                .findByNameContainingIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        keyword, today, today, sort);

        boolean loggedIn = (userId != null);

        // 3) DTO 매핑 (liked 플래그는 로그인 시만 계산)
        var storeDtos = stores.stream()
                .map(s -> StoreSummaryResponse.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .thumbnail(s.getThumbnail())
                        .likeCount(s.getLikeCount())
                        .liked(loggedIn && favoriteStoreRepository.existsByUserIdAndStoreId(userId, s.getId()))
                        .build())
                .toList();

        var eventDtos = events.stream()
                .map(e -> EventSummaryResponse.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .thumbnail(e.getThumbnail())
                        .description(e.getDescription())
                        .likeCount(e.getLikeCount())
                        .liked(loggedIn && favoriteEventRepository.existsByUserIdAndEventId(userId, e.getId()))
                        .startDate(e.getStartDate())
                        .endDate(e.getEndDate())
                        .build())
                .toList();

        return StoreEventResponse.builder()
                .stores(storeDtos)
                .events(eventDtos)
                .build();
    }
}
