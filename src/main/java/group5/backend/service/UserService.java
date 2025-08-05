package group5.backend.service;

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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

        // ✅ Spring Security 인증 객체 생성
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null,
                        List.of(new SimpleGrantedAuthority(user.getRole().name())));

        // ✅ SecurityContext에 인증 정보 등록
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // ✅ 세션에도 저장 (로그아웃 핸들러에서 필요)
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return LoginResponse.of(user);
    }


}
