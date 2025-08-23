package group5.backend.service.ai.recomm;

import group5.backend.dto.category.response.CategoryFeedItemResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final CandidateService candidateService;
    private final UserProfileService userProfileService;
    private final RankingService rankingService;
    private final CategoryFeedAssembler assembler;

    public List<CategoryFeedItemResponse> recommendHome(Long userId, int size) {
        long t0 = System.nanoTime();
        log.debug("[REC] start userId={}, size={}", userId, size);
        try {
            var candidates = candidateService.findForUser(userId, Math.max(size * 50, 300));
            log.debug("[REC] candidates={}", candidates.size());

            var userVec = userProfileService.getOrBuild(userId);
            log.debug("[REC] userVec dim={}", (userVec == null ? 0 : userVec.length));

            var ranked = rankingService.rank(userVec, candidates, size);
            log.debug("[REC] ranked size={} (top ids: {})",
                    ranked.size(),
                    ranked.stream().limit(5).map(s -> s.item().id()).toList());

            if (ranked.size() < size) {
                var fill = candidateService.findPopularFallback(size - ranked.size());
                log.debug("[REC] fallback fill size={}", fill.size());
                for (var c : fill) ranked.add(new RankingService.Scored(c, -1.0));
            }

            var out = assembler.toResponses(ranked, userId);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            log.debug("[REC] done -> out.size={}, took={}ms", out.size(), ms);
            return out;
        } catch (Exception e) {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            log.error("[REC] FAIL userId={}, size={}, took={}ms, msg={}",
                    userId, size, ms, e.toString(), e);
            throw e; // Global이 기존 방식대로 응답 처리
        }
    }
}