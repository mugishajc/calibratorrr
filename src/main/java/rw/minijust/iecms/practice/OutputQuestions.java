package rw.minijust.iecms.practice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * "What is the output?" questions. Guess each answer first, then run main() to check.
 * Answers and explanations are in docs/JAVA_QUESTIONS.md.
 */
public final class OutputQuestions {
    private OutputQuestions() {}

    static class Parent {
        static { System.out.println("   Parent static block"); }
        { System.out.println("   Parent instance block"); }
        Parent() { System.out.println("   Parent constructor"); }
        static String who() { return "Parent.who"; }
        String name() { return "Parent.name"; }
    }

    static class Child extends Parent {
        static { System.out.println("   Child static block"); }
        { System.out.println("   Child instance block"); }
        Child() { System.out.println("   Child constructor"); }
        static String who() { return "Child.who"; }
        @Override String name() { return "Child.name"; }
    }

    @SuppressWarnings("finally")
    static int tryFinally() {
        try {
            return 1;
        } finally {
            return 2;
        }
    }

    static void rename(StringBuilder sb) { sb.append(" changed"); }
    static void reassign(StringBuilder sb) { sb = new StringBuilder("new object"); }

    @SuppressWarnings({"StringEquality", "NumberEquality"})
    public static void main(String[] args) {
        String a = "hello", b = "hello", c = new String("hello");
        System.out.println("Q1  a == b: " + (a == b) + ", a == c: " + (a == c) + ", a.equals(c): " + a.equals(c));

        Integer x = 127, y = 127, p = 128, q = 128;
        System.out.println("Q2  127 == 127: " + (x == y) + ", 128 == 128: " + (p == q));

        System.out.println("Q3  1 + 2 + \"3\" + 4 + 5 = " + (1 + 2 + "3" + 4 + 5));

        int i = 5;
        i = i++ + ++i;
        System.out.println("Q4  i = i++ + ++i (i starts at 5): " + i);

        System.out.println("Q5  try { return 1 } finally { return 2 }: " + tryFinally());

        System.out.println("Q6  'A' + 1 = " + ('A' + 1) + ", (char) ('A' + 1) = " + (char) ('A' + 1));

        Parent pc = new Child();
        System.out.println("Q7  Parent ref to Child: name() = " + pc.name() + ", static who() = " + Parent.who());

        List<Integer> list = new ArrayList<>(List.of(10, 20, 30));
        list.remove(1);
        System.out.println("Q8  [10,20,30].remove(1) -> " + list);
        list.remove(Integer.valueOf(10));
        System.out.println("    then remove(Integer.valueOf(10)) -> " + list);

        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4));
        try {
            for (Integer n : nums) if (n == 2) nums.remove(n);
        } catch (java.util.ConcurrentModificationException e) {
            System.out.println("Q9  remove inside for-each -> ConcurrentModificationException");
        }
        for (Iterator<Integer> it = nums.iterator(); it.hasNext(); ) if (it.next() % 2 == 0) it.remove();
        System.out.println("    safe way with Iterator.remove() -> " + nums);

        System.out.println("Q10 0.1 + 0.2 == 0.3: " + (0.1 + 0.2 == 0.3) + " (" + (0.1 + 0.2) + "), BigDecimal: "
                + new BigDecimal("0.1").add(new BigDecimal("0.2")));

        System.out.println("Q11 Math.round(2.5) = " + Math.round(2.5) + ", Math.round(-2.5) = " + Math.round(-2.5));

        int[] arr1 = {1, 2, 3};
        int[] arr2 = arr1;
        arr2[0] = 9;
        System.out.println("Q12 arr2 = arr1; arr2[0] = 9; arr1[0] = " + arr1[0]);

        StringBuilder sb = new StringBuilder("original");
        rename(sb);
        reassign(sb);
        System.out.println("Q13 pass-by-value: " + sb);

        System.out.println("Q14 10/3 = " + (10 / 3) + ", 10%3 = " + (10 % 3) + ", 10/3.0 = " + (10 / 3.0)
                + ", -7/2 = " + (-7 / 2) + ", -7%2 = " + (-7 % 2));

        String s = null;
        try {
            System.out.println(s.length());
        } catch (NullPointerException e) {
            System.out.println("Q15 null.length() -> NullPointerException");
        }

        System.out.println("Q16 initialisation order of new Child():");
        new Child();

        String t = "Java";
        t.concat(" 21");
        System.out.println("Q17 t.concat(\" 21\") without assigning: " + t);

        System.out.println("Q18 \"abc\".compareTo(\"abd\") = " + "abc".compareTo("abd")
                + ", \"Java\".substring(1, 3) = " + "Java".substring(1, 3));
    }
}
