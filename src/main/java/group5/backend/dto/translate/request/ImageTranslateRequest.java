package group5.backend.dto.translate.request;

import group5.backend.domain.lang.SupportedLanguage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ImageTranslateRequest {

    @Schema(description = "OCR 대상 이미지 URL")
    private String imageUrl;

    @Schema(description = "목표 언어 (enum 이름: KOREAN/ENGLISH/JAPANESE/…)")
    private SupportedLanguage targetLang;
}
