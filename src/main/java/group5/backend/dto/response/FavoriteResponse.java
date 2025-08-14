package group5.backend.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class FavoriteResponse {

    private Long id;  // storeId, eventId, popupId 등
    private String type;  // "store", "event", "popup"
    private boolean liked;  // 좋아요 상태 (true 또는 false)
    private int likeCount;  // 좋아요 개수
    private String name;

    public static FavoriteResponse of(Long id, String type, boolean liked, int likeCount, String name) {
        return FavoriteResponse.builder()
                .id(id)
                .type(type)
                .liked(liked)  // 동적으로 liked 값 설정
                .likeCount(likeCount)
                .name(name)    // 항목의 이름 설정
                .build();
    }
}