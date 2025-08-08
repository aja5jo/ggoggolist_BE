package group5.backend.dto.common.popup.response;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PopupListResponse {
    private List<PopupSummaryResponse> popups;

    public static PopupListResponse of(List<PopupSummaryResponse> popups) {
        return PopupListResponse.builder()
                .popups(popups == null ? List.of() : popups)
                .build();
    }
}
