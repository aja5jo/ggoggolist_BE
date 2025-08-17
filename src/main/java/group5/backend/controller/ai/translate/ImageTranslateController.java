package group5.backend.controller.ai.translate;

import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.dto.translate.menu.MenuDetectResult;
import group5.backend.dto.translate.menu.MenuItem;
import group5.backend.dto.translate.request.ImageTranslateRequest;
import group5.backend.dto.translate.request.TranslateMode;
import group5.backend.dto.translate.response.ImageTranslateResponse;
import group5.backend.dto.translate.response.SmartTranslateResponse;
import group5.backend.exception.gcp.ImageDownloadFailedException;
import group5.backend.exception.gcp.InvalidTargetLanguageException;
import group5.backend.response.ApiResponse;
import group5.backend.service.ai.translate.ImageTranslateService;
import group5.backend.service.ai.translate.MenuBoardDetector;
import group5.backend.service.ai.translate.MenuBoardTranslateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/translate")
@Tag(name = "스마트 이미지 번역", description = "메뉴판 여부 자동 판별 + 강제 모드 지원")
public class ImageTranslateController {

    private final ImageTranslateService imageTranslateService;
    private final MenuBoardDetector menuBoardDetector;
    private final MenuBoardTranslateService menuBoardTranslateService;
    private final HttpClient httpClient;

    @Operation(
            summary = "스마트 번역",
            description = "forceMode에 따라 AUTO(자동 판별), MENU(메뉴판 강제), IMAGE(일반 강제) 모드로 번역합니다."
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SmartTranslateResponse>> smartTranslate(@RequestBody ImageTranslateRequest req) throws Exception {

        if (req.getTargetLang() == SupportedLanguage.KOREAN) {
            throw new InvalidTargetLanguageException("기본 언어가 선택되어 원문 텍스트를 표시합니다.");
        }

        String imageUrl = req.getImageUrl();
        SupportedLanguage targetLang = req.getTargetLang();
        TranslateMode mode = req.getForceMode();

        byte[] imageBytes = download(imageUrl);
        boolean isMenuBoard = false;
        double score = 0.0;

        // 1) 모드 결정
        if (mode == TranslateMode.AUTO) {
            MenuDetectResult det = menuBoardDetector.detect(imageBytes);
            isMenuBoard = det.isMenuBoard();
            score = det.getScore();
        } else if (mode == TranslateMode.MENU) {
            isMenuBoard = true;
        } else if (mode == TranslateMode.IMAGE) {
            isMenuBoard = false;
        }

        // 2) 실행
        if (isMenuBoard) {
            List<MenuItem> items = menuBoardTranslateService.translateMenuBoard(imageBytes, targetLang);
            SmartTranslateResponse body = SmartTranslateResponse.builder()
                    .mode("menu")
                    .score(score)
                    .menuItems(items)
                    .normal(null)
                    .build();
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "메뉴판 번역 성공", body));
        } else {
            ImageTranslateResponse normal = imageTranslateService.translateImage(imageUrl, targetLang);
            SmartTranslateResponse body = SmartTranslateResponse.builder()
                    .mode("normal")
                    .score(score)
                    .menuItems(null)
                    .normal(normal)
                    .build();
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "일반 번역 성공", body));
        }
    }

    // 이미지 다운로드 헬퍼
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