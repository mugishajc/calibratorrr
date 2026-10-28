package rw.minijust.iecms.dto;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDateTime;
import rw.minijust.iecms.model.CaseStatus;
import rw.minijust.iecms.model.CourtCase;
import rw.minijust.iecms.model.Hearing;

public final class Dtos {
    private Dtos() {}

    public record CreateCaseRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Pattern(regexp = "CIVIL|CRIMINAL|COMMERCIAL|LABOUR|ADMINISTRATIVE") String category) {}

    public record ChangeStatusRequest(@NotNull CaseStatus status) {}

    public record ScheduleHearingRequest(@NotNull @Future LocalDateTime scheduledAt, @NotBlank String courtRoom) {}

    public record CaseResponse(Long id, String caseNumber, String title, String category,
                               CaseStatus status, Instant filedAt, int hearings, String createdBy) {
        public static CaseResponse of(CourtCase c) {
            return new CaseResponse(c.getId(), c.getCaseNumber(), c.getTitle(), c.getCategory(),
                    c.getStatus(), c.getFiledAt(), c.getHearings().size(), c.getCreatedBy());
        }
    }

    public record HearingResponse(Long id, LocalDateTime scheduledAt, String courtRoom) {
        public static HearingResponse of(Hearing h) {
            return new HearingResponse(h.getId(), h.getScheduledAt(), h.getCourtRoom());
        }
    }
}
