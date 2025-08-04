package group5.backend.dto.signup.response;

import group5.backend.domain.user.Role;
import group5.backend.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder
@AllArgsConstructor
public class SignupResponse {
    private final Long id;
    private final String email;
    private final Role role;

    public static SignupResponse of(User user) {
        return SignupResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}


