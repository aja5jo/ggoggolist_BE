package group5.backend.dto.common.store.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreUpdateRequest {
    private String name;
    private String address;
    private String number;
    private String intro;
    private String category;
    private String thumbnail;
    private List<String> images;
    private LocalTime startTime;
    private LocalTime endTime;
}