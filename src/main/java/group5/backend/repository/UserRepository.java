package group5.backend.repository;

import group5.backend.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);         // 로그인 등 인증용
    boolean existsByEmail(String email);              // 이메일 중복 체크용
}
