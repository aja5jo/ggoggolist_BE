package group5.backend.exception.gcp;

public class InvalidTargetLanguageException extends IllegalArgumentException {

    public InvalidTargetLanguageException(String message) {
        super(message);
    }
}