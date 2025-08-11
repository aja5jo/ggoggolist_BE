package group5.backend.exception.gcp;

public class MissingGcpApiKeyException extends RuntimeException {
    public MissingGcpApiKeyException(String message) {
        super(message);
    }
}
