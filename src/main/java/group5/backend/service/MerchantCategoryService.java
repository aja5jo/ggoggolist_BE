package group5.backend.service;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.Role;
import group5.backend.domain.user.User;
import group5.backend.exception.category.UserCategoryAccessDeniedException;
import group5.backend.exception.category.UserNotFoundException;
import group5.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantCategoryService {

    private final UserRepository userRepository;

    public void setMerchantCategory(Long userId, Category category) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("해당 유저를 찾을 수 없습니다."));

        if (user.getRole() != Role.MERCHANT) {
            throw new UserCategoryAccessDeniedException("소상공인만 가게 카테고리를 설정할 수 있습니다.");
        }

        user.setMerchantCategory(category); // 1개 덮어쓰기
        userRepository.save(user);
    }
}
