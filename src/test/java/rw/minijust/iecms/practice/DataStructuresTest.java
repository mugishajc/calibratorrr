package rw.minijust.iecms.practice;

import static org.assertj.core.api.Assertions.*;
import static rw.minijust.iecms.practice.DataStructures.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DataStructuresTest {

    @Test
    void stackIsLastInFirstOutAndGrows() {
        ArrayStack<Integer> s = new ArrayStack<>();
        for (int i = 1; i <= 10; i++) s.push(i);
        assertThat(s.size()).isEqualTo(10);
        assertThat(s.peek()).isEqualTo(10);
        assertThat(s.pop()).isEqualTo(10);
        assertThat(s.pop()).isEqualTo(9);
        assertThatThrownBy(() -> new ArrayStack<>().pop()).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void queueIsFirstInFirstOut() {
        LinkedQueue<String> q = new LinkedQueue<>();
        q.enqueue("a");
        q.enqueue("b");
        q.enqueue("c");
        assertThat(q.dequeue()).isEqualTo("a");
        assertThat(q.dequeue()).isEqualTo("b");
        assertThat(q.size()).isEqualTo(1);
        q.dequeue();
        assertThat(q.isEmpty()).isTrue();
        assertThatThrownBy(q::dequeue).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    void linkedListQuestions() {
        assertThat(ListNode.toList(reverse(ListNode.of(1, 2, 3, 4)))).containsExactly(4, 3, 2, 1);
        assertThat(reverse(null)).isNull();
        assertThat(middle(ListNode.of(1, 2, 3, 4, 5)).val).isEqualTo(3);

        ListNode looped = ListNode.of(1, 2, 3, 4);
        looped.next.next.next.next = looped.next; // 4 -> 2
        assertThat(hasCycle(looped)).isTrue();
        assertThat(hasCycle(ListNode.of(1, 2, 3))).isFalse();
    }

    @Test
    void binarySearchFindsIndexOrMinusOne() {
        int[] sorted = {2, 5, 8, 12, 16, 23, 38};
        assertThat(binarySearch(sorted, 23)).isEqualTo(5);
        assertThat(binarySearch(sorted, 2)).isEqualTo(0);
        assertThat(binarySearch(sorted, 7)).isEqualTo(-1);
        assertThat(binarySearch(new int[0], 1)).isEqualTo(-1);
    }

    @Test
    void sortingAlgorithms() {
        int[] a = {5, 1, 4, 2, 8, 2};
        bubbleSort(a);
        assertThat(a).containsExactly(1, 2, 2, 4, 5, 8);
        assertThat(mergeSort(new int[]{38, 27, 43, 3, 9, 82, 10})).containsExactly(3, 9, 10, 27, 38, 43, 82);
    }

    @ParameterizedTest
    @CsvSource({"'{[()]}', true", "'([)]', false", "'((', false", "'', true", "'a(b)c', true"})
    void balancedBrackets(String input, boolean expected) {
        assertThat(isBalanced(input)).isEqualTo(expected);
    }

    @Test
    void hashMapQuestions() {
        assertThat(twoSum(new int[]{2, 7, 11, 15}, 9)).containsExactly(0, 1);
        assertThat(twoSum(new int[]{1, 2}, 10)).isEmpty();
        assertThat(firstUniqueChar("swiss")).isEqualTo('w');
        assertThat(firstUniqueChar("aabb")).isEqualTo('_');
    }

    @Test
    void lruCacheEvictsLeastRecentlyUsed() {
        LruCache<String, Integer> cache = new LruCache<>(2);
        cache.put("a", 1);
        cache.put("b", 2);
        cache.get("a");     // a is now most recently used
        cache.put("c", 3);  // evicts b
        assertThat(cache).containsOnlyKeys("a", "c");
    }

    @Test
    void binarySearchTree() {
        Bst tree = new Bst();
        for (int v : new int[]{50, 30, 70, 20, 40, 60, 80, 30}) tree.insert(v);
        assertThat(tree.inOrder()).containsExactly(20, 30, 40, 50, 60, 70, 80);
        assertThat(tree.contains(60)).isTrue();
        assertThat(tree.contains(65)).isFalse();
        assertThat(tree.height()).isEqualTo(3);
    }

    @Test
    void breadthFirstSearchShortestHops() {
        Map<String, List<String>> courts = Map.of(
                "Kigali", List.of("Muhanga", "Rwamagana"),
                "Muhanga", List.of("Huye", "Kigali"),
                "Rwamagana", List.of("Nyagatare"),
                "Huye", List.of("Rusizi"));
        assertThat(shortestHops(courts, "Kigali", "Rusizi")).isEqualTo(3);
        assertThat(shortestHops(courts, "Kigali", "Kigali")).isZero();
        assertThat(shortestHops(courts, "Rusizi", "Kigali")).isEqualTo(-1);
    }
}
