package group5.backend.dto.translate.response;

import group5.backend.dto.translate.menu.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmartTranslateResponse {
    /** "menu" | "normal" */
    private String mode;
    /** 자동 판별 점수 (튜닝 참고용) */
    private double score;
    /** 메뉴판일 때만 채워짐 */
    private List<MenuItem> menuItems;
    /** 일반 번역일 때만 채워짐 */
    private ImageTranslateResponse normal;
}