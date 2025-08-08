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
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /* =========================
       기본
     ========================= */
    // 특정 Store에 등록된 모든 Event
    List<Event> findByStore(Store store);

    // 특정 Store 내에서 이름이 같은 Event
    Optional<Event> findByStoreAndName(Store store, String name);

    /* =========================
       카테고리 + 진행중 (startDate <= today <= endDate)
     ========================= */
    // Page 반환 + N+1 방지
    @EntityGraph(attributePaths = "store")
    Page<Event> findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category,
            LocalDate startLte,
            LocalDate endGte,
            Pageable pageable
    );

    // List 반환 + 정렬 지정 (overview 등에서 top-N 자를 때 유용)
    @EntityGraph(attributePaths = "store")
    List<Event> findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category,
            LocalDate startDate,
            LocalDate endDate,
            Sort sort
    );

    /* =========================
       단일 필터 조회용 (POPULAR/ONGOING/CLOSING_TODAY/UPCOMING)
       - 진행중: (startDate <= today) AND (endDate >= today)
       - 종료일 null/시작일 null은 '무기한 진행/이미 시작'으로 간주
     ========================= */

    // ONGOING (정렬: likeCount desc, id desc) — POPULAR과 동일 정렬이므로 서비스에서 그대로 재사용 가능
    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where (e.startDate is null or e.startDate <= :today)
          and (e.endDate   is null or e.endDate   >= :today)
        order by e.likeCount desc, e.id desc
    """)
    Page<Event> findOngoing(@Param("today") LocalDate today, Pageable pageable);

    // POPULAR among ONGOING (정렬 동일, 네이밍만 POPULAR에 맞춰 분리)
    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where (e.startDate is null or e.startDate <= :today)
          and (e.endDate   is null or e.endDate   >= :today)
        order by e.likeCount desc, e.id desc
    """)
    Page<Event> findPopularAmongOngoing(@Param("today") LocalDate today, Pageable pageable);

    // CLOSING_TODAY: 오늘 종료되는 이벤트
    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.endDate = :today
        order by e.likeCount desc, e.id desc
    """)
    Page<Event> findClosingToday(@Param("today") LocalDate today, Pageable pageable);

    // UPCOMING: 앞으로 시작할 예정 (시작일 > today)
    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.startDate > :today
        order by e.startDate asc, e.id asc
    """)
    Page<Event> findUpcoming(@Param("today") LocalDate today, Pageable pageable);

    /* =========================
       overview 최적화용 (원하면 사용)
       - Page 대신 List로 top-N만 뽑고 싶을 때 Sort + Pageable.ofSize(N) 조합을 서비스에서 사용해도 됨
       - 필요 없으면 이 섹션은 무시 가능
     ========================= */

    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where (e.startDate is null or e.startDate <= :today)
          and (e.endDate   is null or e.endDate   >= :today)
    """)
    List<Event> findOngoingList(@Param("today") LocalDate today, Sort sort);

    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.endDate = :today
    """)
    List<Event> findClosingTodayList(@Param("today") LocalDate today, Sort sort);

    @EntityGraph(attributePaths = "store")
    @Query("""
        select e from Event e
        where e.startDate > :today
    """)
    List<Event> findUpcomingList(@Param("today") LocalDate today, Sort sort);
}
