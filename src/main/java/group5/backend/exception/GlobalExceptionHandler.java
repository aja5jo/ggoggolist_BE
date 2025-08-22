package group5.backend.exception;

import group5.backend.domain.user.Category;
import group5.backend.exception.event.EventNotFoundException;
import group5.backend.exception.event.HandleInvalidFilterException;
import group5.backend.exception.favorite.FavoriteNotFoundException;
import group5.backend.exception.gcp.*;
import group5.backend.exception.popup.PopupNotFoundException;
import group5.backend.exception.store.StoreNotFoundException;
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

// ✅ 불필요한 import 제거: HttpServletRequest, WebClientResponseException, UUID
// 필요시에만 유지
import org.springframework.dao.DataAccessException;

import java.util.List;
import java.util.Map;

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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateEmail(DuplicateEmailException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(UserNotFoundByEmailException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFoundByEmail(UserNotFoundByEmailException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEntityNotFound(EntityNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(WrongPasswordException.class)
    public ResponseEntity<ApiResponse<?>> handleWrongPassword(WrongPasswordException e) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // 카테고리 사이즈
    @ExceptionHandler(UserInvalidCategorySizeException.class)
    public ResponseEntity<ApiResponse<List<Category>>> handleInvalidCategorySize(UserInvalidCategorySizeException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage(), e.getCurrentCategories());
    }

    @ExceptionHandler(MerchantInvalidCategorySizeException.class)
    public ResponseEntity<ApiResponse<?>> handleMerchantInvalidCategorySize(MerchantInvalidCategorySizeException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleUserNotFound(UserNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(HandleInvalidFilterException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidFilter(HandleInvalidFilterException e) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleStoreNotFound(StoreNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleEventNotFound(EventNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(PopupNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handlePopupNotFound(PopupNotFoundException e) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // GCP 관련
    @ExceptionHandler(MissingGcpApiKeyException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingGcpApiKey(MissingGcpApiKeyException ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ExceptionHandler(ImageDownloadFailedException.class)
    public ResponseEntity<ApiResponse<?>> handleImageDownloadFailed(ImageDownloadFailedException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) status = HttpStatus.BAD_REQUEST;
        return buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(TranslationApiException.class)
    public ResponseEntity<ApiResponse<?>> handleTranslationApiException(TranslationApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(VisionApiException.class)
    public ResponseEntity<ApiResponse<?>> handleVisionApiException(VisionApiException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        return buildErrorResponse(status, ex.getMessage());
    }

    @ExceptionHandler(FavoriteNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleFavoriteNotFoundException(FavoriteNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidTargetLanguageException.class)
    public ResponseEntity<ApiResponse<?>> handleInvalidTargetLanguage(InvalidTargetLanguageException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(TranslateFailedException.class)
    public ResponseEntity<ApiResponse<?>> handleTranslateFailed(TranslateFailedException ex){
        return buildErrorResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    // WebClient(OpenAI 등): 필요한 형태로 유지 (FQCN 사용하므로 import 불필요)
    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden.class)
    public ResponseEntity<?> handle403(org.springframework.web.reactive.function.client.WebClientResponseException.Forbidden e){
        String body = e.getResponseBodyAsString();
        return ResponseEntity.status(403).body(Map.of(
                "success", false, "code", 403, "message", body, "data", null
        ));
    }


    @ExceptionHandler(org.springframework.web.reactive.function.client.WebClientResponseException.class)
    public ResponseEntity<?> handleOpenAi(
            org.springframework.web.reactive.function.client.WebClientResponseException e) {

        String body = e.getResponseBodyAsString();
        // Map.of 는 null 금지 → LinkedHashMap 사용
        java.util.Map<String, Object> payload = new java.util.LinkedHashMap<>();
        payload.put("success", false);
        payload.put("code", e.getRawStatusCode());
        payload.put("message", (body == null || body.isBlank()) ? "외부 API 호출 오류" : body);
        payload.put("data", null); // 이제 안전하게 null 가능

        return ResponseEntity.status(e.getStatusCode()).body(payload);
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllExceptions(Exception e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
    }

    // (선택) DB 전용 핸들러를 유지하고 싶다면: 로그/errId 없이 단순 응답만
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponse<?>> handleData(DataAccessException e) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "데이터 처리 중 오류가 발생했습니다.");
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponse(HttpStatus status, String message) {
        ApiResponse<?> response = new ApiResponse<>(false, status.value(), message);
        return ResponseEntity.status(status).body(response);
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(HttpStatus status, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>(false, status.value(), message, data);
        return ResponseEntity.status(status).body(response);
    }
}
