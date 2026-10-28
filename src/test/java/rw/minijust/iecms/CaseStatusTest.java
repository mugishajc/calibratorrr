package rw.minijust.iecms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import rw.minijust.iecms.model.CaseStatus;
import rw.minijust.iecms.model.CourtCase;

class CaseStatusTest {
    @Test
    void allowsValidTransition() {
        CourtCase c = new CourtCase("CIV/2026/00001", "Land dispute", "CIVIL");
        c.moveTo(CaseStatus.REGISTERED);
        assertEquals(CaseStatus.REGISTERED, c.getStatus());
    }

    @Test
    void rejectsSkippingSteps() {
        CourtCase c = new CourtCase("CIV/2026/00001", "Land dispute", "CIVIL");
        assertThrows(IllegalStateException.class, () -> c.moveTo(CaseStatus.JUDGED));
    }

    @Test
    void closedIsTerminal() {
        assertTrue(CaseStatus.CLOSED.next().isEmpty());
    }
}
