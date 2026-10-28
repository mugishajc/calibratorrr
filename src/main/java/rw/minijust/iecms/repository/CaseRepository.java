package rw.minijust.iecms.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import rw.minijust.iecms.model.CaseStatus;
import rw.minijust.iecms.model.CourtCase;

public interface CaseRepository extends JpaRepository<CourtCase, Long> {
    Page<CourtCase> findByStatus(CaseStatus status, Pageable pageable);

    boolean existsByCaseNumber(String caseNumber);

    // Analytics: number of cases per status (JPQL constructor-free projection)
    @Query("select c.status as status, count(c) as total from CourtCase c group by c.status")
    List<StatusCount> countByStatus();

    interface StatusCount {
        CaseStatus getStatus();
        long getTotal();
    }
}
