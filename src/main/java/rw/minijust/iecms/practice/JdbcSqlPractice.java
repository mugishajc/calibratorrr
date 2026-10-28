package rw.minijust.iecms.practice;

import java.sql.*;

/**
 * Plain JDBC (no Spring) against an in-memory H2 database.
 * Creates sample tables, then runs the common exam SQL queries and prints the results.
 */
public final class JdbcSqlPractice {
    private JdbcSqlPractice() {}

    static final String URL = "jdbc:h2:mem:practice;DB_CLOSE_DELAY=-1";

    static final String[][] QUERIES = {
        {"Second highest salary",
         "SELECT MAX(salary) AS second_highest FROM employee WHERE salary < (SELECT MAX(salary) FROM employee)"},
        {"3rd highest salary (window function)",
         "SELECT DISTINCT salary FROM (SELECT salary, DENSE_RANK() OVER (ORDER BY salary DESC) AS rnk FROM employee) t WHERE rnk = 3"},
        {"Headcount and average salary per department (more than 1 person)",
         "SELECT d.name, COUNT(e.id) AS total, AVG(e.salary) AS avg_salary FROM department d "
         + "JOIN employee e ON e.dept_id = d.id GROUP BY d.name HAVING COUNT(e.id) > 1 ORDER BY d.name"},
        {"Departments with no employees",
         "SELECT d.name FROM department d LEFT JOIN employee e ON e.dept_id = d.id WHERE e.id IS NULL"},
        {"Employees earning more than their department's average",
         "SELECT e.name, e.salary FROM employee e WHERE e.salary > (SELECT AVG(salary) FROM employee WHERE dept_id = e.dept_id)"},
        {"Duplicate emails",
         "SELECT email, COUNT(*) AS times FROM employee GROUP BY email HAVING COUNT(*) > 1"},
        {"Top 3 earners",
         "SELECT name, salary FROM employee ORDER BY salary DESC LIMIT 3"},
        // MySQL: hire_date >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH)
        {"Hired in the last 6 months",
         "SELECT name, hire_date FROM employee WHERE hire_date >= DATEADD('MONTH', -6, CURRENT_DATE)"},
        {"Pending court cases older than 6 months, per category",
         "SELECT category, COUNT(*) AS pending FROM court_case WHERE status <> 'CLOSED' "
         + "AND filed_on < DATEADD('MONTH', -6, CURRENT_DATE) GROUP BY category ORDER BY category"},
    };

    public static void main(String[] args) throws SQLException {
        try (Connection con = DriverManager.getConnection(URL, "sa", "")) {
            createSchema(con);
            for (String[] q : QUERIES) {
                System.out.println("\n== " + q[0] + "\n   " + q[1]);
                print(con, q[1]);
            }

            System.out.println("\n== PreparedStatement with a parameter (prevents SQL injection)");
            findEarningAbove(con, 800_000);

            System.out.println("\n== Transaction: 10% raise for IT, commit or rollback together");
            raiseSalary(con, "IT", 1.10);
            print(con, "SELECT name, salary FROM employee WHERE dept_id = 1");
        }
    }

    static void createSchema(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) {
            st.execute("DROP ALL OBJECTS");
            st.execute("CREATE TABLE department (id INT PRIMARY KEY, name VARCHAR(50) NOT NULL)");
            st.execute("CREATE TABLE employee (id INT PRIMARY KEY, name VARCHAR(100) NOT NULL, email VARCHAR(100), "
                    + "dept_id INT REFERENCES department(id), salary DECIMAL(12,2), hire_date DATE)");
            st.execute("CREATE TABLE court_case (id INT PRIMARY KEY, category VARCHAR(20), status VARCHAR(20), filed_on DATE)");
            st.execute("INSERT INTO department VALUES (1,'IT'),(2,'HR'),(3,'Legal'),(4,'Finance')");
            st.execute("INSERT INTO employee VALUES "
                    + "(1,'Alice','alice@minijust.gov.rw',1,900000,DATEADD('MONTH',-2,CURRENT_DATE)),"
                    + "(2,'Bob','bob@minijust.gov.rw',1,700000,DATEADD('YEAR',-3,CURRENT_DATE)),"
                    + "(3,'Claire','claire@minijust.gov.rw',2,500000,DATEADD('YEAR',-1,CURRENT_DATE)),"
                    + "(4,'David','david@minijust.gov.rw',3,1200000,DATEADD('MONTH',-1,CURRENT_DATE)),"
                    + "(5,'Eric','alice@minijust.gov.rw',3,650000,DATEADD('YEAR',-5,CURRENT_DATE)),"
                    + "(6,'Fiona','fiona@minijust.gov.rw',2,550000,DATEADD('MONTH',-8,CURRENT_DATE))");
            st.execute("INSERT INTO court_case VALUES "
                    + "(1,'CIVIL','FILED',DATEADD('YEAR',-1,CURRENT_DATE)),"
                    + "(2,'CIVIL','SCHEDULED',DATEADD('MONTH',-9,CURRENT_DATE)),"
                    + "(3,'CRIMINAL','IN_HEARING',DATEADD('MONTH',-7,CURRENT_DATE)),"
                    + "(4,'CRIMINAL','CLOSED',DATEADD('YEAR',-2,CURRENT_DATE)),"
                    + "(5,'LABOUR','FILED',DATEADD('MONTH',-1,CURRENT_DATE))");
        }
    }

    static void findEarningAbove(Connection con, double amount) throws SQLException {
        String sql = "SELECT id, name, salary FROM employee WHERE salary > ? ORDER BY salary DESC";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, amount);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.println("   " + rs.getInt("id") + " " + rs.getString("name") + " " + rs.getBigDecimal("salary"));
                }
            }
        }
    }

    static void raiseSalary(Connection con, String department, double factor) throws SQLException {
        String sql = "UPDATE employee SET salary = salary * ? WHERE dept_id = (SELECT id FROM department WHERE name = ?)";
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDouble(1, factor);
            ps.setString(2, department);
            int rows = ps.executeUpdate();
            con.commit();
            System.out.println("   updated " + rows + " rows");
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    static void print(Connection con, String sql) throws SQLException {
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            boolean any = false;
            while (rs.next()) {
                any = true;
                StringBuilder row = new StringBuilder("   ");
                for (int i = 1; i <= md.getColumnCount(); i++) {
                    row.append(md.getColumnLabel(i).toLowerCase()).append('=').append(rs.getObject(i)).append("  ");
                }
                System.out.println(row);
            }
            if (!any) System.out.println("   (no rows)");
        }
    }
}
