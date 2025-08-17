package group5.backend.controller.ai.openai;

import group5.backend.domain.user.User;
import group5.backend.dto.ai.response.AiPreviewResponse;
import group5.backend.dto.ai.request.PopupAiCreateRequest;

import group5.backend.service.ai.openai.PopupAuthoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/popups")
@PreAuthorize("hasAuthority('MERCHANT')")
public class PopupAiController {

    private final PopupAuthoringService authoringService;

    @PostMapping("/preview")
    public ResponseEntity<AiPreviewResponse> preview(
            @AuthenticationPrincipal User merchant,
            @RequestBody @Valid PopupAiCreateRequest payload
    ) {
        var res = authoringService.previewCopy(merchant, payload);
        return ResponseEntity.ok(res);
    }
}
