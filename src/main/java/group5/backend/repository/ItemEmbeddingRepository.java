package group5.backend.repository;

import group5.backend.domain.recomm.ItemEmbedding;
import group5.backend.domain.recomm.ItemType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemEmbeddingRepository extends JpaRepository<ItemEmbedding, Long> {

    List<ItemEmbedding> findByItemTypeAndItemIdInAndModel(ItemType itemType, List<Long> itemIds, String model);

    boolean existsByItemTypeAndItemIdAndModel(ItemType itemType, Long itemId, String model);
}
