package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.common.store.response.StoreEventResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.HomeQueryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users/home")
public class HomeQueryController {en

    private final HomeQueryService homeQueryService;

    @Operation(
            summary = "메인 페이지(스토어+이벤트) 조회",
            description = "로그인: 관심 카테고리 기반 / 비로그인: likeCount 높은 순. 이벤트는 진행 중만 포함"
    )
    @GetMapping("/details")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<StoreEventResponse>> getHomeDetails(
            @AuthenticationPrincipal User loginUser
    ) {
        Long userId = (loginUser != null) ? loginUser.getId() : null;
        StoreEventResponse data = homeQueryService.getHomeDetails(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "메인 페이지 조회 성공", data));
    }
}
