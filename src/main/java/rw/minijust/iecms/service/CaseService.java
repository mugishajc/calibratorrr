package rw.minijust.iecms.service;

import java.time.Year;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.minijust.iecms.dto.Dtos.*;
import rw.minijust.iecms.model.CaseStatus;
import rw.minijust.iecms.model.CourtCase;
import rw.minijust.iecms.model.Hearing;
import rw.minijust.iecms.repository.CaseRepository;
import rw.minijust.iecms.repository.HearingRepository;

@Service
public class CaseService {
    private final CaseRepository cases;
    private final HearingRepository hearings;
    // Demo only: in production use a DB sequence so numbers survive restarts and multiple nodes
    private final AtomicLong sequence = new AtomicLong();

    public CaseService(CaseRepository cases, HearingRepository hearings) {
        this.cases = cases;
        this.hearings = hearings;
    }

    @Transactional
    public CaseResponse file(CreateCaseRequest req) {
        String number = "%s/%d/%05d".formatted(req.category().substring(0, 3), Year.now().getValue(),
                sequence.incrementAndGet());
        return CaseResponse.of(cases.save(new CourtCase(number, req.title(), req.category())));
    }

    @Transactional(readOnly = true)
    public CaseResponse get(Long id) {
        return CaseResponse.of(find(id));
    }

    @Transactional(readOnly = true)
    public Page<CaseResponse> list(CaseStatus status, Pageable pageable) {
        Page<CourtCase> page = status == null ? cases.findAll(pageable) : cases.findByStatus(status, pageable);
        return page.map(CaseResponse::of);
    }

    @Transactional
    public CaseResponse changeStatus(Long id, CaseStatus target) {
        CourtCase c = find(id);
        c.moveTo(target); // dirty checking flushes the change at commit
        return CaseResponse.of(c);
    }

    @Transactional
    public HearingResponse scheduleHearing(Long id, ScheduleHearingRequest req) {
        CourtCase c = find(id);
        if (hearings.existsByCourtRoomAndScheduledAt(req.courtRoom(), req.scheduledAt())) {
            throw new IllegalStateException("Court room " + req.courtRoom() + " is already booked at " + req.scheduledAt());
        }
        if (c.getStatus() == CaseStatus.REGISTERED) {
            c.moveTo(CaseStatus.SCHEDULED);
        } else if (c.getStatus() != CaseStatus.SCHEDULED && c.getStatus() != CaseStatus.IN_HEARING) {
            throw new IllegalStateException("Case in status " + c.getStatus() + " cannot be scheduled");
        }
        Hearing h = new Hearing(req.scheduledAt(), req.courtRoom());
        c.addHearing(h);
        hearings.save(h);
        return HearingResponse.of(h);
    }

    @Transactional(readOnly = true)
    public Map<CaseStatus, Long> statistics() {
        return cases.countByStatus().stream()
                .collect(Collectors.toMap(CaseRepository.StatusCount::getStatus, CaseRepository.StatusCount::getTotal));
    }

    private CourtCase find(Long id) {
        return cases.findById(id).orElseThrow(() -> new NotFoundException("Case " + id + " not found"));
    }
}
