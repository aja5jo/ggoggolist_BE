package group5.backend.controller;

import group5.backend.dto.common.popup.response.PopupDetailResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.PopupQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/popup")
public class PopupQueryController {

    private final PopupQueryService popupQueryService;

    @GetMapping("/{popupId}")
    public ResponseEntity<ApiResponse<PopupDetailResponse>> getPopupDetail(
            @SessionAttribute(name = "USER_ID", required = false) Long userId,
            @PathVariable Long popupId
    ) {
        PopupDetailResponse data = popupQueryService.getPopupDetail(userId, popupId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "팝업 상세 정보 조회", data));
    }


}
