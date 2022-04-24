/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

import java.util.List;

public interface Graph<V, E> {
  Vertex<V> insertVertex(V label);
  Edge<E> insertEdge(Vertex<V> from, Vertex<V> to, E weight);
  void removeVertex(Vertex<V> vertex);
  void removeEdge(Edge<E> edge);
  boolean areAdjacent(Vertex<V> vertex1, Vertex<V> vertex2);
  int degree(Vertex<V> vertex);
  List<Edge<E>> getMinimumSpanningForest(E zeroValue, E infinityValue);
}
