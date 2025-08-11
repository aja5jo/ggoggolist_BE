package group5.backend.exception.gcp;

public class ImageDownloadFailedException extends RuntimeException {
    private final int statusCode;

    public ImageDownloadFailedException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}