/**
 * Created by Vladislav Deryabkin
 */

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Solution for the Problem D (Minimum Spanning Forest).
 */
public class ProblemDSolutionCombined {
  private static final Pattern ADD_BRANCH_COMMAND_PATTERN = Pattern.compile(
      "^ADD (?<branch>\\S+) (?<penalty>\\d+)$"
  );
  private static final Pattern ADD_CONNECTION_COMMAND_PATTERN = Pattern.compile(
      "^CONNECT (?<branch1>\\S+) (?<branch2>\\S+) (?<distance>\\d+)$"
  );
  private static final Map<String, Vertex<BranchInfo>> labelBranchMap = new HashMap<>();
  private static final Graph<BranchInfo, Double> network = new DynamicGraph<>();

  public static void main(String[] args) throws Exception {
    Scanner scanner = new Scanner(System.in);

    int commandsNumber = scanner.nextInt();
    scanner.nextLine();

    for (int i = 0; i < commandsNumber; i++) {
      parseAndExecuteCommand(scanner.nextLine());
    }
  }

  private static void parseAndExecuteCommand(String commandString) throws Exception {
    if (commandString.equals("PRINT_MIN")) {
      /* Compute and print Minimum Spanning Forest of the network */

      List<Edge<Double>> networkMSF = network.getMinimumSpanningForest(
          0.0, Double.MAX_VALUE
      );

      List<String> edgesAsStrings = networkMSF
          .stream()
          .map(Edge::toString)
          .collect(Collectors.toList());

      System.out.println(String.join(" ", edgesAsStrings));

      return;
    }

    Matcher commandMatcher;

    commandMatcher = ADD_BRANCH_COMMAND_PATTERN.matcher(commandString);
    if (commandMatcher.matches()) {
      /* Add branch into the network */

      String branchLabel = commandMatcher.group("branch");
      int branchPenalty = Integer.parseInt(commandMatcher.group("penalty"));
      BranchInfo branchInfo = new BranchInfo(branchLabel, branchPenalty);

      Vertex<BranchInfo> addedBranch = network.insertVertex(branchInfo);
      labelBranchMap.put(branchLabel, addedBranch);

      return;
    }

    commandMatcher = ADD_CONNECTION_COMMAND_PATTERN.matcher(commandString);
    if (commandMatcher.matches()) {
      /* Connect two branches */

      String branch1Label = commandMatcher.group("branch1");
      String branch2Label = commandMatcher.group("branch2");
      int distance = Integer.parseInt(commandMatcher.group("distance"));

      Vertex<BranchInfo> branch1 = labelBranchMap.get(branch1Label);
      Vertex<BranchInfo> branch2 = labelBranchMap.get(branch2Label);

      int branch1Penalty = branch1.getLabel().getPenalty();
      int branch2Penalty = branch2.getLabel().getPenalty();
      double connectionWeight = calculateConnectionWeight(
          branch1Penalty, branch2Penalty, distance
      );

      network.insertEdge(branch1, branch2, connectionWeight);
      return;
    }

    throw new Exception("invalid command");
  }

  private static double calculateConnectionWeight(
      int branch1Penalty,
      int branch2Penalty,
      int distance
  ) {
    return (double) distance / (double) (branch1Penalty + branch2Penalty);
  }

  /**
   * Data-class for storing info about the branch.
   */
  private static final class BranchInfo {
    private final String label;
    private final int penalty;

    public BranchInfo(String label, int penalty) {
      this.label = label;
      this.penalty = penalty;
    }

    public String getLabel() {
      return label;
    }

    public int getPenalty() {
      return penalty;
    }

    @Override
    public String toString() {
      return getLabel();
    }
  }
}


interface PriorityQueue<K extends Comparable<K>, V> {
  void insert(Node<K, V> node);
  Node<K, V> findMin();
  Node<K, V> extractMin();
  boolean isEmpty();
  void decreaseKey(Node<K, V> node, K newKey);
  void delete(Node<K, V> node);
  void union(FibonacciHeap<K, V> other);
}


/**
 * Class that represents node in the PriorityQueue.
 *
 * @param <K> type of the node's key (must be {@link Comparable})
 * @param <V> type of the node's value
 */
class Node<K extends Comparable<K>, V> implements Comparable<Node<K, V>> {
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
      node.left = node;
      node.right = node;
      firstChild = node;
      lastChild = node;
    } else {
      lastChild.insertRight(node);
    }

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


/**
 * Implementation of the min-{@link PriorityQueue} based on {@code FibonacciHeap}.
 *
 * @param <K> type of the key (must be {@link Comparable})
 * @param <V> type of the value
 */
class FibonacciHeap<K extends Comparable<K>, V> implements PriorityQueue<K, V> {
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
      firstRootNode = other.firstRootNode;
      lastRootNode = other.lastRootNode;
      min = other.min;
    } else if (other.firstRootNode != null) {
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

    if (node.right == node) {
      /* Node is the single child of its parent */
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

    int maxDegree = getMaximumDegree();
    ArrayList<Node<K, V>> roots = new ArrayList<>(maxDegree + 1);
    for (int i = 0; i <= maxDegree; i++) {
      roots.add(null);
    }

    // Save root nodes for simpler iterating further
    LinkedList<Node<K, V>> initialRoots = new LinkedList<>();
    Node<K, V> initialRoot = min;
    do {
      initialRoots.add(initialRoot);
      initialRoot = initialRoot.right;
    } while (initialRoot != min);

    for (Node<K, V> x : initialRoots) {
      int degree = x.childrenCount;
      while (roots.get(degree) != null) {
        Node<K, V> nodeWithSameDegree = roots.get(degree);

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
    node.left.right = node.right;
    node.right.left = node.left;
  }
}


/**
 * Class representing vertex of the Graph.
 *
 * @param <L> type of the vertex associated label
 */
class Vertex<L> {
  private final L label;
  public Node queueNode;
  public Edge minEdge;
  // Index of this node in the adjacency matrix
  private int adjacencyMatrixIndex;

  public Vertex(L label, int adjacencyMatrixIndex) {
    this.label = label;
    this.adjacencyMatrixIndex = adjacencyMatrixIndex;
    this.queueNode = null;
    this.minEdge = null;
  }

  public L getLabel() {
    return label;
  }

  public int getAdjacencyMatrixIndex() {
    return adjacencyMatrixIndex;
  }

  public void decrementAdjacencyMatrixIndex() {
    adjacencyMatrixIndex--;
  }

  @Override
  public String toString() {
    return getLabel().toString();
  }
}


interface Graph<V, E> {
  Vertex<V> insertVertex(V label);
  Edge<E> insertEdge(Vertex<V> from, Vertex<V> to, E weight);
  void removeVertex(Vertex<V> vertex);
  void removeEdge(Edge<E> edge);
  boolean areAdjacent(Vertex<V> vertex1, Vertex<V> vertex2);
  int degree(Vertex<V> vertex);
  List<Edge<E>> getMinimumSpanningForest(E zeroValue, E infinityValue);
}


/**
 * Class representing edge of the Graph.
 * It contains 2 vertices and associated weight.
 *
 * @param <W> type of the edge associated weight
 */
class Edge<W> {
  private final Vertex from;
  private final Vertex to;
  private final W weight;

  public Edge(Vertex from, Vertex to, W weight) {
    this.from = from;
    this.to = to;
    this.weight = weight;
  }

  public W getWeight() {
    return weight;
  }

  public Vertex getFrom() {
    return from;
  }

  public Vertex getTo() {
    return to;
  }

  /**
   * Returns second (adjacent) vertex.
   * Warning: this method does not perform check is {@code vertex} belongs to this edge.
   *
   * @return adjacent vertex for {@code vertex}
   */
  public Vertex getSecond(Vertex vertex) {
    Vertex from = getFrom();
    Vertex to = getTo();
    return (vertex == from) ? to : from;
  }

  /**
   * @return this as edge as the string in format VERTEX_FROM:VERTEX_TO
   */
  @Override
  public String toString() {
    return getFrom().toString() + ":" + getTo().toString();
  }
}



/**
 * Implementation of the weighted undirected dynamic {@link Graph}
 * based on the adjacency matrix representation.
 *
 * @param <V> type of the {@link Vertex} label
 * @param <E> type of the {@link Edge} associated weight
 */
class DynamicGraph<V, E extends Comparable<E>> implements Graph<V, E> {
  private final ArrayList<ArrayList<Edge<E>>> adjacencyMatrix;
  private final ArrayList<Vertex<V>> vertices;
  private int size;

  public DynamicGraph() {
    this.adjacencyMatrix = new ArrayList<>();
    this.vertices = new ArrayList<>();
    this.size = 0;
  }

  @Override
  public Vertex<V> insertVertex(V label) {
    Vertex<V> vertex = new Vertex<>(label, size);
    vertices.add(vertex);

    for (ArrayList<Edge<E>> edges : adjacencyMatrix) {
      edges.add(null);
    }

    size++;

    ArrayList<Edge<E>> incidentEdges = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      incidentEdges.add(null);

    }

    adjacencyMatrix.add(incidentEdges);

    return vertex;
  }

  @Override
  public Edge<E> insertEdge(Vertex<V> from, Vertex<V> to, E weight) {
    Edge<E> edge = new Edge<>(from, to, weight);

    int v1Index = from.getAdjacencyMatrixIndex();
    int v2Index = to.getAdjacencyMatrixIndex();

    adjacencyMatrix.get(v1Index).set(v2Index, edge);
    adjacencyMatrix.get(v2Index).set(v1Index, edge);

    return edge;
  }

  @Override
  public void removeVertex(Vertex<V> vertex) {
    int vIndex = vertex.getAdjacencyMatrixIndex();

    for (ArrayList<Edge<E>> edges : adjacencyMatrix) {
      edges.remove(vIndex);
    }

    for (int i = vIndex + 1; i < size; i++) {
      vertices.get(i).decrementAdjacencyMatrixIndex();
    }

    adjacencyMatrix.remove(vIndex);
    size--;
  }

  @Override
  public void removeEdge(Edge<E> edge) {
    int v1Index = edge.getFrom().getAdjacencyMatrixIndex();
    int v2Index = edge.getTo().getAdjacencyMatrixIndex();

    adjacencyMatrix.get(v1Index).set(v2Index, null);
    adjacencyMatrix.get(v2Index).set(v1Index, null);
  }

  @Override
  public boolean areAdjacent(Vertex<V> vertex1, Vertex<V> vertex2) {
    int v1Index = vertex1.getAdjacencyMatrixIndex();
    int v2Index = vertex2.getAdjacencyMatrixIndex();

    return adjacencyMatrix.get(v1Index).get(v2Index) != null;
  }

  @Override
  public int degree(Vertex<V> vertex) {
    return getAdjacentVertices(vertex).size();
  }

  /**
   * @return list containing adjacent vertices of the {@code vertex}
   */
  public List<Vertex<V>> getAdjacentVertices(Vertex<V> vertex) {
    int vIndex = vertex.getAdjacencyMatrixIndex();
    ArrayList<Edge<E>> incidentEdges = adjacencyMatrix.get(vIndex);

    List<Vertex<V>> neighbours = new LinkedList<>();
    for (int i = 0; i < size; i++) {
      Edge<E> incidentEdge = incidentEdges.get(i);
      if (incidentEdge != null) {
        neighbours.add(incidentEdge.getSecond(vertex));
      }
    }

    return neighbours;
  }

  /**
   * Generates the list of edges of the Minimum Spanning Tree of the graph
   * using Prim's algorithm and returns it.
   *
   * @param zeroValue     value for the edge weights, which represents zero.
   *                      Required field, since we use generics for weights.
   * @param infinityValue value for the edge weights, which represents +∞.
   *                      Required field, since we use generics for weights.
   *
   * @return list of edges from the Minimum Spanning Forest of the graph
   */
  @Override
  public List<Edge<E>> getMinimumSpanningForest(E zeroValue, E infinityValue) {
    List<Edge<E>> MSFEdges = new LinkedList<>();
    FibonacciHeap<E, Vertex<V>> queue = new FibonacciHeap<>();

    // Initialize queue of vertices.
    // Set first vertex key 0 and each other vertex key +∞
    for (int i = 0; i < size; i++) {
      Vertex<V> vertex = vertices.get(i);
      E vertexKey = i == 0 ? zeroValue : infinityValue;
      Node<E, Vertex<V>> node = new Node<>(vertexKey, vertex);
      vertex.queueNode = node;
      vertex.minEdge = null;

      queue.insert(node);
    }

    while (!queue.isEmpty()) {
      Vertex<V> minVertex = queue.extractMin().value;
      if (minVertex.minEdge != null) {
        MSFEdges.add(minVertex.minEdge);
      }

      for (Vertex<V> adjacent : getAdjacentVertices(minVertex)) {
        int minVertexIndex = minVertex.getAdjacencyMatrixIndex();
        int adjacentIndex = adjacent.getAdjacencyMatrixIndex();

        Edge<E> edge = adjacencyMatrix.get(minVertexIndex).get(adjacentIndex);
        Node<E, Vertex<V>> adjacentNode = adjacent.queueNode;

        if (queue.contains(adjacentNode)
            && edge.getWeight().compareTo(adjacentNode.key) < 0
        ) {
          queue.decreaseKey(adjacentNode, edge.getWeight());
          // "Remember" edge with minimum weight
          adjacent.minEdge = edge;
        }
      }
    }

    return MSFEdges;
  }
}

