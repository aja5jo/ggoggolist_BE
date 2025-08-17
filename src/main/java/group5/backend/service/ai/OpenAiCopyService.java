package group5.backend.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.openai.OpenAiProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class OpenAiCopyService {

    private final WebClient openAiWebClient;
    private final OpenAiProperties props;
    private final ObjectMapper om = new ObjectMapper();

    @Getter
    @AllArgsConstructor
    public static class CopyResult {
        private final String intro;
        private final String description;
    }

    /* ===================== 공개 API: 팝업 ===================== */

    /** 팝업 전용(텍스트 전용) */
    public CopyResult generatePopupCopy(String title, String placeName, String category, String address, String introHint) {
        return generatePopupCopy(title, placeName, category, address, introHint, List.of());
    }

    /** 팝업 전용(이미지 입력 포함) */
    public CopyResult generatePopupCopy(String title, String placeName, String category, String address, String introHint,
                                        List<String> imageUrls) {
        String system = buildPopupSystemPrompt();
        Object userParts = buildUserParts(title, placeName, category, address, introHint, imageUrls);
        return generateWithPrompts(system, userParts);
    }

    /* ===================== 공개 API: 이벤트 ===================== */

    /** 이벤트 전용(텍스트 전용) */
    public CopyResult generateEventCopy(String title, String storeName, String category, String address, String storeIntroHint) {
        return generateEventCopy(title, storeName, category, address, storeIntroHint, List.of());
    }

    /** 이벤트 전용(이미지 입력 포함) */
    public CopyResult generateEventCopy(String title, String storeName, String category, String address, String storeIntroHint,
                                        List<String> imageUrls) {
        String system = buildEventSystemPrompt();
        Object userParts = buildUserParts(title, storeName, category, address, storeIntroHint, imageUrls);
        return generateWithPrompts(system, userParts);
    }

    /* ===================== 프롬프트 빌더 ===================== */

    private String buildEventSystemPrompt() {
        return """
            너는 소상공인 **이벤트** 홍보 카피라이터다.
            출력은 **JSON 문자열 하나**로만 제공한다. key는 intro, description.
            작성 규칙:
            - intro: 90~130자. 핵심 혜택/기간/대상 요약. 이모지 남발 금지, 과장 금지, 문어체.
            - description: 300~550자. 매장 특장점·혜택·가격/기간·대상·위치·운영시간 포함, 마지막 줄에 간단한 CTA.
            - 해시태그 3~5개를 description 마지막에 포함(한국어, 공백 없이 #형태).
            - 금칙어/비속어/허위과장 금지. 반말 금지.
            예시: {"intro":"...","description":"... #태그1 #태그2 #태그3"}
            """;
    }

    private String buildPopupSystemPrompt() {
        return """
            너는 소상공인 **팝업스토어** 홍보 카피라이터다.
            출력은 **JSON 문자열 하나**로만 제공한다. key는 intro, description.
            작성 규칙:
            - intro: 100~140자. 한정성/기간, 체험 포인트, 굿즈/콜라보 등 후킹.
            - description: 350~650자. 콘셉트·체험 동선·한정 상품/굿즈·포토존·예약/대기 팁·위치·운영시간을 명료히.
            - 해시태그 4~6개를 description 마지막에 포함.
            - 과장/비속어 금지. 문어체.
            예시: {"intro":"...","description":"... #팝업스토어 #한정메뉴 #체험형"}
            """;
    }

    /**
     * 멀티모달 user 메시지 구성: 텍스트 + 이미지들(parts 배열)
     */
    private Object buildUserParts(String title, String placeOrStore, String category, String address, String hint, List<String> imageUrls) {
        String text = """
            [제목] %s
            [매장/장소명] %s
            [카테고리] %s
            [주소] %s
            [소개 힌트] %s
            위 정보와 아래 이미지들을 참고하여 intro/description JSON을 생성해줘.
            이미지에 문구가 있어도 그대로 인용하지 말고, 내용/분위기/제품·메뉴/톤앤매너만 반영해.
            """.formatted(nz(title), nz(placeOrStore), nz(category), nz(address), nz(hint));

        List<Object> parts = new ArrayList<>();
        parts.add(Map.of("type", "text", "text", text));

        if (imageUrls != null) {
            imageUrls.stream()
                    .filter(u -> u != null && !u.isBlank())
                    .filter(u -> u.startsWith("http://") || u.startsWith("https://"))
                    .distinct()
                    .limit(4)
                    .forEach(url -> parts.add(Map.of(
                            "type", "image_url",
                            "image_url", Map.of("url", url)
                    )));
        }
        return parts; // Chat Completions 멀티파트 content 형식
    }

    /* ===================== 공통 생성 로직 ===================== */

    private CopyResult generateWithPrompts(String system, Object userContentParts) {
        Map<String, Object> body = Map.of(
                "model", props.getChatModel(),
                "messages", List.of(
                        Map.of("role", "system", "content", system),
                        Map.of("role", "user",   "content", userContentParts)
                ),
                "max_tokens", 700
        );

        Map<?, ?> res = callWithRetry(body);
        String content  = extractAssistantContent(res);
        String cleaned  = sanitizePossibleJson(content);

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = om.readValue(cleaned, Map.class);
            return new CopyResult(
                    String.valueOf(parsed.getOrDefault("description", "")),
                    String.valueOf(parsed.getOrDefault("intro", ""))
            );
        } catch (Exception ignore) {
            return new CopyResult(cleaned, cleaned);
        }
    }

    /* ===================== 내부 유틸 ===================== */

    @SuppressWarnings("unchecked")
    private Map<?, ?> callWithRetry(Map<String, Object> body) {
        int attempts = 0;
        long backoff = props.getInitialBackoffMs();

        while (true) {
            try {
                return openAiWebClient.post()
                        .uri("/chat/completions")
                        .bodyValue(body)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .block(Duration.ofSeconds(props.getTimeoutSec()));
            } catch (WebClientResponseException e) {
                int status = e.getRawStatusCode();
                if (status == 429 && attempts < props.getMaxRetries()) {
                    long waitMs = parseRetryAfterMs(e, backoff);
                    sleep(waitMs);
                    attempts++;
                    backoff = Math.max(backoff * 2, 1000L);
                    continue;
                }
                String detail = e.getResponseBodyAsString(StandardCharsets.UTF_8);
                throw new RuntimeException("OpenAI 호출 실패: " + status + " - " + detail, e);
            }
        }
    }

    /** choices[0].message.content 를 String으로 안전 추출 */
    @SuppressWarnings("unchecked")
    private String extractAssistantContent(Map<?, ?> res) {
        Object choicesObj = res.get("choices");
        if (!(choicesObj instanceof List<?> choices) || choices.isEmpty()) return "";
        Object first = choices.get(0);
        if (!(first instanceof Map<?, ?> firstChoice)) return "";
        Object messageObj = firstChoice.get("message");
        if (!(messageObj instanceof Map<?, ?> messageMap)) return "";
        Object contentObj = messageMap.get("content");
        return contentObj == null ? "" : String.valueOf(contentObj);
    }

    /** ```json ... ``` 또는 ``` ... ``` 래핑 제거 + trim */
    private static final Pattern FENCE = Pattern.compile("^\\s*```(?:json)?\\s*([\\s\\S]*?)\\s*```\\s*$", Pattern.CASE_INSENSITIVE);

    private String sanitizePossibleJson(String s) {
        if (s == null) return "";
        var m = FENCE.matcher(s.trim());
        if (m.matches()) return m.group(1).trim();
        return s.trim();
    }

    private static long parseRetryAfterMs(WebClientResponseException e, long fallback) {
        try {
            String ra = e.getHeaders() == null ? null : e.getHeaders().getFirst("Retry-After");
            if (ra == null) return fallback;
            return Long.parseLong(ra.trim()) * 1000L;
        } catch (Exception ignore) {
            return fallback;
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms > 0 ? ms : 1000L); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
