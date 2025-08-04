package group5.backend.service;

import group5.backend.domain.user.User;
import group5.backend.dto.login.request.LoginRequest;
import group5.backend.dto.login.response.LoginResponse;
import group5.backend.exception.login.UserNotFoundByEmailException;
import group5.backend.exception.login.WrongPasswordException;
import group5.backend.repository.UserRepository;
import group5.backend.dto.signup.request.SignupRequest;
import group5.backend.dto.signup.response.SignupResponse;
import group5.backend.exception.signup.DuplicateEmailException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
@Slf4j
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
                .orElseThrow(() -> new UserNotFoundByEmailException(request.getEmail()));

        if (!bCryptPasswordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new WrongPasswordException("비밀번호가 일치하지 않습니다.");
        }

        session.setAttribute("user", user);

        return LoginResponse.of(user);
    }

}
