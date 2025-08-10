package group5.backend.service;

import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.dto.translate.response.ImageTranslateResponse;
import group5.backend.exception.gcp.ImageDownloadFailedException;
import group5.backend.service.TranslationApiClient;
import group5.backend.service.VisionApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ImageTranslateService {

    private final VisionApiClient vision;
    private final TranslationApiClient translate;
    private final HttpClient httpClient;

    public ImageTranslateResponse translateImage(String imageUrl, SupportedLanguage targetLang) throws Exception {
        // `httpClient` 사용
        byte[] imageBytes = download(imageUrl);

        // OCR 텍스트 추출
        String ocrText = vision.extractText(imageBytes);

        // 텍스트 번역
        String translatedText = (ocrText == null || ocrText.isBlank()) ? "" : translate.translate(ocrText, targetLang.code());

        return new ImageTranslateResponse(translatedText);
    }

    private byte[] download(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();

        HttpResponse<byte[]> res = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (res.statusCode() != 200 || res.body() == null) {
            throw new ImageDownloadFailedException(res.statusCode(), "이미지 다운로드 실패 (HTTP " + res.statusCode() + ")");
        }

        return res.body();
    }
}
