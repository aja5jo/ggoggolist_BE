package group5.backend.service;

import group5.backend.domain.popup.Popup;
import group5.backend.dto.common.popup.response.PopupDetailResponse;
import group5.backend.exception.popup.PopupNotFoundException;
import group5.backend.repository.FavoritePopupRepository;
import group5.backend.repository.PopupRepository;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupQueryService {

    private final PopupRepository popupRepository;
    private final FavoritePopupRepository favoritePopupRepository;

    public PopupDetailResponse getPopupDetail(@Nullable Long userId, Long popupId) {
        Popup p = popupRepository.findDetailById(popupId)
                .orElseThrow(() -> new PopupNotFoundException(popupId, "해당 ID의 팝업을 찾을 수 없습니다."));

        boolean liked = (userId != null) && favoritePopupRepository.existsByUserIdAndPopupId(userId, popupId);


        return PopupDetailResponse.builder()
                .id(p.getId())
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .category(p.getCategory())
                .name(p.getName())
                .description(p.getDescription())
                .intro(p.getIntro())
                .thumbnail(p.getThumbnail())
                .images(p.getImages() != null ? p.getImages() : List.of())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .startTime(p.getStartTime())
                .endTime(p.getEndTime())
                .address(p.getAddress())
                .likeCount(p.getLikeCount())
                .liked(liked)
                .build();
    }
}