package rw.minijust.iecms.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "court_case", indexes = @Index(columnList = "status"))
public class CourtCase {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String caseNumber;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 20)
    private String category; // CIVIL, CRIMINAL, COMMERCIAL, LABOUR...

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CaseStatus status = CaseStatus.FILED;

    @Column(nullable = false, updatable = false)
    private Instant filedAt = Instant.now();

    // Filled automatically from the signed-in user (see AppConfig.auditorAware)
    @CreatedBy
    @Column(updatable = false, length = 100)
    private String createdBy;

    @LastModifiedBy
    @Column(length = 100)
    private String lastModifiedBy;

    @LastModifiedDate
    private Instant lastModifiedAt;

    @Version
    private Long version; // optimistic locking: two clerks can't overwrite each other

    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Hearing> hearings = new ArrayList<>();

    protected CourtCase() {}

    public CourtCase(String caseNumber, String title, String category) {
        this.caseNumber = caseNumber;
        this.title = title;
        this.category = category;
    }

    public void moveTo(CaseStatus target) {
        if (!status.canMoveTo(target)) {
            throw new IllegalStateException("Cannot move case from " + status + " to " + target);
        }
        this.status = target;
    }

    public void addHearing(Hearing h) {
        hearings.add(h);
        h.setCourtCase(this);
    }

    public Long getId() { return id; }
    public String getCaseNumber() { return caseNumber; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public CaseStatus getStatus() { return status; }
    public Instant getFiledAt() { return filedAt; }
    public List<Hearing> getHearings() { return hearings; }
    public String getCreatedBy() { return createdBy; }
    public String getLastModifiedBy() { return lastModifiedBy; }
    public Instant getLastModifiedAt() { return lastModifiedAt; }
}
