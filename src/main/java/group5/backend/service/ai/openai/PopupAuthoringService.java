package group5.backend.service.ai.openai;

import group5.backend.domain.user.User;
import group5.backend.dto.ai.request.PopupAiCreateRequest;
import group5.backend.dto.ai.response.AiPreviewResponse;
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
