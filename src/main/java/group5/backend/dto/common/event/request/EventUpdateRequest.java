package group5.backend.dto.common.event.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventUpdateRequest {
    private Long storeId;               // 이벤트 등록 대상 스토어
    private String name;
    private String desc; //대표 소개글
    private String intro;
    private String thumbnail;
    private List<String> images;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isPopup;
}