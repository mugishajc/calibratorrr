-- Exam SQL practice (MySQL syntax). The same queries run on H2 in
-- src/main/java/rw/minijust/iecms/practice/JdbcSqlPractice.java

CREATE TABLE department (
  id   INT PRIMARY KEY,
  name VARCHAR(50) NOT NULL
);

CREATE TABLE employee (
  id        INT PRIMARY KEY,
  name      VARCHAR(100) NOT NULL,
  email     VARCHAR(100),
  dept_id   INT,
  salary    DECIMAL(12,2),
  hire_date DATE,
  FOREIGN KEY (dept_id) REFERENCES department(id)
);

CREATE TABLE court_case (
  id       INT PRIMARY KEY,
  category VARCHAR(20),
  status   VARCHAR(20),
  filed_on DATE
);

CREATE INDEX idx_employee_dept ON employee(dept_id);

-- 1. Second highest salary
SELECT MAX(salary) FROM employee
WHERE salary < (SELECT MAX(salary) FROM employee);

-- 2. Nth highest salary (N = 3)
SELECT DISTINCT salary FROM (
  SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk FROM employee
) t WHERE rnk = 3;

-- 3. Headcount and average salary per department, only departments with more than 5 people
SELECT d.name, COUNT(e.id) AS total, AVG(e.salary) AS avg_salary
FROM department d JOIN employee e ON e.dept_id = d.id
GROUP BY d.name
HAVING COUNT(e.id) > 5;

-- 4. Departments with no employees
SELECT d.name FROM department d
LEFT JOIN employee e ON e.dept_id = d.id
WHERE e.id IS NULL;

-- 5. Employees earning more than their department's average (correlated subquery)
SELECT e.name, e.salary FROM employee e
WHERE e.salary > (SELECT AVG(salary) FROM employee WHERE dept_id = e.dept_id);

-- 6. Duplicate emails
SELECT email, COUNT(*) FROM employee GROUP BY email HAVING COUNT(*) > 1;

-- 7. Top 3 earners (Oracle: FETCH FIRST 3 ROWS ONLY)
SELECT name, salary FROM employee ORDER BY salary DESC LIMIT 3;

-- 8. Hired in the last 6 months
SELECT * FROM employee WHERE hire_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH);

-- 9. Highest paid employee in each department
SELECT name, dept_id, salary FROM (
  SELECT name, dept_id, salary,
         ROW_NUMBER() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rn
  FROM employee
) t WHERE rn = 1;

-- 10. Employees with their department name (INNER JOIN), including those without a department (LEFT JOIN)
SELECT e.name, d.name AS department FROM employee e LEFT JOIN department d ON d.id = e.dept_id;

-- 11. 10% raise for one department
UPDATE employee SET salary = salary * 1.10
WHERE dept_id = (SELECT id FROM department WHERE name = 'IT');

-- 12. Delete duplicate emails, keeping the lowest id
DELETE e1 FROM employee e1
JOIN employee e2 ON e1.email = e2.email AND e1.id > e2.id;

-- 13. Pending court cases older than 6 months, per category
SELECT category, COUNT(*) AS pending FROM court_case
WHERE status <> 'CLOSED' AND filed_on < DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
GROUP BY category;

-- 14. Cases filed per year and month
SELECT YEAR(filed_on) AS yr, MONTH(filed_on) AS mon, COUNT(*) AS filed
FROM court_case GROUP BY YEAR(filed_on), MONTH(filed_on) ORDER BY yr, mon;

-- 15. Transaction: both updates succeed or neither does
START TRANSACTION;
UPDATE employee SET dept_id = 2 WHERE id = 1;
UPDATE department SET name = 'Human Resources' WHERE id = 2;
COMMIT;   -- or ROLLBACK;
