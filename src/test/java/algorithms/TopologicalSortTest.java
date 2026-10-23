package algorithms;

import graph.Graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalSortTest {

    @Test
    void sortsSampleDagAndValidatesEveryEdge() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_directed.txt"));

        TopologicalResult result = KahnTopologicalSort.compute(graph);

        assertFalse(result.hasCycle);
        assertEquals(6, result.processedVertices);
        assertEquals(6, result.orderLength());

        assertEquals(7, result.metrics.edgeScans);
        assertEquals(0, result.metrics.dsuOps);
        assertEquals(0, result.metrics.maxDepth);

        assertTrue(KahnTopologicalSort.isValidOrder(
                graph, result.order()));
    }

    @Test
    void detectsCycleInInputFile() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_cyclic.txt"));

        TopologicalResult result = KahnTopologicalSort.compute(graph);

        assertTrue(result.hasCycle);
        assertEquals(1, result.processedVertices);
        assertEquals(0, result.orderLength());

        assertFalse(KahnTopologicalSort.isValidOrder(
                graph, result.order()));
    }

    @Test
    void reportsCycleAfterProcessingAcyclicVertices() {
        Graph graph = new Graph(5, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 1, 1);
        graph.addEdge(3, 4, 1);

        TopologicalResult result = KahnTopologicalSort.compute(graph);

        assertTrue(result.hasCycle);
        assertEquals(3, result.processedVertices);
        assertEquals(0, result.orderLength());
        assertEquals(2, result.metrics.edgeScans);
    }

    @Test
    void handlesDisconnectedAndEmptyGraphs() {
        Graph graph = new Graph(5, true);

        graph.addEdge(0, 1, 3);
        graph.addEdge(3, 4, 2);

        TopologicalResult result = KahnTopologicalSort.compute(graph);

        assertFalse(result.hasCycle);
        assertEquals(5, result.processedVertices);
        assertEquals(5, result.orderLength());
        assertEquals(2, result.metrics.edgeScans);

        assertTrue(KahnTopologicalSort.isValidOrder(
                graph, result.order()));

        Graph empty = new Graph(0, true);

        TopologicalResult emptyResult = KahnTopologicalSort.compute(empty);

        assertFalse(emptyResult.hasCycle);
        assertEquals(0, emptyResult.processedVertices);
        assertEquals(0, emptyResult.orderLength());
        assertEquals(0, emptyResult.metrics.edgeScans);

        assertTrue(KahnTopologicalSort.isValidOrder(
                empty, emptyResult.order()));
    }

    @Test
    void handlesParallelEdgesAndSelfLoops() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 2, -3);

        TopologicalResult result = KahnTopologicalSort.compute(graph);

        assertFalse(result.hasCycle);
        assertEquals(3, result.processedVertices);
        assertEquals(3, result.metrics.edgeScans);

        assertTrue(KahnTopologicalSort.isValidOrder(
                graph, result.order()));

        Graph cyclic = new Graph(3, true);
        cyclic.addEdge(0, 0, -5);
        cyclic.addEdge(1, 2, 1);

        TopologicalResult cyclicResult =
                KahnTopologicalSort.compute(cyclic);

        assertTrue(cyclicResult.hasCycle);
        assertEquals(2, cyclicResult.processedVertices);
        assertEquals(0, cyclicResult.orderLength());
    }

    @Test
    void rejectsInvalidTopologicalOrders() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 1);
        graph.addEdge(1, 2, 1);

        assertTrue(KahnTopologicalSort.isValidOrder(
                graph, new int[]{0, 1, 2}));

        assertFalse(KahnTopologicalSort.isValidOrder(
                graph, new int[]{1, 0, 2}));

        assertFalse(KahnTopologicalSort.isValidOrder(
                graph, new int[]{0, 2, 1}));

        assertFalse(KahnTopologicalSort.isValidOrder(
                graph, new int[]{0, 0, 2}));

        assertFalse(KahnTopologicalSort.isValidOrder(
                graph, new int[]{-1, 1, 2}));

        assertFalse(KahnTopologicalSort.isValidOrder(
                graph, new int[]{0, 1, 3}));

        assertFalse(KahnTopologicalSort.isValidOrder(
                graph, new int[]{0, 1}));
    }

    @Test
    void rejectsUndirectedGraphsAndProtectsResult() {
        Graph undirected = new Graph(3, false);

        undirected.addEdge(0, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> KahnTopologicalSort.compute(undirected));

        assertThrows(IllegalArgumentException.class,
                () -> KahnTopologicalSort.isValidOrder(
                        undirected, new int[]{0, 1, 2}));

        Graph directed = new Graph(2, true);
        directed.addEdge(0, 1, 1);

        TopologicalResult result = KahnTopologicalSort.compute(directed);

        int[] modified = result.order();
        modified[0] = 1;

        assertArrayEquals(new int[]{0, 1}, result.order());
    }

    @Test
    void sortsOneHundredThousandVertexDag() {
        int n = 100_000;

        Graph graph = new Graph(n, true);

        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, 1);
        }

        TopologicalResult result = KahnTopologicalSort.compute(graph);

        assertFalse(result.hasCycle);
        assertEquals(n, result.processedVertices);
        assertEquals(n, result.orderLength());

        assertEquals(
                n - 1,
                result.metrics.edgeScans
        );

        assertTrue(KahnTopologicalSort.isValidOrder(
                graph, result.order()));

        int[] order = result.order();

        assertEquals(0, order[0]);
        assertEquals(n - 1, order[n - 1]);
    }
}