package group5.backend.domain.recomm;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "item_embeddings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_item_embeddings_type_item_model",
                        columnNames = {"item_type", "item_id", "model"}
                )
        }
)
public class ItemEmbedding {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false, length = 16)
    private ItemType itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

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
}
