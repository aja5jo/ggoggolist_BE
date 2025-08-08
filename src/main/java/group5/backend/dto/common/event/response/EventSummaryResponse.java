package group5.backend.dto.common.event.response;

import group5.backend.domain.event.Event;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class EventSummaryResponse {
    private Long id;
    private String name;
    private String thumbnail;
    private String description;
    private int likeCount;
    private boolean liked;
    private LocalDate startDate;
    private LocalDate endDate;

    public static EventSummaryResponse from(Event event, boolean liked) {
        return EventSummaryResponse.builder()
                .id(event.getId())
                .name(event.getName())
                .thumbnail(event.getThumbnail())
                .description(event.getDescription())
                .likeCount(event.getLikeCount())
                .liked(liked)
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .build();
    }
}
