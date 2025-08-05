package group5.backend.dto.category.response;

import group5.backend.domain.user.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CategoryListResponse {

    private int count;
    private List<Category> categories;

    public static CategoryListResponse from(List<Category> categories) {
        return new CategoryListResponse(categories.size(), categories);
    }
    public CategoryListResponse(List<Category> categories) {
        this.categories = categories;
    }
}

