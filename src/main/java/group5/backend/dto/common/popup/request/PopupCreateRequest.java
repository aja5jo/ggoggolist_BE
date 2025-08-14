package group5.backend.dto.common.popup.request;

import group5.backend.domain.user.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class PopupCreateRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Category category;

    @NotBlank(message = "팝업 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "대표 소개글은 필수입니다.")
    private String description;

    private String intro;

    @NotBlank(message = "썸네일은 필수입니다.")
    private String thumbnail;

    // 선택
    private List<String> images;

    @NotNull(message = "시작일은 필수입니다.")
    private LocalDate startDate;

    @NotNull(message = "종료일은 필수입니다.")
    private LocalDate endDate;

    @NotNull(message = "시작 시간은 필수입니다.")
    private LocalTime startTime;

    @NotNull(message = "종료 시간은 필수입니다.")
    private LocalTime endTime;

    @NotBlank(message = "주소는 필수입니다.")
    private String address;
}