package group5.backend.repository;

import group5.backend.domain.event.FavoriteEvent;
import group5.backend.domain.event.Event;
import group5.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteEventRepository extends JpaRepository<FavoriteEvent, Long> {

    // 특정 유저가 즐겨찾기한 이벤트 목록
    List<FavoriteEvent> findByUser(User user);

    // 특정 이벤트를 즐겨찾기한 유저들 (혹시 통계 등 용도)
    List<FavoriteEvent> findByEvent(Event event);

    // 유저가 이미 해당 이벤트를 즐겨찾기했는지 여부 확인
    Optional<FavoriteEvent> findByUserAndEvent(User user, Event event);

    // 즐겨찾기 삭제 (토글용)
    void deleteByUserAndEvent(User user, Event event);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    // User ID와 Event ID로 즐겨찾기를 찾는 메서드 추가
    Optional<FavoriteEvent> findByUserIdAndEventId(Long userId, Long eventId);

    List<FavoriteEvent> findByUserId(Long userId);

}
