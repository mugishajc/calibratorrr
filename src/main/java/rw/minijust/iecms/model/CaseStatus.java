package rw.minijust.iecms.model;

import java.util.Set;

/** Case lifecycle. Each status lists the statuses it may move to. */
public enum CaseStatus {
    FILED, REGISTERED, SCHEDULED, IN_HEARING, JUDGED, CLOSED, DISMISSED;

    public Set<CaseStatus> next() {
        return switch (this) {
            case FILED -> Set.of(REGISTERED, DISMISSED);
            case REGISTERED -> Set.of(SCHEDULED, DISMISSED);
            case SCHEDULED -> Set.of(IN_HEARING, SCHEDULED);
            case IN_HEARING -> Set.of(SCHEDULED, JUDGED);
            case JUDGED -> Set.of(CLOSED);
            case CLOSED, DISMISSED -> Set.of();
        };
    }

    public boolean canMoveTo(CaseStatus target) {
        return next().contains(target);
    }
}
