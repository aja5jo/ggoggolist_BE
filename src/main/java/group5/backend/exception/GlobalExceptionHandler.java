package group5.backend.exception;

import group5.backend.domain.user.Category;
import group5.backend.exception.event.HandleInvalidFilterException;
import group5.backend.response.ApiResponse;
import group5.backend.exception.category.MerchantInvalidCategorySizeException;
import group5.backend.exception.category.UserInvalidCategorySizeException;
import group5.backend.exception.login.UserNotFoundException;
import group5.backend.exception.login.UserNotFoundByEmailException;
import group5.backend.exception.login.WrongPasswordException;
import group5.backend.exception.signup.DuplicateEmailException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.BindException;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("유효하지 않은 요청입니다.");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiResponse<?>> handleBindException(BindException e) {
        String errorMessage = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("유효하지 않은 요청입니다.");
        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage);
    }

    //회원가입 시 역할이 누락되었을 때
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "역할(role)은 필수이며, USER 또는 MERCHANT 중 하나여야 합니다.");
    }

    // 이메일 중복
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateEmail(DuplicateEmailException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    //이메일 없음
    @ExceptionHandler(UserNotFoundByEmailException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFoundByEmail(UserNotFoundByEmailException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // 엔티티 없음
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }


    // 비밀번호 불일치
    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ApiResponse<?>> handleWrongPassword(WrongPasswordException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    //카테고리 사이즈 에러

    //유저
    @ExceptionHandler(UserInvalidCategorySizeException.class)
    public ResponseEntity<ApiResponse<List<Category>>> handleInvalidCategorySize(UserInvalidCategorySizeException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCurrentCategories());
    }

    //소상공인
    @ExceptionHandler(MerchantInvalidCategorySizeException.class)
    public ResponseEntity<ApiResponse<?>> handleMerchantInvalidCategorySize(MerchantInvalidCategorySizeException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    //유저 없음
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(HandleInvalidFilterException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFilter(HandleInvalidFilterException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    // 공통 에러 응답 생성 메서드
    private ResponseEntity<ApiResponse<?>> buildErrorResponse(HttpStatus status, String message) {
        ApiResponse<?> response = new ApiResponse<>(false, status.value(), message);
        return ResponseEntity.status(status).body(response);
    }

    // 추가: data를 포함하는 에러 응답
    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(HttpStatus status, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(false, status.value(), message, data);
        return ResponseEntity.status(status).body(response);
    }
}
