package group5.backend.dto.favorite.store.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FavoriteStoreResponse{
    private Long storeId;
    private boolean liked;
}

