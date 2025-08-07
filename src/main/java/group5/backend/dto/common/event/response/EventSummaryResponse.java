package group5.backend.dto.common.event.response;

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
}
