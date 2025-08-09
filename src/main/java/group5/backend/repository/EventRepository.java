package group5.backend.repository;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // 특정 Store에 등록된 모든 Event 조회
    List<Event> findByStore(Store store);

    // 특정 Store 내에서 이름이 같은 Event 조회
    Optional<Event> findByStoreAndName(Store store, String name);

    // 종료일이 현재 날짜 이전인 모든 이벤트 삭제
    void deleteByEndDateBefore(LocalDate date);

}
