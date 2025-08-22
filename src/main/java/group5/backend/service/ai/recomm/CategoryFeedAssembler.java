// src/main/java/group5/backend/service/ai/recomm/CategoryFeedAssembler.java
package group5.backend.service.ai.recomm;

import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.store.Store;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.repository.EventRepository;
import group5.backend.repository.PopupRepository;
import group5.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CategoryFeedAssembler {

    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;

    public List<CategoryFeedItemResponse> toResponses(List<RankingService.Scored> ranked, Long userId) {
        List<Long> storeIds = new ArrayList<>(), eventIds = new ArrayList<>(), popupIds = new ArrayList<>();

        // ✅ 단순 for문으로 수집
        for (var s : ranked) {
            switch (s.item().type()) {
                case STORE -> storeIds.add(s.item().id());
                case EVENT -> eventIds.add(s.item().id());
                case POPUP -> popupIds.add(s.item().id());
            }
        }

        Map<Long, Store> stores = storeRepository.findAllById(storeIds).stream()
                .collect(Collectors.toMap(Store::getId, it -> it));
        Map<Long, Event> events = eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, it -> it));
        Map<Long, Popup> popups = popupRepository.findAllById(popupIds).stream()
                .collect(Collectors.toMap(Popup::getId, it -> it));

        List<CategoryFeedItemResponse> out = new ArrayList<>(ranked.size());
        for (var s : ranked) {
            var c = s.item();
            boolean liked = false; // TODO: 즐겨찾기 배치조회로 채우기
            switch (c.type()) {
                case STORE -> { var st = stores.get(c.id()); if (st!=null) out.add(CategoryFeedItemResponse.fromStore(st, liked)); }
                case EVENT -> { var ev = events.get(c.id()); if (ev!=null) out.add(CategoryFeedItemResponse.fromEvent(ev, liked)); }
                case POPUP -> { var pp = popups.get(c.id()); if (pp!=null) out.add(CategoryFeedItemResponse.fromPopup(pp, liked)); }
            }
        }
        return out;
    }
}
