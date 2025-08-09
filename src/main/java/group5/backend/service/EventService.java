package group5.backend.service;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.User;
import group5.backend.dto.common.event.request.EventCreateRequest;
import group5.backend.dto.common.event.request.EventUpdateRequest;
import group5.backend.dto.common.event.response.EventCheckResponse;
import group5.backend.dto.common.event.response.EventCreateResponse;
import group5.backend.repository.EventRepository;
import group5.backend.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public EventCreateResponse createEvent(User user, EventCreateRequest request) {
        // 1. 사용자에게 연결된 Store 가져오기
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없는 사용자입니다."));

        // 2. 날짜 유효성 체크
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("이벤트 시작일과 종료일을 다시 확인해주세요.");
        }

        // 3. Event 엔티티 생성
        Event event = Event.builder()
                .name(request.getName())
                .description(request.getDescription())
                .intro(request.getIntro())
                .thumbnail(request.getThumbnail())
                .images(request.getImages())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .likeCount(0)
                .build();

        Event saved = eventRepository.save(event);

        return EventCreateResponse.builder()
                .id(saved.getId())
                .storeId(store.getId())
                .name(saved.getName())
                .description(request.getDescription())
                .intro(saved.getIntro())
                .thumbnail(saved.getThumbnail())
                .images(saved.getImages())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .startTime(saved.getStartTime())
                .endTime(saved.getEndTime())
                .build();
    }

    @Transactional
    public List<EventCheckResponse> getMyEvents(User merchant) {
        Store store = storeRepository.findByOwner(merchant)
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없는 사용자입니다."));

        List<Event> events = eventRepository.findByStore(store);

        return events.stream()
                .map(event -> EventCheckResponse.builder()
                        .id(event.getId())
                        .storeId(store.getId())
                        .name(event.getName())
                        .description(event.getDescription())
                        .intro(event.getIntro())
                        .thumbnail(event.getThumbnail())
                        .images(event.getImages())
                        .startDate(event.getStartDate())
                        .endDate(event.getEndDate())
                        .startTime(event.getStartTime())
                        .endTime(event.getEndTime())
                        .likeCount(event.getLikeCount())
                        .build())
                .collect(Collectors.toList());
    }



    @Transactional
    public EventCreateResponse updateEvent(User user, Long eventId, EventCreateRequest request) {
        // 가게 조회
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없습니다."));

        // 이벤트 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        // 이벤트가 본인 가게에 속해 있는지 검증
        if (!event.getStore().equals(store)) {
            throw new AccessDeniedException("해당 이벤트에 대한 수정 권한이 없습니다.");
        }

        // 필드 업데이트
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setIntro(request.getIntro());
        event.setThumbnail(request.getThumbnail());
        event.setImages(request.getImages());
        event.setStartDate(request.getStartDate());
        event.setEndDate(request.getEndDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());

        Event updated = eventRepository.save(event);

        return EventCreateResponse.builder()
                .id(updated.getId())
                .storeId(updated.getStore().getId())
                .name(updated.getName())
                .description(updated.getDescription())
                .intro(updated.getIntro())
                .thumbnail(updated.getThumbnail())
                .images(updated.getImages())
                .startDate(updated.getStartDate())
                .endDate(updated.getEndDate())
                .startTime(updated.getStartTime())
                .endTime(updated.getEndTime())
                .build();
    }

    @Transactional
    public EventCreateResponse updateEvent(Long eventId, User user, EventUpdateRequest request) {
        Store store = storeRepository.findByOwner(user)
                .orElseThrow(() -> new IllegalArgumentException("등록된 가게가 없습니다."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("해당 이벤트가 존재하지 않습니다."));

        if (!Objects.equals(event.getStore().getId(), store.getId())) {
            throw new AccessDeniedException("본인의 이벤트만 수정할 수 있습니다.");
        }

        if (request.getName() != null) event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getIntro() != null) event.setIntro(request.getIntro());
        if (request.getThumbnail() != null) event.setThumbnail(request.getThumbnail());
        if (request.getImages() != null) event.setImages(request.getImages());
        if (request.getStartDate() != null) event.setStartDate(request.getStartDate());
        if (request.getEndDate() != null) event.setEndDate(request.getEndDate());
        if (request.getStartTime() != null) event.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) event.setEndTime(request.getEndTime());

        Event updated = eventRepository.save(event);

        return EventCreateResponse.builder()
                .id(updated.getId())
                .storeId(updated.getStore().getId())
                .name(updated.getName())
                .description(updated.getDescription())
                .intro(updated.getIntro())
                .thumbnail(updated.getThumbnail())
                .images(updated.getImages())
                .startDate(updated.getStartDate())
                .endDate(updated.getEndDate())
                .startTime(updated.getStartTime())
                .endTime(updated.getEndTime())
                .build();
    }



}

