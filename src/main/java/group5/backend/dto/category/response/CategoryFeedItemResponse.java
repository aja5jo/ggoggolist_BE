package group5.backend.dto.category.response;


import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.Store;
import group5.backend.dto.category.FeedItemType;

import java.time.LocalDate;

public record CategoryFeedItemResponse(
        FeedItemType type,     // STORE, EVENT, POPUP
        Long id,
        String name,
        String thumbnail,
        Integer likeCount,
        boolean liked,
        // EVENT/POPUP일 때만 채움
        String description,
        LocalDate startDate,
        LocalDate endDate
) {
    public static CategoryFeedItemResponse fromStore(Store s, boolean liked) {
        return new CategoryFeedItemResponse(
                FeedItemType.STORE,
                s.getId(),
                s.getName(),
                s.getThumbnail(),
                s.getLikeCount(),
                liked,
                null, null, null
        );
    }

    public static CategoryFeedItemResponse fromEvent(Event e, boolean liked) {
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
    }

    public static CategoryFeedItemResponse fromPopup(Popup p, boolean liked) {
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
    }
}

