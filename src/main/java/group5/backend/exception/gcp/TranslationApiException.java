package group5.backend.exception.gcp;

public class TranslationApiException extends RuntimeException {
    private final int statusCode;
    private final String responseBody;

    public TranslationApiException(int statusCode, String responseBody, String message) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
