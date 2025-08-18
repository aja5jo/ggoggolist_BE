package group5.backend.service.ai.translate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.gcp.GcpProperties;
import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.exception.gcp.TranslateFailedException;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
@Service
public class HtmlTranslateService {

    private final HttpClient httpClient;
    private final GcpProperties gcpProperties;
    private final ObjectMapper om = new ObjectMapper();

    public HtmlTranslateService(HttpClient httpClient, GcpProperties gcpProperties) {
        this.httpClient = httpClient;
        this.gcpProperties = gcpProperties;
    }

    /** HTML 태그 보존 + 한국어(ko) -> target만 번역 */
    public String translateKoHtmlTo(String html, SupportedLanguage target) {
        try {
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + gcpProperties.getKey();

            Map<String, Object> body = new HashMap<>();
            body.put("q", html);
            body.put("source", "ko");
            body.put("target", target.code());
            body.put("format", "html");

            String json = om.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                throw new TranslateFailedException("GCP Translation API 실패 (HTTP "
                        + resp.statusCode() + ")");
            }

            JsonNode root = om.readTree(resp.body());
            JsonNode translations = root.path("data").path("translations");
            if (!translations.isArray() || translations.isEmpty()) {
                throw new TranslateFailedException("번역 응답 파싱 실패");
            }

            String translated = translations.get(0).path("translatedText").asText("");
            // ✅ HTML 엔티티 디코딩
            return StringEscapeUtils.unescapeHtml4(translated);

        } catch (Exception e) {
            throw new TranslateFailedException("HTML 번역 중 오류", e);
        }
    }
}