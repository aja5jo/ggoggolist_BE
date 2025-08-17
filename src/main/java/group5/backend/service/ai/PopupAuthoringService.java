// group5.backend.service.ai.PopupAuthoringService.java
package group5.backend.service.ai;

import group5.backend.domain.user.User;
import group5.backend.dto.ai.PopupAiCreateRequest;
import group5.backend.dto.ai.AiPreviewResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PopupAuthoringService {

    private final OpenAiCopyService copyService;

    @Transactional(readOnly = true)
    public AiPreviewResponse previewCopy(User merchant, PopupAiCreateRequest req) {
        if (merchant == null) throw new org.springframework.security.access.AccessDeniedException("로그인이 필요합니다.");

        List<String> images = (req.getImageUrls() == null || req.getImageUrls().isEmpty())
                ? List.of() : req.getImageUrls();

        var copy = images.isEmpty()
                ? copyService.generatePopupCopy(
                req.getName(),
                req.getName(),
                req.getCategory(),
                req.getAddress(),
                req.getIntroHint()
        )
                : copyService.generatePopupCopy(
                req.getName(),
                req.getName(),
                req.getCategory(),
                req.getAddress(),
                req.getIntroHint(),
                images
        );

        return AiPreviewResponse.builder()
                .intro(copy.getIntro())
                .description(copy.getDescription())
                .build();
    }
}
