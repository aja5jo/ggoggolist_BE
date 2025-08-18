package group5.backend.service;

import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.Store;
import group5.backend.dto.category.FeedItemType;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeFeedService {

    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final FavoriteEventRepository favoriteEventRepository;
    private final FavoritePopupRepository favoritePopupRepository;

    /** 미리보기 6개 */
    public List<CategoryFeedItemResponse> getHomePreview(Long userId) {
        return getHomeCards(userId, 6);
    }

    /** 상세 18개 */
    public List<CategoryFeedItemResponse> getHomeDetail(Long userId) {
        return getHomeCards(userId, 18);
    }

    /**
     * 핵심: 가게 + (진행중)이벤트 + (진행중)팝업 섞어서 likeCount desc, id desc로 정렬 후 limit개 반환
     */
    private List<CategoryFeedItemResponse> getHomeCards(Long userId, int limit) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        boolean isLoggedIn = (userId != null);

        // 1) 각 도메인 조회 (이벤트/팝업은 '진행중'만)
        List<Store> stores  = storeRepository.findAll(); // 정렬은 나중에 합쳐서 공통 정렬
        List<Event> events  = eventRepository.findOngoingList(today);
        List<Popup> popups  = popupRepository.findOngoingList(today);

        // 2) 공통 DTO로 매핑
        Stream<CategoryFeedItemResponse> sStream = stores.stream().map(s -> {
            boolean liked = isLoggedIn && favoriteStoreRepository.existsByUserIdAndStoreId(userId, s.getId());
            return new CategoryFeedItemResponse(
                    FeedItemType.STORE,
                    s.getId(),
                    s.getName(),
                    s.getThumbnail(),
                    s.getLikeCount(),
                    liked,
                    s.getIntro(),       // description에 intro 매핑
                    null, null          // 가게는 날짜 없음
            );
        });

        Stream<CategoryFeedItemResponse> eStream = events.stream().map(e -> {
            boolean liked = isLoggedIn && favoriteEventRepository.existsByUserIdAndEventId(userId, e.getId());
            return new CategoryFeedItemResponse(
                    FeedItemType.EVENT,
                    e.getId(),
                    e.getName(),
                    e.getThumbnail(),
                    e.getLikeCount(),
                    liked,
                    e.getDescription(),
                    e.getStartDate(),
                    e.getEndDate()
            );
        });

        Stream<CategoryFeedItemResponse> pStream = popups.stream().map(p -> {
            boolean liked = isLoggedIn && favoritePopupRepository.existsByUserIdAndPopupId(userId, p.getId());
            return new CategoryFeedItemResponse(
                    FeedItemType.POPUP,
                    p.getId(),
                    p.getName(),
                    p.getThumbnail(),
                    p.getLikeCount(),
                    liked,
                    p.getDescription(),
                    p.getStartDate(),
                    p.getEndDate()
            );
        });

        // 3) 섞고 정렬 후 제한
        Comparator<CategoryFeedItemResponse> byLikeDescIdDesc =
                Comparator.comparing(CategoryFeedItemResponse::likeCount, Comparator.reverseOrder())
                        .thenComparing(CategoryFeedItemResponse::id, Comparator.reverseOrder());

        return Stream.concat(Stream.concat(sStream, eStream), pStream)
                .sorted(byLikeDescIdDesc)
                .limit(limit)
                .toList();
    }
}
