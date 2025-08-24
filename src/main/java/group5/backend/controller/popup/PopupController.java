package group5.backend.controller.popup;

import group5.backend.domain.user.User;
import group5.backend.dto.common.popup.request.PopupCreateRequest;
import group5.backend.dto.common.popup.request.PopupUpdateRequest;
import group5.backend.dto.common.popup.response.PopupCreateResponse;
import group5.backend.dto.common.popup.response.PopupSummaryResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.popup.PopupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/popups")
// @PreAuthorize("hasAuthority('MERCHANT')")
@Tag(name = "소상공인: 팝업 관리", description = "소상공인의 팝업 CRUD")
public class PopupController {

    private final PopupService popupService;

    @Operation(summary = "팝업 등록", description = "새로운 팝업을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<PopupCreateResponse>> create(
            @Valid @RequestBody PopupCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        PopupCreateResponse res = popupService.createPopup(user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "팝업 등록 성공", res));
    }

    @Operation(summary = "내 팝업 목록 조회", description = "내가 등록한 팝업 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PopupSummaryResponse>>> myPopups(
            @AuthenticationPrincipal User user
    ) {
        List<PopupSummaryResponse> list = popupService.myPopups(user);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "팝업 조회 성공", list));
    }

    @Operation(summary = "팝업 전체 수정", description = "팝업의 모든 정보를 수정합니다.")
    @PutMapping("/{popupId}")
    public ResponseEntity<ApiResponse<PopupCreateResponse>> updatePut(
            @Parameter(description = "수정할 팝업 ID") @PathVariable Long popupId,
            @Valid @RequestBody PopupCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        PopupCreateResponse res = popupService.updatePopupPut(user, popupId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "팝업 전체 수정 성공", res));
    }

    @Operation(summary = "팝업 부분 수정", description = "팝업의 일부 정보를 수정합니다.")
    @PatchMapping("/{popupId}")
    public ResponseEntity<ApiResponse<PopupCreateResponse>> updatePatch(
            @Parameter(description = "수정할 팝업 ID") @PathVariable Long popupId,
            @Valid @RequestBody PopupUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        PopupCreateResponse res = popupService.updatePopupPatch(user, popupId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "팝업 부분 수정 성공", res));
    }

    @Operation(summary = "팝업 삭제", description = "내가 등록한 팝업을 삭제합니다.")
    @DeleteMapping("/{popupId}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @Parameter(description = "삭제할 팝업 ID") @PathVariable Long popupId,
            @AuthenticationPrincipal User user
    ) {
        popupService.deletePopup(user, popupId);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "팝업 삭제 성공", null));
    }
}
