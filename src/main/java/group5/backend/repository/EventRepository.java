package group5.backend.repository;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // 특정 Store에 등록된 모든 Event 조회
    List<Event> findByStore(Store store);

    // 특정 Store 내에서 이름이 같은 Event 조회
    Optional<Event> findByStoreAndName(Store store, String name);

    // 팝업 여부로 필터링된 이벤트 목록
    List<Event> findByIsPopupTrue();
}
