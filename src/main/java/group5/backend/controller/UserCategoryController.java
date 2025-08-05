package group5.backend.controller;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryListResponse;
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

    @Secured("ROLE_USER")
    @PostMapping("/{category}")
    public ResponseEntity<CategoryListResponse> toggleCategory(
            @AuthenticationPrincipal User loginUser,
            @PathVariable Category category
    ) {
        CategoryListResponse response = userCategoryService.toggleCategory(loginUser, category);
        return ResponseEntity.ok(response);
    }

    @Secured("ROLE_USER")
    @GetMapping
    public ResponseEntity<CategoryListResponse> getUserCategories(
            @AuthenticationPrincipal User loginUser
    ) {
        CategoryListResponse response = userCategoryService.getAllCategories(loginUser);
        return ResponseEntity.ok(response);
    }
}

