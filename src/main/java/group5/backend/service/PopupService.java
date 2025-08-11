package group5.backend.service;

import group5.backend.domain.popup.Popup;
import group5.backend.dto.category.FeedItemType;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.repository.FavoritePopupRepository;
import group5.backend.repository.PopupRepository;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PopupService {

    private final PopupRepository popupRepository;
    private final FavoritePopupRepository favoritePopupRepository;

    public List<CategoryFeedItemResponse> getThisWeekPopups(Long userId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        LocalDate startOfWeek = today.with(java.time.temporal.WeekFields.ISO.getFirstDayOfWeek());
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        return popupRepository.findThisWeek(startOfWeek, endOfWeek, sort).stream()
                .map(p -> toResponse(p, userId))
                .toList();
    }

    private CategoryFeedItemResponse toResponse(Popup popup, Long userId) {
        boolean liked = (userId != null) && favoritePopupRepository.existsByUserIdAndPopupId(userId, popup.getId());
        return new CategoryFeedItemResponse(
                FeedItemType.POPUP,
                popup.getId(),
                popup.getName(),
                popup.getThumbnail(),
                popup.getLikeCount(),
                liked,
                popup.getDescription(),
                popup.getStartDate(),
                popup.getEndDate()
        );
    }
}


