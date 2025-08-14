package group5.backend.service.imagetranslate;

import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.dto.translate.response.ImageTranslateResponse;
import group5.backend.exception.gcp.ImageDownloadFailedException;

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

    // 이미지 URL과 번역할 언어를 받아서 OCR 및 번역 수행
    public ImageTranslateResponse translateImage(String imageUrl, SupportedLanguage targetLang) throws Exception {
        // 인증 없이 이미지 다운로드 (세션 쿠키 처리)
        byte[] imageBytes = download(imageUrl);

        // OCR 텍스트 추출
        String ocrText = vision.extractText(imageBytes);

        // 텍스트 번역
        String translatedText = (ocrText == null || ocrText.isBlank()) ? "" : translate.translate(ocrText, targetLang.code());

        return new ImageTranslateResponse(translatedText);
    }

    // 이미지 다운로드 (세션 쿠키 자동 처리)
    private byte[] download(String url) throws Exception {
        // HttpClient에서 쿠키를 자동으로 처리하도록 설정
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

