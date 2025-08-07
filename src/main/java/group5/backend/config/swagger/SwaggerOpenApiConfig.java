package group5.backend.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerOpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("JSESSIONID"))
                .components(new Components()
                        .addSecuritySchemes("JSESSIONID", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)  // ✔ APIKEY 타입으로
                                .in(SecurityScheme.In.COOKIE)     // ✔ 세션은 쿠키니까
                                .name("JSESSIONID")                // ✔ JSESSIONID를 쿠키 이름으로 지정
                                .description("세션 기반 인증 (JSESSIONID 사용)")
                        )
                )
                .info(new Info().title("API 문서").version("v1"));
    }
}


