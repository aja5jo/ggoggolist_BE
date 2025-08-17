package group5.backend.dto.ai.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class EventAiCreateRequest {
    @NotBlank(message = "이벤트 이름은 필수입니다.")
    private String name;
    private String category;
    private String address;
    private String introHint; // 선택
    private List<String> imageUrls;

}
