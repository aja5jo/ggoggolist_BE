package group5.backend.config.gcp;

import group5.backend.exception.gcp.MissingGcpApiKeyException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GcpKeyGuard {

    private final GcpProperties props;

    @PostConstruct
    public void ensureKeyPresent() {
        if (props.getKey() == null || props.getKey().isBlank()) {
            throw new MissingGcpApiKeyException("GCP API Key가 없습니다. 환경변수 GCP_API_KEY를 설정하세요.");
        }
    }
}