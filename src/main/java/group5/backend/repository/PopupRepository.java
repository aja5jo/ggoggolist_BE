package group5.backend.repository;

import group5.backend.domain.popup.Popup;
import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PopupRepository extends JpaRepository<Popup, Long> {

    /* ========== 기본 ========== */
    List<Popup> findByUserId(Long userId);
    @Query("SELECT p.id FROM Popup p")
    List<Long> findAllPopupIds(); // 모든 팝업의 ID 반환
    Optional<Popup> findByUserAndName(User user, String name);
    void deleteByEndDateBefore(LocalDate date);
    /* ========== 카테고리 + 진행중 (inclusive) ========== */
    @EntityGraph(attributePaths = "user")
    Page<Popup> findByCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category, LocalDate startLte, LocalDate endGte, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    List<Popup> findByCategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Category category, LocalDate startLte, LocalDate endGte, Sort sort);

    /* ========== 단일 필터용 (정렬은 Pageable/Sort로 통일) ========== */

    // ONGOING: startDate <= today AND endDate >= today
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate <= :today
          and p.endDate   >= :today
    """)
    Page<Popup> findOngoing(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate <= :today
          and p.endDate   >= :today
    """)
    List<Popup> findOngoing(@Param("today") LocalDate today, Sort sort);

    // CLOSING_TODAY: endDate = today
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.endDate = :today
    """)
    Page<Popup> findClosingToday(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.endDate = :today
    """)
    List<Popup> findClosingToday(@Param("today") LocalDate today, Sort sort);

    @EntityGraph(attributePaths = "user")
    @Query("""
    select p from Popup p
    where p.endDate = :today
""")
    List<Popup> findClosingTodayList(@Param("today") LocalDate today, Sort sort);

    // UPCOMING: startDate > today
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate > :today
    """)
    Page<Popup> findUpcoming(@Param("today") LocalDate today, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate > :today
    """)
    List<Popup> findUpcoming(@Param("today") LocalDate today, Sort sort);

    // 진행중
    @Query("SELECT p FROM Popup p " +
            "WHERE :today BETWEEN p.startDate AND p.endDate " +
            "ORDER BY p.likeCount DESC, p.id DESC")
    List<Popup> findOngoingList(@Param("today") LocalDate today);

    // 오늘 마감
    @Query("SELECT p FROM Popup p " +
            "WHERE p.endDate = :today " +
            "ORDER BY p.likeCount DESC, p.id DESC")
    List<Popup> findClosingTodayList(@Param("today") LocalDate today);

    // 예정
    @Query("SELECT p FROM Popup p " +
            "WHERE p.startDate > :today " +
            "ORDER BY p.startDate ASC, p.id ASC")
    List<Popup> findUpcomingList(@Param("today") LocalDate today);

    //이번주 진행 팝업
    @EntityGraph(attributePaths = "user")
    @Query("""
        SELECT p FROM Popup p
        WHERE p.startDate <= :endOfWeek
          AND p.endDate   >= :startOfWeek
    """)
    List<Popup> findThisWeek(@Param("startOfWeek") LocalDate startOfWeek,
                             @Param("endOfWeek") LocalDate endOfWeek,
                             Sort sort);
    @Query("""
      select distinct p
      from Popup p
      left join fetch p.images imgs   
      left join fetch p.user u       
      where p.id = :id
      """)
    Optional<Popup> findDetailById(@Param("id") Long id);


    @Query("SELECT p FROM Popup p WHERE p.startDate <= :today AND p.endDate >= :today")
    List<Popup> findOngoing(@Param("today") LocalDate today);

    @Query("SELECT p FROM Popup p WHERE p.startDate <= :today AND p.endDate >= :today ORDER BY p.likeCount DESC, p.id DESC")
    List<Popup> findTopOngoingByLike(@Param("today") LocalDate today, Pageable pageable);
}