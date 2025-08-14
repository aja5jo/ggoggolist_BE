package group5.backend.dto.common.popup.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PopupUpdateRequest {
    private group5.backend.domain.user.Category category;
    private String name;
    private String description;
    private String intro;
    private String thumbnail;
    private List<String> images;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String address;

}