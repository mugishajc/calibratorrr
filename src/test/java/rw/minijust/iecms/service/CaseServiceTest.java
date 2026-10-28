package rw.minijust.iecms.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.minijust.iecms.dto.Dtos.*;
import rw.minijust.iecms.model.CaseStatus;
import rw.minijust.iecms.model.CourtCase;
import rw.minijust.iecms.repository.CaseRepository;
import rw.minijust.iecms.repository.HearingRepository;

/** Pure unit tests: no Spring context, repositories are mocked. */
@ExtendWith(MockitoExtension.class)
class CaseServiceTest {
    @Mock CaseRepository cases;
    @Mock HearingRepository hearings;
    @InjectMocks CaseService service;

    private static final LocalDateTime NEXT_WEEK = LocalDateTime.now().plusWeeks(1);

    private CourtCase caseIn(CaseStatus... path) {
        CourtCase c = new CourtCase("CIV/2026/00001", "Land dispute", "CIVIL");
        for (CaseStatus s : path) c.moveTo(s);
        return c;
    }

    @Test
    void fileAssignsCaseNumberAndFiledStatus() {
        when(cases.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CaseResponse first = service.file(new CreateCaseRequest("Contract breach", "COMMERCIAL"));
        CaseResponse second = service.file(new CreateCaseRequest("Unpaid wages", "LABOUR"));

        assertThat(first.caseNumber()).matches("COM/\\d{4}/00001");
        assertThat(second.caseNumber()).matches("LAB/\\d{4}/00002");
        assertThat(first.status()).isEqualTo(CaseStatus.FILED);
    }

    @Test
    void unknownCaseThrowsNotFound() {
        when(cases.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void illegalStatusChangeIsRejected() {
        when(cases.findById(1L)).thenReturn(Optional.of(caseIn()));

        assertThatThrownBy(() -> service.changeStatus(1L, CaseStatus.JUDGED))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void schedulingRegisteredCaseMovesItToScheduled() {
        CourtCase c = caseIn(CaseStatus.REGISTERED);
        when(cases.findById(1L)).thenReturn(Optional.of(c));

        HearingResponse h = service.scheduleHearing(1L, new ScheduleHearingRequest(NEXT_WEEK, "Room A"));

        assertThat(h.courtRoom()).isEqualTo("Room A");
        assertThat(c.getStatus()).isEqualTo(CaseStatus.SCHEDULED);
        assertThat(c.getHearings()).hasSize(1);
        verify(hearings).save(any());
    }

    @Test
    void doubleBookedCourtRoomIsRejected() {
        when(cases.findById(1L)).thenReturn(Optional.of(caseIn(CaseStatus.REGISTERED)));
        when(hearings.existsByCourtRoomAndScheduledAt("Room A", NEXT_WEEK)).thenReturn(true);

        assertThatThrownBy(() -> service.scheduleHearing(1L, new ScheduleHearingRequest(NEXT_WEEK, "Room A")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already booked");
        verify(hearings, never()).save(any());
    }

    @Test
    void caseNotYetRegisteredCannotBeScheduled() {
        when(cases.findById(1L)).thenReturn(Optional.of(caseIn()));

        assertThatThrownBy(() -> service.scheduleHearing(1L, new ScheduleHearingRequest(NEXT_WEEK, "Room A")))
                .isInstanceOf(IllegalStateException.class);
    }

    record Count(CaseStatus getStatus, long getTotal) implements CaseRepository.StatusCount {}

    @Test
    void statisticsMapsCountsPerStatus() {
        when(cases.countByStatus()).thenReturn(List.of(new Count(CaseStatus.FILED, 4), new Count(CaseStatus.CLOSED, 2)));

        assertThat(service.statistics())
                .containsEntry(CaseStatus.FILED, 4L)
                .containsEntry(CaseStatus.CLOSED, 2L);
    }
}
