package group5.backend.service.ai.translate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.nodes.TextNode;
import group5.backend.config.gcp.GcpProperties;
import group5.backend.domain.lang.SupportedLanguage;
import group5.backend.exception.gcp.TranslateFailedException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;
import org.springframework.stereotype.Service;

import org.apache.commons.text.StringEscapeUtils;
import org.jsoup.nodes.Document;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class HtmlTranslateService {

    private final HttpClient httpClient;
    private final GcpProperties gcpProperties;
    private final ObjectMapper om = new ObjectMapper();

    // 한국어 포함 여부 판단용
    private static final Pattern HAS_KOREAN = Pattern.compile("[\\uAC00-\\uD7A3]");

    public HtmlTranslateService(HttpClient httpClient, GcpProperties gcpProperties) {
        this.httpClient = httpClient;
        this.gcpProperties = gcpProperties;
    }

    /**
     * (스마트) HTML을 파싱하여 텍스트 노드만 번역.
     * - 태그/속성/DOM 구조 보존
     * - script/style/code/pre 등은 번역 제외
     * - source=ko 고정 (한국어만 번역)
     */
    public String translateKoHtmlSmart(String html, SupportedLanguage target) {
        try {
            Document doc = Jsoup.parse(html);

            // 번역 대상 텍스트 노드 수집
            List<TextNode> targets = new ArrayList<>();
            NodeTraversor.traverse(new NodeVisitor() {
                @Override public void head(Node node, int depth) {
                    if (node instanceof TextNode text) {
                        String parent = node.parent() != null ? node.parent().nodeName() : "";
                        // 번역 제외 태그
                        if (parent.matches("(?i)script|style|code|pre|kbd|samp")) return;

                        String textVal = text.getWholeText();
                        if (textVal != null && !textVal.isBlank() && HAS_KOREAN.matcher(textVal).find()) {
                            targets.add(text);
                        }
                    }
                }
                @Override public void tail(Node node, int depth) {}
            }, doc);

            if (targets.isEmpty()) {
                // 번역할 한국어 텍스트가 없으면 원문 그대로 반환
                return html;
            }

            // 텍스트만 모아 배치 번역 (q 배열 지원)
            List<String> originals = new ArrayList<>(targets.size());
            for (TextNode t : targets) {
                originals.add(t.getWholeText());
            }

            // 너무 큰 문서는 배치로 쪼개서 호출 (간단히 1000개 단위/혹은 바이트 기준으로도 확장 가능)
            int batchSize = 500;
            int n = originals.size();
            int from = 0;
            List<String> translatedAll = new ArrayList<>(n);

            while (from < n) {
                int to = Math.min(from + batchSize, n);
                List<String> slice = originals.subList(from, to);
                translatedAll.addAll(callTranslateApiV2TextBatch(slice, target));
                from = to;
            }

            // 번역 결과를 원래 텍스트 노드에 매핑
            if (translatedAll.size() != targets.size()) {
                throw new TranslateFailedException("번역 항목 수 불일치");
            }
            for (int i = 0; i < targets.size(); i++) {
                String translated = translatedAll.get(i);
                // 엔티티 디코딩 후 삽입
                translated = StringEscapeUtils.unescapeHtml4(translated);
                targets.get(i).text(translated);
            }

            // DOM을 직렬화해서 반환 (태그/속성 보존)
            return doc.outerHtml();

        } catch (TranslateFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new TranslateFailedException("HTML 스마트 번역 처리 중 오류", e);
        }
    }

    /**
     * Google Translate v2 (API Key) 호출 - format=text, source=ko
     * q 배열을 받아 일괄 번역 후, 동일 순서로 결과 반환.
     */
    private List<String> callTranslateApiV2TextBatch(List<String> texts, SupportedLanguage target) {
        try {
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + gcpProperties.getKey();

            Map<String, Object> body = new HashMap<>();
            body.put("q", texts);              // 배열
            body.put("source", "ko");          // 한국어만 번역
            body.put("target", target.code()); // en, ja ...
            body.put("format", "text");        // 텍스트만 번역

            String json = om.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                String snippet = resp.body() != null && resp.body().length() > 500
                        ? resp.body().substring(0, 500) + "..."
                        : resp.body();
                throw new TranslateFailedException("GCP Translation API 실패 (HTTP " + resp.statusCode() + "): " + snippet);
            }

            JsonNode root = om.readTree(resp.body());
            JsonNode translations = root.path("data").path("translations");
            if (!translations.isArray() || translations.isEmpty()) {
                throw new TranslateFailedException("번역 응답 파싱 실패");
            }

            List<String> results = new ArrayList<>(translations.size());
            for (JsonNode node : translations) {
                String txt = node.path("translatedText").asText("");
                results.add(txt);
            }
            return results;

        } catch (TranslateFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new TranslateFailedException("번역 API 호출 오류", e);
        }
    }
}