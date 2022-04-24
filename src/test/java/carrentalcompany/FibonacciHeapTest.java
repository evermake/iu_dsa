package carrentalcompany;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import common.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class FibonacciHeapTest {
  @RepeatedTest(100)
  void testOnUniqueShuffledKeysAndValuesWithInsertionsAndExtractionsInRandomOrder() {
    PriorityQueue<Integer, String> pq = new FibonacciHeap<>();
    java.util.PriorityQueue<Item<Integer, String>> stdPq = new java.util.PriorityQueue<>();

    List<Item<Integer, String>> items = new ArrayList<>();

    int currentKey = Utils.getRandomInteger(-1000000, 100000);
    int currentValue = Utils.getRandomInteger(-1000000, 100000);
    for (int i = 0; i < 1000; i++) {
      items.add(new Item<>(currentKey, Integer.toString(currentValue)));
      currentKey += Utils.getRandomInteger(1, 1000);
      currentValue += Utils.getRandomInteger(1, 1000);
    }

    Collections.shuffle(items);

    int insertedCounter = 0;

    while (insertedCounter < 1000) {
      int itemsToInsert = Utils.getRandomInteger(
          1, Math.min(100, 1000 - insertedCounter)
      );

      for (int i = 0; i < itemsToInsert; i++) {
        Item<Integer, String> item = items.get(i);
        pq.insert(new Node<>(item.key, item.value));
        stdPq.add(item);
      }

      insertedCounter += itemsToInsert;

      int itemsToExtract = Utils.getRandomInteger(1, stdPq.size());

      for (int i = 0; i < itemsToExtract; i++) {
        Item<Integer, String> item = stdPq.poll();
        assert item != null;
        assertEquals(item.value, pq.extractMin().value);
      }
    }

    int itemsToExtract = stdPq.size();

    for (int i = 0; i < itemsToExtract; i++) {
      Item<Integer, String> item = stdPq.poll();
      assert item != null;
      assertEquals(item.value, pq.extractMin().value);
    }

    assertTrue(stdPq.isEmpty());
  }

  @Test
  void testUnion() {
    FibonacciHeap<Integer, String> heap1 = new FibonacciHeap<>();
    FibonacciHeap<Integer, String> heap2 = new FibonacciHeap<>();

    heap1.insert(new Node<>(10, "A"));
    heap1.insert(new Node<>(-100, "B"));
    heap1.insert(new Node<>(0, "C"));
    heap1.insert(new Node<>(1234, "D"));

    heap2.insert(new Node<>(-14, "E"));
    heap2.insert(new Node<>(50, "F"));
    heap2.insert(new Node<>(-800, "G"));

    assertEquals(4, heap1.size);
    heap1.union(heap2);
    assertEquals(7, heap1.size);

    assertEquals("G", heap1.extractMin().value);
    assertEquals("B", heap1.extractMin().value);
    assertEquals("E", heap1.extractMin().value);
    assertEquals("C", heap1.extractMin().value);
    assertEquals("A", heap1.extractMin().value);
    assertEquals("F", heap1.extractMin().value);
    assertEquals("D", heap1.extractMin().value);
    assertTrue(heap1.isEmpty());
  }

  @Test
  void testDecreaseKey() {
    FibonacciHeap<Integer, String> heap1 = new FibonacciHeap<>();

    Node<Integer, String> nodeA = new Node<>(1234, "A");
    Node<Integer, String> nodeB = new Node<>(-500, "B");
    Node<Integer, String> nodeC = new Node<>(12512423, "C");
    Node<Integer, String> nodeD = new Node<>(10000, "D");

    heap1.insert(nodeA);
    heap1.insert(nodeB);
    heap1.insert(nodeC);
    heap1.insert(nodeD);

    heap1.decreaseKey(nodeD, -450);
    assertEquals("B", heap1.extractMin().value);
    assertEquals("D", heap1.extractMin().value);
    heap1.decreaseKey(nodeC, 1233);
    assertEquals("C", heap1.extractMin().value);
    assertEquals("A", heap1.extractMin().value);
  }

  @Test
  void testDelete() {
    FibonacciHeap<Integer, String> heap1 = new FibonacciHeap<>();

    Node<Integer, String> nodeA = new Node<>(1234, "A");
    Node<Integer, String> nodeB = new Node<>(-500, "B");
    Node<Integer, String> nodeC = new Node<>(12512423, "C");
    Node<Integer, String> nodeD = new Node<>(10000, "D");
    Node<Integer, String> nodeE = new Node<>(-1, "E");

    heap1.insert(nodeA);
    heap1.insert(nodeB);
    heap1.insert(nodeC);
    heap1.insert(nodeD);
    heap1.insert(nodeE);

    heap1.delete(nodeB);
    assertEquals("E", heap1.extractMin().value);
    assertEquals("A", heap1.extractMin().value);
    heap1.delete(nodeD);
    assertEquals("C", heap1.extractMin().value);
    assertTrue(heap1.isEmpty());
  }

  private static class Item<K extends Comparable<K>, V> implements
      Comparable<Item<K, V>> {
    K key;
    V value;

    public Item(K key, V value) {
      this.key = key;
      this.value = value;
    }

    @Override
    public int compareTo(Item<K, V> other) {
      return key.compareTo(other.key);
    }
  }
}
