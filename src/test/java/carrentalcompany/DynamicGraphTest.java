package carrentalcompany;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class DynamicGraphTest {
  @Test
  void testBasicOperations() {
    DynamicGraph<String, Integer> graph = new DynamicGraph<>();

    Vertex<String> vertexA = graph.insertVertex("A");
    Vertex<String> vertexB = graph.insertVertex("B");
    Vertex<String> vertexC = graph.insertVertex("C");
    Vertex<String> vertexD = graph.insertVertex("D");
    Vertex<String> vertexE = graph.insertVertex("E");

    // Check there are no adjacent vertices
    assertFalse(graph.areAdjacent(vertexA, vertexB));
    assertFalse(graph.areAdjacent(vertexA, vertexC));
    assertFalse(graph.areAdjacent(vertexA, vertexD));
    assertFalse(graph.areAdjacent(vertexA, vertexE));
    assertFalse(graph.areAdjacent(vertexB, vertexC));
    assertFalse(graph.areAdjacent(vertexB, vertexD));
    assertFalse(graph.areAdjacent(vertexB, vertexE));
    assertFalse(graph.areAdjacent(vertexC, vertexD));
    assertFalse(graph.areAdjacent(vertexC, vertexE));
    assertFalse(graph.areAdjacent(vertexD, vertexE));

    graph.insertEdge(vertexA, vertexB, 100);
    assertTrue(graph.areAdjacent(vertexA, vertexB));
    assertEquals(List.of(new Vertex[]{vertexA}), graph.getAdjacentVertices(vertexB));
    assertEquals(List.of(new Vertex[]{vertexB}), graph.getAdjacentVertices(vertexA));

    Edge<Integer> edgeFromBToC = graph.insertEdge(vertexB, vertexC, 500);
    assertTrue(graph.areAdjacent(vertexB, vertexC));
    assertFalse(graph.areAdjacent(vertexA, vertexC));

    graph.insertEdge(vertexE, vertexA, 123);
    assertTrue(graph.areAdjacent(vertexA, vertexE));
    assertEquals(
        List.of(new Vertex[]{vertexB, vertexE}),
        graph.getAdjacentVertices(vertexA)
    );
    assertEquals(List.of(new Vertex[]{vertexA}), graph.getAdjacentVertices(vertexE));
    assertEquals(List.of(new Vertex[]{}), graph.getAdjacentVertices(vertexD));

    graph.removeVertex(vertexA);
    assertEquals(List.of(new Vertex[]{vertexC}), graph.getAdjacentVertices(vertexB));
    assertEquals(List.of(new Vertex[]{}), graph.getAdjacentVertices(vertexE));

    assertTrue(graph.areAdjacent(vertexB, vertexC));
    graph.removeEdge(edgeFromBToC);
    assertFalse(graph.areAdjacent(vertexB, vertexC));
  }
}