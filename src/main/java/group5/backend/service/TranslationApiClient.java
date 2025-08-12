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
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TranslationApiClient {

    private static final int MAX_ITEMS_PER_REQ = 90;          // 안전한 개수(권장)
    private static final int MAX_CHARS_PER_REQ = 25000;       // 총 길이 제한(권장)

    private final HttpClient httpClient;
    private final ObjectMapper om;
    private final GcpProperties props;

    /** 단건 (기존) - 호환 유지 */
    public String translate(String text, String targetLang) throws Exception {
        if (text == null || text.isBlank()) return "";
        List<String> out = translateBatch(List.of(text), targetLang);
        return out.isEmpty() ? "" : out.get(0);
    }

    /** 다건 (신규) - 순서 보장 */
    public List<String> translateBatch(List<String> texts, String targetLang) throws Exception {
        if (texts == null || texts.isEmpty()) return List.of();

        String url = "https://translation.googleapis.com/language/translate/v2?key=" + props.getKey();

        List<String> results = new ArrayList<>(texts.size());
        // 원본 인덱스 유지
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            String t = texts.get(i);
            if (t == null || t.isBlank()) {
                results.add(""); // 자리 채우기
            } else {
                indices.add(i);
                results.add(null); // 나중에 채움
            }
        }

        // 청크 나누기: 개수/총문자수 모두 고려
        int start = 0;
        while (start < indices.size()) {
            int end = start;
            int chars = 0;
            int count = 0;
            while (end < indices.size() && count < MAX_ITEMS_PER_REQ) {
                String s = texts.get(indices.get(end));
                int add = s.length();
                if (chars + add > MAX_CHARS_PER_REQ && count > 0) break;
                chars += add;
                count++;
                end++;
            }

            // q=... 를 여러 번 붙이는 application/x-www-form-urlencoded 요청
            StringBuilder form = new StringBuilder();
            for (int k = start; k < end; k++) {
                String s = texts.get(indices.get(k));
                form.append("q=").append(URLEncoder.encode(s, StandardCharsets.UTF_8).replaceAll("\\+", "%20")).append("&");
            }
            form.append("target=").append(URLEncoder.encode(targetLang, StandardCharsets.UTF_8)).append("&");
            form.append("model=").append(URLEncoder.encode("nmt", StandardCharsets.UTF_8));

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(form.toString()))
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
            JsonNode arr = root.path("data").path("translations");
            if (!arr.isArray()) throw new IllegalStateException("응답 파싱 실패: translations 배열 없음");

            // 순서대로 매핑
            int p = 0;
            for (int k = start; k < end; k++) {
                String translated = arr.get(p++).path("translatedText").asText("");
                // HTML 엔티티 복원
                translated = org.apache.commons.text.StringEscapeUtils.unescapeHtml4(translated);
                results.set(indices.get(k), translated);
            }

            start = end;
        }

        // null 자리(입력 공백 등) 안전 처리
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i) == null) results.set(i, "");
        }
        return results;
    }
}
