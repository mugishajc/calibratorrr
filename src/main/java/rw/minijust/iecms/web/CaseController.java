package rw.minijust.iecms.web;

import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rw.minijust.iecms.dto.Dtos.*;
import rw.minijust.iecms.model.CaseStatus;
import rw.minijust.iecms.service.CaseService;

@RestController
@RequestMapping("/api/v1/cases")
@CrossOrigin(origins = "http://localhost:3000") // React/Angular dev server
public class CaseController {
    private final CaseService service;

    public CaseController(CaseService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('REGISTRAR', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CaseResponse file(@Valid @RequestBody CreateCaseRequest req) {
        return service.file(req);
    }

    @GetMapping
    public Page<CaseResponse> list(@RequestParam(required = false) CaseStatus status,
                                   @PageableDefault(size = 20, sort = "filedAt") Pageable pageable) {
        return service.list(status, pageable);
    }

    @GetMapping("/{id}")
    public CaseResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('REGISTRAR', 'JUDGE', 'ADMIN')")
    public CaseResponse changeStatus(@PathVariable Long id, @Valid @RequestBody ChangeStatusRequest req,
                                     Authentication auth) {
        boolean isJudge = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_JUDGE"));
        if (req.status() == CaseStatus.JUDGED && !isJudge) {
            throw new AccessDeniedException("Only a judge can record a judgment");
        }
        return service.changeStatus(id, req.status());
    }

    @PostMapping("/{id}/hearings")
    @PreAuthorize("hasAnyRole('REGISTRAR', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public HearingResponse schedule(@PathVariable Long id, @Valid @RequestBody ScheduleHearingRequest req) {
        return service.scheduleHearing(id, req);
    }

    @GetMapping("/statistics")
    public Map<CaseStatus, Long> statistics() {
        return service.statistics();
    }
}
