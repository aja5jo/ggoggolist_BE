package group5.backend.config.security;

import group5.backend.domain.user.User;
import group5.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 시큐리티가 로그인 요청을 가로챌 때
     * username으로 넘겨받은 값(email)을 기준으로 사용자 정보를 조회하는 메서드
     */
    @Override
    public User loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("해당 이메일을 가진 유저를 찾을 수 없습니다: " + email));
    }
}