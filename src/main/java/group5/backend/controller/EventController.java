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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/merchants/stores/events")
@PreAuthorize("hasAuthority('MERCHANT')")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<ApiResponse<EventCreateResponse>> createEvent(
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        EventCreateResponse response = eventService.createEvent(user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 등록 성공", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventCheckResponse>>> getMyEvents(
            @AuthenticationPrincipal User user
    ) {
        if (user == null) {
            throw new InsufficientAuthenticationException("인증 정보가 없습니다."); // → 401
        }
        List<EventCheckResponse> responses = eventService.getMyEvents(user);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 조회 성공", responses));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventCreateResponse>> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        EventCreateResponse response = eventService.updateEvent(user, eventId, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 전체 수정 성공", response));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<ApiResponse<EventCreateResponse>> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest request,
            @AuthenticationPrincipal User user
    ) {
        EventCreateResponse response = eventService.updateEvent(eventId, user, request);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "이벤트 부분 수정 성공", response));
    }



}
