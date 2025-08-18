package group5.backend.controller.ai.translate;

import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.dto.translate.request.TranslateHtmlRequest;
import group5.backend.service.ai.translate.HtmlTranslateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/translate/html/raw")
public class HtmlTranslateController {

    private final HtmlTranslateService service;

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
        // target이 한국어면 원문 그대로 반환 (요금 방지)
        if (req.target() == SupportedLanguage.KOREAN) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                    .header("X-Bypass-Translation", "true")
                    .body(req.html());
        }
        String translated = service.translateKoHtmlSmart(req.html(), req.target());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
                .body(translated);
    }

}
