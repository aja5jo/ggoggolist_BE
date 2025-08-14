package group5.backend.service;

import group5.backend.domain.Store;
import group5.backend.dto.store.StoreDetailResponse;
import group5.backend.exception.store.StoreNotFoundException;
import group5.backend.repository.FavoriteStoreRepository;
import group5.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreQueryService {

    private final StoreRepository storeRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;

    public StoreDetailResponse getStoreDetail(Integer userId, Long storeId) {
        // 1) 가게 조회 (없으면 404)
        Store s = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreNotFoundException("해당 가게를 찾을 수 없습니다."));

        // 2) 로그인 상태면 '내가 찜했는지' 여부
        boolean liked = (userId != null)
                && favoriteStoreRepository.existsByUser_IdAndStore_Id(userId, s.getId());

        // 3) Entity -> DTO 매핑 (명세 스키마)
        return StoreDetailResponse.builder()
                .id(storeId)
                .name(s.getName())
                .address(s.getAddress())
                .number(s.getNumber())
                .intro(s.getIntro())
                .category(s.getCategory() != null ? s.getCategory().name() : null)
                .thumbnail(s.getThumbnail())
                .images(s.getImages() == null ? "" : s.getImages()) // 콤마 문자열 그대로
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .likeCount(s.getLikeCount() == null ? 0 : s.getLikeCount())
                .liked(liked)
                .build();
    }
}
