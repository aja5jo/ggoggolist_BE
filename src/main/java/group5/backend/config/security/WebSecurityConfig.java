package group5.backend.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.http.HttpMethod;

import jakarta.servlet.http.HttpServletResponse;


/**
 * @EnableWebSecurity: @PreAuthorize, @Secured 같은 메서드 수준의 권한 체크 애노테이션을 사용할 수 있게 활성화
 * @PreAuthorize: 메서드 실행 전에 SpEL 표현식을 이용해 권한을 검사하는 애노테이션
 * ex) @PreAuthorize("hasRole('MERCHANT')")
 * @Secured: 지정한 역할(예: ROLE_ADMIN)이 있어야 메서드를 실행할 수 있도록 제한하는 애노테이션
 * ex) @Secured("ROLE_MERCHANT")
 * */
@EnableMethodSecurity(prePostEnabled = true)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;


    // 정적 리소스 보안 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/static/**");
    }

    // 보안 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http

                .cors(Customizer.withDefaults())              // ✅ CORS를 Security 필터에 연결
                .csrf(csrf -> csrf.disable())                 // 개발 단계면 비활성 유지 OK
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // ✅ 추가
                .securityContext(sc -> sc.requireExplicitSave(false))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/v3/api-docs/swagger-config",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/api/signup", "/api/login").permitAll()
                        .requestMatchers("/images/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()  // ✅ CORS preflight
                        .requestMatchers("/api/users/**").hasAuthority("USER")
                        .requestMatchers("/api/merchants/**").hasAuthority("MERCHANT")
                        .requestMatchers("/api/**").permitAll()
                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            // 세션 무효화 + 시큐리티 컨텍스트 정리 (인증 여부와 무관하게 안전)
                            HttpSession session = request.getSession(false);
                            if (session != null) session.invalidate();
                            SecurityContextHolder.clearContext();
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            // JSESSIONID 쿠키 삭제 (Secure + SameSite=None + HttpOnly)
                            ResponseCookie bye = ResponseCookie.from("JSESSIONID", "")
                                    .path("/")
                                    .maxAge(0)
                                    .httpOnly(true)
                                    .secure(true)       // ★ HTTPS 쿠키만 전송
                                    .sameSite("None")   // ★ 크로스사이트에서도 동작
                                    .build();
                            response.setHeader(HttpHeaders.SET_COOKIE, bye.toString());

                            // 응답 JSON
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);

                            ApiResponse<?> body = new ApiResponse<>(true, 200, "로그아웃 성공", null);
                            new ObjectMapper().writeValue(response.getWriter(), body);
                        })
                        .invalidateHttpSession(true)   // 기본 핸들러도 세션 무효화 (중복 무해)
                        .clearAuthentication(true)
                        .permitAll()                   // 비로그인 상태에서도 호출 가능하게
                )

                .build();
    }

    // 인증 관리자 설정
    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            UserDetailsServiceImpl userDetailService) throws Exception{
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
        return new ProviderManager(authProvider);
    }

    // 비밀번호 인코더 빈 등록
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
