package group5.backend.domain.popup;
import group5.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "favorite_popups",
        uniqueConstraints = { @UniqueConstraint(columnNames = {"user_id", "popup_id"}) }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoritePopup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 즐겨찾기한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 즐겨찾기한 팝업
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "popup_id", nullable = false)
    private Popup popup;
}