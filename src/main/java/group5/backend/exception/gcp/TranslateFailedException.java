package group5.backend.exception.gcp;

public class TranslateFailedException extends RuntimeException {
  public TranslateFailedException(String message) {
    super(message);
  }
  public TranslateFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}