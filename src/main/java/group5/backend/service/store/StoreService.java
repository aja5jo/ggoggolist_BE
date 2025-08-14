package group5.backend.service.store;

import group5.backend.domain.store.Store;
import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.common.store.request.StoreCreateRequest;
import group5.backend.dto.common.store.request.StoreUpdateRequest;
import group5.backend.dto.common.store.response.StoreCreateResponse;
import group5.backend.dto.common.store.response.StoreDetailResponse;
import group5.backend.dto.common.store.response.StoreSummaryResponse;
import group5.backend.repository.StoreRepository;
import group5.backend.service.category.MerchantCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final MerchantCategoryService merchantCategoryService;

    @Transactional
    public StoreCreateResponse createStore(User merchant, StoreCreateRequest req) {
        if (storeRepository.existsByOwnerId(merchant.getId())) {
            throw new IllegalStateException("이미 등록된 가게가 있습니다.");
        }
        Category category = Category.valueOf(req.getCategory());
        merchantCategoryService.setMerchantCategory(merchant, category);

        Store entity = Store.builder()
                .owner(merchant)
                .name(req.getName())
                .address(req.getAddress())
                .number(req.getNumber())
                .intro(req.getIntro())
                .category(category)
                .thumbnail(req.getThumbnail())
                .images(req.getImages())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .likeCount(0)
                .build();

        Store saved = storeRepository.save(entity);
        return toCreateResponse(saved);
    }


    @Transactional(readOnly = true)
    public StoreSummaryResponse getMyStore(User merchant) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없는 사용자입니다."));
        return toSummaryResponse(store, false);
    }

    @Transactional
    public StoreDetailResponse updateStorePatch(User merchant, StoreUpdateRequest req) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없습니다."));

        if (req.getCategory() != null) {
            Category category = Category.valueOf(req.getCategory());
            merchantCategoryService.setMerchantCategory(merchant, category);
            store.setCategory(category);
        }
        if (req.getName() != null)      store.setName(req.getName());
        if (req.getAddress() != null)   store.setAddress(req.getAddress());
        if (req.getNumber() != null)    store.setNumber(req.getNumber());
        if (req.getIntro() != null)     store.setIntro(req.getIntro());
        if (req.getThumbnail() != null) store.setThumbnail(req.getThumbnail());
        if (req.getImages() != null)    store.setImages(req.getImages());
        if (req.getStartTime() != null) store.setStartTime(req.getStartTime());
        if (req.getEndTime() != null)   store.setEndTime(req.getEndTime());

        return toDetailResponse(store, false);
    }

    @Transactional
    public void deleteStore(User merchant, Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new AccessDeniedException("가게를 찾을 수 없습니다."));
        if (!store.getOwner().getId().equals(merchant.getId())) {
            throw new AccessDeniedException("본인의 가게만 삭제할 수 있습니다.");
        }
        storeRepository.delete(store); // FK가 DB에서 자동 정리
    }

    private StoreCreateResponse toCreateResponse(Store s) {
        return StoreCreateResponse.builder()
                .id(s.getId())
                .ownerId(s.getOwner().getId())
                .name(s.getName())
                .address(s.getAddress())
                .number(s.getNumber())
                .intro(s.getIntro())
                .category(s.getCategory() != null ? s.getCategory().name() : null)
                .thumbnail(s.getThumbnail())
                .images(s.getImages())
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .build();
    }

    private StoreDetailResponse toDetailResponse(Store s, boolean liked) {
        return StoreDetailResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .address(s.getAddress())
                .number(s.getNumber())
                .intro(s.getIntro())
                .category(s.getCategory() != null ? s.getCategory().name() : null)
                .thumbnail(s.getThumbnail())
                .images(s.getImages() != null && !s.getImages().isEmpty() ? String.join(",", s.getImages()) : null)
                .startTime(s.getStartTime())
                .endTime(s.getEndTime())
                .likeCount(s.getLikeCount())
                .liked(liked)
                .build();
    }

    private StoreSummaryResponse toSummaryResponse(Store s, boolean liked) {
        return StoreSummaryResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .thumbnail(s.getThumbnail())
                .likeCount(s.getLikeCount())
                .liked(liked)
                .build();
    }
}
