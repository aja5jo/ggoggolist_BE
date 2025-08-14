package group5.backend.service.ai.translate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.gcp.GcpProperties;
import group5.backend.exception.gcp.VisionApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VisionApiClient {
    private final HttpClient httpClient;
    private final ObjectMapper om;
    private final GcpProperties props;

    public String extractText(byte[] imageBytes) throws Exception {
        String base64 = Base64.getEncoder().encodeToString(imageBytes);
        String url = "https://vision.googleapis.com/v1/images:annotate?key=" + props.getKey();

        String body = """
        {
          "requests": [{
            "image": { "content": "%s" },
            "features": [{ "type": "TEXT_DETECTION" }]
          }]
        }
        """.formatted(base64);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json; charset=UTF-8")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            throw new VisionApiException(
                    res.statusCode(),
                    res.body(),
                    "Vision API 호출 실패 (HTTP " + res.statusCode() + ")"
            );
        }

        JsonNode root = om.readTree(res.body());
        JsonNode full = root.path("responses").path(0).path("fullTextAnnotation").path("text");
        if (!full.isMissingNode()) return full.asText();

        JsonNode textAnn = root.path("responses").path(0).path("textAnnotations");
        if (textAnn.isArray() && textAnn.size() > 0) {
            return textAnn.get(0).path("description").asText("");
        }
        return "";
    }

    /** 줄 단위로 잘라 반환 (공백/잡음 제거 포함) */
    public List<String> extractLines(byte[] imageBytes) throws Exception {
        String raw = extractText(imageBytes);
        return Arrays.stream(raw.split("\\r?\\n"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()              // 중복 줄 제거(선택)
                .limit(200)              // 과도한 길이 방지(선택)
                .toList();
    }
}