package group5.backend.dto.common.home.response;

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
}
