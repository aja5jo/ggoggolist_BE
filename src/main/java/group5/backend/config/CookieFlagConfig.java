package group5.backend.config;

import jakarta.servlet.SessionCookieConfig;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CookieFlagConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCookieCustomizer() {
        return factory -> factory.addContextCustomizers(context -> {
            // SameSite=None 전역 적용
            Rfc6265CookieProcessor cp = new Rfc6265CookieProcessor();
            cp.setSameSiteCookies("None");
            context.setCookieProcessor(cp);

            // 세션 쿠키 속성 강제
            SessionCookieConfig scc = context.getServletContext().getSessionCookieConfig();
            scc.setName("JSESSIONID");
            scc.setPath("/");
            scc.setHttpOnly(true);
            scc.setSecure(true); // ★ 항상 Secure로 발급
        });
    }
}