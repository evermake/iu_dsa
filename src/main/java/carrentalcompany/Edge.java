/**
 * Created by Vladislav Deryabkin
 */

package carrentalcompany;

/**
 * Class representing edge of the Graph.
 * It contains 2 vertices and associated weight.
 *
 * @param <W> type of the edge associated weight
 */
public class Edge<W> {
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
