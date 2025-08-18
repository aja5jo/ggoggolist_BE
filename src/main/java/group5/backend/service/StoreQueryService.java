package group5.backend.service;

import group5.backend.domain.store.Store;
import group5.backend.dto.common.store.response.StoreDetailResponse;
import group5.backend.exception.store.StoreNotFoundException;
import group5.backend.repository.FavoriteStoreRepository;
import group5.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreQueryService {

    private final StoreRepository storeRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;

    public StoreDetailResponse getStoreDetail(Long storeId, @jakarta.annotation.Nullable Long userId) {
        Store s = storeRepository.findDetailById(storeId)
                .orElseThrow(() -> new StoreNotFoundException(storeId, "해당 ID의 스토어를 찾을 수 없습니다."));

        boolean liked = (userId != null) && favoriteStoreRepository.existsByUserIdAndStoreId(userId, storeId);

        // 이미지 널 가드
        List<String> images = (s.getImages() != null) ? s.getImages() : List.of();

        return StoreDetailResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .address(s.getAddress())
                .category(s.getCategory().name())
                .intro(s.getIntro())
                .number(s.getNumber())
                .thumbnail(s.getThumbnail())
                .images(images)
                .startTime(s.getStartTime())   // null 허용
                .endTime(s.getEndTime())       // null 허용
                .likeCount(s.getLikeCount())
                .liked(liked)
                .build();
    }

    // 비로그인 기본 동작( liked=false )
    public StoreDetailResponse getStoreDetail(Long storeId) {
        return getStoreDetail(storeId, null);
    }
}