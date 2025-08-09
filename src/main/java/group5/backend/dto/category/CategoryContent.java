package group5.backend.dto.category;

import group5.backend.domain.user.Category;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.dto.common.event.response.EventSummaryResponse;
import group5.backend.dto.common.store.response.StoreSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryContent {

    private String category;  // ex: "CAFE"
    private List<CategoryFeedItemResponse> items; // 합산 정렬된 리스트만 유지

    public static CategoryContent of(Category category,
                                     List<CategoryFeedItemResponse> items) {
        return CategoryContent.builder()
                .category(category.name())
                .items(items == null ? List.of() : items)
                .build();
    }
}



