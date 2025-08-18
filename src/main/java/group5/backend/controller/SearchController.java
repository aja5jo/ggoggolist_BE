package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.common.home.StoreEventResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.SearchQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SearchController {

    private final SearchQueryService searchQueryService;

    @Operation(
            summary = "가게/이벤트 통합 검색",
            description = "가게명·이벤트명에 검색어가 포함된 결과를 반환(대소문자 무시). 이벤트는 진행 중인 것만."
    )
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<StoreEventResponse>> search(
            @Parameter(description = "검색어(부분 일치)") @RequestParam(name = "keyword") String keyword,
            @AuthenticationPrincipal User loginUser
    ) {
        // 빈 검색어 400 처리
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, 400, "검색어는 필수입니다.", null));
        }
        Long userId = (loginUser != null) ? loginUser.getId() : null;

        StoreEventResponse data = searchQueryService.search(keyword, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "검색 성공", data));
    }
}