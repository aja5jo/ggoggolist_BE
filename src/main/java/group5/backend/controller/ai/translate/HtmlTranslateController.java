package group5.backend.controller.ai.translate;

import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.dto.translate.request.TranslateHtmlRequest;
import group5.backend.service.ai.translate.HtmlTranslateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/translate/html/raw")
public class HtmlTranslateController {

    private final HtmlTranslateService service;

    public HtmlTranslateController(HtmlTranslateService service) {
        this.service = service;
    }

    /** 요청 예:
     *  POST /api/translate/html/raw
     *  { "html": "<html>...</html>", "target": "ENGLISH" }
     *  응답: text/html (번역된 HTML 또는 원문)
     */
    @PostMapping(produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> translateHtmlRaw(@RequestBody TranslateHtmlRequest req) {
        if (req == null || req.html() == null || req.html().isBlank() || req.target() == null) {
            return ResponseEntity.badRequest().build();
        }

        // ✅ target이 한국어면 번역 호출 없이 원문 그대로 반환 (요금 방지)
        if (req.target() == SupportedLanguage.KOREAN) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .header("X-Bypass-Translation", "true") // (선택) 프론트에서 모드 표시용
                    .body(req.html());
        }

        // 한국어(ko) → target 번역
        String translated = service.translateKoHtmlTo(req.html(), req.target());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(translated);
    }
}