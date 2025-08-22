package group5.backend.config.ai;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {
    private String baseUrl;
    private String apiKey;

    private String chatModel;
    private String imageModel;
    private String embeddingModel = "text-embedding-3-small";
    private int embeddingDim = 1536;

    private int timeoutSec = 30;
    private int maxRetries = 3;
    private long initialBackoffMs = 1000;
}
