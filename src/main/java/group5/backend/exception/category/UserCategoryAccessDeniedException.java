package group5.backend.exception.category;

public class UserCategoryAccessDeniedException extends RuntimeException {
    public UserCategoryAccessDeniedException(String message) {
        super(message);
    }
}
