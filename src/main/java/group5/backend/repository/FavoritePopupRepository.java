package group5.backend.repository;

import group5.backend.domain.popup.FavoritePopup;
import group5.backend.domain.popup.Popup;
import group5.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoritePopupRepository extends JpaRepository<FavoritePopup, Long> {

    // 특정 유저의 즐겨찾기 팝업들
    List<FavoritePopup> findByUser(User user);

    // 특정 팝업을 즐겨찾기한 유저들 (통계 등)
    List<FavoritePopup> findByPopup(Popup popup);

    // 유저가 이미 즐겨찾기 했는지 여부
    Optional<FavoritePopup> findByUserAndPopup(User user, Popup popup);

    // 토글 삭제용
    void deleteByUserAndPopup(User user, Popup popup);

    // 존재 여부 (성능용)
    boolean existsByUserIdAndPopupId(Long userId, Long popupId);

    // User ID와 Popup ID로 즐겨찾기를 찾는 메서드 추가
    Optional<FavoritePopup> findByUserIdAndPopupId(Long userId, Long popupId);

    List<FavoritePopup> findByUserId(Long userId);

}

