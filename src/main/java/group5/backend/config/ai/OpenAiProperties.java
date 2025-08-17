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
    private int timeoutSec = 30;
    private int maxRetries = 3;
    private long initialBackoffMs = 1000;
}
