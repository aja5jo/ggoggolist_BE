package group5.backend.dto.common.popup.response;


import group5.backend.domain.popup.Popup;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PopupSummaryResponse {
    private Long id;
    private String name;
    private String thumbnail;
    private String description;
    private int likeCount;
    private boolean liked;
    private LocalDate startDate;
    private LocalDate endDate;

    public static PopupSummaryResponse from(Popup popup, boolean liked) {
        return PopupSummaryResponse.builder()
                .id(popup.getId())
                .name(popup.getName())
                .thumbnail(popup.getThumbnail())
                .description(popup.getDescription())
                .likeCount(popup.getLikeCount())
                .liked(liked)
                .startDate(popup.getStartDate())
                .endDate(popup.getEndDate())
                .build();
    }
}


