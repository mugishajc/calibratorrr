package rw.minijust.iecms.audit;

import jakarta.persistence.*;
import java.time.Instant;

/** Append-only record of who did what. There is deliberately no update or delete endpoint. */
@Entity
@Table(indexes = {@Index(columnList = "username"), @Index(columnList = "entityType, entityId")})
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant at = Instant.now();

    @Column(nullable = false, updatable = false, length = 100)
    private String username;

    @Column(nullable = false, updatable = false, length = 50)
    private String action;

    @Column(updatable = false, length = 50)
    private String entityType;

    @Column(updatable = false, length = 100)
    private String entityId;

    @Column(updatable = false, length = 500)
    private String details;

    protected AuditLog() {}

    public AuditLog(String username, String action, String entityType, String entityId, String details) {
        this.username = username;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    public Long getId() { return id; }
    public Instant getAt() { return at; }
    public String getUsername() { return username; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public String getEntityId() { return entityId; }
    public String getDetails() { return details; }
}
