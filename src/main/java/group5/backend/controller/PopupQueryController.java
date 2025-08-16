package group5.backend.controller;

import group5.backend.dto.common.popup.response.PopupDetailResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.PopupQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import group5.backend.domain.user.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/popup")
public class PopupQueryController {

    private final PopupQueryService popupQueryService;


    @GetMapping("/{popupId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PopupDetailResponse>> getPopupDetail(
            @PathVariable Long popupId,
            @AuthenticationPrincipal group5.backend.domain.user.User loginUser
    ) {
        Long userId = (loginUser == null) ? null : loginUser.getId();
        PopupDetailResponse data = popupQueryService.getPopupDetail(userId, popupId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "팝업 상세 정보 조회", data));
    }


}
