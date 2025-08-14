package group5.backend.exception.event;

public class EventNotFoundException extends RuntimeException {
    private final Long eventId;

    public EventNotFoundException(Long eventId, String message) {
        super(message);
        this.eventId = eventId;
    }

    public Long getEventId() {
        return eventId;
    }
}
