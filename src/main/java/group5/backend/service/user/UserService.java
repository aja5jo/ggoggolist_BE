package group5.backend.service.user;

import group5.backend.domain.user.User;
import group5.backend.dto.login.request.LoginRequest;
import group5.backend.dto.login.response.LoginResponse;
import group5.backend.exception.login.UserNotFoundException;
import group5.backend.exception.login.WrongPasswordException;
import group5.backend.repository.UserRepository;
import group5.backend.dto.signup.request.SignupRequest;
import group5.backend.dto.signup.response.SignupResponse;
import group5.backend.exception.signup.DuplicateEmailException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SignupResponse signup(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 비밀번호 암호화 후 저장
        String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());
        User user = request.toEntity(encodedPassword);
        User savedUser = userRepository.save(user);

        SignupResponse response = SignupResponse.of(savedUser);
        return response;
    }

    public LoginResponse login(LoginRequest request, HttpSession session) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("존재하지 않는 이메일입니다."));

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new WrongPasswordException("비밀번호가 일치하지 않습니다.");
        }

        // 1) 인증 객체
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,                         // principal: 도메인 User 사용 중이면 그대로 OK
                null,
                List.of(new SimpleGrantedAuthority(user.getRole().name())) // "USER" / "MERCHANT"
        );

        // 2) SecurityContext 생성/등록
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        // 3) 세션에 표준 키로 저장 (★ 중요)
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );

        // 4) 응답
        return LoginResponse.of(user);
    }

}
