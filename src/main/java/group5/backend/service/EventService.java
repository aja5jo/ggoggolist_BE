package group5.backend.service;

import group5.backend.domain.event.Event;
import group5.backend.domain.store.Store;
import group5.backend.domain.user.User;
import group5.backend.dto.common.event.request.EventCreateRequest;
import group5.backend.dto.common.event.request.EventUpdateRequest;
import group5.backend.dto.common.event.response.EventCheckResponse;
import group5.backend.dto.common.event.response.EventCreateResponse;
import group5.backend.repository.EventRepository;
import group5.backend.repository.FavoriteEventRepository;
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
    private final FavoriteEventRepository favoriteEventRepository;

    @Transactional
    public List<EventCheckResponse> getMyEvents(User merchant) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없는 사용자입니다."));

        List<Event> events = eventRepository.findByStoreId(store.getId());

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
    public EventCreateResponse createEvent(User merchant, EventCreateRequest request) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없는 사용자입니다."));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("이벤트 시작일과 종료일을 다시 확인해주세요.");
        }

        Event event = Event.builder()
                .store(store)
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
        return toResponse(saved);
    }

    @Transactional
    public EventCreateResponse updateEventPut(User merchant, Long eventId, EventCreateRequest request) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없습니다."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("이벤트를 찾을 수 없습니다."));

        if (!event.getStore().equals(store)) {
            throw new AccessDeniedException("해당 이벤트에 대한 수정 권한이 없습니다.");
        }

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
        return toResponse(updated);
    }

    @Transactional
    public EventCreateResponse updateEventPatch(User merchant, Long eventId,  EventUpdateRequest request) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
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
        return toResponse(updated);
    }

    @Transactional
    public void deleteEvent(User merchant, Long eventId) {
        Store store = storeRepository.findByOwnerId(merchant.getId())
                .orElseThrow(() -> new AccessDeniedException("등록된 가게가 없습니다."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NoSuchElementException("이벤트를 찾을 수 없습니다."));

        if (!Objects.equals(event.getStore().getId(), store.getId())) {
            throw new AccessDeniedException("본인 이벤트만 삭제할 수 있습니다.");
        }

        // 즐겨찾기 등 FK가 걸려 있으면 선삭제 필요
        favoriteEventRepository.deleteByEvent_Id(eventId);
        eventRepository.delete(event);
    }

    private EventCreateResponse toResponse(Event e) {
        return EventCreateResponse.builder()
                .id(e.getId())
                .storeId(e.getStore().getId())
                .name(e.getName())
                .description(e.getDescription())
                .intro(e.getIntro())
                .thumbnail(e.getThumbnail())
                .images(e.getImages())
                .startDate(e.getStartDate())
                .endDate(e.getEndDate())
                .startTime(e.getStartTime())
                .endTime(e.getEndTime())
                .build();
    }




}

