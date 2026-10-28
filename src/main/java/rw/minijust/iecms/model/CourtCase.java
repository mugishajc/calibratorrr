package rw.minijust.iecms.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
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
}
