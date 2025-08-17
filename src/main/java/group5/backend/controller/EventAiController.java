// group5.backend.controller.EventAiController.java
package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.ai.EventAiCreateRequest;
import group5.backend.dto.ai.AiPreviewResponse;
import group5.backend.repository.StoreRepository;
import group5.backend.service.ai.EventAuthoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/stores/events")
@PreAuthorize("hasAuthority('MERCHANT')")
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
