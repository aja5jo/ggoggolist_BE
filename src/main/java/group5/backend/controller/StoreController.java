package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.common.store.request.StoreCreateRequest;
import group5.backend.dto.common.store.request.StoreUpdateRequest;
import group5.backend.dto.common.store.response.StoreCreateResponse;
import group5.backend.dto.common.store.response.StoreDetailResponse;
import group5.backend.dto.common.store.response.StoreSummaryResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.StoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/stores")
@PreAuthorize("hasAuthority('MERCHANT')")
public class StoreController {

    private final StoreService storeService;

    @PostMapping
    public ResponseEntity<ApiResponse<StoreCreateResponse>> createStore(
            @Valid @RequestBody StoreCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        StoreCreateResponse response = storeService.createStore(user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "내 가게 등록 성공", response));
    }

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

//    @PutMapping("/{storeId}")
//    public ResponseEntity<ApiResponse<StoreDetailResponse>> updateStorePut(
//            @PathVariable Long storeId,
//            @Valid @RequestBody StoreUpdateRequest request,
//            @AuthenticationPrincipal User user
//    ) {
//        StoreDetailResponse response = storeService.updateStorePut(user, request);
//        return ResponseEntity.ok(new ApiResponse<>(true, 200, "가게 전체 수정 성공", response));
//    }

    @PatchMapping("/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> updateStorePatch(
            @PathVariable Long storeId,
            @Valid @RequestBody StoreUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        StoreDetailResponse response = storeService.updateStorePatch(user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "가게 부분 수정 성공", response));
    }

    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(
            @AuthenticationPrincipal User user,
            @PathVariable Long storeId
    ) {
        storeService.deleteStore(user, storeId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "가게 삭제 성공", null));
    }
}
