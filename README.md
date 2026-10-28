# IECMS mini — Java exam practice

[![CI/CD](https://github.com/mugishajc/calibratorrr/actions/workflows/ci.yml/badge.svg)](https://github.com/mugishajc/calibratorrr/actions/workflows/ci.yml)

A small Spring Boot 3 project for a senior Java developer exam: a court case API, an employee CRUD API, plain JDBC, SQL queries and common coding questions. It runs on an in-memory H2 database, so nothing needs installing beyond Java 21+ and Maven.

## What the project does

A simplified version of a court case management system (like IECMS):

- **File a case.** A clerk submits a title and category (civil, criminal, commercial, labour, administrative). The system gives it a case number such as `CIV/2026/00001` and status `FILED`.
- **Move the case through its workflow.** `FILED → REGISTERED → SCHEDULED → IN_HEARING → JUDGED → CLOSED`, or `DISMISSED`. Steps cannot be skipped: an illegal move returns HTTP 409.
- **Schedule hearings.** Adds a hearing date and courtroom to a case. Dates must be in the future, and the same courtroom cannot be booked twice for the same time.
- **List and search cases.** Paginated, sortable, and filterable by status.
- **Statistics.** Number of cases per status, a small data-analytics example.
- **Manage employees.** A basic create, read, update, delete API, the classic "write a CRUD API" exam task.
- **Safe and consistent.** Input is validated, errors come back in one standard JSON format, and `@Version` stops two users from overwriting each other's changes.

Alongside the API there is exam practice: SQL queries that run on a sample database, short Java coding answers, "what is the output?" questions, and scenario questions with model answers.

```bash
mvn verify                    # run all tests + coverage check (70% minimum)
mvn spring-boot:run           # API on http://localhost:8080, Swagger at /swagger-ui.html
mvn -q compile exec:java -Dexec.mainClass=rw.minijust.iecms.practice.JavaSnippets
mvn -q compile exec:java -Dexec.mainClass=rw.minijust.iecms.practice.JdbcSqlPractice
mvn -q compile exec:java -Dexec.mainClass=rw.minijust.iecms.practice.OutputQuestions
```

## What is where

| Topic | File |
|---|---|
| Case status workflow (FILED → REGISTERED → SCHEDULED → IN_HEARING → JUDGED → CLOSED) | `model/CaseStatus.java` |
| JPA entities, one-to-many, `@Version` optimistic locking | `model/CourtCase.java`, `model/Hearing.java` |
| Spring Data derived queries, `GROUP BY` statistics | `repository/CaseRepository.java` |
| `@Transactional` service with business rules | `service/CaseService.java` |
| REST controller: POST/GET/PATCH, pagination, CORS | `web/CaseController.java` |
| Global error handling (RFC 7807) | `web/ApiExceptionHandler.java` |
| Validation with records | `dto/Dtos.java` |
| CRUD API to write from memory | `employee/` |
| Short coding questions | `practice/JavaSnippets.java` |
| JDBC, PreparedStatement, transactions, SQL | `practice/JdbcSqlPractice.java`, `sql/queries.sql` (30 queries) |
| "What is the output?" questions | `practice/OutputQuestions.java`, answers in `docs/JAVA_QUESTIONS.md` |
| Scenario questions with model answers | `docs/JAVA_QUESTIONS.md` |
| 125 questions and answers: OOP, core Java, data structures, algorithms, threads, SQL, JPA | `docs/QUESTIONS_AND_ANSWERS.md` |
| Stack, queue, linked list, BST, LRU cache, binary search, sorting, BFS | `practice/DataStructures.java` |

## Testing

56 tests, about 94% line coverage. The build fails below 70%.

| Level | What it checks | Example |
|---|---|---|
| Unit (no Spring, mocks) | Business rules in isolation, runs in milliseconds | `service/CaseServiceTest` with Mockito |
| Parameterized | Every allowed and forbidden status change from one table | `CaseStatusTest.transitionRules` |
| Web slice (`@WebMvcTest`) | HTTP status codes, validation, error format; service mocked | `web/CaseControllerTest` |
| Integration (`@SpringBootTest`) | Full flow through controller, service, JPA and database | `CaseApiTest.fullCaseLifecycle`, `EmployeeApiTest` |
| Data | Each practice SQL query returns the expected rows | `practice/SqlQueriesTest` |

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`):

1. **On every push and pull request:** build with Java 21, run all tests, enforce the coverage gate, and upload the test reports, coverage report and jar.
2. **On push to `main` only, after tests pass:** build the Docker image (multi-stage, runs as a non-root user) and publish it to GitHub Container Registry as `ghcr.io/mugishajc/calibratorrr:latest` plus a commit-SHA tag.

Dependabot opens weekly pull requests for Maven and GitHub Actions updates. A health endpoint is at `/actuator/health` for load balancers and Kubernetes probes.

```bash
docker run -p 8080:8080 ghcr.io/mugishajc/calibratorrr:latest
```

## What should be done next

To take this from a demo to production:

1. **Security.** Add Spring Security with JWT or OAuth2/Keycloak and roles (registrar, judge, lawyer, public). Protect every endpoint and restrict who can change a case's status.
2. **Real database and migrations.** Move from in-memory H2 to PostgreSQL or MySQL, and manage the schema with Flyway instead of `ddl-auto=update`. Use a database sequence for case numbers so they survive restarts and work across several servers.
3. **Audit trail.** Record who viewed or changed each case and when (Hibernate Envers or an audit table). Essential for confidential court data.
4. **Parties and documents.** Add plaintiffs, defendants and lawyers (verified through NIDA), and document upload stored in object storage.
5. **Integration tests on the real database.** Use Testcontainers so tests run against the same database as production.
6. **Deployment stage.** Add a deploy job to staging, then production with a manual approval (GitHub Environments), and zero-downtime rollout.
7. **Code quality and security scanning.** SonarQube or SpotBugs, Checkstyle, and OWASP dependency-check or GitHub CodeQL in the pipeline.
8. **Observability.** Structured JSON logs, metrics with Micrometer and Prometheus, dashboards in Grafana, and alerts on errors and slow requests.
9. **Frontend.** A React or Angular app for clerks and judges, calling this API.
10. **Notifications.** SMS or email to parties when a hearing is scheduled or moved, sent asynchronously through a message queue.

## Sample requests

```bash
curl -X POST localhost:8080/api/v1/cases -H 'Content-Type: application/json' \
  -d '{"title":"Land dispute","category":"CIVIL"}'
curl -X PATCH localhost:8080/api/v1/cases/1/status -H 'Content-Type: application/json' -d '{"status":"REGISTERED"}'
curl -X POST localhost:8080/api/v1/cases/1/hearings -H 'Content-Type: application/json' \
  -d '{"scheduledAt":"2027-01-15T09:00:00","courtRoom":"Room A"}'
curl localhost:8080/api/v1/cases/statistics
curl -X POST localhost:8080/api/employees -H 'Content-Type: application/json' \
  -d '{"name":"Alice","email":"alice@example.rw","department":"IT","salary":900000}'
```

## Likely exam questions

### Core Java
1. **JDK vs JRE vs JVM?** The JDK is the development kit (compiler and tools), the JRE is the runtime, and the JVM executes bytecode.
2. **Four OOP pillars?** Encapsulation (private fields, public methods), inheritance (`extends`), polymorphism (overriding and overloading), abstraction (abstract classes and interfaces).
3. **Overloading vs overriding?** Overloading is the same name with different parameters, resolved at compile time. Overriding is a subclass replacing a parent method, resolved at runtime.
4. **Abstract class vs interface?** An abstract class can hold state and constructors, and a class extends only one. A class can implement many interfaces, which can have `default` methods.
5. **`==` vs `equals()`?** `==` compares references, `equals()` compares content. Override `hashCode()` together with `equals()`, or HashMap and HashSet break.
6. **`String` vs `StringBuilder` vs `StringBuffer`?** String is immutable. StringBuilder is mutable and fast. StringBuffer is mutable and synchronized.
7. **`ArrayList` vs `LinkedList`?** ArrayList has fast access by index. LinkedList has fast insert and delete at its ends.
8. **How does HashMap work?** `hashCode()` picks a bucket, and `equals()` finds the key inside it. Since Java 8, a crowded bucket becomes a tree. Average get/put is O(1).
9. **`HashMap` vs `Hashtable` vs `ConcurrentHashMap`?** Not thread-safe; thread-safe but old and slow; the modern thread-safe choice.
10. **Checked vs unchecked exceptions?** Checked exceptions must be caught or declared (`IOException`, `SQLException`). Unchecked ones extend `RuntimeException`.
11. **`final` / `finally` / `finalize`?** Cannot change or override; always runs after try; deprecated cleanup hook.
12. **Java 8+ features?** Lambdas, streams, `Optional`, the `java.time` API, default methods, records, `switch` expressions, text blocks, virtual threads (21).
13. **Thread vs Runnable vs ExecutorService?** Prefer an `ExecutorService` thread pool over creating threads by hand. `synchronized` and locks prevent race conditions.
14. **What is a deadlock and how do you avoid it?** Two threads each wait for a lock the other holds. Always take locks in the same order, or use timeouts.
15. **Immutable class?** `final` class, `private final` fields, no setters, defensive copies of mutable fields. Records do this for you.

### Spring and Spring Boot
16. **IoC and DI?** Spring creates the objects (beans) and injects their dependencies. Constructor injection is preferred: it allows final fields and easy testing.
17. **`@Component` / `@Service` / `@Repository` / `@Controller` / `@RestController`?** All register beans. `@Repository` also translates database exceptions. `@RestController` = `@Controller` + `@ResponseBody`.
18. **What does Spring Boot add?** Auto-configuration, starter dependencies, an embedded server (Tomcat), and Actuator for health and metrics.
19. **`@Transactional`?** Everything in the method commits together or rolls back together. By default it rolls back on unchecked exceptions only.
20. **Bean scopes?** singleton (default), prototype, request, session.
21. **JPA LAZY vs EAGER?** LAZY loads related data only when accessed. EAGER always loads it. Default to LAZY.
22. **The N+1 problem?** Loading a list, then one extra query per row for a lazy relationship. Fix with `JOIN FETCH` or `@EntityGraph`.
23. **Optimistic vs pessimistic locking?** Optimistic uses a `@Version` column and fails on conflict. Pessimistic locks the row (`SELECT ... FOR UPDATE`).
24. **How does JWT authentication work?** The user logs in and receives a signed token. The client sends `Authorization: Bearer <token>`, and a filter validates it on each request.
25. **Spring profiles?** Separate configuration per environment: `application-dev.properties`, `application-prod.properties`.

### REST APIs
26. **What makes an API RESTful?** Resources identified by URLs, HTTP verbs as actions, stateless requests, standard status codes, JSON representations.
27. **PUT vs PATCH vs POST?** PUT replaces the whole resource (idempotent). PATCH changes part of it. POST creates (not idempotent).
28. **Status codes?** 200 OK, 201 Created, 204 No Content, 400 Bad Request, 401 not authenticated, 403 not allowed, 404 Not Found, 409 Conflict, 422 Unprocessable, 500 server error.
29. **API versioning?** In the URL (`/api/v1`), a header, or the media type. The URL is the simplest.
30. **Documenting APIs?** OpenAPI/Swagger with springdoc (included here: `/swagger-ui.html`).
31. **What is CORS?** The browser blocks calls to a different origin unless the server allows it, e.g. `@CrossOrigin` or a global CORS config.

### Databases
32. **Normalization?** 1NF: atomic values. 2NF: no partial dependency on a composite key. 3NF: no transitive dependency.
33. **ACID?** Atomicity, Consistency, Isolation, Durability.
34. **INNER vs LEFT JOIN?** INNER returns only matching rows. LEFT returns all rows from the left table, with NULLs where there is no match.
35. **WHERE vs HAVING?** WHERE filters rows before grouping. HAVING filters groups after `GROUP BY`.
36. **DELETE vs TRUNCATE vs DROP?** Delete chosen rows (can roll back); remove all rows quickly; remove the whole table.
37. **What is an index?** A structure that speeds up reads on a column, at the cost of slower writes and extra storage.
38. **Preventing SQL injection?** Use `PreparedStatement` or JPA parameters. Never build SQL by concatenating strings.

### Integration, testing, performance, security
39. **How would IECMS integrate with NIDA, RCS or Irembo?** REST calls with timeouts, retries and idempotency keys, or messaging (Kafka/RabbitMQ) for asynchronous work. Log every call for audit.
40. **Unit vs integration tests?** Unit tests check one class with dependencies mocked (JUnit, Mockito). Integration tests run real components together (`@SpringBootTest`, MockMvc).
41. **Git workflow?** Feature branches, pull requests with code review, merge into main, tagged releases. Merge keeps history; rebase rewrites it into a straight line.
42. **Improving performance?** Measure first, then add indexes, fix N+1 queries, add pagination and caching (`@Cacheable`, Redis), and tune the connection pool.
43. **OWASP Top 10?** Broken access control, cryptographic failures, injection, insecure design, misconfiguration, vulnerable components, authentication failures, integrity failures, logging failures, SSRF.
44. **Protecting confidential case data?** Role-based access, BCrypt password hashing, HTTPS, encryption at rest, audit logs of who viewed or changed a case, and compliance with Rwanda's data protection law (Law No. 058/2021).
45. **Recommending hardware for a national system?** Size from expected users, cases and documents: CPU and RAM for the app servers, fast storage for the database, separate storage for scanned documents, backups and a disaster-recovery site, and load balancing for high availability.

### Frontend
46. **React: props vs state?** Props are passed in from the parent and are read-only. State belongs to the component and changes with `useState`.
47. **`useEffect`?** Runs side effects such as API calls after render. The dependency array controls when it runs again.
48. **Angular building blocks?** Modules or standalone components, components, services (via DI), routing, and RxJS observables for HTTP.

### Short coding questions (answers in `practice/JavaSnippets.java`)
Palindrome, reverse a string, second largest number, duplicates in an array, prime check, Fibonacci, factorial, anagram, word frequency with streams, Rwandan National ID validation, average salary per department and top earners with streams, FizzBuzz, thread-safe singleton.
