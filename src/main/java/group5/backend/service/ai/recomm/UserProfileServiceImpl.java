package group5.backend.service.ai.recomm;

import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.ai.OpenAiProperties;
import group5.backend.domain.recomm.ItemType;
import group5.backend.domain.recomm.UserProfileEmbedding;
import group5.backend.repository.ItemEmbeddingRepository;
import group5.backend.repository.UserProfileEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {

    private final UserProfileEmbeddingRepository repo;
    private final ItemEmbeddingRepository itemEmbRepo;
    private final PreferencePort pref;
    private final OpenAIEmbeddingClient openai;
    private final OpenAiProperties props;
    private final ObjectMapper om = new ObjectMapper();

    @Override
    public float[] getOrBuild(Long userId) {
        // 읽기 전용으로 먼저 확인
        var existing = findExistingProfile(userId);
        if (existing != null) {
            log.debug("[UPROF] hit cache userId={}, dim={}", userId, existing.length);
            return existing;
        }

        // 없으면 새 트랜잭션에서 빌드&저장
        return buildAndSaveInNewTransaction(userId);
    }

    @Override
    @Transactional
    public void invalidate(Long userId) {
        repo.deleteById(userId);
    }

    /**
     * 읽기 전용으로 기존 프로필 조회
     */
    @Transactional(readOnly = true)
    private float[] findExistingProfile(Long userId) {
        return repo.findById(userId)
                .map(e -> {
                    log.debug("[UPROF] found cached profile userId={}, model={}, dim={}",
                            userId, e.getModel(), e.getDim());
                    return fromJson(e.getVecJson());
                })
                .orElse(null);
    }

    /**
     * 새로운 쓰기 트랜잭션에서 프로필을 빌드하고 저장
     */
    @Transactional
    private float[] buildAndSaveInNewTransaction(Long userId) {
        return buildAndSave(userId);
    }

    /** 좋아요한 아이템 임베딩들의 '단순 평균'으로 프로필 생성 */
    protected float[] buildAndSave(Long userId) {
        long t0 = System.nanoTime();

        var likedStoreIds = pref.likedStoreIds(userId);
        var likedEventIds = pref.likedEventIds(userId);
        var likedPopupIds = pref.likedPopupIds(userId);
        log.debug("[UPROF] build userId={} likes S/E/P = {}/{}/{}",
                userId,
                (likedStoreIds == null ? 0 : likedStoreIds.size()),
                (likedEventIds == null ? 0 : likedEventIds.size()),
                (likedPopupIds == null ? 0 : likedPopupIds.size()));

        // 1) 좋아요 임베딩 수집
        List<float[]> vecs = new ArrayList<>();
        addTypeVectors(vecs, ItemType.STORE, likedStoreIds);
        addTypeVectors(vecs, ItemType.EVENT, likedEventIds);
        addTypeVectors(vecs, ItemType.POPUP, likedPopupIds);

        float[] profile;
        if (!vecs.isEmpty()) {
            profile = mean(vecs, openai.dim());
            l2norm(profile);
            log.debug("[UPROF] profile from likes, dim={}", profile.length);
        } else {
            // 2) 좋아요가 전혀 없으면 관심 카테고리 텍스트 임베딩으로 대체
            var cats = pref.preferredCategories(userId);
            String seed = (cats == null || cats.isEmpty())
                    ? "User is interested in local stores, ongoing events, and popups in the neighborhood."
                    : "User interests: " + String.join(", ", cats) + ". Prefer ongoing events and popups.";
            profile = openai.embed(seed);
            log.debug("[UPROF] profile from fallback text, dim={}", profile.length);
        }

        // 3) 저장 - 실패해도 프로필은 반환
        try {
            save(userId, profile);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            log.debug("[UPROF] saved userId={}, took={}ms", userId, ms);
        } catch (Exception e) {
            long ms = (System.nanoTime() - t0) / 1_000_000;
            log.warn("[UPROF] save failed userId={}, took={}ms, but returning profile anyway. Error: {}",
                    userId, ms, e.getMessage());
        }

        return profile;
    }

    /** 특정 타입 아이디 목록의 임베딩들을 모두 vecs에 추가 */
    private void addTypeVectors(List<float[]> vecs, ItemType type, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        var rows = itemEmbRepo.findByItemTypeAndItemIdInAndModel(type, ids, openai.modelName());
        for (var r : rows) {
            float[] v = fromJson(r.getVecJson());
            if (v != null && v.length == openai.dim()) vecs.add(v);
        }
    }

    private void save(Long userId, float[] vec) {
        try {
            String json = om.writeValueAsString(vec);
            var row = UserProfileEmbedding.builder()
                    .userId(userId)
                    .model(openai.modelName())
                    .dim(props.getEmbeddingDim())
                    .vecJson(json)
                    .build();
            repo.save(row);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save user profile embedding for userId: " + userId, e);
        }
    }

    private float[] fromJson(String s) {
        try { return om.readValue(s, float[].class); }
        catch (Exception e) { throw new RuntimeException("Failed to parse vector JSON", e); }
    }

    // ===== 벡터 유틸 =====
    private static float[] mean(List<float[]> vecs, int dim) {
        float[] acc = new float[dim];
        int n = 0;
        for (float[] v : vecs) {
            if (v == null || v.length != dim) continue;
            for (int i = 0; i < dim; i++) acc[i] += v[i];
            n++;
        }
        if (n == 0) return acc;
        float inv = 1.0f / n;
        for (int i = 0; i < dim; i++) acc[i] *= inv;
        return acc;
    }

    private static void l2norm(float[] a) {
        double s = 0;
        for (float x : a) s += x * x;
        if (s == 0) return;
        float inv = (float) (1.0 / Math.sqrt(s));
        for (int i = 0; i < a.length; i++) a[i] *= inv;
    }
}