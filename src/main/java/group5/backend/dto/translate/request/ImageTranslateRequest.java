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

    @Schema(description = "번역 모드 (AUTO: 자동 판별, MENU: 메뉴판 강제, IMAGE: 일반 강제)")
    private TranslateMode forceMode = TranslateMode.AUTO;
}
