package group5.backend.service.popup;

import group5.backend.domain.popup.Popup;
import group5.backend.domain.user.User;
import group5.backend.dto.category.FeedItemType;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.dto.common.popup.request.PopupCreateRequest;
import group5.backend.dto.common.popup.request.PopupUpdateRequest;
import group5.backend.dto.common.popup.response.PopupCreateResponse;
import group5.backend.dto.common.popup.response.PopupSummaryResponse;
import group5.backend.repository.FavoritePopupRepository;
import group5.backend.repository.PopupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class PopupService {

    private final PopupRepository popupRepository;
    private final FavoritePopupRepository favoritePopupRepository;

    private void checkDate(PopupCreateRequest req) {
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new IllegalArgumentException("시작/종료일을 다시 확인해주세요.");
        }
    }
    @Transactional
    public PopupCreateResponse createPopup(User merchant, PopupCreateRequest req) {

        checkDate(req);
        Popup popup = Popup.builder()
                .user(merchant)
                .category(req.getCategory())
                .name(req.getName())
                .description(req.getDescription())
                .intro(req.getIntro())
                .thumbnail(req.getThumbnail())
                .images(req.getImages())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .address(req.getAddress())
                .likeCount(0)
                .build();

        Popup saved = popupRepository.save(popup);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PopupSummaryResponse> myPopups(User merchant) {
        List<Popup> popups = popupRepository.findByUserId(merchant.getId());
        return popups.stream()
                .map(p -> PopupSummaryResponse.from(
                        p,
                        favoritePopupRepository.existsByUserIdAndPopupId(merchant.getId(), p.getId())
                ))
                .toList();
    }
    @Transactional
    public PopupCreateResponse updatePopupPut(User merchant, Long popupId, PopupCreateRequest req) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new NoSuchElementException("팝업을 찾을 수 없습니다."));
        if (!popup.getUser().getId().equals(merchant.getId())) {
            throw new AccessDeniedException("본인 팝업만 수정할 수 있습니다.");
        }
        popup.setCategory(req.getCategory());
        popup.setName(req.getName());
        popup.setDescription(req.getDescription());
        popup.setIntro(req.getIntro());
        popup.setThumbnail(req.getThumbnail());
        popup.setImages(req.getImages());
        popup.setStartDate(req.getStartDate());
        popup.setEndDate(req.getEndDate());
        popup.setStartTime(req.getStartTime());
        popup.setEndTime(req.getEndTime());
        popup.setAddress(req.getAddress());

        if (popup.getStartDate() != null && popup.getEndDate() != null
                && popup.getEndDate().isBefore(popup.getStartDate())) {
            throw new IllegalArgumentException("시작/종료일을 다시 확인해주세요.");
        }

        Popup updated = popupRepository.save(popup);

        return toResponse(updated);
    }

    @Transactional
    public PopupCreateResponse updatePopupPatch(User merchant, Long popupId, PopupUpdateRequest req) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new NoSuchElementException("팝업을 찾을 수 없습니다."));
        if (!popup.getUser().getId().equals(merchant.getId())) {
            throw new AccessDeniedException("본인 팝업만 수정할 수 있습니다.");
        }

        if (req.getCategory() != null) popup.setCategory(req.getCategory());
        if (req.getName() != null) popup.setName(req.getName());
        if (req.getDescription() != null) popup.setDescription(req.getDescription());
        if (req.getIntro() != null) popup.setIntro(req.getIntro());
        if (req.getThumbnail() != null) popup.setThumbnail(req.getThumbnail());
        if (req.getImages() != null) popup.setImages(req.getImages());
        if (req.getStartDate() != null) popup.setStartDate(req.getStartDate());
        if (req.getEndDate() != null) popup.setEndDate(req.getEndDate());
        if (req.getStartTime() != null) popup.setStartTime(req.getStartTime());
        if (req.getEndTime() != null) popup.setEndTime(req.getEndTime());
        if (req.getAddress() != null) popup.setAddress(req.getAddress());

        if (popup.getStartDate() != null && popup.getEndDate() != null
                && popup.getEndDate().isBefore(popup.getStartDate())) {
            throw new IllegalArgumentException("시작/종료일을 다시 확인해주세요.");
        }

        Popup updated = popupRepository.save(popup);
        return toResponse(updated);
    }

    private PopupCreateResponse toResponse(Popup p) {
        return PopupCreateResponse.builder()
                .id(p.getId())
                .userId(p.getUser().getId())
                .category(p.getCategory()) // Category enum 그대로
                .name(p.getName())
                .description(p.getDescription())
                .intro(p.getIntro())
                .thumbnail(p.getThumbnail())
                .images(p.getImages())
                .startDate(p.getStartDate())
                .endDate(p.getEndDate())
                .startTime(p.getStartTime())
                .endTime(p.getEndTime())
                .address(p.getAddress())
                .build();
    }

    @Transactional
    public void deletePopup(User merchant, Long popupId) {
        Popup popup = popupRepository.findById(popupId)
                .orElseThrow(() -> new NoSuchElementException("팝업을 찾을 수 없습니다."));

        if (!Objects.equals(popup.getUser().getId(), merchant.getId())) {
            throw new AccessDeniedException("본인 팝업만 삭제할 수 있습니다.");
        }

        favoritePopupRepository.deleteByPopup_Id(popupId);
        popupRepository.delete(popup);
    }

    @Transactional
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
    @Transactional
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




