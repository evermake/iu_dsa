/**
 * Created by Vladislav Deryabkin
 */

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class containing solution for the Range Queries problem (Problem A on CodeForces).
 */
public class ProblemBSolutionCombined {
  /**
   * Parses command from {@code commandString} and executes it on
   * {@code operationsHistory} {@link RangeMap}.
   *
   * @param commandString     command string to parse and execute
   * @param operationsHistory {@link RangeMap} object containing operations history
   *                          on which command will be executed
   *
   * @throws InvalidInputException when fails to parse the command
   */
  private static void performCommandOnOperationsHistory(
      String commandString,
      RangeMap<Date, Integer> operationsHistory
  ) throws InvalidInputException {
    Command matchedCommand = null;
    Matcher commandMatcher = null;
    for (Command command : Command.values()) {
      commandMatcher = command.match(commandString);
      if (commandMatcher.matches()) {
        matchedCommand = command;
        break;
      }
    }

    if (matchedCommand == null) {
      throw new InvalidInputException();
    }

    String arg1 = commandMatcher.group(1);
    String arg2 = commandMatcher.group(2);

    try {
      switch (matchedCommand) {
        case DEPOSIT:
          Date depositDate = DateParser.fromString(arg1);
          Integer depositAmount = Integer.parseInt(arg2);
          operationsHistory.add(depositDate, depositAmount);
          break;
        case WITHDRAW:
          Date withdrawDate = DateParser.fromString(arg1);
          Integer withdrawAmount = Integer.parseInt(arg2);
          operationsHistory.add(withdrawDate, withdrawAmount * -1);
          break;
        case REPORT:
          Date dateFrom = DateParser.fromString(arg1);
          Date dateTo = DateParser.fromString(arg2);

          if (!operationsHistory.contains(dateFrom)) {
            operationsHistory.add(dateFrom, 0);
          }

          List<Integer> operations = operationsHistory.lookupRange(dateFrom, dateTo);
          Integer rangeSum = operations.stream().mapToInt(Integer::intValue).sum();
          System.out.println(rangeSum);
          break;
      }
    } catch (IllegalArgumentException e) {
      throw new InvalidInputException();
    } catch (ParseException e) {
      throw new InvalidInputException();
    }
  }

  public static void main(String[] args) throws InvalidInputException {
    RangeMap<Date, Integer> operationsHistory = new BTreeRangeMap<>();
    Scanner scanner = new Scanner(System.in);
    int n = scanner.nextInt();
    scanner.nextLine();

    for (int i = 0; i < n; i++) {
      performCommandOnOperationsHistory(
          scanner.nextLine(),
          operationsHistory
      );
    }
  }

  private static final class DateParser {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
        "yyyy-MM-dd"
    );
 
    public static Date fromString(String dateString) throws ParseException {
      return DATE_FORMAT.parse(dateString);
    }
  }


  /**
   * {@code InvalidInputException} is thrown when program
   * reads input, which is in the incorrect format.
   */
  private static final class InvalidInputException extends Exception {
  }
}


/**
 * Enum of possible commands in the task.
 * Items also contain regexp patterns for easier parsing implementation.
 */
enum Command {
  DEPOSIT("^(\\d{4}-\\d{2}-\\d{2}) DEPOSIT (\\d+)$"),
  WITHDRAW("^(\\d{4}-\\d{2}-\\d{2}) WITHDRAW (\\d+)$"),
  REPORT("^REPORT FROM (\\d{4}-\\d{2}-\\d{2}) TO (\\d{4}-\\d{2}-\\d{2})$");

  private final Pattern pattern;

  Command(String regex) {
    this.pattern = Pattern.compile(regex);
  }

  /**
   * @param command command string to match
   *
   * @return {@link Matcher} of the command for the given string
   */
  public Matcher match(String command) {
    return pattern.matcher(command);
  }
}


interface RangeMap<K, V> {
  int size();
  boolean isEmpty();
  void add(K key, V value);
  V remove(K key);
  boolean contains(K key);
  V lookup(K key);
  List<V> lookupRange(K from, K to);
}


/**
 * {@link RangeMap} implementation based on BTree.
 * BTree is made of nodes and buckets in the following structure:
 * <p>
 * --------(Node)------   [Bucket] (Node) [Bucket] (Node) ...
 * /                   \             |               |
 * (Node) [Bucket] ... (Node)       ...             ...
 *
 * <p>
 * Node has only children and pointers to its neighbour buckets and nodes.
 * Bucket contains key and values associated with key. Buckets are not necessary and
 * are only created for proper handling items with equal keys.
 *
 * @param <K>     type of the node's key
 * @param <V>type of the node's value
 */
class BTreeRangeMap<K extends Comparable<K>, V> implements RangeMap<K, V> {
  private final int t = 10;
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

    // Find "from" value and iteratively add its successors into the list
    for (
        Value value = root.lookupValue(from);
        value != null && value.parent.key.compareTo(to) <= 0;
        value = value.getSuccessor()
    ) {
      lookupResult.add(value.value);
    }

    return lookupResult;
  }

  /**
   * Node of the BTree. All main BTree operations are implemented here.
   * <p>
   * It has fields
   * - {@code previousNode, previousBucket, nextNode, nextBucket} that
   * helps for faster traversing in the tree.
   * - {@code firstNode, firstBucket, lastNode, lastBucket} for easier accessing its
   * child nodes and buckets.
   */
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
      Bucket medianBucket = getMedianBucket();
      
      // Save nodes and buckets around the median bucket
      // ... [B] (N) [M] (N) [B] ...
      //             ^^^^
      Node preMedianNode = medianBucket.previousNode;
      Bucket preMedianBucket = medianBucket.previousBucket;
      Node postMedianNode = medianBucket.nextNode;
      Bucket postMedianBucket = medianBucket.nextBucket;

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

      if (!leaf) {
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

    /**
     * Returns median bucket (at index t - 1) in the node.
     * Important: assuming that node is full!
     */
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
     * Add new bucket to the right of this node.
     * Also creates and adds to the right one new node, because between each bucket
     * must be a node.
     * Updates all necessary pointers and increments parent's children and buckets
     * counters.
     *
     * @param bucket bucket to add
     *
     * @return newly created node that will be the next node of the added bucket
     */
    Node addBucketToTheRight(Bucket bucket) {
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
      } else {
        /* Insert into subtree */

        Node childToInsert = leftNeighbour == null ? firstNode : leftNeighbour.nextNode;
    
        if (childToInsert.isFull()) {
          childToInsert.split();

          /*
            Child has been split -> bucket was lifted and new node was inserted
            after childToInsert -> we should decide into which node of two
            to insert now
          */

          Bucket liftedBucket = childToInsert.nextBucket;

          if (bucket.key.compareTo(liftedBucket.key) > 0) {
            childToInsert = childToInsert.nextNode;
          }
        }

        childToInsert.insertNonFull(bucket);
      }
    }

    void insertBucketInFrontLeaf(Bucket bucket) {
      bucket.parent = this;

      if (bucketsCount == 0) {
        firstBucket = bucket;
        lastBucket = bucket;
      } else {
        Bucket oldFirstBucket = firstBucket;
        bucket.nextBucket = oldFirstBucket;
        oldFirstBucket.previousBucket = bucket;

        firstBucket = bucket;
      }

      bucketsCount++;
    }

    void appendBucketToBucketLeaf(Bucket leftBucket, Bucket newBucket) {
      newBucket.parent = this;

      if (leftBucket.nextBucket == null) {
        leftBucket.nextBucket = newBucket;
        newBucket.previousBucket = leftBucket;
        lastBucket = newBucket;
      } else {
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

  /**
   * Class which contains all values for the specific key (as linked list).
   * I created this class to properly handle items with equal keys.
   */
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

  /**
   * Class that represents single value. It is stored in {@link Bucket}, which
   * stores the associated key.
   */
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

    /**
     * @return successor value
     */
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

      for (Node node = bucket.parent; node != null; node = node.parent) {
        if (node.nextBucket != null) {
          return node.nextBucket.firstValue;
        }
      }

      return null;
    }
  }
}
