package group5.backend.controller;

import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.dto.translate.request.ImageTranslateRequest;
import group5.backend.dto.translate.response.ImageTranslateResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.ImageTranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/translate/image")
@Tag(name = "이미지 OCR 번역", description = "이미지 속 한국어 텍스트 대상 언어로 번역")
public class ImageTranslateController {

    private final ImageTranslateService imageTranslateService;

    @Operation(
            summary = "이미지 OCR 번역",
            description = "imageUrl에서 이미지를 다운로드하여 OCR → 대상 언어로 번역한 텍스트를 반환합니다."
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ImageTranslateResponse>> translateImage(
            @RequestBody ImageTranslateRequest req
    ) throws Exception {
        String imageUrl = req.getImageUrl();
        SupportedLanguage targetLang = req.getTargetLang();

        ImageTranslateResponse data = imageTranslateService.translateImage(imageUrl, targetLang);

        return ResponseEntity.ok(new ApiResponse<>(true, 200, "번역 성공", data));
    }
}
