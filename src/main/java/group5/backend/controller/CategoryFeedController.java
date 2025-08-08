package group5.backend.controller;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.category.CategoryContent;
import group5.backend.dto.category.response.CategoryWithContentsResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.CategoryFeedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryFeedController {

    private final CategoryFeedService categoryFeedService;

    @Operation(
            summary = "카테고리별 스토어/이벤트 조회",
            description = """
            - 비로그인: 모든 카테고리(알파벳 순)
            - USER: 관심 카테고리 최대 3개 먼저(알파벳) → 나머지
            - MERCHANT: 본인 카테고리 1개 먼저 → 나머지
            - 각 카테고리마다 스토어+이벤트 합쳐서 4개 반환(이벤트는 진행 중만)
            """
    )
    @GetMapping
    public ResponseEntity<ApiResponse> getCategoryFeed(
            @AuthenticationPrincipal User loginUser // 비로그인 시 null
    ) {
        final int TOTAL_LIMIT_PER_CATEGORY = 4; // 고정값

        CategoryWithContentsResponse responseBody =
                categoryFeedService.buildCategoryFeed(loginUser, TOTAL_LIMIT_PER_CATEGORY);

        return ResponseEntity.ok(
                new ApiResponse(true, 200, "전체 카테고리 목록 조회 성공", responseBody)
        );
    }

    @Operation(
            summary = "단일 카테고리 피드 조회 (가게+이벤트 합산 정렬)",
            description = """
            - path로 받은 카테고리에 한해, 가게/이벤트를 likeCount desc, id desc로 합쳐 정렬해 '전부' 반환합니다.
            - 이벤트는 진행 중(startDate ≤ today ≤ endDate)만 포함합니다.
            - 응답은 { category, items[] } 형태이며 items는 STORE/EVENT가 섞여 있습니다.
            """
    )
    @GetMapping("/{category}")
    public ResponseEntity<ApiResponse> getCategoryFeedByCategory(
            @AuthenticationPrincipal User loginUser,
            @Parameter(description = "카테고리명 (예: CAFE, FOOD, ...)", required = true, example = "CAFE")
            @PathVariable("category") Category category
    ) {
        CategoryContent content = categoryFeedService.buildSingleCategoryFeed(loginUser, category);
        return ResponseEntity.ok(new ApiResponse(true, 200, "카테고리 피드 조회 성공", content));
    }
}

