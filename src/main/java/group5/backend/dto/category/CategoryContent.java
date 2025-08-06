package group5.backend.dto.category;

import group5.backend.dto.common.event.response.EventSummaryResponse;
import group5.backend.dto.common.store.response.StoreSummaryResponse;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class CategoryContent {
    private String category;
    private List<StoreSummaryResponse> stores;
    private List<EventSummaryResponse> events;
}
