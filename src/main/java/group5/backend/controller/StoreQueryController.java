package group5.backend.controller;

import group5.backend.dto.store.StoreDetailResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.StoreQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StoreQueryController {

    private final StoreQueryService storeQueryService;

    @GetMapping("/api/store/{storeId}")
    public ResponseEntity<ApiResponse<StoreDetailResponse>> getStoreDetail(
            @SessionAttribute(name = "USER_ID", required = false) Integer userId,
            @PathVariable Long storeId
    ) {
        var data = storeQueryService.getStoreDetail(userId, storeId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "가게 상세 정보 조회 성공", data));
    }
}
