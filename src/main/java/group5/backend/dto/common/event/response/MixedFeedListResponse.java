package group5.backend.dto.common.event.response;

import group5.backend.dto.category.response.CategoryFeedItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MixedFeedListResponse {
    // EVENT/POPUP 섞여서 내려감
    private List<CategoryFeedItemResponse> items;

    public static MixedFeedListResponse of(List<CategoryFeedItemResponse> items) {
        return MixedFeedListResponse.builder()
                .items(items == null ? List.of() : items)
                .build();
    }
}
