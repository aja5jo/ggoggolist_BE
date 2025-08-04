package group5.backend.service;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.Role;
import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryListResponse;
import group5.backend.exception.category.UserCategoryAccessDeniedException;
import group5.backend.exception.category.UserNotFoundException;
import group5.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserCategoryService {

    private final UserRepository userRepository;

    public CategoryListResponse toggleCategory(Long userId, Category category) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));

        // 🔐 USER 권한만 허용
        if (user.getRole() != Role.USER) {
            throw new UserCategoryAccessDeniedException("일반 사용자만 관심 카테고리를 설정할 수 있습니다.");
        }

        user.toggleCategory(category);
        userRepository.save(user);

        return new CategoryListResponse(user.getCategories());
    }

    public CategoryListResponse getAllCategories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));

        // 🔐 USER 권한만 허용
        if (user.getRole() != Role.USER) {
            throw new UserCategoryAccessDeniedException("일반 사용자만 관심 카테고리를 조회할 수 있습니다.");
        }

        return new CategoryListResponse(user.getCategories());
    }
}

