package group5.backend.exception.store;

public class StoreNotFoundException extends RuntimeException {
    private final Long storeId;

    public StoreNotFoundException(Long storeId, String message) {
        super(message);
        this.storeId = storeId;
    }

    public Long getStoreId() { return storeId; }
}

