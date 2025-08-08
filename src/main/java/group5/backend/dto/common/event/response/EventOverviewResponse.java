package group5.backend.dto.common.event.response;

import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.dto.common.popup.response.PopupSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class EventOverviewResponse {
    private List<CategoryFeedItemResponse> popular;       // EVENT/POPUP 섞임
    private List<CategoryFeedItemResponse> ongoing;       // "
    private List<CategoryFeedItemResponse> closingToday;  // "
    private List<CategoryFeedItemResponse> upcoming;      // "

    public static EventOverviewResponse of(
            List<CategoryFeedItemResponse> popular,
            List<CategoryFeedItemResponse> ongoing,
            List<CategoryFeedItemResponse> closingToday,
            List<CategoryFeedItemResponse> upcoming
    ) {
        return EventOverviewResponse.builder()
                .popular(popular == null ? List.of() : popular)
                .ongoing(ongoing == null ? List.of() : ongoing)
                .closingToday(closingToday == null ? List.of() : closingToday)
                .upcoming(upcoming == null ? List.of() : upcoming)
                .build();
    }
}
