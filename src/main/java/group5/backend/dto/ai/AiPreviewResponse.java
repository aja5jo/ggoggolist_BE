package group5.backend.dto.ai;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AiPreviewResponse {
    private String intro;
    private String description;
}