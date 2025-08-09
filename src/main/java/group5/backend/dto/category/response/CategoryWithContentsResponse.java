package group5.backend.dto.category.response;

import group5.backend.dto.category.CategoryContent;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CategoryWithContentsResponse {
    private List<CategoryContent> categories;

    public static CategoryWithContentsResponse of(List<CategoryContent> categories) {
        return CategoryWithContentsResponse.builder()
                .categories(categories == null ? List.of() : categories)
                .build();
    }
}


