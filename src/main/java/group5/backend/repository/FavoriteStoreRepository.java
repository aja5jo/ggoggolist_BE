package group5.backend.repository;

import group5.backend.domain.store.FavoriteStore;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.User;
import group5.backend.dto.favorite.FavoriteNameItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteStoreRepository extends JpaRepository<FavoriteStore, Long> {

    // 특정 유저가 즐겨찾기한 가게 목록
    List<FavoriteStore> findByUser(User user);

    // 특정 가게를 즐겨찾기한 유저 목록
    List<FavoriteStore> findByStore(Store store);

    // 유저가 이미 해당 가게를 즐겨찾기했는지 여부 확인
    Optional<FavoriteStore> findByUserAndStore(User user, Store store);

    // 유저와 스토어를 기준으로 즐겨찾기 삭제
    void deleteByUserAndStore(User user, Store store);

    boolean existsByUserIdAndStoreId(Long userId, Long storeId);

    // User ID와 Store ID로 즐겨찾기를 찾는 메서드 추가
    Optional<FavoriteStore> findByUserIdAndStoreId(Long userId, Long storeId);

    List<FavoriteStore> findByUserId(Long userId);

}
