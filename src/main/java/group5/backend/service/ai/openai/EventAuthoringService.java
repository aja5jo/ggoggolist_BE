package group5.backend.service.ai.openai;

import group5.backend.domain.store.Store;
import group5.backend.domain.user.User;
import group5.backend.dto.ai.request.EventAiCreateRequest;
import group5.backend.dto.ai.response.AiPreviewResponse;
import group5.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EventAuthoringService {

    private final StoreRepository storeRepository;
    private final OpenAiCopyService copyService;

    @Transactional(readOnly = true)
    public AiPreviewResponse previewCopy(User merchant, EventAiCreateRequest req) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
                .orElseThrow(() -> new NoSuchElementException("해당 사용자에 연결된 매장이 없습니다."));

        List<String> images = (req.getImageUrls() == null || req.getImageUrls().isEmpty())
                ? List.of() : req.getImageUrls();

        var copy = images.isEmpty()
                ? copyService.generateEventCopy(
                req.getName(),
                store.getName(),
                req.getCategory(),
                req.getAddress(),
                req.getIntroHint()
        )
                : copyService.generateEventCopy(
                req.getName(),
                store.getName(),
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
