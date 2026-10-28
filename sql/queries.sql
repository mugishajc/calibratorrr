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
  manager_id INT,
  FOREIGN KEY (dept_id) REFERENCES department(id),
  FOREIGN KEY (manager_id) REFERENCES employee(id)
);

CREATE TABLE court_case (
  id       INT PRIMARY KEY,
  category VARCHAR(20),
  status    VARCHAR(20),
  filed_on  DATE,
  closed_on DATE
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

-- 16. Each employee with their manager's name (self join)
SELECT e.name AS employee, m.name AS manager
FROM employee e LEFT JOIN employee m ON m.id = e.manager_id;

-- 17. Employees who earn more than their manager
SELECT e.name, e.salary, m.name AS manager, m.salary AS manager_salary
FROM employee e JOIN employee m ON m.id = e.manager_id
WHERE e.salary > m.salary;

-- 18. Running total of salaries by hire date
SELECT name, hire_date, salary,
       SUM(salary) OVER (ORDER BY hire_date) AS running_total
FROM employee;

-- 19. Case counts per category, one column per status (pivot with CASE)
SELECT category,
       SUM(CASE WHEN status = 'FILED'  THEN 1 ELSE 0 END) AS filed,
       SUM(CASE WHEN status = 'JUDGED' THEN 1 ELSE 0 END) AS judged,
       SUM(CASE WHEN status = 'CLOSED' THEN 1 ELSE 0 END) AS closed,
       COUNT(*) AS total
FROM court_case GROUP BY category;

-- 20. Salary band for each employee
SELECT name, salary,
       CASE WHEN salary >= 1000000 THEN 'High'
            WHEN salary >= 600000  THEN 'Medium'
            ELSE 'Low' END AS band
FROM employee;

-- 21. Average number of days to close a case, per category
SELECT category, AVG(DATEDIFF(closed_on, filed_on)) AS avg_days_to_close
FROM court_case WHERE closed_on IS NOT NULL
GROUP BY category;

-- 22. Departments where at least one employee earns over 1,000,000 (EXISTS)
SELECT d.name FROM department d
WHERE EXISTS (SELECT 1 FROM employee e WHERE e.dept_id = d.id AND e.salary > 1000000);

-- 23. Employees who share a salary with someone else
SELECT name, salary FROM employee
WHERE salary IN (SELECT salary FROM employee GROUP BY salary HAVING COUNT(*) > 1);

-- 24. Departments whose total salary bill is above 2,000,000
SELECT d.name, SUM(e.salary) AS total_salary
FROM department d JOIN employee e ON e.dept_id = d.id
GROUP BY d.name HAVING SUM(e.salary) > 2000000;

-- 25. Names starting with 'A', and emails on a given domain
SELECT name FROM employee WHERE name LIKE 'A%';
SELECT name, email FROM employee WHERE email LIKE '%@minijust.gov.rw';

-- 26. UNION removes duplicates, UNION ALL keeps them
SELECT name FROM employee WHERE dept_id = 1
UNION
SELECT name FROM employee WHERE salary > 1000000;

-- 27. A view for open cases
CREATE VIEW open_cases AS
SELECT id, category, status, filed_on FROM court_case WHERE status NOT IN ('CLOSED');

-- 28. Stored procedure: count cases in a given status
DELIMITER //
CREATE PROCEDURE count_cases_by_status(IN p_status VARCHAR(20), OUT p_total INT)
BEGIN
  SELECT COUNT(*) INTO p_total FROM court_case WHERE status = p_status;
END //
DELIMITER ;
CALL count_cases_by_status('FILED', @total);
SELECT @total;

-- 29. Employees hired each year
SELECT YEAR(hire_date) AS yr, COUNT(*) AS hired
FROM employee GROUP BY YEAR(hire_date) ORDER BY yr;

-- 30. Rank employees by salary within their department
SELECT name, dept_id, salary,
       RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rank_in_dept
FROM employee;
