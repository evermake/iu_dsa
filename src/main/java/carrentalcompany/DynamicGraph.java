/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of the weighted undirected dynamic {@link Graph}
 * based on the adjacency matrix representation.
 *
 * @param <V> type of the {@link Vertex} label
 * @param <E> type of the {@link Edge} associated weight
 */
public class DynamicGraph<V, E extends Comparable<E>> implements Graph<V, E> {
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
