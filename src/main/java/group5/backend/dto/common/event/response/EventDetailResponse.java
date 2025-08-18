package group5.backend.dto.common.event.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

import java.util.List;

@Builder
@Data
public class EventDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String intro;
    private String thumbnail;
    private List<String> images;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;

    private int likeCount;
    private boolean liked;
    private StoreSimpleDto store;

    @Builder
    @Data
    public static class StoreSimpleDto {
        private Long storeId;
        private String storeName;
        private String address;
        private String phone;
        private String storeImageUrl;
    }
}