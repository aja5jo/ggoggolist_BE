package group5.backend.service.ai.recomm;

import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.ai.OpenAiProperties;
import group5.backend.domain.recomm.ItemType;
import group5.backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingPreloadService {

    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;
    private final ItemEmbeddingRepository itemEmbeddingRepository;
    private final OpenAIEmbeddingClient openAIEmbeddingClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;
    private final RankingService rankingService;

    // 첫 번째 요청 시 임베딩을 미리 생성하여 저장하는 메서드
    @PostConstruct
    public void preloadData() {
        // 애플리케이션 시작 시 임베딩을 미리 로드
        List<ItemCandidate> allCandidates = getAllCandidates();  // 모든 후보 아이템을 가져옵니다.
        log.info("[EmbeddingPreloadService] Preloading embeddings for {} candidates.", allCandidates.size());
        rankingService.hydrateMissingEmbeddings(allCandidates);  // 임베딩을 채워 넣기
        log.info("[EmbeddingPreloadService] Embedding preload completed.");
    }

    private List<ItemCandidate> getAllCandidates() {
        List<ItemCandidate> candidates = new ArrayList<>();

        // 매장 아이템
        List<Long> storeIds = storeRepository.findAllStoreIds();
        for (Long storeId : storeIds) {
            candidates.add(new ItemCandidate(ItemType.STORE, storeId, 0, "", "", null, null));
        }

        // 이벤트 아이템
        List<Long> eventIds = eventRepository.findAllEventIds();
        for (Long eventId : eventIds) {
            candidates.add(new ItemCandidate(ItemType.EVENT, eventId, 0, "", "", null, null));
        }

        // 팝업 아이템
        List<Long> popupIds = popupRepository.findAllPopupIds();
        for (Long popupId : popupIds) {
            candidates.add(new ItemCandidate(ItemType.POPUP, popupId, 0, "", "", null, null));
        }

        log.info("[EmbeddingPreloadService] Found {} candidates.", candidates.size());
        return candidates;
    }
}

