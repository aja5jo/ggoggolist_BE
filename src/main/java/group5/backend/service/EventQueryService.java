package group5.backend.service;

import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.dto.category.FeedItemType;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.dto.common.event.FilterType;
import group5.backend.dto.common.event.response.EventOverviewResponse;
import group5.backend.dto.common.event.response.MixedFeedListResponse;
import group5.backend.dto.common.event.response.EventDetailResponse;
import group5.backend.exception.event.EventNotFoundException;
import group5.backend.exception.event.HandleInvalidFilterException;
import group5.backend.repository.EventRepository;
import group5.backend.repository.FavoriteEventRepository;
import group5.backend.repository.FavoritePopupRepository;
import group5.backend.repository.PopupRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventQueryService {

    private static final int OVERVIEW_TOP_N = 8; // 섹션별 8개

    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;
    private final FavoriteEventRepository favoriteEventRepository;
    private final FavoritePopupRepository favoritePopupRepository;

    /**
     * /api/events (filter 없음)
     * popular / ongoing / closingToday / upcoming
     * → 이벤트+팝업 합쳐서 정렬 후 섹션별 상위 8개
     */
    public EventOverviewResponse getEventOverview(@Nullable Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 정렬 정의
        Sort likeDescIdDesc = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Sort startAscIdAsc = Sort.by(Sort.Direction.ASC, "startDate")
                .and(Sort.by(Sort.Direction.ASC, "id"));

        // popular: 진행중 + likeCount desc, id desc
        List<CategoryFeedItemResponse> popular = limitTopN(
                merge(
                        eventRepository.findOngoingList(today),
                        popupRepository.findOngoingList(today),
                        userId
                ),
                Comparator.comparing(CategoryFeedItemResponse::likeCount, Comparator.reverseOrder())
                        .thenComparing(CategoryFeedItemResponse::id, Comparator.reverseOrder()),
                OVERVIEW_TOP_N
        );

// ✅ ongoing: 진행중 + startDate asc, id asc
        List<CategoryFeedItemResponse> ongoing = limitTopN(
                merge(
                        eventRepository.findOngoingList(today),
                        popupRepository.findOngoingList(today),
                        userId
                ),
                Comparator.comparing(CategoryFeedItemResponse::startDate)   // asc
                        .thenComparing(CategoryFeedItemResponse::id),     // asc
                OVERVIEW_TOP_N
        );


        List<CategoryFeedItemResponse> closingToday = limitTopN(
                merge(
                        eventRepository.findClosingTodayList(today, likeDescIdDesc),
                        popupRepository.findClosingTodayList(today, likeDescIdDesc),
                        userId
                ),
                Comparator.comparing(CategoryFeedItemResponse::likeCount, Comparator.reverseOrder())
                        .thenComparing(CategoryFeedItemResponse::id, Comparator.reverseOrder()),
                OVERVIEW_TOP_N
        );

        List<CategoryFeedItemResponse> upcoming = limitTopN(
                merge(
                        eventRepository.findUpcomingList(today),
                        popupRepository.findUpcomingList(today),
                        userId
                ),
                Comparator.comparing(CategoryFeedItemResponse::startDate) // asc
                        .thenComparing(CategoryFeedItemResponse::id),
                OVERVIEW_TOP_N
        );

        return EventOverviewResponse.of(popular, ongoing, closingToday, upcoming);
    }

    /**
     * /api/events?filter=... (filter 있음)
     * → 이벤트+팝업 전부 합쳐 정렬, 제한 없이 전부 반환 (리스트)
     * 컨트롤러에서 이 메서드를 호출 중
     */
    public List<CategoryFeedItemResponse> getEventsByFilter(FilterType filter, @Nullable Long userId) {
        if (filter == null) throw new HandleInvalidFilterException(buildAllowedMessage());

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Sort likeDescIdDesc = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "id"));
        Sort startAscIdAsc = Sort.by(Sort.Direction.ASC, "startDate")
                .and(Sort.by(Sort.Direction.ASC, "id"));

        List<CategoryFeedItemResponse> merged;
        Comparator<CategoryFeedItemResponse> comparator;

        switch (filter) {
            case POPULAR: // = ONGOING 정렬
            case ONGOING:
                merged = merge(
                        eventRepository.findOngoingList(today),
                        popupRepository.findOngoingList(today),
                        userId
                );
                comparator = Comparator
                        .comparing(CategoryFeedItemResponse::likeCount, Comparator.reverseOrder())
                        .thenComparing(CategoryFeedItemResponse::id, Comparator.reverseOrder());
                break;

            case CLOSING_TODAY:
                merged = merge(
                        eventRepository.findClosingTodayList(today, likeDescIdDesc),
                        popupRepository.findClosingTodayList(today, likeDescIdDesc),
                        userId
                );
                comparator = Comparator
                        .comparing(CategoryFeedItemResponse::likeCount, Comparator.reverseOrder())
                        .thenComparing(CategoryFeedItemResponse::id, Comparator.reverseOrder());
                break;

            case UPCOMING:
                merged = merge(
                        eventRepository.findUpcomingList(today),
                        popupRepository.findUpcomingList(today),
                        userId
                );
                comparator = Comparator
                        .comparing(CategoryFeedItemResponse::startDate) // asc
                        .thenComparing(CategoryFeedItemResponse::id);
                break;

            default:
                throw new HandleInvalidFilterException(buildAllowedMessage());
        }

        return merged.stream().sorted(comparator).toList();
    }

    /**
     * 필요하면 유지 — 같은 로직을 래핑해 MixedFeedListResponse로도 반환 가능
     */
    public MixedFeedListResponse getMixedByFilter(FilterType filter, @Nullable Long userId) {
        return MixedFeedListResponse.of(getEventsByFilter(filter, userId));
    }

    /* ======================= 내부 유틸 ======================= */

    // Event/Popup → CategoryFeedItemResponse 로 매핑 후 합치기
    private List<CategoryFeedItemResponse> merge(List<Event> events,
                                                 List<Popup> popups,
                                                 @Nullable Long userId) {
        Stream<CategoryFeedItemResponse> es = events.stream().map(e -> toItem(e, userId));
        Stream<CategoryFeedItemResponse> ps = popups.stream().map(p -> toItem(p, userId));
        return Stream.concat(es, ps).toList();
    }

    // 정렬 후 상위 N 개만
    private List<CategoryFeedItemResponse> limitTopN(List<CategoryFeedItemResponse> items,
                                                     Comparator<CategoryFeedItemResponse> comparator,
                                                     int n) {
        if (items.isEmpty()) return items;
        return items.stream().sorted(comparator).limit(n).toList();
    }

    private CategoryFeedItemResponse toItem(Event e, @Nullable Long userId) {
        boolean liked = userId != null && favoriteEventRepository.existsByUserIdAndEventId(userId, e.getId());
        return new CategoryFeedItemResponse(
                FeedItemType.EVENT,
                e.getId(), e.getName(), e.getThumbnail(),
                e.getLikeCount(), liked,
                e.getDescription(), e.getStartDate(), e.getEndDate()
        );
    }

    private CategoryFeedItemResponse toItem(Popup p, @Nullable Long userId) {
        boolean liked = userId != null && favoritePopupRepository.existsByUserIdAndPopupId(userId, p.getId());
        return new CategoryFeedItemResponse(
                FeedItemType.POPUP,
                p.getId(), p.getName(), p.getThumbnail(),
                p.getLikeCount(), liked,
                p.getDescription(), p.getStartDate(), p.getEndDate()
        );
    }

    private String buildAllowedMessage() {
        String allowed = Arrays.stream(FilterType.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
        return "유효하지 않은 필터 값입니다. (" + allowed + " 중 하나)";
    }


    /* ======================= 수민- 이벤트 디테일 반환 ======================= */

    @Transactional(readOnly = true)
    public EventDetailResponse getEventDetail(Long eventId, @Nullable Long userId) {
        // 1) fetch-join 메서드로 단건 로딩
        Event e = eventRepository.findDetailById(eventId)
                .orElseThrow(() -> new EventNotFoundException(eventId, "해당 ID의 이벤트를 찾을 수 없습니다."));

        // 2) liked 계산 (비로그인 안전)
        boolean liked = (userId != null) && favoriteEventRepository.existsByUserIdAndEventId(userId, eventId);

        // 3) 이미지 널 가드
        List<String> images = (e.getImages() != null) ? e.getImages() : List.of();

        // 4) 스토어 널 가드
        var store = e.getStore();
        EventDetailResponse.StoreSimpleDto storeDto = null;
        if (store != null) {
            storeDto = EventDetailResponse.StoreSimpleDto.builder()
                    .storeId(store.getId())
                    .storeName(store.getName())
                    .address(store.getAddress())
                    .phone(store.getNumber())
                    .storeImageUrl(store.getThumbnail())
                    .build();
        }

        // 5) DTO 조립 (시간/문자열은 그대로 null 허용)
        return EventDetailResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .description(e.getDescription())
                .intro(e.getIntro())
                .thumbnail(e.getThumbnail())
                .images(images)
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .likeCount(e.getLikeCount())
                .liked(liked)
                .store(storeDto)
                .build();
    }


    private List<String> parseImages(String images) {
        if (images == null || images.isBlank()) return List.of();
        if (!images.contains(",")) return List.of(images.trim());
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }




}

