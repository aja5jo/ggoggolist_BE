package group5.backend.config.jackson;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

@Configuration
public class GlobalJacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            // Java 8 날짜/시간 모듈 등록
            builder.modulesToInstall(JavaTimeModule.class);

            // ✅ 숫자/배열 타임스탬프 금지 → 문자열(ISO-8601)로 직렬화
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        };
    }
}
