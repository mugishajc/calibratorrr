package rw.minijust.iecms;

import static org.junit.jupiter.api.Assertions.*;
import static rw.minijust.iecms.practice.JavaSnippets.*;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import rw.minijust.iecms.practice.JdbcSqlPractice;

class PracticeTest {
    @Test
    void snippets() {
        assertTrue(isPalindrome("Was it a car or a cat I saw"));
        assertEquals("TSUJINIM", reverse("MINIJUST"));
        assertEquals(7, secondLargest(new int[]{4, 9, 2, 9, 7}));
        assertEquals(Set.of(1, 2), duplicates(new int[]{1, 2, 3, 2, 4, 1}));
        assertTrue(isPrime(97));
        assertFalse(isPrime(1));
        assertEquals(55, fib(10));
        assertEquals(3_628_800, factorial(10));
        assertTrue(isAnagram("Listen", "Silent"));
        assertEquals(3L, wordFrequency("the court heard the case and the judge").get("the"));
        assertTrue(isValidNationalId("1199080012345678"));
        assertFalse(isValidNationalId("11990800"));
        List<Staff> staff = List.of(new Staff("A", "IT", 900), new Staff("B", "IT", 700), new Staff("C", "HR", 500));
        assertEquals(800.0, averageSalaryByDepartment(staff).get("IT"));
        assertEquals(List.of("A", "B"), topEarners(staff, 2));
    }

    @Test
    void allSqlQueriesRun() {
        assertDoesNotThrow(() -> JdbcSqlPractice.main(new String[0]));
    }
}
