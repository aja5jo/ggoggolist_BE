package group5.backend.dto.ai.response;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AiPreviewResponse {
    private String description;
    private String intro;
}