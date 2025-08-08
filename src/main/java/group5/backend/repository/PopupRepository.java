package group5.backend.repository;

import group5.backend.domain.popup.Popup;
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

    /* =========================
       기본
     ========================= */
    // 특정 유저가 만든 팝업
    List<Popup> findByUser(User user);

    // 동일 유저 내 동일 이름 팝업 존재 여부/조회
    Optional<Popup> findByUserAndName(User user, String name);

    /* =========================
       진행중 / 마감 / 예정 (startDate <= today <= endDate)
     ========================= */

    // ONGOING: 진행중 (정렬: likeCount desc, id desc)
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate <= :today
          and p.endDate   >= :today
        order by p.likeCount desc, p.id desc
    """)
    Page<Popup> findOngoing(@Param("today") LocalDate today, Pageable pageable);

    // POPULAR among ONGOING (정렬 동일, 네이밍만 구분)
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate <= :today
          and p.endDate   >= :today
        order by p.likeCount desc, p.id desc
    """)
    Page<Popup> findPopularAmongOngoing(@Param("today") LocalDate today, Pageable pageable);

    // CLOSING_TODAY: 오늘 종료
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.endDate = :today
        order by p.likeCount desc, p.id desc
    """)
    Page<Popup> findClosingToday(@Param("today") LocalDate today, Pageable pageable);

    // UPCOMING: 앞으로 시작(시작일 > today)
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate > :today
        order by p.startDate asc, p.id asc
    """)
    Page<Popup> findUpcoming(@Param("today") LocalDate today, Pageable pageable);

    /* =========================
       overview 등 top-N용 List 버전
     ========================= */
    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate <= :today
          and p.endDate   >= :today
    """)
    List<Popup> findOngoingList(@Param("today") LocalDate today, Sort sort);

    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.endDate = :today
    """)
    List<Popup> findClosingTodayList(@Param("today") LocalDate today, Sort sort);

    @EntityGraph(attributePaths = "user")
    @Query("""
        select p from Popup p
        where p.startDate > :today
    """)
    List<Popup> findUpcomingList(@Param("today") LocalDate today, Sort sort);
}
