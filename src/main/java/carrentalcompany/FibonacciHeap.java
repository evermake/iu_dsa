package carrentalcompany;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class FibonacciHeap<K extends Comparable<K>, V> implements PriorityQueue<K, V> {
  Node min;
  Node firstRootNode;
  Node lastRootNode;
  int size;
  Map<K, Deque<V>> valuesMap;

  public FibonacciHeap() {
    this.min = null;
    this.firstRootNode = null;
    this.lastRootNode = null;
    this.size = 0;
    this.valuesMap = new HashMap<>();
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public void insert(K key, V value) {
    Node node = new Node(key);
    appendToRootList(node);

    if (min == null || node.key.compareTo(min.key) < 0) {
      min = node;
    }

    if (valuesMap.containsKey(key)) {
      valuesMap.get(key).offer(value);
    } else {
      Deque<V> keyValues = new LinkedList<>();
      keyValues.offer(value);
      valuesMap.put(key, keyValues);
    }

    size++;
  }

  @Override
  public V findMin() {
    return (min == null) ? null : valuesMap.get(min.key).peek();
  }

  @Override
  public V extractMin() {
    if (min == null) {
      return null;
    }

    Deque<V> keyValues = valuesMap.get(min.key);
    V minValue = keyValues.pollLast();
    if (keyValues.isEmpty()) {
      valuesMap.remove(min.key);
    }

    /* Transfer min children into the root list */
    for (Node child = min.firstChild; child != null; child = min.firstChild) {
      cut(child);
      appendToRootList(child);
    }

    Node minNeighbour = (min.nextNode == null) ? min.previousNode : min.nextNode;
    removeNodeFromRootList(min);

    if (minNeighbour == null) {
      /* Min was the only node in the heap */
      assert size == 1;
      min = null;
    } else {
      min = minNeighbour;
      consolidate();
    }

    size--;

    return minValue;
  }

  @Override
  public void decreaseKey(K oldKey, K newKey) {

  }

  public void decreaseKey(Node node, K newKey) {
    assert node != null;

    if (newKey.compareTo(node.key) > 0) {
      throw new IllegalArgumentException("new key should be greater than old key");
    }

    Node parent = node.parent;
    node.key = newKey;
    if (parent != null && node.key.compareTo(parent.key) < 0) {
      cut(node);
      cascadingCut(parent);
    }

    if (node.key.compareTo(min.key) < 0) {
      min = node;
    }
  }

  @Override
  public void delete(K key) {

  }

  @Override
  public void union(PriorityQueue<K, V> other) {

  }

  public void union(FibonacciHeap<K, V> other) {
    if (lastRootNode == null) {
      assert firstRootNode == null && size == 0;
      firstRootNode = other.firstRootNode;
      lastRootNode = other.lastRootNode;
    } else if (other.firstRootNode != null) {
      assert other.lastRootNode != null && other.size > 0;
      lastRootNode.nextNode = other.firstRootNode;
      other.firstRootNode.previousNode = lastRootNode;
      lastRootNode = other.lastRootNode;
    }

    size += other.size;
  }

  /**
   * Appends a node to the end of the heap root list.
   */
  private void appendToRootList(Node node) {
    assert node.parent == null;

    if (lastRootNode == null) {
      assert firstRootNode == null && size == 0;
      firstRootNode = node;
    } else {
      lastRootNode.nextNode = node;
    }
    node.previousNode = lastRootNode;
    lastRootNode = node;
  }

  private void consolidate() {
    int maxDegree = getMaximumDegree();
    ArrayList<Node> roots = new ArrayList<>(maxDegree + 1);
    for (int i = 0; i <= maxDegree; i++) {
      roots.add(null);
    }

    LinkedList<Node> initialRoots = new LinkedList<>();

    for (
        Node initialRoot = firstRootNode;
        initialRoot != null;
        initialRoot = initialRoot.nextNode
    ) {
      assert initialRoot.parent == null;
      initialRoots.add(initialRoot);
    }

    for (Node initialRoot : initialRoots) {
      if (initialRoot.parent != null) {
        // the node has been already removed from root list
        continue;
      }

      Node x = initialRoot;
      int degree = x.childrenCount;
      while (roots.get(degree) != null) {
        Node nodeWithSameDegree = roots.get(degree);
        assert x != nodeWithSameDegree;

        Node parentNode = x;
        Node childNode = nodeWithSameDegree;
        if (x.key.compareTo(nodeWithSameDegree.key) > 0) {
          parentNode = nodeWithSameDegree;
          childNode = x;
        }

        removeNodeFromRootList(childNode);
        parentNode.appendChild(childNode);
        childNode.looser = false;

        roots.set(degree, null);
        degree++;
        x = parentNode;
      }
      roots.set(degree, x);
    }

    // Custom algorithm
    /* Update min node */
    min = null;
    for (Node root = firstRootNode; root != null; root = root.nextNode) {
      if (min == null || root.key.compareTo(min.key) < 0) {
        min = root;
      }
    }

    // Algorithm from Cormen

//    min = null;
//    for (Node root : roots) {
//      if (root == null) {
//        continue;
//      }
//
//      if (min == null) {
//        root.nextNode = null;
//        root.previousNode = null;
//        firstRootNode = root;
//        lastRootNode = root;
//        min = root;
//      } else {
//        appendToRootList(root);
//        if (root.key.compareTo(min.key) < 0) {
//          min = root;
//        }
//      }
//    }

  }

  /**
   * Returns maximum possible degree of any node in Fibonacci heap.
   * <p>
   * Maximum degree of any node D(n), where n - current size of the heap:
   * <p>
   * D(n) ≤ ⌊logᵩ(n)⌋ - where logᵩ(n) logarithm of n with base φ (golden ratio ≈ 1.61).
   *
   * <i>According to corollary 19.5 Cormen et al.</i>
   * <p>
   * Here function returns floored value of log(n) / log(1.5),
   * since log_1.5(n) ≥ logᵩn
   *
   * @return current maximum degree of any node
   */
  private int getMaximumDegree() {
    return (int) Math.floor(Math.log(size) / Math.log(1.5));
  }

  private void removeNodeFromRootList(Node node) {
    assert node.parent == null;

    if (node.previousNode == null) {
      /* Node was first */
      assert firstRootNode == node;
      firstRootNode = node.nextNode;
    } else {
      node.previousNode.nextNode = node.nextNode;
    }

    if (node.nextNode == null) {
      /* Node was last */
      assert lastRootNode == node;
      lastRootNode = node.previousNode;
    } else {
      node.nextNode.previousNode = node.previousNode;
    }

    node.previousNode = null;
    node.nextNode = null;
  }

  void cut(Node node) {
    Node parent = node.parent;
    assert parent != null;

    if (node.previousNode == null) {
      /* Node was first */
      assert parent.firstChild == node;
      parent.firstChild = node.nextNode;
    } else {
      node.previousNode.nextNode = node.nextNode;
    }

    if (node.nextNode == null) {
      /* Node was last */
      assert parent.lastChild == node;
      parent.lastChild = node.previousNode;
    } else {
      node.nextNode.previousNode = node.previousNode;
    }

    node.previousNode = null;
    node.nextNode = null;
    node.parent = null;
    node.looser = false;
    parent.childrenCount--;
  }

  void cascadingCut(Node node) {
    Node parent = node.parent;
    if (parent != null) {
      if (!parent.looser) {
        parent.looser = true;
      } else {
        cut(node);
        cascadingCut(parent);
      }
    }
  }

  private final class Node {
    K key;
    Node parent;
    Node previousNode;
    Node nextNode;
    Node firstChild;
    Node lastChild;
    int childrenCount;
    boolean looser;

    Node(K key) {
      this.key = key;
      this.parent = null;
      this.previousNode = null;
      this.nextNode = null;
      this.firstChild = null;
      this.lastChild = null;
      this.childrenCount = 0;
      this.looser = false;
    }

    private void appendChild(Node node) {
      node.parent = this;
      node.nextNode = null;

      if (lastChild == null) {
        /* Node is first child */
        assert firstChild == null && childrenCount == 0;
        node.previousNode = null;
        firstChild = node;
      } else {
        node.previousNode = lastChild;
        lastChild.nextNode = node;
      }

      lastChild = node;
      childrenCount++;
    }

    /**
     * Inserts a node to the right of this node.
     */
//    void insertRight(Node node) {
//      node.parent = parent;
//
//      if (nextNode == null) {
//        /* Node was the last */
//        parent.lastChild = node;
//      } else {
//        nextNode.previousNode = node;
//      }
//
//      node.nextNode = nextNode;
//      node.previousNode = this;
//      nextNode = node;
//
//      if (parent != null) {
//        parent.childrenCount++;
//      }
//    }
  }
}
