/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

public interface PriorityQueue<K extends Comparable<K>, V> {
  void insert(Node<K, V> node);
  Node<K, V> findMin();
  Node<K, V> extractMin();
  boolean isEmpty();
  void decreaseKey(Node<K, V> node, K newKey);
  void delete(Node<K, V> node);
  void union(FibonacciHeap<K, V> other);
}
