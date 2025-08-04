package group5.backend.dto.login.response;

import group5.backend.domain.user.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private String email;
    private String role;

    public static LoginResponse of(User user) {
        return LoginResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

