package group5.backend.dto.common.popup.response;


import group5.backend.dto.common.event.response.EventSummaryResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PopupResponse{
    private List<EventSummaryResponse> popup;
}

