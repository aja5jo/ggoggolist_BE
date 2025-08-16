package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.category.response.CategoryFeedItemResponse;
import group5.backend.dto.common.event.FilterType;
import group5.backend.dto.common.event.response.EventOverviewResponse;
import group5.backend.dto.common.event.response.EventPageResponse;
import group5.backend.dto.common.event.response.EventDetailResponse;
import group5.backend.dto.common.popup.response.PopupSummaryResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.EventQueryService;
import group5.backend.service.PopupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.SessionAttribute;


import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "이벤트/팝업 조회", description = "유저가 항목별 이벤트와 팝업 조회")
public class EventQueryController {

    private final EventQueryService eventQueryService;
    private final PopupService popupService;
    @Operation(
            summary = "이벤트/팝업 조회 (필터별 or 전체 Overview)",
            description = """
            - **filter 쿼리 파라미터가 있는 경우** → 해당 필터 조건(POPULAR, ONGOING, CLOSING_TODAY, UPCOMING)에 맞는 이벤트+팝업 목록 반환
            - **filter가 없는 경우** → 인기/진행중/오늘마감/예정 섹션별 8개씩(이벤트+팝업 섞임) 전체 Overview 반환
            - 로그인 불필요(공개). 로그인 시 'liked' 반영
            - 정렬 기준:
              · 인기 / 진행중 / 오늘마감 → likeCount desc, id desc
              · 예정 → startDate asc, id asc
            """
    )
    @Parameter(name = "filter", description = "필터 타입 (POPULAR, ONGOING, CLOSING_TODAY, UPCOMING)", required = false, example = "POPULAR")
    @GetMapping("/events")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse> getEventsOrOverview(
            @RequestParam(value = "filter", required = false) FilterType filter,
            @AuthenticationPrincipal User loginUser
    ) {
        Long userId = (loginUser == null) ? null : loginUser.getId();

        if (filter != null) {
            // 필터별 조회
            List<CategoryFeedItemResponse> data = eventQueryService.getEventsByFilter(filter, userId);
            return ResponseEntity.ok(new ApiResponse(true, 200, "필터별 조회 성공", data));
        } else {
            // 전체 overview 조회
            EventOverviewResponse data = eventQueryService.getEventOverview(userId);
            return ResponseEntity.ok(new ApiResponse(true, 200, "전체 overview 조회 성공", data));
        }
    }

    @Operation(
            summary = "이번 주 팝업 스테이션 조회",
            description = "이번 주(월~일)에 진행 중인 모든 팝업 스테이션을 반환"
    )



    @GetMapping("/events/{eventId}")
    @Transactional(readOnly = true) // (3) 읽기 전용 트랜잭션
    public ResponseEntity<ApiResponse<EventDetailResponse>> getEventDetail(
            @PathVariable Long eventId,
            @AuthenticationPrincipal User loginUser   // (1) 로그인 사용자 주입
    ) {
        Long userId = (loginUser == null) ? null : loginUser.getId();
        var data = eventQueryService.getEventDetail(eventId, userId); // (2) 오버로드 호출
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 상세 정보 조회 성공", data));
    }
}



