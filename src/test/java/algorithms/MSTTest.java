package algorithms;

import graph.Edge;
import graph.Graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Random;

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

    @Test
    void primMatchesKruskalOnConnectedGraph() {
        Graph graph = new Graph(4, false);

        graph.addEdge(0, 1, 4);
        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 1, 2);
        graph.addEdge(1, 3, 1);
        graph.addEdge(2, 3, 5);

        MSTResult kruskal = KruskalMST.compute(graph);
        MSTResult prim = PrimMST.compute(graph);

        assertEquals(4L, prim.totalWeight);
        assertEquals(kruskal.totalWeight, prim.totalWeight);
        assertEquals(1, prim.components);
        assertEquals(3, prim.edgeCount());

        assertValidForest(prim, graph.vertexCount());
    }

    @Test
    void primMatchesKruskalOnDisconnectedGraph() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_disconnected.txt"));

        MSTResult kruskal = KruskalMST.compute(graph);
        MSTResult prim = PrimMST.compute(graph);

        assertEquals(9L, prim.totalWeight);
        assertEquals(4, prim.components);
        assertEquals(3, prim.edgeCount());

        assertEquals(kruskal.totalWeight, prim.totalWeight);
        assertEquals(kruskal.components, prim.components);

        assertEquals(12, prim.metrics.edgeScans);
        assertEquals(0, prim.metrics.dsuOps);
        assertEquals(0, prim.metrics.maxDepth);

        assertValidForest(prim, graph.vertexCount());
    }

    @Test
    void primHandlesNegativeWeightsAndSelfLoops() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 0, -100);
        graph.addEdge(0, 1, -5);
        graph.addEdge(1, 2, 2);
        graph.addEdge(0, 2, 10);

        MSTResult result = PrimMST.compute(graph);

        assertEquals(-3L, result.totalWeight);
        assertEquals(1, result.components);
        assertEquals(2, result.edgeCount());

        assertEquals(8, result.metrics.edgeScans);

        for (Edge edge : result.edges) {
            assertNotEquals(edge.from, edge.to);
        }

        assertValidForest(result, graph.vertexCount());
    }

    @Test
    void primChoosesCheapestParallelEdge() {
        Graph graph = new Graph(2, false);

        graph.addEdge(0, 1, 7);
        graph.addEdge(0, 1, 3);
        graph.addEdge(0, 1, 5);

        MSTResult result = PrimMST.compute(graph);

        assertEquals(3L, result.totalWeight);
        assertEquals(1, result.components);
        assertEquals(1, result.edgeCount());
        assertEquals(3, result.edges.get(0).weight);

        assertEquals(6, result.metrics.edgeScans);
        assertTrue(result.metrics.comparisons > 0);
    }

    @Test
    void primHandlesEmptySingleAndIsolatedVertices() {
        Graph empty = new Graph(0, false);

        MSTResult emptyResult = PrimMST.compute(empty);

        assertEquals(0L, emptyResult.totalWeight);
        assertEquals(0, emptyResult.components);
        assertEquals(0, emptyResult.edgeCount());

        Graph single = new Graph(1, false);
        single.addEdge(0, 0, -100);

        MSTResult singleResult = PrimMST.compute(single);

        assertEquals(0L, singleResult.totalWeight);
        assertEquals(1, singleResult.components);
        assertEquals(0, singleResult.edgeCount());

        Graph disconnected = new Graph(5, false);
        disconnected.addEdge(0, 1, 3);

        MSTResult forest = PrimMST.compute(disconnected);

        assertEquals(3L, forest.totalWeight);
        assertEquals(4, forest.components);
        assertEquals(1, forest.edgeCount());

        assertValidForest(forest, disconnected.vertexCount());
    }

    @Test
    void primRejectsDirectedGraphs() {
        Graph graph = new Graph(3, true);
        graph.addEdge(0, 1, 5);

        assertThrows(IllegalArgumentException.class,
                () -> PrimMST.compute(graph));
    }

    @Test
    void comparesBothAlgorithmsOnOneHundredRandomConnectedGraphs() {
        Random random = new Random(12345);

        for (int trial = 0; trial < 100; trial++) {
            int n = 2 + random.nextInt(39);
            Graph graph = new Graph(n, false);

            for (int v = 1; v < n; v++) {
                int parent = random.nextInt(v);
                int weight = random.nextInt(201) - 100;

                graph.addEdge(parent, v, weight);
            }

            int extraEdges = n + random.nextInt(n * 3);

            for (int i = 0; i < extraEdges; i++) {
                int from = random.nextInt(n);
                int to = random.nextInt(n);
                int weight = random.nextInt(201) - 100;

                graph.addEdge(from, to, weight);
            }

            MSTResult kruskal = KruskalMST.compute(graph);
            MSTResult prim = PrimMST.compute(graph);

            assertEquals(
                    kruskal.totalWeight,
                    prim.totalWeight,
                    "Weight mismatch on trial " + trial
            );

            assertEquals(1, kruskal.components);
            assertEquals(1, prim.components);

            assertEquals(n - 1, kruskal.edgeCount());
            assertEquals(n - 1, prim.edgeCount());

            assertEquals(
                    2L * graph.edgeCount(),
                    prim.metrics.edgeScans
            );

            assertValidForest(kruskal, n);
            assertValidForest(prim, n);
        }
    }

    private static void assertValidForest(MSTResult result, int n) {
        DisjointSetUnion check = new DisjointSetUnion(n);
        long weightSum = 0;

        for (Edge edge : result.edges) {
            assertTrue(
                    check.union(edge.from, edge.to),
                    "The selected edges contain a cycle"
            );

            weightSum += edge.weight;
        }

        assertEquals(result.totalWeight, weightSum);
        assertEquals(result.components, check.components());
        assertEquals(n - result.components, result.edgeCount());
    }
}