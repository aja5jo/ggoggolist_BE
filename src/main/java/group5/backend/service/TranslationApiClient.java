package group5.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.gcp.GcpProperties;
import group5.backend.exception.gcp.TranslationApiException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringEscapeUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class TranslationApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper om;
    private final GcpProperties props;

    public String translate(String text, String targetLang) throws Exception {
        if (text == null || text.isBlank()) return "";

        String url = "https://translation.googleapis.com/language/translate/v2?key=" + props.getKey();

        String form = "q=" + URLEncoder.encode(text, StandardCharsets.UTF_8)
                + "&target=" + URLEncoder.encode(targetLang, StandardCharsets.UTF_8)
                + "&model=" + URLEncoder.encode("nmt", StandardCharsets.UTF_8);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new TranslationApiException(
                    res.statusCode(),
                    res.body(),
                    "번역 API 호출 실패 (HTTP " + res.statusCode() + ")"
            );
        }

        JsonNode root = om.readTree(res.body());
        String translated = root.path("data").path("translations").path(0).path("translatedText").asText("");

        // HTML 엔티티 → 실제 문자로 변환 (예: &#39; → ')
        return StringEscapeUtils.unescapeHtml4(translated);
    }
}