/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Implementation of the min-{@link PriorityQueue} based on {@code FibonacciHeap}.
 *
 * @param <K> type of the key (must be {@link Comparable})
 * @param <V> type of the value
 */
public class FibonacciHeap<K extends Comparable<K>, V> implements PriorityQueue<K, V> {
  Node<K, V> min;
  Node<K, V> firstRootNode;
  Node<K, V> lastRootNode;
  int size;

  public FibonacciHeap() {
    this.min = null;
    this.firstRootNode = null;
    this.lastRootNode = null;
    this.size = 0;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public void insert(Node<K, V> node) {
    node.containingQueue = this;
    insertIntoRootList(node);

    if (min == null || node.compareTo(min) < 0) {
      min = node;
    }

    size++;
  }

  @Override
  public Node<K, V> findMin() {
    return (min == null) ? null : min;
  }

  @Override
  public Node<K, V> extractMin() {
    if (min == null) {
      return null;
    }

    Node<K, V> minNode = min;

    /* Transfer min children into the root list */
    for (Node<K, V> child = min.firstChild; child != null; child = min.firstChild) {
      cutAndInsertIntoRootList(child);
    }

    removeNodeFromRootList(min);

    if (min.right == min) {
      /* Min was the only node in the heap */
      assert size == 1;
      min = null;
    } else {
      min = min.right;
      consolidate();
    }

    minNode.containingQueue = null;
    size--;

    return minNode;
  }

  @Override
  public void decreaseKey(Node<K, V> node, K newKey) {
    assert node != null;

    if (newKey == null) {
      /* If newKey equals null it means node key should be -∞ */
      node.makeMinimal();
    } else {
      if (newKey.compareTo(node.key) > 0) {
        throw new IllegalArgumentException("new key should be greater than old key");
      }

      node.key = newKey;
    }

    Node<K, V> parent = node.parent;
    if (parent != null && node.compareTo(parent) < 0) {
      cutAndInsertIntoRootList(node);
      cascadingCut(parent);
    }

    if (node.compareTo(min) < 0) {
      min = node;
    }
  }

  @Override
  public void delete(Node<K, V> node) {
    if (node != null) {
      decreaseKey(node, null);
      extractMin();
    }
  }

  public void union(FibonacciHeap<K, V> other) {
    if (lastRootNode == null) {
      assert firstRootNode == null && size == 0;

      firstRootNode = other.firstRootNode;
      lastRootNode = other.lastRootNode;
      min = other.min;
    } else if (other.firstRootNode != null) {
      assert other.lastRootNode != null && other.size > 0;

      lastRootNode.right = other.firstRootNode;
      other.firstRootNode.left = lastRootNode;
      other.lastRootNode.right = firstRootNode;
      firstRootNode.left = other.lastRootNode;
      lastRootNode = other.lastRootNode;

      if (other.min.compareTo(min) < 0) {
        min = other.min;
      }
    }

    size += other.size;
  }

  public boolean contains(Node<K, V> node) {
    return node.containingQueue == this;
  }

  private void cutAndInsertIntoRootList(Node<K, V> node) {
    assert node.parent != null;

    if (node.right == node) {
      /* Node is the single child of its parent */
      assert node.left == node;
      assert node.parent.childrenCount == 1;
      assert node.parent.firstChild == node;
      assert node.parent.lastChild == node;
      node.parent.firstChild = null;
      node.parent.lastChild = null;
    } else {
      node.right.left = node.left;
      node.left.right = node.right;

      if (node.parent.firstChild == node) {
        node.parent.firstChild = node.right;
      }
      if (node.parent.lastChild == node) {
        node.parent.lastChild = node.left;
      }
    }

    node.parent.childrenCount--;

    node.left = null;
    node.right = null;
    node.parent = null;
    node.looser = false;

    insertIntoRootList(node);
  }

  private void cascadingCut(Node<K, V> node) {
    Node<K, V> parent = node.parent;
    if (parent != null) {
      if (!node.looser) {
        node.looser = true;
      } else {
        cutAndInsertIntoRootList(node);
        cascadingCut(parent);
      }
    }
  }

  private void insertIntoRootList(Node<K, V> node) {
    assert node.parent == null;

    if (min == null) {
      /* Node is the first node in the tree */
      firstRootNode = node;
      lastRootNode = node;
      node.left = node;
      node.right = node;
    } else {
      min.insertLeft(node);
      if (min == firstRootNode) {
        firstRootNode = node;
      }
    }
  }

  private void consolidate() {
    assert min != null;

    int maxDegree = getMaximumDegree();
    ArrayList<Node<K, V>> roots = new ArrayList<>(maxDegree + 1);
    for (int i = 0; i <= maxDegree; i++) {
      roots.add(null);
    }

    // Save root nodes for simpler iterating further
    LinkedList<Node<K, V>> initialRoots = new LinkedList<>();
    Node<K, V> initialRoot = min;
    do {
      assert initialRoot.parent == null;
      initialRoots.add(initialRoot);
      initialRoot = initialRoot.right;
    } while (initialRoot != min);

    for (Node<K, V> x : initialRoots) {
      int degree = x.childrenCount;
      while (roots.get(degree) != null) {
        Node<K, V> nodeWithSameDegree = roots.get(degree);
        assert x != nodeWithSameDegree;

        Node<K, V> parentNode = x;
        Node<K, V> childNode = nodeWithSameDegree;
        if (x.compareTo(nodeWithSameDegree) > 0) {
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

    // Insert remaining nodes into root list and update min
    min = null;
    for (Node<K, V> root : roots) {
      if (root != null) {
        if (min == null) {
          firstRootNode = root;
          lastRootNode = root;
          root.right = root;
          root.left = root;
          min = root;
        } else {
          insertIntoRootList(root);
          if (root.compareTo(min) < 0) {
            min = root;
          }
        }
      }
    }
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

  /**
   * Removes node by updating its neighbours pointers.
   */
  private void removeNodeFromRootList(Node<K, V> node) {
    assert node.parent == null;
    node.left.right = node.right;
    node.right.left = node.left;
  }
}
