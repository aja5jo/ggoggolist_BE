package group5.backend.controller;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryListResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.UserCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/categories")
public class UserCategoryController {

    private final UserCategoryService userCategoryService;

    @Secured("USER")
    @PostMapping("/{category}")
    public ResponseEntity<ApiResponse<CategoryListResponse>> toggleCategory(
            @AuthenticationPrincipal User loginUser,
            @PathVariable Category category
    ) {
        CategoryListResponse response = userCategoryService.toggleCategory(loginUser, category);
        return ResponseEntity.ok(
                new ApiResponse<>(true, 200, "카테고리 토글 성공", response)
        );
    }

    @Secured("USER")
    //@GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getUserCategories(
            @AuthenticationPrincipal User loginUser
    ) {
        CategoryListResponse response = userCategoryService.getAllCategories(loginUser);
        return ResponseEntity.ok(
                new ApiResponse<>(true, 200, "카테고리 조회 성공", response)
        );
    }

}

