package algorithms;

import graph.Edge;
import graph.Graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MSTTest {

    @Test
    void findsMinimumWeightOnConnectedGraph() {
        Graph graph = new Graph(4, false);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 5);

        MSTResult result = KruskalMST.compute(graph);

        assertEquals(4L, result.totalWeight);
        assertEquals(1, result.components);
        assertEquals(3, result.edgeCount());

        DisjointSetUnion check = new DisjointSetUnion(4);

        for (Edge edge : result.edges) {
            assertTrue(check.union(edge.from, edge.to));
        }

        assertEquals(1, check.components());
    }

    @Test
    void findsForestOnDisconnectedGraph() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_disconnected.txt"));

        MSTResult result = KruskalMST.compute(graph);

        assertEquals(9L, result.totalWeight);
        assertEquals(4, result.components);
        assertEquals(3, result.edgeCount());

        assertEquals(6, result.metrics.edgeScans);
        assertEquals(0, result.metrics.maxDepth);
    }

    @Test
    void supportsNegativeWeightsAndSkipsSelfLoops() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 0, -100);
        graph.addEdge(0, 1, -5);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 10);

        MSTResult result = KruskalMST.compute(graph);

        assertEquals(-3L, result.totalWeight);
        assertEquals(1, result.components);
        assertEquals(2, result.edgeCount());

        for (Edge edge : result.edges) {
            assertNotEquals(edge.from, edge.to);
        }
    }

    @Test
    void selectsCheapestParallelEdge() {
        Graph graph = new Graph(2, false);

        graph.addEdge(0, 1, 7);
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 1, 5);

        MSTResult result = KruskalMST.compute(graph);

        assertEquals(3L, result.totalWeight);
        assertEquals(1, result.components);
        assertEquals(1, result.edgeCount());
        assertEquals(3, result.edges.get(0).weight);
    }

    @Test
    void preservesIsolatedVertices() {
        Graph graph = new Graph(5, false);

        graph.addEdge(0, 1, 3);
        graph.addEdge(1, 2, 4);

        MSTResult result = KruskalMST.compute(graph);

        assertEquals(7L, result.totalWeight);
        assertEquals(3, result.components);
        assertEquals(2, result.edgeCount());
    }

    @Test
    void handlesEmptyAndSingleVertexGraphs() {
        Graph empty = new Graph(0, false);

        MSTResult emptyResult = KruskalMST.compute(empty);

        assertEquals(0L, emptyResult.totalWeight);
        assertEquals(0, emptyResult.components);
        assertEquals(0, emptyResult.edgeCount());

        Graph single = new Graph(1, false);
        single.addEdge(0, 0, -100);

        MSTResult singleResult = KruskalMST.compute(single);

        assertEquals(0L, singleResult.totalWeight);
        assertEquals(1, singleResult.components);
        assertEquals(0, singleResult.edgeCount());
    }

    @Test
    void rejectsDirectedGraphs() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 5);

        assertThrows(IllegalArgumentException.class,
                () -> KruskalMST.compute(graph));
    }

    @Test
    void recordsActualOperationCounters() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 1, 10);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 2, 3);

        MSTResult result = KruskalMST.compute(graph);

        assertEquals(4L, result.totalWeight);
        assertEquals(2, result.metrics.edgeScans);
        assertEquals(10, result.metrics.dsuOps);
        assertTrue(result.metrics.comparisons >= 2);
        assertEquals(0, result.metrics.maxDepth);
    }

    @Test
    void doesNotModifyOriginalGraph() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 1, 10);
        graph.addEdge(1, 2, 1);
        graph.addEdge(0, 2, 3);

        MSTResult result = KruskalMST.compute(graph);

        assertEquals(3, graph.edgeCount());
        assertEquals(10, graph.edges().get(0).weight);
        assertEquals(1, graph.edges().get(1).weight);
        assertEquals(3, graph.edges().get(2).weight);

        assertThrows(UnsupportedOperationException.class,
                () -> result.edges.clear());
    }
}