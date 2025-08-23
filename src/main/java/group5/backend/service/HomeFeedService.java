package group5.backend.service;

import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.Store;
import group5.backend.dto.category.FeedItemType;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.service.ai.recomm.RankingService;
import group5.backend.service.ai.recomm.RecommendationService;
import group5.backend.service.ai.recomm.CandidateService;
import group5.backend.service.ai.recomm.CategoryFeedAssembler;


import group5.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional()
public class HomeFeedService {

    private final RecommendationService recommendationService;
    private final CandidateService candidateService;
    private final CategoryFeedAssembler categoryFeedAssembler;

    /** 홈 미리보기(6개) */
    public List<CategoryFeedItemResponse> getHomePreview(Long userId) {
        int size = 6;
        log.debug("[HOME] /home start userId={}, size={}", userId, size);
        try {
            if (userId != null) {
                var res = recommendationService.recommendHome(userId, size);
                log.debug("[HOME] /home personalized size={}", res.size());
                return res;
            }
            var candidates = candidateService.findPopularFallback(size);
            var scored = candidates.stream().map(c -> new RankingService.Scored(c, -1.0)).toList();
            var res = categoryFeedAssembler.toResponses(scored, null);
            log.debug("[HOME] /home popular size={}", res.size());
            return res;
        } catch (Exception e) {
            log.error("[HOME] /home FAIL userId={}, size={}, msg={}", userId, size, e.toString(), e);
            throw e; // Global이 기존 방식대로 응답 처리
        }
    }

    /** 홈 더보기(18개) */
    public List<CategoryFeedItemResponse> getHomeDetail(Long userId) {
        int size = 18;
        log.debug("[HOME] /home/detail start userId={}, size={}", userId, size);
        try {
            if (userId != null) {
                var res = recommendationService.recommendHome(userId, size);
                log.debug("[HOME] /home/detail personalized size={}", res.size());
                return res;
            }
            var candidates = candidateService.findPopularFallback(size);
            var scored = candidates.stream().map(c -> new RankingService.Scored(c, -1.0)).toList();
            var res = categoryFeedAssembler.toResponses(scored, null);
            log.debug("[HOME] /home/detail popular size={}", res.size());
            return res;
        } catch (Exception e) {
            log.error("[HOME] /home/detail FAIL userId={}, size={}, msg={}", userId, size, e.toString(), e);
            throw e;
        }
    }
}