package group5.backend.dto.common.event;
import group5.backend.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventCleanupScheduler {

    private final EventRepository eventRepository;
    /**
     * 매일 새벽 2시에 종료된 이벤트 삭제
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void deleteExpiredEvents() {
        LocalDate today = LocalDate.now();
        log.info("이벤트 정리 작업 시작: 종료일 < {}", today);

        eventRepository.deleteByEndDateBefore(today);

        log.info("이벤트 정리 완료");
    }
}
