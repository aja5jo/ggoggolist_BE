package group5.backend.dto.category.response;


import group5.backend.dto.category.FeedItemType;

import java.time.LocalDate;

public record CategoryFeedItemResponse(
        FeedItemType type,
        Long id,
        String name,
        String thumbnail,
        Integer likeCount,
        boolean liked,
        // EVENT일 때만 채움
        String description,
        LocalDate startDate,
        LocalDate endDate
) {}
