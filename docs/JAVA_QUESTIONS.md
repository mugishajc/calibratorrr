# Java exam questions

## Part A: "What is the output?"

Guess first, then check by running:

```bash
mvn -q compile exec:java -Dexec.mainClass=rw.minijust.iecms.practice.OutputQuestions
```

| # | Code | Output | Why |
|---|---|---|---|
| 1 | `"hello" == "hello"`, `"hello" == new String("hello")`, `.equals(...)` | `true`, `false`, `true` | Literals share the string pool; `new` makes a new object. `==` compares references. |
| 2 | `Integer 127 == 127`, `Integer 128 == 128` | `true`, `false` | `Integer` caches -128..127. Compare wrappers with `equals()`. |
| 3 | `1 + 2 + "3" + 4 + 5` | `3345` | Left to right: `1+2=3`, then string concatenation from `"3"` onwards. |
| 4 | `int i = 5; i = i++ + ++i;` | `12` | `i++` gives 5 (i becomes 6), `++i` makes 7, and 5 + 7 = 12. |
| 5 | `try { return 1; } finally { return 2; }` | `2` | `finally` always runs, and its `return` replaces the earlier one. |
| 6 | `'A' + 1`, `(char) ('A' + 1)` | `66`, `B` | A `char` is promoted to `int` in arithmetic. |
| 7 | `Parent p = new Child(); p.name(); Parent.who()` | `Child.name`, `Parent.who` | Instance methods are overridden (runtime). Static methods are only hidden (compile time). |
| 8 | `[10,20,30].remove(1)`, then `remove(Integer.valueOf(10))` | `[10, 30]`, `[30]` | `remove(int)` removes by index; `remove(Object)` removes by value. |
| 9 | Removing from a list inside a for-each loop | `ConcurrentModificationException` | Use `Iterator.remove()` or `list.removeIf(...)`. |
| 10 | `0.1 + 0.2 == 0.3` | `false` | Binary floating point is approximate. Use `BigDecimal` for money. |
| 11 | `Math.round(2.5)`, `Math.round(-2.5)` | `3`, `-2` | `round` is `floor(x + 0.5)`. |
| 12 | `int[] b = a; b[0] = 9; a[0]` | `9` | Both variables point at the same array. |
| 13 | Method appends to a `StringBuilder`, another method reassigns it | `original changed` | Java passes references by value: you can change the object, not the caller's variable. |
| 14 | `10/3`, `10%3`, `10/3.0`, `-7/2`, `-7%2` | `3`, `1`, `3.333...`, `-3`, `-1` | Integer division truncates toward zero; the remainder takes the sign of the dividend. |
| 15 | `String s = null; s.length()` | `NullPointerException` | Guard with a null check or `Optional`. |
| 16 | `new Child()` order | Parent static -> Child static -> Parent instance block -> Parent constructor -> Child instance block -> Child constructor | Static blocks run once at class load; parents initialise before children. |
| 17 | `t.concat(" 21")` without assigning | `Java` | Strings are immutable; `concat` returns a new string. |
| 18 | `"abc".compareTo("abd")`, `"Java".substring(1, 3)` | `-1`, `av` | `compareTo` returns the character difference; `substring` end index is exclusive. |

## Part B: Write the code

1. Write a REST API for employees with create, read, update and delete. -> `employee/EmployeeController.java`
2. Write a JDBC method that finds employees earning more than an amount, safe from SQL injection. -> `JdbcSqlPractice.findEarningAbove`
3. Group a list of employees by department and compute average salary with streams. -> `JavaSnippets.averageSalaryByDepartment`
4. Check whether a string is a palindrome, ignoring case and punctuation. -> `JavaSnippets.isPalindrome`
5. Find the second largest number in an array in one pass. -> `JavaSnippets.secondLargest`
6. Count how often each word appears in a text. -> `JavaSnippets.wordFrequency`
7. Write a thread-safe singleton. -> `JavaSnippets.Config`
8. Model a case workflow so a case cannot skip steps (e.g. FILED straight to JUDGED). -> `model/CaseStatus.java`
9. Return a 404 with a clear message when a record is not found, for every endpoint. -> `web/ApiExceptionHandler.java`
10. Prevent two hearings in the same courtroom at the same time. -> `CaseService.scheduleHearing`

## Part C: Scenario questions (short model answers)

**1. Users say the case search page is slow. What do you do?**
Measure first: check the logs and APM for which endpoint and query is slow. Run `EXPLAIN` on the query, add a missing index, fix N+1 queries with `JOIN FETCH`, add pagination, and cache data that rarely changes. Then measure again to confirm.

**2. Two clerks edit the same case at the same time. How do you stop one from silently overwriting the other?**
Optimistic locking: a `@Version` column. The second save fails with a conflict (HTTP 409), and the user reloads and retries.

**3. How would you integrate IECMS with NIDA to verify a party's identity?**
A dedicated client class calling NIDA's REST API over HTTPS, with timeouts, retries with backoff, and a circuit breaker so NIDA downtime doesn't break IECMS. Cache results briefly, never log personal data in plain text, and keep an audit record of each lookup.

**4. You receive old code with no tests and must change it. How do you proceed?**
Read it and run it. Write tests that capture the current behaviour, then refactor in small steps (extract methods, rename, remove duplication), running the tests after each step. Commit often on a feature branch and open a pull request for review.

**5. How do you manage source code and releases for a team?**
Git with a protected `main` branch, feature branches, pull requests with at least one reviewer, CI that runs the tests on every push, semantic version tags for releases, and a changelog.

**6. How do you protect confidential case data?**
Role-based access (judge, registrar, lawyer, public), least privilege, BCrypt-hashed passwords, HTTPS everywhere, encryption at rest for documents, an audit log of who viewed or changed each case, regular backups, and compliance with Law No. 058/2021 on personal data protection.

**7. A change request asks for a new field on the case form. Walk through the steps.**
Clarify the requirement with the user, update the design document, add a database migration (Flyway/Liquibase), update the entity, DTO, validation, API and frontend, write tests, get a code review, deploy to staging for user acceptance testing, then to production, and update the documentation.

**8. How do you document an API for other teams?**
OpenAPI/Swagger generated from the code (springdoc), with example requests and responses, error formats, authentication steps and versioning rules, published at a stable URL.

**9. Which design patterns have you used?**
Singleton (Spring beans), Factory, Builder (complex objects), Strategy (different fee calculations per case type), Observer (events, e.g. notify parties when a hearing is scheduled), Repository and DTO (this project), Adapter (wrapping an external system's API).

**10. What are SOLID principles?**
Single responsibility, Open/closed (extend without modifying), Liskov substitution (subclasses can replace parents), Interface segregation (small focused interfaces), Dependency inversion (depend on abstractions; Spring DI does this).
