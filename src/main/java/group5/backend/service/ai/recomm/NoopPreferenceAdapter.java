package group5.backend.service.ai.recomm;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/** 해커톤 기본: 선호 데이터가 없어도 시스템이 돌아가도록 NOOP 구현 */
@Component
public class NoopPreferenceAdapter implements PreferencePort {
    @Override public Set<String> preferredCategories(Long userId) { return Collections.emptySet(); }
    @Override public List<Long> likedStoreIds(Long userId) { return Collections.emptyList(); }
    @Override public List<Long> likedEventIds(Long userId) { return Collections.emptyList(); }
    @Override public List<Long> likedPopupIds(Long userId) { return Collections.emptyList(); }
}
