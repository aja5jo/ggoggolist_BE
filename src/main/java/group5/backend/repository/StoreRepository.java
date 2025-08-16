package group5.backend.repository;

import group5.backend.domain.store.Store;
import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    // Store 이름으로 조회 (Optional)
    Optional<Store> findByName(String name);

    // 특정 유저(merchant)가 등록한 가게 목록
    //Optional<Store> findByOwner(User owner);
    Optional<Store> findByOwnerId(Long ownerId);

    // 카테고리별 스토어 조회 (정렬/개수는 Pageable로 제어)
    Page<Store> findByCategory(Category category, Pageable pageable);

    boolean existsByOwnerId(Long ownerId);
    // 추가: 전체 + 정렬
    List<Store> findByCategory(Category category, Sort sort);

    // 관심 카테고리 여러 개로 페이지 조회 (정렬은 Pageable로)
    Page<Store> findByCategoryIn(List<Category> categories, Pageable pageable);

    boolean existsByOwnerId(Long ownerId);

    Optional<Store> findByOwnerId(Long ownerId);

    @Query("""
      select distinct s
      from Store s
      left join fetch s.images imgs
      left join fetch s.owner o
      where s.id = :id
      """)
    Optional<Store> findDetailById(@Param("id") Long id);
}
