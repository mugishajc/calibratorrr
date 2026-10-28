package rw.minijust.iecms.notification;

import java.time.LocalDateTime;

public record HearingScheduledEvent(String caseNumber, LocalDateTime scheduledAt, String courtRoom) {}
