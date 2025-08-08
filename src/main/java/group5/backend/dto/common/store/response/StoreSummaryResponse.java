package group5.backend.dto.common.store.response;

import group5.backend.domain.store.Store;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StoreSummaryResponse {
    private Long id;
    private String name;
    private String thumbnail;
    private int likeCount;
    private boolean liked;

    public static StoreSummaryResponse from(Store store, boolean liked) {
        return StoreSummaryResponse.builder()
                .id(store.getId())
                .name(store.getName())
                .thumbnail(store.getThumbnail())
                .likeCount(store.getLikeCount())
                .liked(liked)
                .build();
    }
}


