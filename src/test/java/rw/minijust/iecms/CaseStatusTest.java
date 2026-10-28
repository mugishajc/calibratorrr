package rw.minijust.iecms;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
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

    @ParameterizedTest(name = "{0} -> {1} allowed: {2}")
    @CsvSource({
        "FILED, REGISTERED, true",
        "FILED, DISMISSED, true",
        "FILED, JUDGED, false",
        "REGISTERED, SCHEDULED, true",
        "SCHEDULED, IN_HEARING, true",
        "IN_HEARING, SCHEDULED, true",
        "IN_HEARING, JUDGED, true",
        "JUDGED, CLOSED, true",
        "JUDGED, FILED, false",
        "CLOSED, FILED, false",
        "DISMISSED, REGISTERED, false"
    })
    void transitionRules(CaseStatus from, CaseStatus to, boolean allowed) {
        assertEquals(allowed, from.canMoveTo(to));
    }
}
