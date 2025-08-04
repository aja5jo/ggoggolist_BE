package group5.backend.controller;

import group5.backend.domain.user.Category;
import group5.backend.dto.category.response.CategoryListResponse;
import group5.backend.service.UserCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/{userId}/categories")
public class UserCategoryController {

    private final UserCategoryService userCategoryService;

    // [POST] 카테고리 토글 (추가 또는 삭제)
    @PostMapping("/{category}")
    public ResponseEntity<CategoryListResponse> toggleCategory(
            @PathVariable Long userId,
            @PathVariable Category category
    ) {
        CategoryListResponse response = userCategoryService.toggleCategory(userId, category);
        return ResponseEntity.ok(response);
    }

    // [GET] 전체 카테고리 목록 조회
    @GetMapping
    public ResponseEntity<CategoryListResponse> getUserCategories(
            @PathVariable Long userId
    ) {
        CategoryListResponse response = userCategoryService.getAllCategories(userId);
        return ResponseEntity.ok(response);
    }
}