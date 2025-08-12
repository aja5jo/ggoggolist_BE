package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.common.store.request.StoreCreateRequest;
import group5.backend.dto.common.store.request.StoreUpdateRequest;
import group5.backend.dto.common.store.response.StoreCreateResponse;
import group5.backend.dto.common.store.response.StoreDetailResponse;
import group5.backend.dto.common.store.response.StoreSummaryResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.StoreService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

@Tag(name = "소상공인: 가게 ", description = "소상공인의 가게 CRUD")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/stores")
@PreAuthorize("hasAuthority('MERCHANT')")
public class StoreController {

    private final StoreService storeService;

    @Operation(summary = "가게 등록", description = "내 가게를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<StoreCreateResponse>> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        StoreCreateResponse response = storeService.createStore(user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "내 가게 등록 성공", response));
    }

    @Operation(summary = "내 가게 조회", description = "내 가게를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<StoreSummaryResponse>> getMyStore(
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new InsufficientAuthenticationException("인증 정보가 없습니다.");
        }
        StoreSummaryResponse response = storeService.getMyStore(user);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "내 가게 조회 성공", response));
    }

    @Operation(summary = "가게 부분 수정", description = "가게의 일부 정보를 수정합니다.")
    @PatchMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> updateStorePatch(
            @Parameter(description = "수정할 가게 ID") @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        StoreDetailResponse response = storeService.updateStorePatch(user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "가게 부분 수정 성공", response));
    }

    @Operation(summary = "가게 삭제", description = "내 가게를 삭제합니다.")
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @AuthenticationPrincipal User user,
            @Parameter(description = "삭제할 가게 ID") @PathVariable Long storeId
    ) {
        storeService.deleteStore(user, storeId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "가게 삭제 성공", null));
    }
}