package group5.backend.dto.common.popup.response;

import group5.backend.domain.user.Category;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
public class PopupCreateResponse {
    private Long id;
    private Long userId;
    private Category category;
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
