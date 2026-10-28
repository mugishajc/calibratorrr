package rw.minijust.iecms.practice;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Checks that each practice query returns the expected answer on the sample data. */
class SqlQueriesTest {
    Connection con;

    @BeforeEach
    void setUp() throws SQLException {
        con = DriverManager.getConnection(JdbcSqlPractice.URL, "sa", "");
        JdbcSqlPractice.createSchema(con);
    }

    @AfterEach
    void tearDown() throws SQLException {
        con.close();
    }

    private List<String> firstColumn(String title) throws SQLException {
        String sql = Arrays.stream(JdbcSqlPractice.QUERIES).filter(q -> q[0].equals(title))
                .findFirst().orElseThrow()[1];
        List<String> values = new ArrayList<>();
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) values.add(rs.getString(1));
        }
        return values;
    }

    @Test
    void secondHighestSalary() throws SQLException {
        assertThat(firstColumn("Second highest salary")).containsExactly("900000.00");
    }

    @Test
    void thirdHighestSalary() throws SQLException {
        assertThat(firstColumn("3rd highest salary (window function)")).containsExactly("700000.00");
    }

    @Test
    void departmentWithoutEmployees() throws SQLException {
        assertThat(firstColumn("Departments with no employees")).containsExactly("Finance");
    }

    @Test
    void earningAboveDepartmentAverage() throws SQLException {
        assertThat(firstColumn("Employees earning more than their department's average"))
                .containsExactlyInAnyOrder("Alice", "Fiona", "David");
    }

    @Test
    void duplicateEmails() throws SQLException {
        assertThat(firstColumn("Duplicate emails")).containsExactly("alice@minijust.gov.rw");
    }

    @Test
    void topThreeEarners() throws SQLException {
        assertThat(firstColumn("Top 3 earners")).containsExactly("David", "Alice", "Bob");
    }

    @Test
    void recentHires() throws SQLException {
        assertThat(firstColumn("Hired in the last 6 months")).containsExactlyInAnyOrder("Alice", "David");
    }

    @Test
    void pendingOldCasesPerCategory() throws SQLException {
        assertThat(firstColumn("Pending court cases older than 6 months, per category"))
                .containsExactly("CIVIL", "CRIMINAL");
    }

    @Test
    void raiseIsAppliedInOneTransaction() throws SQLException {
        JdbcSqlPractice.raiseSalary(con, "IT", 1.10);
        try (Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT SUM(salary) FROM employee WHERE dept_id = 1")) {
            rs.next();
            assertThat(rs.getBigDecimal(1)).isEqualByComparingTo("1760000");
        }
    }
}
