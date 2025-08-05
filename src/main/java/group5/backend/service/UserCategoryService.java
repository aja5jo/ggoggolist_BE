package group5.backend.service;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryListResponse;
import group5.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserCategoryService {

    private final UserRepository userRepository;

    public CategoryListResponse toggleCategory(User loginUser, Category category) {
        loginUser.toggleCategory(category);
        userRepository.save(loginUser);

        return CategoryListResponse.from(loginUser.getCategories());
    }

    public CategoryListResponse getAllCategories(User loginUser) {
        return CategoryListResponse.from(loginUser.getCategories());
    }


}


