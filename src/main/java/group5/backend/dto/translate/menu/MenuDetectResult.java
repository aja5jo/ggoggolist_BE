package group5.backend.dto.translate.menu;

import lombok.Value;
import java.util.List;

@Value
public class MenuDetectResult {
    boolean isMenuBoard;
    double score;
    List<String> reasons;
}
