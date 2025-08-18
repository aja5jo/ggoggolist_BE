package group5.backend.controller;

import group5.backend.dto.common.store.response.StoreDetailResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.StoreQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/store")
public class StoreQueryController {

    private final StoreQueryService storeQueryService;

    @Operation(summary = "스토어 상세 조회", description = "로그인 시 liked 반영")
    @GetMapping("/{storeId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetail(
            @PathVariable Long storeId,
            @AuthenticationPrincipal group5.backend.domain.user.User loginUser // 프로젝트의 사용자 타입으로 교체 가능
    ) {
        Long userId = (loginUser == null) ? null : loginUser.getId();
        var data = storeQueryService.getStoreDetail(storeId, userId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "스토어 상세 조회 성공", data));
    }
}