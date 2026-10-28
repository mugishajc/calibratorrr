# IECMS mini — Java exam practice

[![CI/CD](https://github.com/mugishajc/calibratorrr/actions/workflows/ci.yml/badge.svg)](https://github.com/mugishajc/calibratorrr/actions/workflows/ci.yml)

A small Spring Boot 3 project for a senior Java developer exam: a court case API, an employee CRUD API, plain JDBC, SQL queries and common coding questions. It runs on an in-memory H2 database, so nothing needs installing beyond Java 21+ and Maven.


## What the project does

A simplified court case management system (like IECMS), with a small web page and a secured REST API.

- **Sign in.** Users log in with a username and password and get a JWT token valid for 1 hour. Passwords are stored as BCrypt hashes. After 5 wrong passwords the account is locked for 15 minutes.
- **Roles.** Each user has one role, checked on every request:

  | Role | Can do |
  |---|---|
  | ADMIN | Everything, plus create users, read the audit log and manage employees |
  | REGISTRAR | File cases, schedule hearings, move cases through the workflow |
  | JUDGE | Move cases through the workflow, and is the only role that can record a judgment |
  | VIEWER | Read cases and statistics only |

- **File a case.** A registrar submits a title and category (civil, criminal, commercial, labour, administrative). The system gives it a number such as `CIV/2026/00001` and status `FILED`, and records who filed it.
- **Move the case through its workflow.** `FILED → REGISTERED → SCHEDULED → IN_HEARING → JUDGED → CLOSED`, or `DISMISSED`. Steps cannot be skipped: an illegal move returns HTTP 409.
- **Schedule hearings.** Dates must be in the future, and a courtroom cannot be booked twice at the same time. After the booking is saved, an event notifies the parties in the background (logged here; SMS or email in production).
- **List, search and statistics.** Paginated, sortable, filterable by status; counts per status are cached and refreshed whenever a case changes.
- **Audit trail.** Every login, failed login, case filed, status change, hearing and new user is recorded with who did it and when. Admins can read it; nobody can edit it.
- **Traceability.** Every response carries an `X-Request-Id` header, and the same id appears in every log line for that request.
- **Safe and consistent.** Input is validated, errors come back in one standard JSON format, and `@Version` stops two users from overwriting each other's changes.
- **Employees.** A basic create, read, update, delete API (admin only), the classic "write a CRUD API" exam task.

Alongside the app there is exam practice: SQL queries on a sample database, data structures written from scratch, short coding answers, "what is the output?" questions, and 125 questions with answers.

## How to run it

### 1. Install the prerequisites

- **Java 21 or newer.** Check with `java -version`.
- **Maven 3.9+.** Check with `mvn -v`.
- **Git**, to clone the project.

No database is needed: the app uses an in-memory H2 database that starts empty each time.

### 2. Get the code and start the app

```bash
git clone https://github.com/mugishajc/calibratorrr.git
cd calibratorrr
mvn spring-boot:run
```

Wait until the log shows `Started IecmsApplication`. The app is now on **http://localhost:8080**. Stop it with `Ctrl+C`.

To use another port: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090`

### 3. Use the web page (frontend)

1. Open **http://localhost:8080** in your browser.
2. Sign in with one of the demo users. All use the password **`Demo@12345`**:
   - `registrar`: can file cases and move them along
   - `judge`: can record judgments
   - `admin`: can do everything
   - `viewer`: can only look
3. As **registrar**, type a title, choose a category and click **File case**. The case appears in the table and the counts update.
4. Use the buttons in the **Move to** column to move a case: `REGISTERED` → `SCHEDULED` → `IN_HEARING`.
5. Try `JUDGED` as the registrar: you get "Your role is not allowed to do this." Click **Sign out**, sign in as **judge**, and the same button works.
6. Sign in as **viewer**: you can see everything but there are no buttons and no filing form.

The page is plain HTML and JavaScript in `src/main/resources/static/index.html`, served by Spring Boot, so it needs no separate install. A React or Angular app would call the same API in the same way.

### 4. Try the API in Swagger

1. Open **http://localhost:8080/swagger-ui.html**.
2. Under **auth-controller**, run `POST /api/v1/auth/login` with:
   ```json
   { "username": "registrar", "password": "Demo@12345" }
   ```
3. Copy the `accessToken` value from the response.
4. Click **Authorize** at the top, paste the token, and click **Authorize**.
5. Every endpoint can now be tried from the page.

### 5. Or use the command line

```bash
# Log in and keep the token
TOKEN=$(curl -s localhost:8080/api/v1/auth/login -H 'Content-Type: application/json' \
  -d '{"username":"registrar","password":"Demo@12345"}' | sed 's/.*"accessToken":"\([^"]*\)".*/\1/')

# File a case
curl -s localhost:8080/api/v1/cases -H "Authorization: Bearer $TOKEN" -H 'Content-Type: application/json' \
  -d '{"title":"Land dispute","category":"CIVIL"}'

# Move it along, schedule a hearing, see the list and the statistics
curl -s -X PATCH localhost:8080/api/v1/cases/1/status -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"status":"REGISTERED"}'
curl -s localhost:8080/api/v1/cases/1/hearings -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' -d '{"scheduledAt":"2027-01-15T09:00:00","courtRoom":"Room A"}'
curl -s "localhost:8080/api/v1/cases?status=SCHEDULED" -H "Authorization: Bearer $TOKEN"
curl -s localhost:8080/api/v1/cases/statistics -H "Authorization: Bearer $TOKEN"

# Without a token you get 401
curl -i localhost:8080/api/v1/cases
```

### 6. Run the tests and the practice programs

```bash
mvn verify      # all 64 tests + coverage check (fails below 70%)
mvn -q compile exec:java -Dexec.mainClass=rw.minijust.iecms.practice.JavaSnippets
mvn -q compile exec:java -Dexec.mainClass=rw.minijust.iecms.practice.JdbcSqlPractice
mvn -q compile exec:java -Dexec.mainClass=rw.minijust.iecms.practice.OutputQuestions
```

The coverage report is in `target/site/jacoco/index.html`.

### 7. Run with Docker

```bash
docker build -t iecms-mini .
docker run -p 8080:8080 -e APP_JWT_SECRET="a-long-random-secret-of-at-least-32-characters" iecms-mini
```

Or use the image built by CI: `ghcr.io/mugishajc/calibratorrr:latest`.

### Settings for a real environment

| Variable | Purpose | Default |
|---|---|---|
| `APP_JWT_SECRET` | Key that signs login tokens, at least 32 characters. **Must** be set and kept secret. | A dev-only value |
| `APP_DEMO_USERS_ENABLED` | Creates the four demo users on an empty database. Set to `false` in production. | `true` |

## API endpoints

| Method | Path | Who | What |
|---|---|---|---|
| POST | `/api/v1/auth/login` | anyone | Log in, returns a JWT |
| GET | `/api/v1/auth/me` | signed in | Current user and roles |
| POST | `/api/v1/users` | ADMIN | Create a user (strong password required) |
| POST | `/api/v1/cases` | REGISTRAR, ADMIN | File a case |
| GET | `/api/v1/cases?status=&page=&size=&sort=` | signed in | List cases |
| GET | `/api/v1/cases/{id}` | signed in | One case |
| PATCH | `/api/v1/cases/{id}/status` | REGISTRAR, JUDGE, ADMIN (JUDGED: judge only) | Move a case |
| POST | `/api/v1/cases/{id}/hearings` | REGISTRAR, ADMIN | Schedule a hearing |
| GET | `/api/v1/cases/statistics` | signed in | Cases per status |
| GET | `/api/v1/audit` | ADMIN | Audit trail, newest first |
| GET/POST/PUT/DELETE | `/api/employees` | ADMIN | Employee CRUD |
| GET | `/actuator/health` | anyone | Health check |

## What is where

| Topic | File |
|---|---|
| Security config, JWT signing and checking, public vs protected URLs | `security/SecurityConfig.java` |
| Login, lockout after 5 failures, user creation | `security/AuthService.java`, `security/AuthController.java` |
| Users and roles | `security/AppUser.java`, `security/Role.java`, `security/DemoUserSeeder.java` |
| Audit trail | `audit/` |
| Async notification after commit | `notification/` |
| Caching, JPA auditing (`createdBy`), Swagger security, request ids | `config/` |
| Case status workflow | `model/CaseStatus.java` |
| JPA entities, one-to-many, `@Version`, `@CreatedBy` | `model/CourtCase.java`, `model/Hearing.java` |
| Spring Data queries, `GROUP BY` statistics | `repository/CaseRepository.java` |
| `@Transactional` service with business rules | `service/CaseService.java` |
| REST controller with role checks | `web/CaseController.java` |
| Global error handling (RFC 7807) | `web/ApiExceptionHandler.java` |
| Web page | `src/main/resources/static/index.html` |
| Employee CRUD | `employee/` |
| Coding answers, data structures, output questions, JDBC + SQL | `practice/` |
| 30 SQL queries | `sql/queries.sql` |
| 125 questions and answers: OOP, core Java, data structures, algorithms, threads, SQL, JPA | `docs/QUESTIONS_AND_ANSWERS.md` |
| Output questions, write-the-code tasks, scenario questions | `docs/JAVA_QUESTIONS.md` |

## Testing

64 tests, all run by `mvn verify` and by CI on every push. The build fails if line coverage drops below 70%.

| Level | What it checks | Example |
|---|---|---|
| Unit (no Spring, mocks) | Business rules, audit calls and events in isolation | `service/CaseServiceTest` with Mockito |
| Parameterized | Every allowed and forbidden status change from one table | `CaseStatusTest.transitionRules` |
| Web slice (`@WebMvcTest`) | HTTP status codes, validation, error format; service mocked | `web/CaseControllerTest` |
| Security, end to end | Real login and JWT, 401 without a token, 403 for the wrong role, judge-only judgments, lockout, weak passwords, audit trail, request ids | `AuthIntegrationTest` |
| Integration (`@SpringBootTest`) | Full case lifecycle through controller, service, JPA and database | `CaseApiTest`, `EmployeeApiTest` |
| Data | Each practice SQL query returns the expected rows; data structures behave correctly | `practice/SqlQueriesTest`, `practice/DataStructuresTest` |

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`):

1. **On every push and pull request:** build with Java 21, run all tests, enforce the coverage gate, and upload the test reports, coverage report and jar.
2. **On push to `main` only, after tests pass:** build the Docker image (multi-stage, runs as a non-root user) and publish it to GitHub Container Registry as `ghcr.io/mugishajc/calibratorrr:latest` plus a commit-SHA tag.

Dependabot opens weekly pull requests for Maven and GitHub Actions updates.

## What should be done next

To take this from a demo to production:

1. **Real database and migrations.** Move from in-memory H2 to PostgreSQL or MySQL, and manage the schema with Flyway instead of `ddl-auto=update`. Use a database sequence for case numbers so they survive restarts and work across several servers.
2. **Single sign-on.** Replace the built-in login with Keycloak or the government identity provider (OAuth2/OpenID Connect), add refresh tokens and token revocation, and two-factor authentication for judges and admins.
3. **Parties and documents.** Add plaintiffs, defendants and lawyers (identity verified through NIDA), and document upload stored in object storage with virus scanning.
4. **Real notifications.** Send SMS and email through a message queue (RabbitMQ or Kafka) with retries, instead of the log line.
5. **Integration tests on the real database.** Use Testcontainers so tests run against the same database as production.
6. **Deployment stage.** Add a deploy job to staging, then production with a manual approval (GitHub Environments), and zero-downtime rollout.
7. **Code quality and security scanning.** SonarQube or SpotBugs, Checkstyle, and OWASP dependency-check or GitHub CodeQL in the pipeline.
8. **Observability.** JSON logs shipped to a central store, metrics with Micrometer and Prometheus, Grafana dashboards, and alerts on errors and slow requests.
9. **Full frontend.** A React or Angular app with routing, forms for hearings, and audit-log views, built and tested in the same pipeline.
10. **Distributed cache and rate limiting.** Redis for the statistics cache and for login rate limits once there is more than one server.

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
