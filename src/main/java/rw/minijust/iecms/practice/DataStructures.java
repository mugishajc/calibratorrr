package rw.minijust.iecms.practice;

import java.util.*;

/** Data structure and algorithm questions written from scratch. Tested in DataStructuresTest. */
public final class DataStructures {
    private DataStructures() {}

    /** Stack backed by a growing array. push/pop/peek are O(1). */
    public static class ArrayStack<T> {
        private Object[] items = new Object[4];
        private int size;

        public void push(T item) {
            if (size == items.length) items = Arrays.copyOf(items, size * 2);
            items[size++] = item;
        }

        @SuppressWarnings("unchecked")
        public T pop() {
            if (isEmpty()) throw new NoSuchElementException("stack is empty");
            T item = (T) items[--size];
            items[size] = null; // let the GC reclaim it
            return item;
        }

        @SuppressWarnings("unchecked")
        public T peek() {
            if (isEmpty()) throw new NoSuchElementException("stack is empty");
            return (T) items[size - 1];
        }

        public boolean isEmpty() { return size == 0; }
        public int size() { return size; }
    }

    /** FIFO queue backed by linked nodes. enqueue/dequeue are O(1). */
    public static class LinkedQueue<T> {
        private static class Node<T> { T value; Node<T> next; Node(T v) { value = v; } }
        private Node<T> head, tail;
        private int size;

        public void enqueue(T value) {
            Node<T> n = new Node<>(value);
            if (tail == null) head = tail = n;
            else { tail.next = n; tail = n; }
            size++;
        }

        public T dequeue() {
            if (head == null) throw new NoSuchElementException("queue is empty");
            T v = head.value;
            head = head.next;
            if (head == null) tail = null;
            size--;
            return v;
        }

        public boolean isEmpty() { return size == 0; }
        public int size() { return size; }
    }

    /** Singly linked list node used by the list questions. */
    public static class ListNode {
        public int val;
        public ListNode next;
        public ListNode(int val) { this.val = val; }

        public static ListNode of(int... values) {
            ListNode dummy = new ListNode(0), cur = dummy;
            for (int v : values) { cur.next = new ListNode(v); cur = cur.next; }
            return dummy.next;
        }

        public static List<Integer> toList(ListNode head) {
            List<Integer> out = new ArrayList<>();
            for (ListNode n = head; n != null; n = n.next) out.add(n.val);
            return out;
        }
    }

    /** Reverse a linked list in place. O(n) time, O(1) space. */
    public static ListNode reverse(ListNode head) {
        ListNode prev = null;
        while (head != null) {
            ListNode next = head.next;
            head.next = prev;
            prev = head;
            head = next;
        }
        return prev;
    }

    /** Floyd's tortoise and hare: true if the list loops back on itself. */
    public static boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next;
            fast = fast.next.next;
            if (slow == fast) return true;
        }
        return false;
    }

    /** Middle node in one pass: fast moves two steps for each step of slow. */
    public static ListNode middle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        return slow;
    }

    /** Binary search on a sorted array. O(log n). Returns the index or -1. */
    public static int binarySearch(int[] sorted, int target) {
        int lo = 0, hi = sorted.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2; // avoids int overflow of (lo + hi)
            if (sorted[mid] == target) return mid;
            if (sorted[mid] < target) lo = mid + 1;
            else hi = mid - 1;
        }
        return -1;
    }

    /** Bubble sort. O(n^2), stops early when a pass makes no swaps. */
    public static void bubbleSort(int[] a) {
        for (int i = 0; i < a.length - 1; i++) {
            boolean swapped = false;
            for (int j = 0; j < a.length - 1 - i; j++) {
                if (a[j] > a[j + 1]) { int t = a[j]; a[j] = a[j + 1]; a[j + 1] = t; swapped = true; }
            }
            if (!swapped) return;
        }
    }

    /** Merge sort. O(n log n), stable, O(n) extra space. */
    public static int[] mergeSort(int[] a) {
        if (a.length <= 1) return a;
        int mid = a.length / 2;
        int[] left = mergeSort(Arrays.copyOfRange(a, 0, mid));
        int[] right = mergeSort(Arrays.copyOfRange(a, mid, a.length));
        int[] out = new int[a.length];
        int i = 0, j = 0, k = 0;
        while (i < left.length && j < right.length) out[k++] = left[i] <= right[j] ? left[i++] : right[j++];
        while (i < left.length) out[k++] = left[i++];
        while (j < right.length) out[k++] = right[j++];
        return out;
    }

    /** Balanced brackets using a stack: "{[()]}" is balanced, "([)]" is not. */
    public static boolean isBalanced(String s) {
        Deque<Character> stack = new ArrayDeque<>();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(', '[', '{' -> stack.push(c);
                case ')' -> { if (stack.isEmpty() || stack.pop() != '(') return false; }
                case ']' -> { if (stack.isEmpty() || stack.pop() != '[') return false; }
                case '}' -> { if (stack.isEmpty() || stack.pop() != '{') return false; }
                default -> { }
            }
        }
        return stack.isEmpty();
    }

    /** Two sum with a HashMap: indexes of two numbers adding up to target. O(n). */
    public static int[] twoSum(int[] nums, int target) {
        Map<Integer, Integer> seen = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            Integer j = seen.get(target - nums[i]);
            if (j != null) return new int[]{j, i};
            seen.put(nums[i], i);
        }
        return new int[0];
    }

    /** First character that appears only once, or '_' if none. */
    public static char firstUniqueChar(String s) {
        Map<Character, Integer> counts = new LinkedHashMap<>();
        for (char c : s.toCharArray()) counts.merge(c, 1, Integer::sum);
        return counts.entrySet().stream().filter(e -> e.getValue() == 1)
                .map(Map.Entry::getKey).findFirst().orElse('_');
    }

    /** Least-recently-used cache: LinkedHashMap in access order evicts the oldest entry. */
    public static class LruCache<K, V> extends LinkedHashMap<K, V> {
        private final int capacity;

        public LruCache(int capacity) {
            super(16, 0.75f, true);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
            return size() > capacity;
        }
    }

    /** Binary search tree: insert and contains are O(log n) when balanced, O(n) worst case. */
    public static class Bst {
        private record Node(int value, Node left, Node right) {}
        private Node root;

        public void insert(int v) { root = insert(root, v); }

        private Node insert(Node n, int v) {
            if (n == null) return new Node(v, null, null);
            if (v < n.value) return new Node(n.value, insert(n.left, v), n.right);
            if (v > n.value) return new Node(n.value, n.left, insert(n.right, v));
            return n; // ignore duplicates
        }

        public boolean contains(int v) {
            Node n = root;
            while (n != null) {
                if (v == n.value) return true;
                n = v < n.value ? n.left : n.right;
            }
            return false;
        }

        /** In-order traversal (left, root, right) of a BST gives sorted values. */
        public List<Integer> inOrder() {
            List<Integer> out = new ArrayList<>();
            inOrder(root, out);
            return out;
        }

        private void inOrder(Node n, List<Integer> out) {
            if (n == null) return;
            inOrder(n.left, out);
            out.add(n.value);
            inOrder(n.right, out);
        }

        public int height() { return height(root); }

        private int height(Node n) {
            return n == null ? 0 : 1 + Math.max(height(n.left), height(n.right));
        }
    }

    /** Breadth-first search: fewest hops between two nodes of an unweighted graph, or -1. */
    public static int shortestHops(Map<String, List<String>> graph, String from, String to) {
        Map<String, Integer> dist = new HashMap<>(Map.of(from, 0));
        Deque<String> queue = new ArrayDeque<>(List.of(from));
        while (!queue.isEmpty()) {
            String node = queue.poll();
            if (node.equals(to)) return dist.get(node);
            for (String next : graph.getOrDefault(node, List.of())) {
                if (!dist.containsKey(next)) {
                    dist.put(next, dist.get(node) + 1);
                    queue.add(next);
                }
            }
        }
        return -1;
    }
}
