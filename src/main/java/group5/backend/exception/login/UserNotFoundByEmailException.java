package group5.backend.exception.login;

public class UserNotFoundByEmailException extends RuntimeException {
    public UserNotFoundByEmailException(String email) {
        super("존재하지 않는 이메일입니다: " + email);
    }
}
