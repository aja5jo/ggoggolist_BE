package group5.backend.exception;

import group5.backend.domain.user.Category;
import group5.backend.exception.event.HandleInvalidFilterException;
import group5.backend.exception.favorite.FavoriteNotFoundException;
import group5.backend.exception.gcp.ImageDownloadFailedException;
import group5.backend.exception.gcp.MissingGcpApiKeyException;
import group5.backend.exception.gcp.TranslationApiException;
import group5.backend.exception.gcp.VisionApiException;
import group5.backend.response.ApiResponse;
import group5.backend.exception.category.MerchantInvalidCategorySizeException;
import group5.backend.exception.category.UserInvalidCategorySizeException;
import group5.backend.exception.login.UserNotFoundException;
import group5.backend.exception.login.UserNotFoundByEmailException;
import group5.backend.exception.login.WrongPasswordException;
import group5.backend.exception.signup.DuplicateEmailException;
import group5.backend.exception.store.StoreNotFoundException;
import group5.backend.exception.event.EventNotFoundException;
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

    /* ---------- 400 Bad Request ---------- */
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

    //필드 누락
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
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
    //ENUM 매칭 오류
    @ExceptionHandler(HandleInvalidFilterException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFilter(HandleInvalidFilterException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    //가게 없음
    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleStoreNotFound(StoreNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    //이벤트 없음
    @ExceptionHandler(EventNotFoundException.class)
    public org.springframework.http.ResponseEntity<group5.backend.response.ApiResponse<?>> handleEventNotFound(
            EventNotFoundException e) {
        return buildErrorResponse(org.springframework.http.HttpStatus.NOT_FOUND, e.getMessage());
    }

    //api key 관련 예외
    @ExceptionHandler(MissingGcpApiKeyException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingGcpApiKey(MissingGcpApiKeyException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    //이미지 다운로드 관련 예외
    @ExceptionHandler(ImageDownloadFailedException.class)
    public ResponseEntity<ApiResponse<?>> handleImageDownloadFailed(ImageDownloadFailedException ex) {
        // HTTP 상태 코드가 실제 실패 코드를 반영하게 하고 싶으면 ex.getStatusCode()를 그대로 사용 가능
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.BAD_REQUEST; // 기본값
        }
        return buildErrorResponse(status, ex.getMessage());
    }

    //translation 관련 예외
    @ExceptionHandler(TranslationApiException.class)
    public ResponseEntity<ApiResponse<?>> handleTranslationApiException(TranslationApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        // 필요하면 ex.getResponseBody()를 로그로 남겨서 API 응답 본문 추적
        return buildErrorResponse(status, ex.getMessage());
    }
    //vision 관련 예외
    @ExceptionHandler(VisionApiException.class)
    public ResponseEntity<ApiResponse<?>> handleVisionApiException(VisionApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return buildErrorResponse(status, ex.getMessage());
    }

    // FavoriteNotFoundException 처리
    @ExceptionHandler(FavoriteNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleFavoriteNotFoundException(FavoriteNotFoundException ex) {
        // FavoriteNotFoundException이 발생하면 404 Not Found 응답
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
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
