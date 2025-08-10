package group5.backend.dto.favorite;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FavoriteNameItem {
    private FavoriteType type;  // STORE, EVENT, POPUP
    private Long id;
    private String name;
}