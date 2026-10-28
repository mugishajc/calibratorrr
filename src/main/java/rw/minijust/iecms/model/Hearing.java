package rw.minijust.iecms.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Hearing {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CourtCase courtCase;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Column(nullable = false)
    private String courtRoom;

    protected Hearing() {}

    public Hearing(LocalDateTime scheduledAt, String courtRoom) {
        this.scheduledAt = scheduledAt;
        this.courtRoom = courtRoom;
    }

    void setCourtCase(CourtCase c) { this.courtCase = c; }
    public Long getId() { return id; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public String getCourtRoom() { return courtRoom; }
}
