package group5.backend.controller;

import group5.backend.domain.user.User;
import group5.backend.dto.common.event.request.EventCreateRequest;
import group5.backend.dto.common.event.request.EventUpdateRequest;
import group5.backend.dto.common.event.response.EventCheckResponse;
import group5.backend.dto.common.event.response.EventCreateResponse;
import group5.backend.response.ApiResponse;
import group5.backend.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/stores/events")
@PreAuthorize("hasAuthority('MERCHANT')")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "이벤트 등록", description = "새로운 이벤트를 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<EventCreateResponse>> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        EventCreateResponse response = eventService.createEvent(user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 등록 성공", response));
    }

    @Operation(summary = "내 이벤트 조회", description = "내가 등록한 이벤트 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventCheckResponse>>> getMyEvents(
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new InsufficientAuthenticationException("인증 정보가 없습니다.");
        }
        List<EventCheckResponse> responses = eventService.getMyEvents(user);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 조회 성공", responses));
    }

    @Operation(summary = "이벤트 전체 수정", description = "이벤트의 전체 정보를 수정합니다.")
    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventCreateResponse>> updateEvent(
            @Parameter(description = "수정할 이벤트 ID") @PathVariable Long eventId,
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        EventCreateResponse response = eventService.updateEventPut(user, eventId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 전체 수정 성공", response));
    }

    @Operation(summary = "이벤트 부분 수정", description = "이벤트의 일부 정보를 수정합니다.")
    @PatchMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventCreateResponse>> updateEvent(
            @Parameter(description = "수정할 이벤트 ID") @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        EventCreateResponse response = eventService.updateEventPatch(user,eventId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 부분 수정 성공", response));
    }
}
