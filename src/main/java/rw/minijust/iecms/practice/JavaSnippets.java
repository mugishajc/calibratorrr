package rw.minijust.iecms.practice;

import java.util.*;
import java.util.stream.Collectors;

/** Common short coding questions. Run main() to see each answer. */
public final class JavaSnippets {
    private JavaSnippets() {}

    public record Staff(String name, String department, double salary) {}

    public static boolean isPalindrome(String s) {
        String c = s.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
        int i = 0, j = c.length() - 1;
        while (i < j) if (c.charAt(i++) != c.charAt(j--)) return false;
        return true;
    }

    public static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    public static int secondLargest(int[] a) {
        int first = Integer.MIN_VALUE, second = Integer.MIN_VALUE;
        for (int x : a) {
            if (x > first) { second = first; first = x; }
            else if (x > second && x != first) second = x;
        }
        return second;
    }

    public static Set<Integer> duplicates(int[] a) {
        Set<Integer> seen = new HashSet<>(), dups = new TreeSet<>();
        for (int x : a) if (!seen.add(x)) dups.add(x);
        return dups;
    }

    public static boolean isPrime(int n) {
        if (n < 2) return false;
        for (int i = 2; (long) i * i <= n; i++) if (n % i == 0) return false;
        return true;
    }

    public static long fib(int n) {
        long a = 0, b = 1;
        for (int i = 0; i < n; i++) { long t = a + b; a = b; b = t; }
        return a;
    }

    public static long factorial(int n) {
        return n <= 1 ? 1 : n * factorial(n - 1);
    }

    public static boolean isAnagram(String a, String b) {
        char[] x = a.toLowerCase().toCharArray(), y = b.toLowerCase().toCharArray();
        Arrays.sort(x);
        Arrays.sort(y);
        return Arrays.equals(x, y);
    }

    public static Map<String, Long> wordFrequency(String text) {
        return Arrays.stream(text.toLowerCase().split("\\W+"))
                .filter(w -> !w.isEmpty())
                .collect(Collectors.groupingBy(w -> w, TreeMap::new, Collectors.counting()));
    }

    // Rwandan National ID: 16 digits
    public static boolean isValidNationalId(String id) {
        return id != null && id.matches("\\d{16}");
    }

    public static Map<String, Double> averageSalaryByDepartment(List<Staff> staff) {
        return staff.stream().collect(Collectors.groupingBy(Staff::department, TreeMap::new,
                Collectors.averagingDouble(Staff::salary)));
    }

    public static List<String> topEarners(List<Staff> staff, int n) {
        return staff.stream()
                .sorted(Comparator.comparingDouble(Staff::salary).reversed())
                .limit(n).map(Staff::name).toList();
    }

    public static void fizzBuzz(int n) {
        for (int i = 1; i <= n; i++) {
            System.out.print(i % 15 == 0 ? "FizzBuzz " : i % 3 == 0 ? "Fizz " : i % 5 == 0 ? "Buzz " : i + " ");
        }
        System.out.println();
    }

    // Thread-safe lazy singleton (holder idiom)
    public static final class Config {
        private Config() {}
        private static class Holder { static final Config INSTANCE = new Config(); }
        public static Config getInstance() { return Holder.INSTANCE; }
    }

    public static void main(String[] args) {
        List<Staff> staff = List.of(
                new Staff("Alice", "IT", 900_000), new Staff("Bob", "IT", 700_000),
                new Staff("Claire", "HR", 500_000), new Staff("David", "Legal", 1_200_000));

        System.out.println("isPalindrome(\"Was it a car or a cat I saw\") = " + isPalindrome("Was it a car or a cat I saw"));
        System.out.println("reverse(\"MINIJUST\") = " + reverse("MINIJUST"));
        System.out.println("secondLargest([4, 9, 2, 9, 7]) = " + secondLargest(new int[]{4, 9, 2, 9, 7}));
        System.out.println("duplicates([1, 2, 3, 2, 4, 1]) = " + duplicates(new int[]{1, 2, 3, 2, 4, 1}));
        System.out.println("isPrime(97) = " + isPrime(97));
        System.out.println("fib(10) = " + fib(10));
        System.out.println("factorial(10) = " + factorial(10));
        System.out.println("isAnagram(\"Listen\", \"Silent\") = " + isAnagram("Listen", "Silent"));
        System.out.println("wordFrequency = " + wordFrequency("the court heard the case and the judge ruled"));
        System.out.println("isValidNationalId(\"1199080012345678\") = " + isValidNationalId("1199080012345678"));
        System.out.println("averageSalaryByDepartment = " + averageSalaryByDepartment(staff));
        System.out.println("topEarners(2) = " + topEarners(staff, 2));
        System.out.println("same singleton instance = " + (Config.getInstance() == Config.getInstance()));
        System.out.print("fizzBuzz(15) = ");
        fizzBuzz(15);
    }
}
