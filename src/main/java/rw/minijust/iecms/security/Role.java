package rw.minijust.iecms.security;

public enum Role {
    ADMIN,      // manages users, reads the audit log and HR data
    REGISTRAR,  // files cases, schedules hearings, moves cases through the workflow
    JUDGE,      // records judgments
    VIEWER      // read-only
}
