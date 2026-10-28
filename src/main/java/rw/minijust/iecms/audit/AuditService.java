package rw.minijust.iecms.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository repo;

    public AuditService(AuditLogRepository repo) {
        this.repo = repo;
    }

    /** Records an action by the currently signed-in user, inside the caller's transaction. */
    public void record(String action, String entityType, String entityId, String details) {
        record(currentUser(), action, entityType, entityId, details);
    }

    public void record(String username, String action, String entityType, String entityId, String details) {
        repo.save(new AuditLog(username, action, entityType, entityId, details));
    }

    public Page<AuditLog> latest(Pageable pageable) {
        return repo.findAllByOrderByAtDescIdDesc(pageable);
    }

    public static String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() ? "system" : auth.getName();
    }
}
