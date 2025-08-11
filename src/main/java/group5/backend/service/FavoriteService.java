package group5.backend.service;


import group5.backend.domain.event.Event;
import group5.backend.domain.event.FavoriteEvent;
import group5.backend.domain.popup.FavoritePopup;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.FavoriteStore;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.User;
import group5.backend.dto.favorite.response.FavoriteResponse;
import group5.backend.exception.favorite.FavoriteNotFoundException;
import group5.backend.repository.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;
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

    @PersistenceContext
    private EntityManager entityManager;


    // 유저의 store 즐겨찾기 토글
    public FavoriteResponse toggleStoreFavorite(User loginUser, Long storeId) {
        // Store를 가져옵니다.
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new FavoriteNotFoundException("Store not found"));

        // FavoriteStore 찾기
        Optional<FavoriteStore> existingFavoriteStore = favoriteStoreRepository.findByUserIdAndStoreId(loginUser.getId(), storeId);

        boolean liked;
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

        boolean liked;
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

        boolean liked;
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

    @Transactional
    public List<FavoriteResponse> getAllFavorites(User loginUser) {
        log.info("User ID: {} 의 전체 즐겨찾기 목록 조회 시작", loginUser.getId());

        List<FavoriteResponse> favoriteResponses = new ArrayList<>();

        // Store 즐겨찾기 조회
        log.info("Store 즐겨찾기 목록 조회 시작");
        List<Object[]> favoriteStores = entityManager.createNativeQuery(
                        "SELECT s.id, s.name FROM favorite_stores fs " +
                                "JOIN stores s ON fs.store_id = s.id WHERE fs.user_id = :userId")
                .setParameter("userId", loginUser.getId())
                .getResultList();
        log.info("총 {}개의 Store 즐겨찾기 항목 조회됨", favoriteStores.size());

        for (Object[] result : favoriteStores) {
            Long storeId = (Long) result[0];  // 첫 번째 컬럼은 store ID
            String storeName = (String) result[1];  // 두 번째 컬럼은 store Name
            favoriteResponses.add(FavoriteResponse.of(storeId, "store", false, 0, storeName));  // type 추가
        }

        // Event 즐겨찾기 조회
        log.info("Event 즐겨찾기 목록 조회 시작");
        List<Object[]> favoriteEvents = entityManager.createNativeQuery(
                        "SELECT e.id, e.name FROM favorite_events fe " +
                                "JOIN events e ON fe.event_id = e.id WHERE fe.user_id = :userId")
                .setParameter("userId", loginUser.getId())
                .getResultList();
        log.info("총 {}개의 Event 즐겨찾기 항목 조회됨", favoriteEvents.size());

        for (Object[] result : favoriteEvents) {
            Long eventId = (Long) result[0];  // 첫 번째 컬럼은 event ID
            String eventName = (String) result[1];  // 두 번째 컬럼은 event Name
            favoriteResponses.add(FavoriteResponse.of(eventId, "event", false, 0, eventName));  // type 추가
        }

        // Popup 즐겨찾기 조회
        log.info("Popup 즐겨찾기 목록 조회 시작");
        List<Object[]> favoritePopups = entityManager.createNativeQuery(
                        "SELECT p.id, p.name FROM favorite_popups fp " +
                                "JOIN popups p ON fp.popup_id = p.id WHERE fp.user_id = :userId")
                .setParameter("userId", loginUser.getId())
                .getResultList();
        log.info("총 {}개의 Popup 즐겨찾기 항목 조회됨", favoritePopups.size());

        for (Object[] result : favoritePopups) {
            Long popupId = (Long) result[0];  // 첫 번째 컬럼은 popup ID
            String popupName = (String) result[1];  // 두 번째 컬럼은 popup Name
            favoriteResponses.add(FavoriteResponse.of(popupId, "popup", false, 0, popupName));  // type 추가
        }

        log.info("유저 ID: {} 의 전체 즐겨찾기 목록 조회 완료", loginUser.getId());
        return favoriteResponses;
    }




}

