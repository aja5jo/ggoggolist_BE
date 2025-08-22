// src/main/java/group5/backend/service/ai/recomm/CandidateService.java
package group5.backend.service.ai.recomm;

import group5.backend.domain.event.Event;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.recomm.ItemType;
import group5.backend.domain.store.Store;
import group5.backend.repository.EventRepository;
import group5.backend.repository.PopupRepository;
import group5.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandidateService {

    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final PopupRepository popupRepository;

    public List<ItemCandidate> findPopularFallback(int limit) {
        LocalDate today = LocalDate.now();
        List<ItemCandidate> out = new ArrayList<>();
        log.debug("[CAND] findPopularFallback limit={}", limit);

        // 매장: 전체에서 like desc, id desc로 상위 N
        storeRepository.findAll().stream()
                .sorted(Comparator.comparingInt(Store::getLikeCount).reversed()
                        .thenComparing(Store::getId, Comparator.reverseOrder()))
                .limit(limit)
                .forEach(s -> out.add(ItemCandidate.of(
                        ItemType.STORE, s.getId(), s.getLikeCount(),
                        s.getCategory()!=null? s.getCategory().name():null,
                        s.getAddress(), null, null
                )));

        // 이벤트/팝업: 진행중만 추려서 정렬 후 상위 N
        eventRepository.findOngoing(today).stream()
                .sorted(Comparator.comparingInt(Event::getLikeCount).reversed()
                        .thenComparing(Event::getId, Comparator.reverseOrder()))
                .limit(limit)
                .forEach(e -> out.add(ItemCandidate.of(
                        ItemType.EVENT, e.getId(), e.getLikeCount(),
                        e.getStore()!=null && e.getStore().getCategory()!=null? e.getStore().getCategory().name():null,
                        e.getStore()!=null? e.getStore().getAddress():null,
                        e.getStartDate(), e.getEndDate()
                )));

        popupRepository.findOngoing(today).stream()
                .sorted(Comparator.comparingInt(Popup::getLikeCount).reversed()
                        .thenComparing(Popup::getId, Comparator.reverseOrder()))
                .limit(limit)
                .forEach(p -> out.add(ItemCandidate.of(
                        ItemType.POPUP, p.getId(), p.getLikeCount(),
                        p.getCategory()!=null? p.getCategory().name():null,
                        p.getAddress(), p.getStartDate(), p.getEndDate()
                )));

        // 마지막으로 전체를 한 번 더 like desc -> id desc 정렬한 뒤 limit
        out.sort(Comparator.comparingInt(ItemCandidate::likeCount).reversed()
                .thenComparing(ItemCandidate::id, Comparator.reverseOrder()));
        log.debug("[CAND] result size={}", out.size());
        return out.size() > limit ? out.subList(0, limit) : out;
    }
    /** 로그인 유저용 후보 수집 **/
    public List<ItemCandidate> findForUser(Long userId, int limitFetch) {
        // TODO: 원하면 userId 기반으로 선호 카테고리/지역 필터를 얹어서 개선
        log.debug("[CAND] findForUser userId={}, limitFetch={}", userId, limitFetch);
        return findPopularFallback(limitFetch);
    }
}
