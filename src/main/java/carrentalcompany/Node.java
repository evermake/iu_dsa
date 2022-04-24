/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

/**
 * Class that represents node in the PriorityQueue.
 *
 * @param <K> type of the node's key (must be {@link Comparable})
 * @param <V> type of the node's value
 */
public class Node<K extends Comparable<K>, V> implements Comparable<Node<K, V>> {
  K key;
  V value;
  Node<K, V> parent;
  Node<K, V> left;
  Node<K, V> right;
  Node<K, V> firstChild;
  Node<K, V> lastChild;
  PriorityQueue<K, V> containingQueue;
  int childrenCount;
  boolean looser;
  // Boolean flag, which indicates that the key of this node is minimal
  // or, in other words, key = -∞. This field is required for implementing
  // PriorityQueue::decreaseKey method, since we do not know the exact type
  // of the key.
  boolean isMinimal;

  Node(K key, V value) {
    this.key = key;
    this.value = value;
    this.parent = null;
    this.left = this;
    this.right = this;
    this.firstChild = null;
    this.lastChild = null;
    this.childrenCount = 0;
    this.looser = false;
    this.isMinimal = false;
    this.containingQueue = null;
  }

  /**
   * Appends {@code node} into the end of this node children
   */
  public void appendChild(Node<K, V> node) {
    node.parent = this;

    if (childrenCount == 0) {
      assert firstChild == null && lastChild == null;
      node.left = node;
      node.right = node;
      firstChild = node;
      lastChild = node;
    } else {
      lastChild.insertRight(node);
    }

    assert lastChild == node;
    childrenCount++;
  }

  /**
   * Inserts {@code node} to the left of this node.
   */
  public void insertLeft(Node<K, V> node) {
    node.parent = parent;

    left.right = node;
    node.right = this;
    node.left = left;
    left = node;

    if (parent != null && this == parent.firstChild) {
      parent.firstChild = node;
    }
  }

  /**
   * Inserts {@code node} to the right of this node.
   */
  public void insertRight(Node<K, V> node) {
    node.parent = parent;

    right.left = node;
    node.left = this;
    node.right = right;
    right = node;

    if (parent != null && this == parent.lastChild) {
      parent.lastChild = node;
    }
  }

  /**
   * Sets isMinimal flag of the node to true, indicating that
   * node's key = -∞.
   */
  public void makeMinimal() {
    this.isMinimal = true;
  }

  @Override
  public int compareTo(Node<K, V> other) {
    if (isMinimal) {
      // this node is minimal -> it is the minimum
      return -1;
    } else if (other.isMinimal) {
      // comparing node is minimal -> it is the minimum
      return 1;
    } else {
      // otherwise, compare by keys
      return key.compareTo(other.key);
    }
  }
}
