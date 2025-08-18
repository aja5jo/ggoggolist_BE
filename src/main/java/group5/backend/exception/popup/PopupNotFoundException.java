package group5.backend.exception.popup;

public class PopupNotFoundException extends RuntimeException {
    private final Long popupId;

    public PopupNotFoundException(Long popupId, String message) {
        super(message);
        this.popupId = popupId;
    }

    public Long getPopupId() {
        return popupId;
    }
}