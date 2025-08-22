// group5/backend/service/ai/recomm/ItemCandidate.java
package group5.backend.service.ai.recomm;

import group5.backend.domain.recomm.ItemType;
import java.time.LocalDate;

public record ItemCandidate(
        ItemType type,
        Long id,
        int likeCount,
        String category,   // enum name string
        String address,
        LocalDate startDate, LocalDate endDate
) {
    public static ItemCandidate of(ItemType t, Long id, int likeCount, String category, String address, LocalDate sd, LocalDate ed) {
        return new ItemCandidate(t, id, likeCount, category, address, sd, ed);
    }
}
