package group5.backend.dto.common.home;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StoreEventResponse {
    private List<StoreSummaryResponse> stores;
    private List<EventSummaryResponse> events;
}
