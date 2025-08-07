package group5.backend.controller;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryListResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.UserCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    @Operation(
            summary = "관심 카테고리 토글",
            description = "현재 로그인한 사용자의 관심 카테고리를 선택 또는 해제합니다.",
            security = @SecurityRequirement(name = "JSESSIONID")
    )
    @PostMapping("/{category}")
    public ResponseEntity<ApiResponse> toggleCategory(
            @Parameter(hidden = true) @AuthenticationPrincipal User loginUser,
            @Parameter(description = "토글할 카테고리 (예: CAFE, K_POP 등)", example = "CAFE")
            @PathVariable Category category
    ) {
        CategoryListResponse response = userCategoryService.toggleCategory(loginUser, category);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, 200, "카테고리 토글 성공", response));
    }

    /*@Secured("USER")
    @GetMapping
    public ResponseEntity<ApiResponse<CategoryListResponse>> getUserCategories(
            @AuthenticationPrincipal User loginUser
    ) {
        CategoryListResponse response = userCategoryService.getAllCategories(loginUser);
        return ResponseEntity.ok(
                new ApiResponse<>(true, 200, "카테고리 조회 성공", response)
        );
    }*/
}
