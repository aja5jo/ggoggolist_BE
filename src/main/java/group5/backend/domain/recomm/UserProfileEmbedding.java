package group5.backend.domain.recomm;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_profile_embeddings")
public class UserProfileEmbedding {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "model", nullable = false, length = 64)
    private String model;

    @Column(name = "dim", nullable = false)
    private int dim;

    @Lob
    @Column(name = "vec_json", nullable = false, columnDefinition = "LONGTEXT")
    private String vecJson;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
