package group5.backend.controller.ai.openai;

import group5.backend.domain.user.User;
import group5.backend.dto.ai.request.EventAiCreateRequest;
import group5.backend.dto.ai.response.AiPreviewResponse;
import group5.backend.repository.StoreRepository;
import group5.backend.service.ai.openai.EventAuthoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/stores/events")
// @PreAuthorize("hasAuthority('MERCHANT')")
public class EventAiController {

    private final EventAuthoringService authoringService;
    private final StoreRepository storeRepository;

    @PostMapping("/preview")
    public ResponseEntity<AiPreviewResponse> preview(
            @AuthenticationPrincipal User merchant,
            @RequestBody @Valid EventAiCreateRequest payload
    ) {
        var res = authoringService.previewCopy(merchant, payload);
        return ResponseEntity.ok(res);
    }
}
