package group5.backend.controller.ai.openai;

import group5.backend.config.ai.OpenAiProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/__diag/openai")
public class OpenAiDiagController {

    private final WebClient openAiWebClient;
    private final OpenAiProperties props;

    @GetMapping
    public ResponseEntity<Map<String, Object>> ping() {
        // 1) 모델 목록 조회
        Map models = openAiWebClient.get()
                .uri("/models")
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        // 2) 초미니 채팅 스모크(토큰 최소)
        Map body = Map.of(
                "model", props.getChatModel(),            // 예: gpt-4o-mini
                "messages", List.of(
                        Map.of("role", "user", "content", "ping")
                ),
                "max_tokens", 5
        );
        Map chat = openAiWebClient.post()
                .uri("/chat/completions")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block(Duration.ofSeconds(10));

        return ResponseEntity.ok(Map.of(
                "models_ok", models != null,
                "models_sample_count", ((List<?>) models.getOrDefault("data", List.of())).size(),
                "chat_ok", chat != null,
                "chat_reply_excerpt", extractContentExcerpt(chat, 60)
        ));
    }

    @SuppressWarnings("unchecked")
    private static String extractContentExcerpt(Map chat, int max) {
        try {
            var choices = (List<Map<String, Object>>) chat.get("choices");
            var msg = (Map<String, Object>) choices.get(0).get("message");
            var content = String.valueOf(msg.get("content"));
            return content.length() > max ? content.substring(0, max) + "..." : content;
        } catch (Exception e) {
            return "(no content)";
        }
    }
}
