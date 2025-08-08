package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.common.event.FilterType;
import group5.backend.dto.common.event.response.EventOverviewResponse;
import group5.backend.dto.common.event.response.EventPageResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.EventQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventQueryController {

    private final EventQueryService eventQueryService;

    /**
     * 1) 단일 필터 조회: /api/events?filter=POPULAR|ONGOING|CLOSING_TODAY|UPCOMING
     */
    @Operation(
            summary = "필터 조건별 이벤트 목록 조회",
            description = """
            - 로그인 불필요(공개). 로그인 시 'liked' 반영
            - filter: POPULAR, ONGOING, CLOSING_TODAY, UPCOMING
            - 페이징/정렬은 요청 Pageable 그대로 사용
            """
    )
    @GetMapping(params = "filter")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> getEventsByFilter(
            @Parameter(description = "필터 타입", example = "POPULAR", required = true)
            @RequestParam FilterType filter,
            @AuthenticationPrincipal User loginUser,
            @ParameterObject
            @PageableDefault(size = 20, sort = {"likeCount","id"}, direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Long userId = (loginUser == null) ? null : loginUser.getId();
        EventPageResponse data = eventQueryService.getEvents(filter, userId, pageable);
        return ResponseEntity.ok(new ApiResponse(true, 200, "이벤트 목록 조회 성공", data));
    }

    /**
     * 2) 전체(overview) 조회: /api/events
     *    인기/진행중/오늘마감/예정 섹션별 8개 고정
     */
    @Operation(
            summary = "이벤트 전체(인기/진행중/오늘마감/예정) 묶음 조회",
            description = """
            - 로그인 불필요(공개). 로그인 시 'liked' 반영
            - 섹션별 8개씩 고정 반환 (서비스 상수 OVERVIEW_TOP_N)
            - 쿼리 파라미터 없이 호출
            """
    )
    @GetMapping
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> getEventOverview(
            @AuthenticationPrincipal User loginUser
    ) {
        Long userId = (loginUser == null) ? null : loginUser.getId();
        EventOverviewResponse data = eventQueryService.getEventOverview(userId);
        return ResponseEntity.ok(new ApiResponse(true, 200, "이벤트 전체 목록 조회 성공", data));
    }
}
