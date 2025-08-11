package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.PopupService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/popups")
@RequiredArgsConstructor
public class PopupController {

    private final PopupService popupService;

    @Operation(
            summary = "이번 주 팝업 스테이션 조회",
            description = "이번 주(월~일)에 진행 중인 모든 팝업 스테이션을 반환"
    )
    @GetMapping
    public ResponseEntity<ApiResponse> getThisWeekPopups(@AuthenticationPrincipal User loginUser) {
        Long userId = (loginUser != null) ? loginUser.getId() : null;
        return ResponseEntity.ok(
                new ApiResponse(true, 200, "이번 주 팝업 스테이션 조회 성공",
                        popupService.getThisWeekPopups(userId))
        );
    }
}
