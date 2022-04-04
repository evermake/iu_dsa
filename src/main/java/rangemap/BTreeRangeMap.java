package rangemap;

import java.util.LinkedList;
import java.util.List;

public class BTreeRangeMap<K extends Comparable<K>, V> implements RangeMap<K, V> {
  private final int t = 4;
  private Node root;
  private int size;

  public BTreeRangeMap() {
    this.size = 0;
    this.root = new Node(null, true);
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public void add(K key, V value) {
    Bucket existingBucket = root.lookupBucket(key);

    if (existingBucket != null) {
      existingBucket.addValueLast(value);
    } else {
      Bucket newBucket = new Bucket(key, value);

      if (root.isFull()) {
        /* Split root */
        Node newRoot = new Node(null, false);
        Node oldRoot = root;
        oldRoot.parent = newRoot;

        newRoot.firstNode = root;
        newRoot.lastNode = root;
        newRoot.childrenCount = 1;

        oldRoot.split();

        root = newRoot;
      }

      root.insertNonFull(newBucket);
    }

    size++;
  }

  @Override
  public V remove(K key) {
    return null;
  }

  @Override
  public boolean contains(K key) {
    return root.lookupBucket(key) != null;
  }

  @Override
  public V lookup(K key) {
    Value value = root.lookupValue(key);
    if (value != null) {
      return value.value;
    }
    return null;
  }

  @Override
  public List<V> lookupRange(K from, K to) {
    List<V> lookupResult = new LinkedList<>();

    for (
        Value value = root.lookupValue(from);
        value != null && value.parent.key.compareTo(to) <= 0;
        value = value.getSuccessor()
    ) {
      lookupResult.add(value.value);
    }

    return lookupResult;
  }

  private final class Node {
    Node parent;
    boolean leaf;
    int childrenCount;
    int bucketsCount;
    Node previousNode;
    Bucket previousBucket;
    Node nextNode;
    Bucket nextBucket;
    Node firstNode;
    Bucket firstBucket;
    Node lastNode;
    Bucket lastBucket;

    public Node(Node parent, boolean leaf) {
      this.parent = parent;
      this.leaf = leaf;
      this.childrenCount = 0;
      this.bucketsCount = 0;
      this.previousNode = null;
      this.previousBucket = null;
      this.nextNode = null;
      this.nextBucket = null;
      this.firstNode = null;
      this.firstBucket = null;
      this.lastNode = null;
      this.lastBucket = null;
    }

    Value lookupValue(K key) {
      Bucket bucket = lookupBucket(key);
      if (bucket != null) {
        return bucket.firstValue;
      }
      return null;
    }

    Bucket lookupBucket(K key) {
      Bucket bucket = firstBucket;
      while (bucket != null && key.compareTo(bucket.key) > 0) {
        bucket = bucket.nextBucket;
      }

      if (bucket != null && key.compareTo(bucket.key) == 0) {
        /* Bucket is found */
        return bucket;
      }

      /* Bucket in the current node is not found */

      if (leaf) {
        return null;
      }

      // Search in the subtree
      Node childToLookup = bucket == null ? lastNode : bucket.previousNode;
      return childToLookup.lookupBucket(key);
    }

    void split() {
      assert isFull();

      Bucket medianBucket = getMedianBucket();
      assert medianBucket.parent == this;

      // Save nodes and buckets around the median bucket
      // ... [B] (N) [M] (N) [B] ...
      //             ^^^^
      Node preMedianNode = medianBucket.previousNode;
      Bucket preMedianBucket = medianBucket.previousBucket;
      Node postMedianNode = medianBucket.nextNode;
      Bucket postMedianBucket = medianBucket.nextBucket;

      assert preMedianBucket != null;
      assert postMedianBucket != null;

      // Add median bucket to the right of this node.
      // It will be added after the pointer to this node in the parent
      // and newly created node (the pointer to which will
      // located be after this added bucket) will be returned
      // ... (N) [M] (N) ...
      //    this    newNode
      //      |       -
      //     ...
      Node newNode = addBucketToTheRight(medianBucket);

      // "Transfer" right part of buckets and children lists
      // to the newly created node
      newNode.firstBucket = postMedianBucket;
      newNode.lastBucket = lastBucket;
      lastBucket = preMedianBucket;
      postMedianBucket.previousBucket = null;
      preMedianBucket.nextBucket = null;

      if (leaf) {
        /* do not transfer children, since leaf has no children */
        assert preMedianNode == null;
        assert postMedianNode == null;
        assert newNode.leaf;
      } else {
        newNode.firstNode = postMedianNode;
        newNode.lastNode = lastNode;

        lastNode = preMedianNode;

        preMedianNode.nextBucket = null;
        preMedianNode.nextNode = null;
        postMedianNode.previousNode = null;
        postMedianNode.previousBucket = null;

        childrenCount = t;
        newNode.childrenCount = t;
      }

      // Update sizes
      newNode.bucketsCount = t - 1;
      bucketsCount = t - 1;

      newNode.updateChildrenAndBucketsParent();
    }

    boolean isFull() {
      return bucketsCount >= (2 * t) - 1;
    }

    boolean nonEmpty() {
      return bucketsCount > 0;
    }

    Bucket getMedianBucket() {
      int i = 0;
      Bucket bucket = firstBucket;
      while (i < t - 1 && bucket != null) {
        bucket = bucket.nextBucket;
        i++;
      }
      return bucket;
    }

    /**
     * TODO: write docstring
     *
     * @param bucket bucket to add
     *
     * @return newly created node that will be the next node of the added bucket
     */
    Node addBucketToTheRight(Bucket bucket) {
      assert !parent.leaf;

      Node newNode = new Node(parent, this.leaf);
      bucket.parent = parent;

      Node oldNextNode = nextNode;
      Bucket oldNextBucket = nextBucket;

      nextNode = newNode;
      nextBucket = bucket;
      newNode.previousNode = this;
      newNode.previousBucket = bucket;
      newNode.nextNode = oldNextNode;
      newNode.nextBucket = oldNextBucket;

      bucket.previousNode = this;
      bucket.previousBucket = previousBucket;
      bucket.nextNode = newNode;
      bucket.nextBucket = oldNextBucket;

      if (oldNextNode == null) {
        /* Node was last node */
        assert oldNextBucket == null;

        parent.lastNode = newNode;
        parent.lastBucket = bucket;
      } else {
        oldNextNode.previousNode = newNode;
        // oldNextNode.previousBucket does not change
        oldNextBucket.previousNode = newNode;
        oldNextBucket.previousBucket = bucket;
      }

      if (previousBucket == null) {
        /* Node is first node */
        assert previousNode == null;
        assert parent.firstNode == this;
        parent.firstBucket = bucket;
      } else {
        previousBucket.nextBucket = bucket;
      }

      parent.childrenCount++;
      parent.bucketsCount++;

      return newNode;
    }

    void updateChildrenAndBucketsParent() {
      for (Node child = firstNode; child != null; child = child.nextNode) {
        child.parent = this;
      }
      for (Bucket bucket = firstBucket; bucket != null; bucket = bucket.nextBucket) {
        bucket.parent = this;
      }
    }

    void insertNonFull(Bucket bucket) {
      Bucket leftNeighbour = lastBucket;
      while (leftNeighbour != null && bucket.key.compareTo(leftNeighbour.key) < 0) {
        leftNeighbour = leftNeighbour.previousBucket;
      }

      if (leaf) {
        /* Just insert bucket into buckets list */

        if (leftNeighbour == null) {
          /* Key is minimum */
          insertBucketInFrontLeaf(bucket);
        } else {
          appendBucketToBucketLeaf(leftNeighbour, bucket);
        }

        assert bucket.parent == this;
      } else {
        /* Insert into subtree */

        Node childToInsert = leftNeighbour == null ? firstNode : leftNeighbour.nextNode;
        assert childToInsert.parent == this;

        if (childToInsert.isFull()) {
          childToInsert.split();

          /*
            Child has been split -> bucket was lifted and new node was inserted
            after childToInsert -> we should decide into which node of two
            to insert now
          */

          Bucket liftedBucket = childToInsert.nextBucket;

          assert liftedBucket.parent == childToInsert.parent;
          assert liftedBucket.parent == this;
          assert childToInsert.nextNode == liftedBucket.nextNode;

          if (bucket.key.compareTo(liftedBucket.key) > 0) {
            childToInsert = childToInsert.nextNode;
          }
        }

        childToInsert.insertNonFull(bucket);
      }
    }

    void insertBucketInFrontLeaf(Bucket bucket) {
      assert leaf;
      assert firstNode == null;
      assert lastNode == null;

      bucket.parent = this;

      if (bucketsCount == 0) {
        /* Bucket is first */
        assert firstBucket == null;
        assert lastBucket == null;

        firstBucket = bucket;
        lastBucket = bucket;
      } else {
        assert firstBucket != null;
        assert lastBucket != null;

        Bucket oldFirstBucket = firstBucket;
        bucket.nextBucket = oldFirstBucket;
        oldFirstBucket.previousBucket = bucket;

        firstBucket = bucket;
      }

      bucketsCount++;
    }

    void appendBucketToBucketLeaf(Bucket leftBucket, Bucket newBucket) {
      assert leaf;
      assert leftBucket.parent == this;

      newBucket.parent = this;

      if (leftBucket.nextBucket == null) {
        /* appending in the end */
        assert lastBucket == leftBucket;

        leftBucket.nextBucket = newBucket;
        newBucket.previousBucket = leftBucket;
        lastBucket = newBucket;
      } else {
        assert lastBucket != null;
        assert lastBucket != leftBucket;

        Bucket oldNextBucket = leftBucket.nextBucket;
        leftBucket.nextBucket = newBucket;
        newBucket.previousBucket = leftBucket;
        newBucket.nextBucket = oldNextBucket;
        oldNextBucket.previousBucket = newBucket;
      }

      bucketsCount++;
    }

    Value getMinimumValue() {
      return getMinimumBucket().firstValue;
    }

    Bucket getMinimumBucket() {
      if (!leaf && firstNode != null && firstNode.nonEmpty()) {
        return firstNode.getMinimumBucket();
      }

      return firstBucket;
    }
  }

  private final class Bucket {
    K key;
    int size;
    Node parent;
    Node previousNode;
    Node nextNode;
    Bucket previousBucket;
    Bucket nextBucket;
    Value firstValue;
    Value lastValue;

    public Bucket(K key, V initialValue) {
      this.key = key;
      this.parent = null;
      this.previousNode = null;
      this.previousBucket = null;
      this.nextNode = null;
      this.nextBucket = null;

      firstValue = new Value(initialValue, this);
      lastValue = firstValue;
      this.size = 1;
    }

    void addValueLast(V value) {
      Value newValue = new Value(value, this);

      if (size == 0) {
        firstValue = newValue;
      } else {
        Value oldLastValue = lastValue;
        oldLastValue.nextValue = newValue;
        newValue.previousValue = oldLastValue;
      }

      lastValue = newValue;
      size++;
    }
  }

  private final class Value {
    V value;
    Bucket parent;
    Value previousValue;
    Value nextValue;

    Value(V value, Bucket parent) {
      this.value = value;
      this.parent = parent;
      this.previousValue = null;
      this.nextValue = null;
    }

    Value getSuccessor() {
      if (nextValue != null) {
        return nextValue;
      }

      Bucket bucket = parent;

      if (bucket.nextNode != null && bucket.nextNode.nonEmpty()) {
        return bucket.nextNode.getMinimumValue();
      } else if (bucket.nextBucket != null) {
        return bucket.nextBucket.firstValue;
      }

      Node node = parent.parent;

      if (node != null && node.nextBucket != null) {
        return node.nextBucket.firstValue;
      }

      return null;
    }
  }
}
