package rw.minijust.iecms.employee;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/** The "write a CRUD API from memory" answer: entity + repository + controller. */
@RestController
@RequestMapping("/api/employees")
@PreAuthorize("hasRole('ADMIN')") // HR data
public class EmployeeController {
    private final EmployeeRepository repo;

    public EmployeeController(EmployeeRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Employee> all(@RequestParam(required = false) String department) {
        return department == null ? repo.findAll() : repo.findByDepartment(department);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> one(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Employee> create(@Valid @RequestBody Employee e) {
        e.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(e));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Employee> update(@PathVariable Long id, @Valid @RequestBody Employee e) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        e.setId(id);
        return ResponseEntity.ok(repo.save(e));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
