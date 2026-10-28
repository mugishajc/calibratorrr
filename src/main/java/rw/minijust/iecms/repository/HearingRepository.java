package rw.minijust.iecms.repository;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.minijust.iecms.model.Hearing;

public interface HearingRepository extends JpaRepository<Hearing, Long> {
    // A courtroom can't host two hearings in the same slot
    boolean existsByCourtRoomAndScheduledAt(String courtRoom, LocalDateTime scheduledAt);
}
