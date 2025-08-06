package group5.backend.dto.common.store.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class StoreCreateRequest {

    @NotBlank(message = "가게 이름은 필수 입력값입니다.")
    private String name;

    @NotBlank(message = "주소는 필수 입력값입니다.")
    private String address;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    private String number;

    @NotBlank(message = "가게 소개는 필수 입력값입니다.")
    private String intro;

    @NotBlank(message = "카테고리는 필수 입력값입니다.")
    private String category;

    @NotBlank(message = "썸네일 이미지는 필수입니다.")
    private String thumbnail;

    // 필수 아님: null/빈 배열 모두 허용
    private List<String> images;

    @NotNull(message = "오픈 시간은 필수입니다.")
    private LocalTime startTime;

    @NotNull(message = "마감 시간은 필수입니다.")
    private LocalTime endTime;
}
