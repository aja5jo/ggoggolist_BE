package group5.backend.service;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryListResponse;
import group5.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantCategoryService {

    private final UserRepository userRepository;

    public CategoryListResponse setMerchantCategory(User loginUser, Category category) {
        loginUser.setMerchantCategory(category); // 무조건 1개 설정
        userRepository.save(loginUser);
        return CategoryListResponse.from(loginUser.getCategories());
    }

    // 가게 카테고리 조회
    public CategoryListResponse getMerchantCategory(User loginUser) {
        return CategoryListResponse.from(loginUser.getCategories());
    }
}


