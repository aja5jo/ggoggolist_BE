package group5.backend.exception.category;

import group5.backend.domain.user.Category;
import java.util.List;

public class UserInvalidCategorySizeException extends RuntimeException {
    private final List<Category> currentCategories;

    public UserInvalidCategorySizeException(String message, List<Category> currentCategories) {
        super(message);
        this.currentCategories = currentCategories;
    }

    public List<Category> getCurrentCategories() {
        return currentCategories;
    }
}

