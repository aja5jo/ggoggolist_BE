package group5.backend.dto.favorite.event.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoriteEventResponse{
    private Long eventId ;
    private boolean liked;
}


