/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

/**
 * Class representing vertex of the Graph.
 *
 * @param <L> type of the vertex associated label
 */
public class Vertex<L> {
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
