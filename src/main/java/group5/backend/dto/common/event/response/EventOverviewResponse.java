package group5.backend.dto.common.event.response;

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
    private List<EventSummaryResponse> popular;
    private List<EventSummaryResponse> ongoing;
    private List<EventSummaryResponse> closingToday;
    private List<EventSummaryResponse> upcoming;

    // 정적 팩토리 메서드
    public static EventOverviewResponse of(
            List<EventSummaryResponse> popular,
            List<EventSummaryResponse> ongoing,
            List<EventSummaryResponse> closingToday,
            List<EventSummaryResponse> upcoming
    ) {
        return EventOverviewResponse.builder()
                .popular(popular)
                .ongoing(ongoing)
                .closingToday(closingToday)
                .upcoming(upcoming)
                .build();
    }
}


