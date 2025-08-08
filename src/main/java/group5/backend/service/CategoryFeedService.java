package group5.backend.service;

import group5.backend.domain.user.Category;
import group5.backend.domain.user.User;
import group5.backend.dto.category.CategoryContent;
import group5.backend.dto.category.FeedItemType;
import group5.backend.dto.category.response.CategoryFeedItemResponse;

import group5.backend.dto.category.response.CategoryWithContentsResponse;
import group5.backend.dto.common.event.response.EventSummaryResponse;
import group5.backend.dto.common.store.response.StoreSummaryResponse;
import group5.backend.repository.EventRepository;
import group5.backend.repository.FavoriteEventRepository;
import group5.backend.repository.FavoriteStoreRepository;
import group5.backend.repository.StoreRepository;
import group5.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryFeedService {

    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final EventRepository eventRepository;
    private final FavoriteStoreRepository favoriteStoreRepository;
    private final FavoriteEventRepository favoriteEventRepository;

    /**
     * 카테고리별: 가게+이벤트를 합쳐 likeCount desc, id desc로 정렬 후 상위 totalLimitPerCategory만 반환.
     * 응답은 CategoryContent.items(합산 TopN)만 채워서 내려간다.
     */
    public CategoryWithContentsResponse buildCategoryFeed(User loginUser, int totalLimitPerCategory) {

        // 1) 요청 시점에 유저 최신화 (POST로 관심카테고리 바꾼 직후도 반영)
        User freshUser = null;
        if (loginUser != null) {
            freshUser = userRepository.findById(loginUser.getId()).orElse(null);
        }

        // 2) 카테고리 정렬은 freshUser 기준
        List<Category> orderedCategories = orderCategories(freshUser);

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        Long userId = (freshUser == null) ? null : freshUser.getId();
        List<CategoryContent> blocks = new ArrayList<>(orderedCategories.size());

        int fetchSize = Math.max(totalLimitPerCategory * 2, totalLimitPerCategory);

        for (Category c : orderedCategories) {
            Pageable page = PageRequest.of(0, fetchSize, sort);

            // 3) 후보 스토어
            List<StoreSummaryResponse> storeCandidates = storeRepository.findByCategory(c, page)
                    .stream()
                    .map(s -> StoreSummaryResponse.from(
                            s,
                            userId != null && favoriteStoreRepository.existsByUserIdAndStoreId(userId, s.getId())
                    ))
                    .collect(Collectors.toList());

            // 4) 후보 이벤트(진행 중)
            List<EventSummaryResponse> eventCandidates = eventRepository
                    .findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(c, today, today, page)
                    .stream()
                    .map(e -> EventSummaryResponse.from(
                            e,
                            userId != null && favoriteEventRepository.existsByUserIdAndEventId(userId, e.getId())
                    ))
                    .collect(Collectors.toList());

            // 5) 합산 정렬용 내부 타입
            class MixedItem {
                private final Integer likeCount;
                private final Long id;
                private final boolean isStore;
                private final StoreSummaryResponse store;
                private final EventSummaryResponse event;

                MixedItem(Integer likeCount, Long id, boolean isStore,
                          StoreSummaryResponse store, EventSummaryResponse event) {
                    this.likeCount = likeCount;
                    this.id = id;
                    this.isStore = isStore;
                    this.store = store;
                    this.event = event;
                }
            }

            // 6) 합치고 정렬
            List<MixedItem> merged = new ArrayList<>(storeCandidates.size() + eventCandidates.size());
            for (StoreSummaryResponse s : storeCandidates) {
                merged.add(new MixedItem(s.getLikeCount(), s.getId(), true, s, null));
            }
            for (EventSummaryResponse e : eventCandidates) {
                merged.add(new MixedItem(e.getLikeCount(), e.getId(), false, null, e));
            }

            merged.sort(Comparator
                    .comparing((MixedItem m) -> m.likeCount).reversed()
                    .thenComparing((MixedItem m) -> m.id).reversed());

            if (merged.size() > totalLimitPerCategory) {
                merged = new ArrayList<>(merged.subList(0, totalLimitPerCategory));
            }

            // 7) 최종 items 구성 (정렬 순서 유지)
            List<CategoryFeedItemResponse> items = new ArrayList<>(merged.size());
            for (MixedItem mi : merged) {
                if (mi.isStore) {
                    StoreSummaryResponse s = mi.store;
                    items.add(new CategoryFeedItemResponse(
                            FeedItemType.STORE,
                            s.getId(),
                            s.getName(),
                            s.getThumbnail(),
                            s.getLikeCount(),
                            s.isLiked(),
                            null,
                            null,
                            null
                    ));
                } else {
                    EventSummaryResponse e = mi.event;
                    items.add(new CategoryFeedItemResponse(
                            FeedItemType.EVENT,
                            e.getId(),
                            e.getName(),
                            e.getThumbnail(),
                            e.getLikeCount(),
                            e.isLiked(),
                            e.getDescription(),
                            e.getStartDate(),
                            e.getEndDate()
                    ));
                }
            }

            // 8) items만 담아서 블록 추가
            blocks.add(CategoryContent.of(c, items));
        }

        return CategoryWithContentsResponse.of(blocks);
    }

    /**
     * 단일 카테고리: 가게+이벤트 합쳐 likeCount desc, id desc로 '전체' 반환
     * 응답은 CategoryContent.items 만 포함
     */
    public CategoryContent buildSingleCategoryFeed(User loginUser, Category category) {
        // 1) 유저 최신화 (POST 직후에도 반영)
        User freshUser = null;
        if (loginUser != null) {
            freshUser = userRepository.findById(loginUser.getId()).orElse(null);
        }
        Long userId = (freshUser == null) ? null : freshUser.getId();

        // 2) 카테고리 파싱 (불필요 → 바로 category 사용)

        // 3) 정렬 기준
        Sort sort = Sort.by(Sort.Direction.DESC, "likeCount")
                .and(Sort.by(Sort.Direction.DESC, "id"));

        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 4) 후보 수집 (전체)
        List<StoreSummaryResponse> storeCandidates =
                storeRepository.findByCategory(category, sort).stream()
                        .map(s -> StoreSummaryResponse.from(
                                s,
                                userId != null && favoriteStoreRepository.existsByUserIdAndStoreId(userId, s.getId())
                        ))
                        .collect(Collectors.toList());

        List<EventSummaryResponse> eventCandidates =
                eventRepository.findByStore_CategoryAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                category, today, today, sort).stream()
                        .map(e -> EventSummaryResponse.from(
                                e,
                                userId != null && favoriteEventRepository.existsByUserIdAndEventId(userId, e.getId())
                        ))
                        .collect(Collectors.toList());

        // 5) 합산 정렬
        class MixedItem {
            private final Integer likeCount;
            private final Long id;
            private final boolean isStore;
            private final StoreSummaryResponse store;
            private final EventSummaryResponse event;

            MixedItem(Integer likeCount, Long id, boolean isStore,
                      StoreSummaryResponse store, EventSummaryResponse event) {
                this.likeCount = likeCount;
                this.id = id;
                this.isStore = isStore;
                this.store = store;
                this.event = event;
            }
        }

        List<MixedItem> merged = new ArrayList<>(storeCandidates.size() + eventCandidates.size());
        for (StoreSummaryResponse s : storeCandidates) {
            merged.add(new MixedItem(s.getLikeCount(), s.getId(), true, s, null));
        }
        for (EventSummaryResponse e : eventCandidates) {
            merged.add(new MixedItem(e.getLikeCount(), e.getId(), false, null, e));
        }

        merged.sort(Comparator
                .comparing((MixedItem m) -> m.likeCount).reversed()
                .thenComparing((MixedItem m) -> m.id).reversed());

        // 6) items 조립
        List<CategoryFeedItemResponse> items = new ArrayList<>(merged.size());
        for (MixedItem mi : merged) {
            if (mi.isStore) {
                StoreSummaryResponse s = mi.store;
                items.add(new CategoryFeedItemResponse(
                        FeedItemType.STORE,
                        s.getId(),
                        s.getName(),
                        s.getThumbnail(),
                        s.getLikeCount(),
                        s.isLiked(),
                        null,
                        null,
                        null
                ));
            } else {
                EventSummaryResponse e = mi.event;
                items.add(new CategoryFeedItemResponse(
                        FeedItemType.EVENT,
                        e.getId(),
                        e.getName(),
                        e.getThumbnail(),
                        e.getLikeCount(),
                        e.isLiked(),
                        e.getDescription(),
                        e.getStartDate(),
                        e.getEndDate()
                ));
            }
        }

        return CategoryContent.of(category, items);
    }

    // (참고) 기존 전체 카테고리용 buildCategoryFeed(...)는 그대로 유지

    /**
     * 카테고리 정렬:
     * - 비로그인: 전부 알파벳 순
     * - USER: 관심 카테고리 최대 3개(알파벳 순) 먼저 → 나머지(알파벳 순)
     * - MERCHANT: 본인 카테고리 1개 먼저 → 나머지(알파벳 순)
     */
    private List<Category> orderCategories(User loginUser) {
        List<Category> allAlpha = Arrays.stream(Category.values())
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());

        if (loginUser == null) return allAlpha;

        if (loginUser.isMerchant()) {
            Category mc = loginUser.getMerchantCategory();
            if (mc == null) return allAlpha;
            List<Category> rest = allAlpha.stream()
                    .filter(c -> c != mc)
                    .collect(Collectors.toList());
            List<Category> ordered = new ArrayList<>(allAlpha.size());
            ordered.add(mc);
            ordered.addAll(rest);
            return ordered;
        }

        Set<Category> selected = loginUser.getCategorySet();
        if (selected.isEmpty()) return allAlpha;

        List<Category> top = selected.stream()
                .sorted(Comparator.comparing(Enum::name))
                .limit(3)
                .collect(Collectors.toList());

        Set<Category> topSet = new HashSet<>(top);
        List<Category> rest = allAlpha.stream()
                .filter(c -> !topSet.contains(c))
                .collect(Collectors.toList());

        List<Category> ordered = new ArrayList<>(allAlpha.size());
        ordered.addAll(top);
        ordered.addAll(rest);
        return ordered;
    }
}






