package group5.backend.domain.event;

import group5.backend.domain.store.Store;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 이벤트가 소속된 가게 (null 허용 안 함)
    @ManyToOne(optional = false) // JPA 레벨에서 필수 관계
    @JoinColumn(name = "store_id", nullable = false) // DB 컬럼도 NOT NULL
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Store store;

    @Column(nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    private String intro;

    @Column(nullable = false)
    private String thumbnail;

    @ElementCollection
    @CollectionTable(name = "event_images", joinColumns = @JoinColumn(name = "event_id"))
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

    @Column(name = "like_count", nullable = false)
    private int likeCount;

}

