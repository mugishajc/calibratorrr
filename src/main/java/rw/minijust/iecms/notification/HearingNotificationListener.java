package rw.minijust.iecms.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Runs only after the hearing is committed (no message for a rolled-back booking)
 * and on a background thread (a slow SMS gateway never slows the API).
 */
@Component
public class HearingNotificationListener {
    private static final Logger log = LoggerFactory.getLogger(HearingNotificationListener.class);

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onHearingScheduled(HearingScheduledEvent event) {
        // Production: send SMS/email to the parties through a message queue with retries
        log.info("Notifying parties of case {}: hearing on {} in {}",
                event.caseNumber(), event.scheduledAt(), event.courtRoom());
    }
}
