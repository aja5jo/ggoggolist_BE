package group5.backend.controller;

import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.exception.gcp.ImageDownloadFailedException;
import group5.backend.service.TranslationApiClient;
import group5.backend.service.VisionApiClient;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/translate-full-page")
@RequiredArgsConstructor
public class FullPageTranslateController {

    private final VisionApiClient vision;
    private final TranslationApiClient translate;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Operation(summary = "text + (imageUrl | imageFile) 번역")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> translateFullPage(
            @RequestParam String text,
            @RequestParam SupportedLanguage targetLang,   // 🔽 Swagger에 드롭다운으로 노출
            @RequestParam(required = false) String imageUrl,
            @RequestPart(required = false) MultipartFile imageFile
    ) throws Exception {

        // 1) 일반 텍스트 번역
        String translatedText = translate.translate(text, targetLang.code());

        // 2) 이미지 바이트 수집
        byte[] imageBytes = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageBytes = imageFile.getBytes();
        } else if (imageUrl != null && !imageUrl.isBlank()) {
            imageBytes = download(imageUrl);
        }

        // 3) OCR + 번역
        String translatedImageText = "";
        if (imageBytes != null && imageBytes.length > 0) {
            String ocr = vision.extractText(imageBytes);
            translatedImageText = translate.translate(ocr, targetLang.code());
        }

        Map<String, String> resp = new HashMap<>();
        resp.put("translatedText", translatedText);
        resp.put("translatedImageText", translatedImageText);
        return ResponseEntity.ok(resp);
    }

    private byte[] download(String url) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        HttpResponse<byte[]> res = http.send(req, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() != 200) {
            throw new ImageDownloadFailedException(
                    res.statusCode(),
                    "이미지 다운로드 실패 (HTTP " + res.statusCode() + ")"
            );
        }
        return res.body();
    }
}