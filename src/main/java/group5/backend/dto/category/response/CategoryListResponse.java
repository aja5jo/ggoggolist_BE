package group5.backend.dto.category.response;

import group5.backend.domain.user.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class CategoryListResponse {
    private List<Category> categories;
}
