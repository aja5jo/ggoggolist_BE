package group5.backend.service;


import group5.backend.domain.event.Event;
import group5.backend.dto.common.event.FilterType;
import group5.backend.dto.common.event.response.EventOverviewResponse;
import group5.backend.dto.common.event.response.EventPageResponse;
import group5.backend.dto.common.event.response.EventSummaryResponse;
import group5.backend.exception.event.HandleInvalidFilterException;
import group5.backend.repository.EventRepository;
import group5.backend.repository.FavoriteEventRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.data.domain.*;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventQueryService {

    private static final int OVERVIEW_TOP_N = 8; // overview 각 섹션별 노출 개수

    private final EventRepository eventRepository;
    private final FavoriteEventRepository favoriteEventRepository;

    /**
     * 전체(overview) 조회: /api/events (filter 파라미터 없음)
     * 인기 / 진행중 / 오늘마감 / 예정
     * 각 섹션별 OVERVIEW_TOP_N 개만 노출
     */
    public EventOverviewResponse getEventOverview(@Nullable Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        Sort popularSort  = Sort.by(Sort.Direction.DESC, "likeCount").and(Sort.by(Sort.Direction.DESC, "id"));
        Sort upcomingSort = Sort.by(Sort.Direction.ASC, "startDate").and(Sort.by(Sort.Direction.ASC, "id"));

        // 인기
        List<EventSummaryResponse> popular = eventRepository
                .findOngoing(today, PageRequest.of(0, OVERVIEW_TOP_N, popularSort))
                .map(e -> toSummary(e, userId))
                .getContent();

        // 진행중
        List<EventSummaryResponse> ongoing = eventRepository
                .findOngoing(today, PageRequest.of(0, OVERVIEW_TOP_N, popularSort))
                .map(e -> toSummary(e, userId))
                .getContent();

        // 오늘 마감
        List<EventSummaryResponse> closingToday = eventRepository
                .findClosingToday(today, PageRequest.of(0, OVERVIEW_TOP_N, popularSort))
                .map(e -> toSummary(e, userId))
                .getContent();

        // 예정
        List<EventSummaryResponse> upcoming = eventRepository
                .findUpcoming(today, PageRequest.of(0, OVERVIEW_TOP_N, upcomingSort))
                .map(e -> toSummary(e, userId))
                .getContent();

        return EventOverviewResponse.of(popular, ongoing, closingToday, upcoming);
    }

    /**
     * 단일 필터 조회: /api/events?filter={filter}
     * Pageable 그대로 사용 → 프론트 요청 개수만큼 반환
     */
    public EventPageResponse getEvents(@Nullable FilterType filter,
                                       @Nullable Long userId,
                                       Pageable pageable) {
        if (filter == null) {
            throw new HandleInvalidFilterException(buildAllowedMessage());
        }

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Page<Event> page;

        switch (filter) {
            case POPULAR -> page = eventRepository.findPopularAmongOngoing(today, pageable);
            case ONGOING -> page = eventRepository.findOngoing(today, pageable);
            case CLOSING_TODAY -> page = eventRepository.findClosingToday(today, pageable);
            case UPCOMING -> page = eventRepository.findUpcoming(today, pageable);
            default -> throw new HandleInvalidFilterException(buildAllowedMessage());
        }

        Page<EventSummaryResponse> mapped = page.map(e -> toSummary(e, userId));
        return EventPageResponse.from(mapped);
    }

    /* =======================
       내부 유틸
     ======================= */

    private EventSummaryResponse toSummary(Event e, @Nullable Long userId) {
        boolean liked = (userId != null) && favoriteEventRepository.existsByUserIdAndEventId(userId, e.getId());
        return EventSummaryResponse.from(e, liked);
    }

    private String buildAllowedMessage() {
        String allowed = Arrays.stream(FilterType.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        return "유효하지 않은 필터 값입니다. (" + allowed + " 중 하나)";
    }
}