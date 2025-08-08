package group5.backend.repository;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
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

    // 팝업 여부로 필터링된 이벤트 목록
    List<Event> findByIsPopupTrue();

    // ✅ [신규] 카테고리 + "진행 중" 필터 (startDate <= today <= endDate)
    // Event -> Store 연관을 한 번에 로딩해서 N+1 방지
    @EntityGraph(attributePaths = "store")
    Page<Event> findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category,
            LocalDate startLte,
            LocalDate endGte,
            Pageable pageable
    );

    // 추가: 전체 + 정렬
    List<Event> findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category, LocalDate startDate, LocalDate endDate, Sort sort);
}
