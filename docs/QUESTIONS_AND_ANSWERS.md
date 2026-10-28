# Questions and answers: OOP, data structures, SQL

Short answers to the questions most often asked in Java developer exams. Code for many of them is in `src/main/java/rw/minijust/iecms/practice/`.

Contents: [1. OOP](#1-object-oriented-programming) · [2. Core Java](#2-core-java) · [3. Data structures and collections](#3-data-structures-and-collections) · [4. Algorithms](#4-algorithms) · [5. Exceptions and multithreading](#5-exceptions-and-multithreading) · [6. SQL theory](#6-sql-theory) · [7. SQL query questions](#7-sql-query-questions) · [8. JPA and Hibernate](#8-jpa-and-hibernate)

---

## 1. Object-oriented programming

**1. What is OOP?**
A way of organising code around objects that bundle data (fields) and behaviour (methods). It makes code easier to reuse, extend and maintain.

**2. Class vs object?**
A class is the blueprint. An object is an instance created from it with `new`. `Car` is a class; `new Car("RAD 123A")` is an object.

**3. What are the four pillars of OOP?**
- **Encapsulation:** hide the data, expose controlled methods.
- **Inheritance:** a class reuses another class's code (`extends`).
- **Polymorphism:** one interface, many implementations.
- **Abstraction:** show what an object does, hide how.

**4. Explain encapsulation with an example.**
```java
public class Account {
    private double balance;                 // hidden
    public void deposit(double amount) {    // controlled access
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        balance += amount;
    }
    public double getBalance() { return balance; }
}
```
Nobody can set a negative balance directly, because every change goes through `deposit`.

**5. What is inheritance? Does Java support multiple inheritance?**
A subclass gets the fields and methods of its parent (`class Judge extends Employee`), an IS-A relationship. Java allows only one parent class, which avoids the "diamond problem" where two parents define the same method. A class can implement many interfaces.

**6. What is polymorphism? What are the two types?**
- **Compile-time (static):** method overloading, the same name with different parameters.
- **Runtime (dynamic):** method overriding. `Shape s = new Circle(); s.area();` calls `Circle.area()`, chosen at runtime by the object's real type.

**7. What is abstraction?**
Exposing only the essential operations. A `PaymentService` interface has `pay()`; callers don't need to know whether it is mobile money or a bank transfer. Achieved with abstract classes and interfaces.

**8. Abstract class vs interface?**

| | Abstract class | Interface |
|---|---|---|
| Instance fields | Yes | No (only constants) |
| Constructors | Yes | No |
| Method bodies | Yes | `default`, `static` and `private` methods (Java 8/9+) |
| How many per class | One (`extends`) | Many (`implements`) |
| Use when | Related classes share state and code | Unrelated classes share a capability (`Comparable`, `Serializable`) |

**9. Overloading vs overriding?**

| Overloading | Overriding |
|---|---|
| Same class (or subclass), same name, different parameters | Subclass, same name and same parameters |
| Decided at compile time | Decided at runtime |
| Return type can differ | Return type the same or a subtype (covariant) |
| Works on static methods | Static, `final` and `private` methods cannot be overridden |

**10. What are the rules for overriding?**
Same name and parameters; the return type is the same or a subtype; access cannot be more restrictive (a `public` method stays `public`); it cannot throw new or broader checked exceptions; use `@Override` so the compiler checks.

**11. Composition vs inheritance? Which is better?**
Inheritance is IS-A (`Car extends Vehicle`). Composition is HAS-A (`Car` has an `Engine` field). Prefer composition: it is more flexible, avoids deep fragile hierarchies, and the parts can be swapped at runtime.

**12. Association vs aggregation vs composition?**
- **Association:** objects use each other (a lawyer and a client).
- **Aggregation:** a "has-a" where the part can exist alone (a department has employees; employees remain if it closes).
- **Composition:** a strong "has-a" where the part dies with the whole (a house and its rooms; a case and its hearings in this project).

**13. What is a constructor? Can it be private?**
A special method with the class name and no return type, run by `new` to initialise the object. If you write none, Java adds a default no-argument one. It can be private (Singleton, factory methods). It cannot be `static`, `final` or `abstract`, and it is not inherited.

**14. What is constructor chaining?**
One constructor calling another: `this(...)` in the same class or `super(...)` in the parent. It must be the first statement. If you don't call `super(...)`, Java inserts `super()` automatically.

**15. `this` vs `super`?**
`this` refers to the current object (e.g. `this.name = name`). `super` refers to the parent's members (`super.toString()`, `super(...)`).

**16. What does `static` mean?**
The member belongs to the class, not to each object: one copy shared by all instances. Static methods cannot use `this` or instance fields directly. Static blocks run once when the class loads.

**17. What does `final` mean on a variable, method and class?**
A final variable cannot be reassigned; a final method cannot be overridden; a final class cannot be extended (`String` is final).

**18. Access modifiers?**

| Modifier | Same class | Same package | Subclass (other package) | Everywhere |
|---|---|---|---|---|
| `private` | ✔ | | | |
| default (none) | ✔ | ✔ | | |
| `protected` | ✔ | ✔ | ✔ | |
| `public` | ✔ | ✔ | ✔ | ✔ |

**19. How do you make a class immutable?**
Make the class `final`, all fields `private final`, set them in the constructor, provide no setters, and return copies of mutable fields. Or use a `record`.

**20. What methods does every object get from `Object`?**
`equals`, `hashCode`, `toString`, `getClass`, `clone`, `wait`, `notify`, `notifyAll` (and the deprecated `finalize`).

**21. What is the `equals` / `hashCode` contract?**
If two objects are equal by `equals`, they must have the same `hashCode`. If you override one, override the other, or the objects will misbehave in `HashMap` and `HashSet`.

**22. Shallow copy vs deep copy?**
A shallow copy copies the fields, so both objects share the same inner objects. A deep copy also copies the inner objects, so changes to one don't affect the other.

**23. Upcasting vs downcasting?**
Upcasting (`Animal a = new Dog();`) is automatic and safe. Downcasting (`Dog d = (Dog) a;`) needs a cast and fails with `ClassCastException` if the object isn't really a Dog. Check first: `if (a instanceof Dog d) { ... }`.

**24. Two interfaces have the same default method. What happens?**
The class must override the method, otherwise it won't compile. Inside, it can choose one: `A.super.hello();`.

**25. What is a marker interface?**
An interface with no methods that tags a class, e.g. `Serializable` or `Cloneable`. Annotations mostly do this job today.

**26. Types of nested class?**
Static nested class, inner (non-static) class, local class (inside a method), and anonymous class (declared and instantiated in one expression). Lambdas replaced most anonymous classes.

**27. What are sealed classes (Java 17)?**
A class that lists exactly which classes may extend it: `sealed interface Shape permits Circle, Square {}`. This is useful for closed sets of types.

**28. What are SOLID principles?**
- **S**ingle responsibility: a class has one reason to change.
- **O**pen/closed: open for extension, closed for modification.
- **L**iskov substitution: a subclass can replace its parent without breaking anything.
- **I**nterface segregation: many small interfaces beat one large one.
- **D**ependency inversion: depend on abstractions, not concrete classes (Spring DI).

**29. Coupling and cohesion?**
Aim for low coupling (classes depend little on each other) and high cohesion (each class does one focused job).

**30. Common design patterns?**
- **Singleton:** exactly one instance (`JavaSnippets.Config`).
- **Factory:** a method decides which subclass to create.
- **Builder:** builds complex objects step by step (`Employee.builder().name("A").build()`).
- **Strategy:** swappable algorithms behind an interface (fee calculation per case type).
- **Observer:** objects get notified of events (notify parties when a hearing is scheduled).
- **Adapter:** wraps an incompatible interface (wrapping an external system's API).
- **Decorator:** adds behaviour by wrapping (`BufferedReader` around `FileReader`).
- **Repository / DTO:** used in this project's data and API layers.

---

## 2. Core Java

**31. JDK vs JRE vs JVM?**
The JVM runs bytecode. The JRE is the JVM plus libraries needed to run programs. The JDK is the JRE plus the compiler (`javac`) and tools needed to develop.

**32. Why is Java platform-independent?**
`javac` compiles to bytecode (`.class`), and any JVM on any operating system can run it: "write once, run anywhere".

**33. Primitive types?**
`byte` (8-bit), `short` (16), `int` (32), `long` (64), `float` (32), `double` (64), `char` (16-bit Unicode), `boolean`. Each has a wrapper class (`Integer`, `Double`...). Autoboxing converts between them automatically.

**34. Stack vs heap memory?**
The stack holds method calls and local variables. It is per thread, fast, and freed when the method returns. The heap holds objects, is shared by all threads, and is cleaned by the garbage collector.

**35. How does garbage collection work?**
The JVM finds objects no longer reachable from any live reference and frees their memory. You cannot force it; `System.gc()` is only a hint.

**36. Why is `String` immutable?**
Security (values such as file paths and URLs can't change), safe sharing between threads, string pool reuse, and cached hash codes for `HashMap` keys.

**37. `String` vs `StringBuilder` vs `StringBuffer`?**
`String` is immutable. `StringBuilder` is mutable and fast, but not thread-safe. `StringBuffer` is mutable and synchronized, so slower. Use `StringBuilder` when building strings in a loop.

**38. What is the string pool?**
A special memory area where string literals are stored once and reused. `"a" == "a"` is true; `new String("a")` creates a separate object.

**39. Is Java pass-by-value or pass-by-reference?**
Always pass-by-value. For objects, the value passed is a copy of the reference: a method can change the object's contents but cannot make the caller's variable point to a different object.

**40. What does `var` do (Java 10)?**
Lets the compiler infer a local variable's type: `var list = new ArrayList<String>();`. The type is still fixed at compile time.

**41. Key Java 8+ features?**
Lambdas, functional interfaces, streams, `Optional`, the `java.time` API, default methods (8); `var` (10); `switch` expressions, text blocks, records (14–16); sealed classes, pattern matching (17); virtual threads, record patterns (21).

**42. What is a functional interface? Give examples.**
An interface with exactly one abstract method, so a lambda can implement it. Examples: `Runnable`, `Comparator<T>`, `Function<T,R>`, `Predicate<T>`, `Supplier<T>`, `Consumer<T>`.

**43. Streams: intermediate vs terminal operations?**
Intermediate operations (`filter`, `map`, `sorted`, `distinct`, `limit`) are lazy and return a stream. Terminal operations (`collect`, `forEach`, `count`, `reduce`, `findFirst`) run the pipeline. A stream can be used only once.

**44. `map` vs `flatMap`?**
`map` transforms each element into one element. `flatMap` turns each element into a stream and flattens them into one stream (e.g. a list of lists into a single list).

**45. What is `Optional` for?**
To show that a value may be absent, instead of returning `null`: `repo.findById(id).orElseThrow(...)`. It helps avoid `NullPointerException`.

**46. What are generics? What is type erasure?**
Generics give compile-time type safety (`List<String>` only holds strings) and remove casts. Type erasure means the generic type information is removed after compilation, so at runtime `List<String>` and `List<Integer>` are the same class.

**47. What does `? extends T` vs `? super T` mean?**
`? extends T` lets you read Ts from it (a producer). `? super T` lets you write Ts into it (a consumer). Remember "PECS": Producer Extends, Consumer Super.

**48. What is a record?**
A compact immutable data class: `record Point(int x, int y) {}`. It generates the constructor, accessors, `equals`, `hashCode` and `toString`.

**49. What is reflection?**
Inspecting and calling classes, methods and fields at runtime (`getClass().getDeclaredMethods()`). Frameworks such as Spring and Hibernate rely on it.

**50. What is serialization?**
Converting an object into bytes (to save it or send it) and back. A class implements `Serializable`; `transient` fields are skipped. REST APIs usually serialize to JSON with Jackson instead.

---

## 3. Data structures and collections

**51. Array vs `ArrayList`?**
An array has a fixed size and can hold primitives. An `ArrayList` grows automatically (about 1.5 times when full), holds only objects, and has handy methods.

**52. `ArrayList` vs `LinkedList`?**
`ArrayList` uses an array: O(1) access by index, and inserting in the middle is O(n) because elements shift. `LinkedList` is a doubly linked list: O(1) insert and remove at the ends, but O(n) access by index. `ArrayList` is the right choice almost always.

**53. Time complexity of common operations?**

| Structure | Get / search | Insert | Remove | Notes |
|---|---|---|---|---|
| Array / `ArrayList` | O(1) by index, O(n) search | O(1) at end (amortised), O(n) in middle | O(n) | |
| `LinkedList` | O(n) | O(1) at ends | O(1) at ends | |
| `HashMap` / `HashSet` | O(1) average | O(1) average | O(1) average | Unordered |
| `LinkedHashMap` | O(1) | O(1) | O(1) | Keeps insertion (or access) order |
| `TreeMap` / `TreeSet` | O(log n) | O(log n) | O(log n) | Sorted (red-black tree) |
| `PriorityQueue` | O(1) peek | O(log n) | O(log n) poll | Binary heap, smallest first by default |
| `ArrayDeque` (stack/queue) | O(1) at ends | O(1) | O(1) | |
| Balanced BST | O(log n) | O(log n) | O(log n) | O(n) if unbalanced |

**54. How does `HashMap` work internally?**
It keeps an array of buckets. `hashCode()` of the key picks a bucket; `equals()` finds the exact key inside it. Different keys in the same bucket (a collision) are stored in a linked list, which becomes a balanced tree when it holds more than 8 entries (Java 8+). When the map is 75% full (the load factor), the array doubles and entries are redistributed. It allows one `null` key.

**55. What happens if two keys have the same `hashCode`?**
They land in the same bucket. `equals()` tells them apart, so both are stored. Too many collisions slow the map down, so a good `hashCode` spreads values evenly.

**56. `HashMap` vs `Hashtable` vs `ConcurrentHashMap`?**
`HashMap`: not thread-safe, allows a null key. `Hashtable`: legacy, every method synchronized, no nulls. `ConcurrentHashMap`: thread-safe with fine-grained locking, the modern choice for concurrency.

**57. `HashMap` vs `LinkedHashMap` vs `TreeMap`?**
No order; insertion order (or access order, used for LRU caches); sorted by key.

**58. How does `HashSet` work?**
Internally it is a `HashMap` whose keys are the set elements. That is why elements need correct `equals` and `hashCode`.

**59. `Comparable` vs `Comparator`?**
`Comparable` defines a class's natural order inside the class (`compareTo`). `Comparator` defines extra orders outside it: `Comparator.comparing(Employee::getSalary).reversed()`.

**60. Fail-fast vs fail-safe iterators?**
Fail-fast iterators (`ArrayList`, `HashMap`) throw `ConcurrentModificationException` if the collection changes during iteration. Fail-safe or weakly consistent ones (`ConcurrentHashMap`, `CopyOnWriteArrayList`) work on a snapshot or tolerate changes.

**61. How do you remove items safely while looping?**
`iterator.remove()` or `list.removeIf(x -> x < 0)`. Never `list.remove()` inside a for-each loop.

**62. Stack vs queue?**
A stack is LIFO (last in, first out): undo, the call stack, bracket matching. A queue is FIFO (first in, first out): job queues, BFS, request handling. In Java use `ArrayDeque` for both, not the legacy `Stack` class.

**63. What is a `PriorityQueue`?**
A queue that always gives you the smallest element first (or the largest with a reversed comparator). It is built on a binary heap: insert and remove are O(log n).

**64. `List.of` vs `Arrays.asList` vs `Collections.unmodifiableList`?**
`List.of` is fully immutable and rejects nulls. `Arrays.asList` is fixed-size but you can `set` elements, and it is backed by the array. `unmodifiableList` is a read-only view of a list that can still change underneath.

**65. What is a linked list? How do you reverse one?**
A chain of nodes where each points to the next. To reverse it, walk the list and flip each `next` pointer to the previous node: O(n) time, O(1) space (`DataStructures.reverse`).

**66. How do you detect a cycle in a linked list?**
Floyd's algorithm: a slow pointer moves 1 step and a fast one moves 2. If they ever meet, there is a cycle (`DataStructures.hasCycle`).

**67. How do you find the middle of a linked list in one pass?**
Same two pointers: when the fast one reaches the end, the slow one is in the middle.

**68. What is a binary tree? A binary search tree?**
A binary tree is one where each node has at most two children. In a BST, values in the left subtree are smaller and values in the right are larger, so search is O(log n) when balanced. Self-balancing trees (red-black, AVL) keep it that way; `TreeMap` uses a red-black tree.

**69. Tree traversal orders?**
- **In-order** (left, root, right): sorted output for a BST.
- **Pre-order** (root, left, right): copying a tree.
- **Post-order** (left, right, root): deleting a tree.
- **Level-order** (BFS with a queue): level by level.

**70. What is a graph? How do you store one?**
Nodes connected by edges. An adjacency list (`Map<Node, List<Node>>`) suits sparse graphs and is the usual choice. An adjacency matrix (a 2D array) gives O(1) edge lookup but uses O(V²) memory.

**71. BFS vs DFS?**
BFS uses a queue and explores level by level; it finds the shortest path in an unweighted graph (`DataStructures.shortestHops`). DFS uses a stack or recursion and goes deep first; it is used for cycle detection, topological sort and mazes.

**72. What is a hash table collision, and how is it handled?**
Two keys map to the same slot. Chaining stores them in a list at that slot (Java's approach). Open addressing finds another free slot.

**73. How would you build an LRU cache?**
A `LinkedHashMap` in access order that overrides `removeEldestEntry` (`DataStructures.LruCache`). In an interview, explain the idea: a hash map for O(1) lookup plus a doubly linked list for usage order.

**74. What is Big-O notation?**
How running time or memory grows as the input grows, ignoring constants. From fastest to slowest: O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2ⁿ).

**75. What is the time complexity of these?**
A single loop over n is O(n). Two nested loops are O(n²). Halving the problem each step (binary search) is O(log n). Looking up a `HashMap` is O(1). Sorting is O(n log n).

---

## 4. Algorithms

**76. Sorting algorithms compared?**

| Algorithm | Best | Average | Worst | Extra memory | Stable |
|---|---|---|---|---|---|
| Bubble sort | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Selection sort | O(n²) | O(n²) | O(n²) | O(1) | No |
| Insertion sort | O(n) | O(n²) | O(n²) | O(1) | Yes |
| Merge sort | O(n log n) | O(n log n) | O(n log n) | O(n) | Yes |
| Quick sort | O(n log n) | O(n log n) | O(n²) | O(log n) | No |
| Heap sort | O(n log n) | O(n log n) | O(n log n) | O(1) | No |

**77. What does Java use in `Arrays.sort` and `Collections.sort`?**
Dual-pivot quicksort for primitive arrays, and TimSort (a stable merge/insertion hybrid) for objects.

**78. What does "stable sort" mean?**
Equal elements keep their original order. Important when sorting by one field after another.

**79. Binary search: how and when?**
Only on sorted data. Compare with the middle element, then discard half, so O(log n). Compute the middle as `lo + (hi - lo) / 2` to avoid integer overflow.

**80. What is recursion? What is the risk?**
A method that calls itself with a smaller problem until it reaches a base case. Without a base case, or with very deep recursion, you get a `StackOverflowError`. Loops avoid that.

**81. What is dynamic programming?**
Solving a problem by storing the answers of overlapping subproblems so each is computed once, e.g. Fibonacci with a loop or memo is O(n) instead of O(2ⁿ).

**82. Classic interview problems and their usual approach?**
- Two sum → `HashMap` of seen values (O(n)).
- Duplicates → `HashSet`.
- Balanced brackets → stack.
- Anagram → sort both, or count letters.
- First unique character → `LinkedHashMap` of counts.
- Shortest path, unweighted → BFS.
- Top k items → `PriorityQueue` of size k.

---

## 5. Exceptions and multithreading

**83. Exception hierarchy?**
`Throwable` → `Error` (serious JVM problems such as `OutOfMemoryError`; don't catch them) and `Exception`. Under `Exception`, `RuntimeException` and its subclasses are unchecked; everything else is checked.

**84. Checked vs unchecked exceptions?**
Checked exceptions (`IOException`, `SQLException`) must be caught or declared with `throws`. Unchecked exceptions (`NullPointerException`, `IllegalArgumentException`) don't have to be; they usually mean a bug.

**85. `throw` vs `throws`?**
`throw` raises an exception: `throw new IllegalStateException("...")`. `throws` in a method signature declares what it may raise.

**86. How do you create a custom exception?**
Extend `RuntimeException` (unchecked) or `Exception` (checked) and pass the message to `super(message)`. See `NotFoundException` in this project.

**87. What is try-with-resources?**
`try (Connection c = ...) { }` closes the resource automatically, even on error. The resource must implement `AutoCloseable`.

**88. Does `finally` always run?**
Yes, except when the JVM exits (`System.exit`) or crashes. A `return` inside `finally` overrides the `try`'s return.

**89. Process vs thread?**
A process has its own memory. Threads run inside a process and share its memory, so they are cheaper but need synchronization.

**90. Ways to create a thread?**
Extend `Thread`, implement `Runnable` (no result) or `Callable` (returns a result, can throw), or, preferably, submit tasks to an `ExecutorService`. From Java 21, use virtual threads for many blocking tasks.

**91. Thread states?**
NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED.

**92. `synchronized` vs `volatile` vs `Atomic*`?**
`synchronized` gives one thread at a time plus visibility. `volatile` gives visibility only, so `count++` is still unsafe. `AtomicInteger.incrementAndGet()` is a lock-free, thread-safe counter.

**93. `wait()` vs `sleep()`?**
`wait()` releases the lock and waits for `notify()`; it must be called inside `synchronized`. `sleep()` pauses but keeps any lock it holds.

**94. What is a race condition?**
Two threads change shared data at the same time and the result depends on timing, e.g. two `count++` calls losing an update. Fix it with locks, atomics or immutability.

**95. What is a deadlock? How do you prevent it?**
Two threads each hold a lock the other needs, so both wait forever. Prevent it by always taking locks in the same order, using timeouts (`tryLock`), or holding fewer locks.

**96. What is `CompletableFuture`?**
A way to run tasks asynchronously and combine them: `supplyAsync(...).thenApply(...).thenCombine(...)`. Useful for calling several services in parallel.

---

## 6. SQL theory

**97. DDL vs DML vs DCL vs TCL?**
- **DDL** (structure): `CREATE`, `ALTER`, `DROP`, `TRUNCATE`.
- **DML** (data): `SELECT`, `INSERT`, `UPDATE`, `DELETE`.
- **DCL** (permissions): `GRANT`, `REVOKE`.
- **TCL** (transactions): `COMMIT`, `ROLLBACK`, `SAVEPOINT`.

**98. Primary key vs unique key vs foreign key?**
A primary key identifies each row uniquely; there is one per table and it cannot be NULL. A unique key also prevents duplicates; a table can have several, and they can allow NULL. A foreign key points to another table's key to enforce the relationship.

**99. Types of join?**
- **INNER JOIN:** only matching rows.
- **LEFT JOIN:** all rows from the left table, NULLs where nothing matches.
- **RIGHT JOIN:** the reverse.
- **FULL OUTER JOIN:** all rows from both sides (MySQL doesn't have it: use `LEFT JOIN ... UNION ... RIGHT JOIN`).
- **CROSS JOIN:** every combination.
- **SELF JOIN:** a table joined to itself (an employee and their manager).

**100. In what order does SQL run a query?**
`FROM` / `JOIN` → `WHERE` → `GROUP BY` → `HAVING` → `SELECT` → `DISTINCT` → `ORDER BY` → `LIMIT`. That is why you can't use a `SELECT` alias inside `WHERE`.

**101. `WHERE` vs `HAVING`?**
`WHERE` filters rows before grouping and can't use aggregates. `HAVING` filters groups after `GROUP BY` and can use `COUNT`, `SUM` and so on.

**102. `DELETE` vs `TRUNCATE` vs `DROP`?**
`DELETE` removes chosen rows, can have `WHERE`, and can be rolled back. `TRUNCATE` removes all rows quickly and resets auto-increment. `DROP` deletes the whole table and its structure.

**103. `UNION` vs `UNION ALL`?**
`UNION` removes duplicate rows (slower). `UNION ALL` keeps everything (faster).

**104. What is normalization? Explain 1NF, 2NF, 3NF.**
Organising tables to remove duplicate data and update problems.
- **1NF:** each column holds one value; no repeating groups.
- **2NF:** 1NF, and every non-key column depends on the whole primary key (matters for composite keys).
- **3NF:** 2NF, and non-key columns don't depend on other non-key columns (e.g. move `department_name` out of `employee` into `department`).
Denormalization deliberately adds duplication back to speed up reads, for example in reporting tables.

**105. What is an index? When should you not add one?**
A structure (usually a B-tree) that makes lookups fast, like a book's index. Every insert and update must also update it, so avoid indexes on small tables, on columns with few distinct values (e.g. gender), and on tables with heavy writes and few reads. Index columns used in `WHERE`, `JOIN` and `ORDER BY`.

**106. Clustered vs non-clustered index?**
A clustered index decides the physical order of rows; there is one per table, usually the primary key. A non-clustered index is a separate structure pointing to the rows, and a table can have many.

**107. What is a view?**
A saved query that acts like a virtual table. It simplifies complex queries and limits what users can see.

**108. Stored procedure vs function vs trigger?**
A procedure runs logic and is called with `CALL`; it can modify data and return several results. A function returns one value and can be used inside `SELECT`. A trigger runs automatically on `INSERT`, `UPDATE` or `DELETE` (e.g. writing an audit log).

**109. What are ACID properties?**
- **Atomicity:** all or nothing.
- **Consistency:** rules and constraints always hold.
- **Isolation:** concurrent transactions don't interfere.
- **Durability:** committed data survives a crash.

**110. Isolation levels and the problems they prevent?**

| Level | Dirty read | Non-repeatable read | Phantom read |
|---|---|---|---|
| READ UNCOMMITTED | possible | possible | possible |
| READ COMMITTED (PostgreSQL, Oracle default) | prevented | possible | possible |
| REPEATABLE READ (MySQL default) | prevented | prevented | possible* |
| SERIALIZABLE | prevented | prevented | prevented |

*MySQL InnoDB also prevents most phantoms at this level.

**111. `RANK` vs `DENSE_RANK` vs `ROW_NUMBER`?**
For salaries 100, 100, 90: `ROW_NUMBER` gives 1, 2, 3; `RANK` gives 1, 1, 3; `DENSE_RANK` gives 1, 1, 2.

**112. How do you handle NULL in SQL?**
`NULL` means unknown, so `= NULL` is never true: use `IS NULL` / `IS NOT NULL`. `COUNT(*)` counts all rows; `COUNT(column)` skips NULLs. Replace NULLs with `COALESCE(column, 0)`.

**113. `CHAR` vs `VARCHAR`?**
`CHAR(n)` is fixed length and padded with spaces. `VARCHAR(n)` is variable length and stores only what is needed.

**114. Subquery vs join? What is a correlated subquery?**
Joins are usually faster and clearer for combining tables. A correlated subquery references the outer query and runs once per outer row, e.g. "employees above their own department's average" (query 5 in `sql/queries.sql`).

**115. How do you find why a query is slow?**
Run `EXPLAIN` to see whether it uses an index or scans the whole table. Then add indexes, avoid `SELECT *`, avoid functions on indexed columns in `WHERE`, and paginate.

**116. How do you paginate?**
`LIMIT 20 OFFSET 40` is simple but gets slower on deep pages. Keyset pagination (`WHERE id > last_seen_id ORDER BY id LIMIT 20`) stays fast.

**117. What is SQL injection and how do you prevent it?**
An attacker puts SQL into input, e.g. `' OR '1'='1`, to change your query. Prevent it with `PreparedStatement` placeholders or JPA parameters, never string concatenation. Also validate input and give the database user only the permissions it needs.

---

## 7. SQL query questions

All of these are answered in `sql/queries.sql`, and the main ones are tested in `SqlQueriesTest`.

| Question | Query # |
|---|---|
| Second highest salary | 1 |
| Nth highest salary | 2 |
| Count and average per group, filtered with HAVING | 3 |
| Rows with no match in another table | 4 |
| Above their own group's average (correlated subquery) | 5 |
| Find duplicates | 6 |
| Top N rows | 7 |
| Date range (last 6 months) | 8 |
| Highest in each group | 9 |
| LEFT JOIN to keep unmatched rows | 10 |
| UPDATE with a subquery | 11 |
| Delete duplicates, keep one | 12 |
| Count per category with date condition | 13 |
| Group by year and month | 14 |
| Transaction | 15 |
| Self join (employee and manager) | 16 |
| Earns more than their manager | 17 |
| Running total | 18 |
| Pivot with CASE | 19 |
| Categorise with CASE | 20 |
| Average duration between two dates | 21 |
| EXISTS | 22 |
| Values shared by several rows | 23 |
| HAVING on SUM | 24 |
| LIKE pattern matching | 25 |
| UNION | 26 |
| View | 27 |
| Stored procedure | 28 |
| Count per year | 29 |
| Rank within group | 30 |

A few more that come up often:

```sql
-- Employees whose salary is above the company average
SELECT name, salary FROM employee WHERE salary > (SELECT AVG(salary) FROM employee);

-- Department with the highest total salary
SELECT d.name, SUM(e.salary) AS total
FROM department d JOIN employee e ON e.dept_id = d.id
GROUP BY d.name ORDER BY total DESC LIMIT 1;

-- Number of employees per department, including empty departments
SELECT d.name, COUNT(e.id) AS headcount
FROM department d LEFT JOIN employee e ON e.dept_id = d.id
GROUP BY d.name;

-- Odd and even id rows
SELECT * FROM employee WHERE MOD(id, 2) = 1;

-- Copy a table's structure and data
CREATE TABLE employee_backup AS SELECT * FROM employee;

-- Add a column, then a constraint
ALTER TABLE employee ADD COLUMN phone VARCHAR(20);
ALTER TABLE employee ADD CONSTRAINT uq_employee_email UNIQUE (email);

-- Names in upper case, and the first 3 letters of each
SELECT UPPER(name), SUBSTRING(name, 1, 3) FROM employee;

-- Replace NULL salaries with 0 when summing
SELECT SUM(COALESCE(salary, 0)) FROM employee;

-- Grant read-only access to a reporting user
GRANT SELECT ON hr_db.* TO 'report_user'@'%';
```

---

## 8. JPA and Hibernate

**118. JPA vs Hibernate?**
JPA is the specification (a set of interfaces and annotations). Hibernate is the most common implementation of it. Spring Data JPA sits on top and generates repositories.

**119. Entity lifecycle states?**
Transient (new, not tracked), managed (tracked: changes are saved automatically at commit), detached (was managed, session closed), removed (scheduled for deletion).

**120. What is the first-level cache?**
Within one transaction or session, Hibernate returns the same object for the same id without querying again. It is on by default. The second-level cache is shared across sessions and must be configured.

**121. Default fetch types?**
`@ManyToOne` and `@OneToOne` default to EAGER; `@OneToMany` and `@ManyToMany` default to LAZY. Set `@ManyToOne(fetch = LAZY)` explicitly, as this project does.

**122. What does `mappedBy` do?**
It marks the non-owning side of a two-way relationship. The other side holds the foreign key (`Hearing.courtCase` owns it; `CourtCase.hearings` is `mappedBy = "courtCase"`).

**123. Cascade types?**
`PERSIST`, `MERGE`, `REMOVE`, `REFRESH`, `DETACH`, `ALL`. These pass operations from parent to children. `orphanRemoval = true` deletes a child removed from the collection.

**124. `LazyInitializationException`: why does it happen?**
A lazy collection is accessed after the session is closed. Fix it by loading what you need in the service inside `@Transactional`, using `JOIN FETCH`, or returning DTOs.

**125. JPQL vs native SQL?**
JPQL queries entities and fields (`select c from CourtCase c where c.status = :s`) and works on any database. Native SQL uses table names and database-specific features. Use `@Query(nativeQuery = true)` only when needed.
