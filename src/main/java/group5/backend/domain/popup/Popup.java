package group5.backend.domain.popup;
import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "popups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Popup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팝업 생성 유저 (1:N 관계)
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    private String intro;

    @Column(nullable = false)
    private String thumbnail;

    @ElementCollection
    @CollectionTable(name = "popup_images", joinColumns = @JoinColumn(name = "popup_id"))
    @Column(name = "image_url")
    private List<String> images;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private String address;   // 팝업 주소

    @Column(name = "like_count", nullable = false)
    private int likeCount;
}

