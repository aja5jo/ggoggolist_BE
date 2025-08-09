package group5.backend.dto.common.event.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class EventCreateRequest {

    @NotBlank(message = "이벤트 이름은 필수 입력값입니다.")
    private String name;

    @NotBlank(message = "이벤트 대표 소개글은 필수 입력값입니다.")
    private String description;

    @NotBlank(message = "이벤트 상세 설명은 필수 입력값입니다.")
    private String intro;

    @NotBlank(message = "썸네일 이미지는 필수입니다.")
    private String thumbnail;

    // 필수 아님: null 또는 빈 리스트 허용
    private List<String> images;

    @NotNull(message = "이벤트 시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "이벤트 종료일은 필수입니다.")
    private LocalDate endDate;

    @NotNull(message = "이벤트 시작 시간은 필수입니다.")
    private LocalTime startTime;

    @NotNull(message = "이벤트 종료 시간은 필수입니다.")
    private LocalTime endTime;

    @NotNull(message = "팝업 여부는 필수입니다.")
    private Boolean isPopup;
}
