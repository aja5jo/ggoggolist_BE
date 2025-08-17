package group5.backend.config.ai;

import io.netty.channel.ChannelOption;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(OpenAiProperties.class)
@RequiredArgsConstructor
public class OpenAiConfig {
    private final OpenAiProperties props;

    @Bean
    public WebClient openAiWebClient() {
        HttpClient http = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(props.getTimeoutSec()))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getTimeoutSec() * 1000);
        return WebClient.builder()
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .clientConnector(new ReactorClientHttpConnector(http))
                .build();
    }
}
