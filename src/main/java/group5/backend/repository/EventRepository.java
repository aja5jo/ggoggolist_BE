package group5.backend.repository;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // 특정 Store에 등록된 모든 Event 조회
    //List<Event> findByStore(Store store);

    List<Event> findByStoreId(Long storeId);

    // 특정 Store 내에서 이름이 같은 Event 조회
    Optional<Event> findByStoreAndName(Store store, String name);
    @Query("""
        select distinct e
        from Event e
        join fetch e.store s
        left join fetch e.images
        where s.id = :storeId
        order by e.startDate desc, e.id desc
        """)
    List<Event> findByStoreIdWithStoreAndImages(@Param("storeId") Long storeId);
    @Query("""
      select distinct e
      from Event e
      join fetch e.store s
      left join fetch e.images imgs
      where e.id = :id
      """)
    Optional<Event> findDetailById(@Param("id") Long id);
    /* ========== 카테고리 + 진행중 (inclusive) ========== */
    @EntityGraph(attributePaths = "store")
    Page<Event> findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category, LocalDate startLte, LocalDate endGte, Pageable pageable);

    @EntityGraph(attributePaths = "store")
    List<Event> findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category, LocalDate startLte, LocalDate endGte, Sort sort);

    /* ========== 단일 필터용 (정렬은 Pageable/Sort로 통일) ========== */

    // ONGOING: startDate <= today AND endDate >= today
    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.startDate <= :today
          and e.endDate   >= :today
    """)
    Page<Event> findOngoing(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.startDate <= :today
          and e.endDate   >= :today
    """)
    List<Event> findOngoing(@Param("today") LocalDate today, Sort sort);

    // CLOSING_TODAY: endDate = today
    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.endDate = :today
    """)
    Page<Event> findClosingToday(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.endDate = :today
    """)
    List<Event> findClosingToday(@Param("today") LocalDate today, Sort sort);

    @EntityGraph(attributePaths = "store")
    @Query("""
    select e from Event e
    where e.endDate = :today
""")
    List<Event> findClosingTodayList(@Param("today") LocalDate today, Sort sort);

    // UPCOMING: startDate > today
    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.startDate > :today
    """)
    Page<Event> findUpcoming(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.startDate > :today
    """)
    List<Event> findUpcoming(@Param("today") LocalDate today, Sort sort);

    // 진행중
    @Query("SELECT e FROM Event e " +
            "WHERE :today BETWEEN e.startDate AND e.endDate " +
            "ORDER BY e.likeCount DESC, e.id DESC")
    List<Event> findOngoingList(@Param("today") LocalDate today);

    // 오늘 마감
    @Query("SELECT e FROM Event e " +
            "WHERE e.endDate = :today " +
            "ORDER BY e.likeCount DESC, e.id DESC")
    List<Event> findClosingTodayList(@Param("today") LocalDate today);

    // 예정
    @Query("SELECT e FROM Event e " +
            "WHERE e.startDate > :today " +
            "ORDER BY e.startDate ASC, e.id ASC")
    List<Event> findUpcomingList(@Param("today") LocalDate today);
    // 종료일이 현재 날짜 이전인 모든 이벤트 삭제
    void deleteByEndDateBefore(LocalDate date);
    // 관심 카테고리 목록 + 진행중( start <= today <= end ) + 페이지네이션
    @EntityGraph(attributePaths = "store")
    Page<Event> findByStore_CategoryInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            List<Category> categories,
            LocalDate startLte,
            LocalDate endGte,
            Pageable pageable
    );

    // 이름 부분일치(대소문자 무시) + 진행중 필터(startDate<=today<=endDate)
    List<Event> findByNameContainingIgnoreCaseAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String keyword,
            LocalDate startLte,
            LocalDate endGte,
            Sort sort
    );

    @Query("SELECT e FROM Event e WHERE e.startDate <= :today AND e.endDate >= :today")
    List<Event> findOngoing(@Param("today") LocalDate today);

    @Query("SELECT e FROM Event e WHERE e.startDate <= :today AND e.endDate >= :today ORDER BY e.likeCount DESC, e.id DESC")
    List<Event> findTopOngoingByLike(@Param("today") LocalDate today, Pageable pageable);


}
