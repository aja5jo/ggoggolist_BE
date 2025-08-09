package group5.backend.domain.store;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "stores")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소상공인 유저 (1:1 관계)
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User owner;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String number;

    private String intro;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    // 대표 이미지
    @Column(nullable = false)
    private String thumbnail;

    // 이미지 URL 배열
    @ElementCollection
    @CollectionTable(name = "store_images", joinColumns = @JoinColumn(name = "store_id"))
    @Column(name = "image_url")
    private List<String> images;

    // 영업 시간
    private LocalTime startTime;
    private LocalTime endTime;

    @Column(name = "like_count", nullable = false)
    private int likeCount;

}

