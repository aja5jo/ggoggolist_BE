package group5.backend.dto.common.event.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@Data
@Builder
public class EventCheckResponse {
        private Long id;
        private Long storeId;
        private String name;
        private String description;
        private String intro;
        private String thumbnail;
        private List<String> images;
        private LocalDate startDate;
        private LocalDate endDate;
        private LocalTime startTime;
        private LocalTime endTime;
        private Integer likeCount;
}
