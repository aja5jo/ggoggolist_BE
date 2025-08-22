package group5.backend.repository;

import group5.backend.domain.recomm.UserProfileEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileEmbeddingRepository extends JpaRepository<UserProfileEmbedding, Long> {
}
