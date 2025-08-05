package group5.backend.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import group5.backend.config.security.UserDetailsServiceImpl;
import group5.backend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import jakarta.servlet.http.HttpServletResponse;


/**
 * @EnableWebSecurity: @PreAuthorize, @Secured 같은 메서드 수준의 권한 체크 애노테이션을 사용할 수 있게 활성화
 * @PreAuthorize: 메서드 실행 전에 SpEL 표현식을 이용해 권한을 검사하는 애노테이션
 * ex) @PreAuthorize("hasRole('MERCHANT')")
 * @Secured: 지정한 역할(예: ROLE_ADMIN)이 있어야 메서드를 실행할 수 있도록 제한하는 애노테이션
 * ex) @Secured("ROLE_MERCHANT")
 * */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsServiceImpl userDetailService;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;


    // 정적 리소스, h2-console 보안 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                .requestMatchers("/h2-console/**", "/static/**");
    }

    // 보안 필터 체인 설정
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // REST API에서는 CSRF 비활성화
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users",              // POST: 회원가입 (로그인 없이 가능)
                                "/api/users/login"        // POST: 로그인 (로그인 없이 가능)
                        ).permitAll()

                        .requestMatchers("/api/public/**").permitAll() // 비회원도 접근 가능한 공개 API

                        .requestMatchers("/api/users/**").hasAnyRole("USER")       // 일반 유저 전용 API
                        .requestMatchers("/api/merchants/**").hasAnyRole("MERCHANT") // 소상공인 전용 API

                        .anyRequest().authenticated() // 그 외는 인증 필요
                )



                .logout(logout -> logout
                        .logoutUrl("/api/users/logout")
                        .addLogoutHandler((request, response, authentication) -> {
                            if (authentication == null) {
                                throw new InsufficientAuthenticationException("로그인이 되어 있지 않습니다.");
                            }
                        })
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json");
                            response.setCharacterEncoding("UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);

                            ApiResponse<?> logoutResponse = new ApiResponse<>(true, 200, "로그아웃 성공", null);
                            ObjectMapper objectMapper = new ObjectMapper();
                            response.getWriter().write(objectMapper.writeValueAsString(logoutResponse));
                        })
                        .invalidateHttpSession(true)
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
