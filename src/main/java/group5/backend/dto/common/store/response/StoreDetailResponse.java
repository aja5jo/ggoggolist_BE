package group5.backend.dto.common.store.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class StoreDetailResponse{
    private Long id;
    private String name;
    private String address;
    private String number;
    private String intro;
    private String category;
    private String thumbnail;
    private List<String> images;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer likeCount;
    private boolean liked;
}