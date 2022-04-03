package rangemap;

import java.util.LinkedList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

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
    Item item = new Item(key, value);

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

    root.insertNonFull(item);

    size++;
  }

  @Override
  public V remove(K key) {
    return null;
  }

  @Override
  public boolean contains(K key) {
    return root.lookupItem(key) != null;
  }

  @Override
  public V lookup(K key) {
    Item item = root.lookupItem(key);
    if (item == null) {
      return null;
    }
    return item.value;
  }

  @Override
  public List<V> lookupRange(K from, K to) {
    List<V> lookupResult = new LinkedList<>();

    for (
        Item item = root.lookupItem(from);
        item != null && item.key.compareTo(to) <= 0;
        item = item.getSuccessor()
    ) {
      lookupResult.add(item.value);
    }

    return lookupResult;
  }

  private final class Node {
    Node parent;
    boolean leaf;
    int childrenCount;
    int itemsCount;
    Node previousNode;
    Item previousItem;
    Node nextNode;
    Item nextItem;
    Node firstNode;
    Item firstItem;
    Node lastNode;
    Item lastItem;

    public Node(Node parent, boolean leaf) {
      this.parent = parent;
      this.leaf = leaf;
      this.childrenCount = 0;
      this.itemsCount = 0;
      this.previousNode = null;
      this.previousItem = null;
      this.nextNode = null;
      this.nextItem = null;
      this.firstNode = null;
      this.firstItem = null;
      this.lastNode = null;
      this.lastItem = null;
    }

    Item lookupItem(K key) {
      Item item = firstItem;
      while (item != null && key.compareTo(item.key) > 0) {
        item = item.nextItem;
      }

      if (item != null && key.compareTo(item.key) == 0) {
        /* Item is found */
        return item;
      }

      /* Item in the current node is not found */

      if (leaf) {
        return null;
      }

      // Search in the subtree
      Node childToLookup = item == null ? lastNode : item.previousNode;
      return childToLookup.lookupItem(key);
    }

    void split() {
      assert isFull();

      Item medianItem = getItem(t - 1);
      assert medianItem.parent == this;

      // Save nodes and items around the median item
      // ... [I] (N) [M] (N) [I] ...
      //             ^^^^
      Node preMedianNode = medianItem.previousNode;
      Item preMedianItem = medianItem.previousItem;
      Node postMedianNode = medianItem.nextNode;
      Item postMedianItem = medianItem.nextItem;

      assert preMedianItem != null;
      assert postMedianItem != null;

      // Add median item to the right of this node.
      // It will be added after the pointer to this node in the parent
      // and newly created node (the pointer to which will
      // located be after this added item) will be returned
      // ... (N) [M] (N) ...
      //    this    newNode
      //      |       -
      //     ...
      Node newNode = addItemToTheRight(medianItem);

      // "Transfer" right part of items and children lists
      // to the newly created node
      newNode.firstItem = postMedianItem;
      newNode.lastItem = lastItem;
      lastItem = preMedianItem;
      postMedianItem.previousItem = null;
      preMedianItem.nextItem = null;

      if (leaf) {
        /* do not transfer children, since leaf has no children */
        assert preMedianNode == null;
        assert postMedianNode == null;
        assert newNode.leaf;
      } else {
        newNode.firstNode = postMedianNode;
        newNode.lastNode = lastNode;

        lastNode = preMedianNode;

        preMedianNode.nextItem = null;
        preMedianNode.nextNode = null;
        postMedianNode.previousNode = null;
        postMedianNode.previousItem = null;

        childrenCount = t;
        newNode.childrenCount = t;
      }

      // Update sizes
      newNode.itemsCount = t - 1;
      itemsCount = t - 1;

      newNode.updateChildrenAndItemsParent();
    }

    boolean isFull() {
      return itemsCount >= (2 * t) - 1;
    }

    boolean nonEmpty() {
      return itemsCount > 0;
    }

    Item getItem(int index) {
      int i = 0;
      Item item = firstItem;
      while (i < index && item != null) {
        item = item.nextItem;
        i++;
      }
      return item;
    }

    /**
     * TODO: write docstring
     *
     * @param item item to add
     *
     * @return newly created node that will be the next node of the added item
     */
    Node addItemToTheRight(Item item) {
      assert !parent.leaf;

      Node newNode = new Node(parent, this.leaf);
      item.parent = parent;

      Node oldNextNode = nextNode;
      Item oldNextItem = nextItem;

      nextNode = newNode;
      nextItem = item;
      newNode.previousNode = this;
      newNode.previousItem = item;
      newNode.nextNode = oldNextNode;
      newNode.nextItem = oldNextItem;

      item.previousNode = this;
      item.previousItem = previousItem;
      item.nextNode = newNode;
      item.nextItem = oldNextItem;

      if (oldNextNode == null) {
        /* Node was last node */
        assert oldNextItem == null;

        parent.lastNode = newNode;
        parent.lastItem = item;
      } else {
        oldNextNode.previousNode = newNode;
        // oldNextNode.previousItem does not change
        oldNextItem.previousNode = newNode;
        oldNextItem.previousItem = item;
      }

      if (previousItem == null) {
        /* Node is first node */
        assert previousNode == null;
        assert parent.firstNode == this;
        parent.firstItem = item;
      } else {
        previousItem.nextItem = item;
      }

      parent.childrenCount++;
      parent.itemsCount++;

      return newNode;
    }

    void updateChildrenAndItemsParent() {
      for (Node child = firstNode; child != null; child = child.nextNode) {
        child.parent = this;
      }
      for (Item item = firstItem; item != null; item = item.nextItem) {
        item.parent = this;
      }
    }

    void insertNonFull(Item item) {
      Item leftNeighbour = lastItem;
      while (leftNeighbour != null && item.key.compareTo(leftNeighbour.key) < 0) {
        leftNeighbour = leftNeighbour.previousItem;
      }

      if (leaf) {
        /* Just insert item into items list */

        if (leftNeighbour == null) {
          /* Key is minimum */
          insertItemInFrontLeaf(item);
        } else {
          appendItemToItemLeaf(leftNeighbour, item);
        }

        assert item.parent == this;
      } else {
        /* Insert into subtree */

        Node childToInsert = leftNeighbour == null ? firstNode : leftNeighbour.nextNode;
        assert childToInsert.parent == this;

        if (childToInsert.isFull()) {
          childToInsert.split();

          /*
            Child has been split -> item was lifted and new node was inserted
            after childToInsert -> we should decide into which node of two
            to insert now
          */

          Item liftedItem = childToInsert.nextItem;

          assert liftedItem.parent == childToInsert.parent;
          assert liftedItem.parent == this;
          assert childToInsert.nextNode == liftedItem.nextNode;

          if (item.key.compareTo(liftedItem.key) > 0) {
            childToInsert = childToInsert.nextNode;
          }
        }

        childToInsert.insertNonFull(item);
      }
    }

    void insertItemInFrontLeaf(Item item) {
      assert leaf;
      assert firstNode == null;
      assert lastNode == null;

      item.parent = this;

      if (itemsCount == 0) {
        /* item is first */
        assert firstItem == null;
        assert lastItem == null;

        firstItem = item;
        lastItem = item;
      } else {
        assert firstItem != null;
        assert lastItem != null;

        Item oldFirstItem = firstItem;
        item.nextItem = oldFirstItem;
        oldFirstItem.previousItem = item;

        firstItem = item;
      }

      itemsCount++;
    }

    void appendItemToItemLeaf(Item leftItem, Item newItem) {
      assert leaf;
      assert leftItem.parent == this;

      newItem.parent = this;

      if (leftItem.nextItem == null) {
        /* appending in the end */
        assert lastItem == leftItem;

        leftItem.nextItem = newItem;
        newItem.previousItem = leftItem;
        lastItem = newItem;
      } else {
        assert lastItem != null;
        assert lastItem != leftItem;

        Item oldNextItem = leftItem.nextItem;
        leftItem.nextItem = newItem;
        newItem.previousItem = leftItem;
        newItem.nextItem = oldNextItem;
        oldNextItem.previousItem = newItem;
      }

      itemsCount++;
    }

    Item getMinimumItem() {
      if (!leaf && firstNode != null && firstNode.nonEmpty()) {
        return firstNode.getMinimumItem();
      }

      return firstItem;
    }
  }

  private final class Item {
    K key;
    V value;
    Node parent;
    Node previousNode;
    Item previousItem;
    Node nextNode;
    Item nextItem;

    public Item(K key, V value) {
      this.key = key;
      this.value = value;
      this.parent = null;
      this.previousNode = null;
      this.previousItem = null;
      this.nextNode = null;
      this.nextItem = null;
    }

    @Nullable
    Item getSuccessor() {
      if (nextNode != null && nextNode.nonEmpty()) {
        return nextNode.getMinimumItem();
      } else if (nextItem != null) {
        return nextItem;
      } else if (parent != null && parent.nextItem != null) {
        return parent.nextItem;
      }

      return null;
    }
  }
}
