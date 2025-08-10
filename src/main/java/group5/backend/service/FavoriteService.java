package group5.backend.service;


import group5.backend.domain.event.Event;
import group5.backend.domain.event.FavoriteEvent;
import group5.backend.domain.popup.FavoritePopup;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.FavoriteStore;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.User;
import group5.backend.dto.favorite.FavoriteNameItem;
import group5.backend.dto.favorite.FavoriteType;
import group5.backend.dto.favorite.response.FavoriteNameListResponse;
import group5.backend.dto.favorite.response.FavoriteResponse;
import group5.backend.exception.favorite.FavoriteNotFoundException;
import group5.backend.repository.*;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteStoreRepository favoriteStoreRepository;
    private final FavoriteEventRepository favoriteEventRepository;
    private final FavoritePopupRepository favoritePopupRepository;
    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;


    // 유저의 store 즐겨찾기 토글
    public FavoriteResponse toggleStoreFavorite(User loginUser, Long storeId) {
        // Store를 가져옵니다.
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new FavoriteNotFoundException("Store not found"));

        // FavoriteStore 찾기
        Optional<FavoriteStore> existingFavoriteStore = favoriteStoreRepository.findByUserIdAndStoreId(loginUser.getId(), storeId);

        boolean liked = false;
        if (existingFavoriteStore.isPresent()) {
            // 즐겨찾기에서 제거: liked가 true이면 likeCount 감소
            favoriteStoreRepository.delete(existingFavoriteStore.get());
            store.setLikeCount(store.getLikeCount() - 1);  // likeCount 감소
            liked = false;  // 현재 상태는 제거되었으므로 false
        } else {
            // 즐겨찾기 추가: liked가 false이면 likeCount 증가
            FavoriteStore favoriteStore = FavoriteStore.builder()
                    .user(loginUser)
                    .store(store)
                    .build();
            favoriteStoreRepository.save(favoriteStore);  // 즐겨찾기 추가
            store.setLikeCount(store.getLikeCount() + 1);  // likeCount 증가
            liked = true;  // 현재 상태는 추가되었으므로 true
        }

        storeRepository.save(store);  // 저장된 변경 사항 반영

        return FavoriteResponse.of(store.getId(), "store", liked, store.getLikeCount(),store.getName());
    }

    // 유저의 event 즐겨찾기 토글
    public FavoriteResponse toggleEventFavorite(User loginUser, Long eventId) {
        // Event를 가져옵니다.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new FavoriteNotFoundException("Event not found"));

        // FavoriteEvent 찾기
        Optional<FavoriteEvent> existingFavoriteEvent = favoriteEventRepository.findByUserIdAndEventId(loginUser.getId(), eventId);

        boolean liked = false;
        if (existingFavoriteEvent.isPresent()) {
            // 즐겨찾기에서 제거: liked가 true이면 likeCount 감소
            favoriteEventRepository.delete(existingFavoriteEvent.get());
            event.setLikeCount(event.getLikeCount() - 1);  // likeCount 감소
            liked = false;  // 현재 상태는 제거되었으므로 false
        } else {
            // 즐겨찾기 추가: liked가 false이면 likeCount 증가
            FavoriteEvent favoriteEvent = FavoriteEvent.builder()
                    .user(loginUser)
                    .event(event)
                    .build();
            favoriteEventRepository.save(favoriteEvent);  // 즐겨찾기 추가
            event.setLikeCount(event.getLikeCount() + 1);  // likeCount 증가
            liked = true;  // 현재 상태는 추가되었으므로 true
        }

        eventRepository.save(event);  // 저장된 변경 사항 반영

        return FavoriteResponse.of(event.getId(), "event", liked, event.getLikeCount(),event.getName());
    }

    // 유저의 popup 즐겨찾기 토글
    public FavoriteResponse togglePopupFavorite(User loginUser, Long popupId) {
        // Popup을 가져옵니다.
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new FavoriteNotFoundException("Popup not found"));

        // FavoritePopup 찾기
        Optional<FavoritePopup> existingFavoritePopup = favoritePopupRepository.findByUserIdAndPopupId(loginUser.getId(), popupId);

        boolean liked = false;
        if (existingFavoritePopup.isPresent()) {
            // 즐겨찾기에서 제거: liked가 true이면 likeCount 감소
            favoritePopupRepository.delete(existingFavoritePopup.get());
            popup.setLikeCount(popup.getLikeCount() - 1);  // likeCount 감소
            liked = false;  // 현재 상태는 제거되었으므로 false
        } else {
            // 즐겨찾기 추가: liked가 false이면 likeCount 증가
            FavoritePopup favoritePopup = FavoritePopup.builder()
                    .user(loginUser)
                    .popup(popup)
                    .build();
            favoritePopupRepository.save(favoritePopup);  // 즐겨찾기 추가
            popup.setLikeCount(popup.getLikeCount() + 1);  // likeCount 증가
            liked = true;  // 현재 상태는 추가되었으므로 true
        }

        popupRepository.save(popup);  // 저장된 변경 사항 반영

        return FavoriteResponse.of(popup.getId(), "popup", liked, popup.getLikeCount(),popup.getName());
    }

    // 유저의 즐겨찾기 항목들 (id, type, name만 반환)
    @Transactional
    public FavoriteNameListResponse getFavoriteItems(User loginUser) {
        List<FavoriteNameItem> items = new ArrayList<>();

        // Store 즐겨찾기 조회
        items.addAll(favoriteStoreRepository.findStoreNameItemsByUserId(loginUser.getId()));

        // Event 즐겨찾기 조회
        items.addAll(favoriteEventRepository.findEventNameItemsByUserId(loginUser.getId()));

        // Popup 즐겨찾기 조회
        items.addAll(favoritePopupRepository.findPopupNameItemsByUserId(loginUser.getId()));

        return new FavoriteNameListResponse(items);  // FavoriteItemListResponse로 감싸서 반환
    }


}

