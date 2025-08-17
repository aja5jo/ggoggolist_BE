package group5.backend.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import group5.backend.domain.user.Category;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class PopupAiCreateRequest {
    @NotBlank(message = "팝업 이름은 필수입니다.")
    private String name;
    @NotNull (message = "카테고리 입력은 필수입니다.")
    private String category;
    @NotBlank(message = "주소 입력은 필수입니다.")
    private String address;
    private String introHint; // 선택

    private List<String> imageUrls;// 선택

}