// src/main/java/group5/backend/service/ai/recomm/OpenAIEmbeddingClient.java
package group5.backend.service.ai.recomm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAIEmbeddingClient {

    private final WebClient openAiWebClient;
    private final group5.backend.config.ai.OpenAiProperties props;

    public float[] embed(String text) {
        long t0 = System.nanoTime();
        try {
            // base-url: https://api.openai.com/v1  이므로 여기서는 "/embeddings" 만!
            var resp = openAiWebClient.post()
                    .uri("/embeddings")
                    .bodyValue(new EmbeddingRequest(props.getEmbeddingModel(), text))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, r -> r.createException())
                    .bodyToMono(EmbeddingResponse.class)
                    .block();

            float[] vec = (resp == null) ? null : resp.firstVector();
            long ms = (System.nanoTime() - t0) / 1_000_000;
            log.debug("[EMB] ok model={}, dim={}, took={}ms",
                    props.getEmbeddingModel(), (vec == null ? 0 : vec.length), ms);
            return vec;
        } catch (Exception e) {
            log.error("[EMB] failed model={} {}", props.getEmbeddingModel(), e.toString(), e);
            throw e;
        }
    }

    public String modelName() { return props.getEmbeddingModel(); }
    public int dim() { return props.getEmbeddingDim(); }

    // ---- 요청/응답 DTO ----
    record EmbeddingRequest(String model, String input) {}

    record EmbeddingResponse(java.util.List<Data> data) {
        float[] firstVector() {
            if (data == null || data.isEmpty() || data.get(0).embedding == null) return null;
            var list = data.get(0).embedding;
            float[] v = new float[list.size()];
            for (int i = 0; i < list.size(); i++) v[i] = list.get(i).floatValue();
            return v;
        }
        record Data(java.util.List<Double> embedding) {}
    }
}
