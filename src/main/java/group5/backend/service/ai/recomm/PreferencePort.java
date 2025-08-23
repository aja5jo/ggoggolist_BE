package group5.backend.service.ai.recomm;

import java.util.List;
import java.util.Set;

/** 선호/이력 조회용 포트 (DB/레디스/기존 서비스로 구현체만 갈아끼우면 됨) */
public interface PreferencePort {
    /** 사용자의 관심 카테고리 집합 (enum name 문자열, 예: "FOOD", "CAFE") */
    Set<String> preferredCategories(Long userId);

    /** 사용자가 '좋아요'한 아이템 id 목록 */
    List<Long> likedStoreIds(Long userId);
    List<Long> likedEventIds(Long userId);
    List<Long> likedPopupIds(Long userId);
}
