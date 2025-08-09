package group5.backend.dto.common.event.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventPageResponse {
    private List<EventSummaryResponse> items;
    private int page;           // 0-based
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean hasNext;

    public static EventPageResponse from(Page<EventSummaryResponse> page) {
        return EventPageResponse.builder()
                .items(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .build();
    }
}