package group5.backend.dto.favorite.response;
import group5.backend.dto.favorite.FavoriteNameItem;
import lombok.*;
import java.util.List;

@Getter
@AllArgsConstructor
public class FavoriteNameListResponse {
    private List<FavoriteNameItem> items;
}
