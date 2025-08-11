
package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.login.request.LoginRequest;
import group5.backend.dto.login.response.LoginResponse;
import group5.backend.response.ApiResponse;
import group5.backend.dto.signup.request.SignupRequest;
import group5.backend.dto.signup.response.SignupResponse;
import group5.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserApiController {

    private final UserService userService;

    @Operation(summary = "회원가입", description = "이메일, 비밀번호 등의 정보를 통해 회원가입을 수행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = userService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, 201, "회원가입 성공", response));
    }

    @Operation(
            summary = "로그인",
            description = "이메일과 비밀번호를 통해 로그인합니다. 세션이 생성됩니다.",
            security = @SecurityRequirement(name = "JSESSIONID")
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        LoginResponse response = userService.login(request, session);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, 200, "로그인 성공", response));
    }

    // 현재 로그인한 사용자의 권한 확인
    @GetMapping("/auth/me")
    public Map<String, Object> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return Map.of("message", "비로그인 상태입니다.");
        }
        var auths = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return Map.of(
                "email", user.getEmail(),
                "authorities", auths
        );
    }
}
