// src/main/java/group5/backend/dto/ai/StoreAiCreateRequest.java
package group5.backend.dto.ai;

import group5.backend.domain.user.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class StoreAiCreateRequest {
    @NotBlank(message = "가게 이름은 필수입니다.")
    private String name;
    private Category category;
    private String address;
    private List<String> keywords;
    private String introHint;
    private List<String> imageUrls;
}
