package algorithms;

import graph.Graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class SCCTest {

    @Test
    void findsComponentsInCyclicFile() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_cyclic.txt"));

        SCCResult result = KosarajuSCC.compute(graph);

        assertEquals(3, result.components);
        assertEquals(5, result.vertexCount());

        assertEquals(result.componentOf(0), result.componentOf(1));
        assertEquals(result.componentOf(1), result.componentOf(2));

        assertNotEquals(result.componentOf(0), result.componentOf(3));
        assertNotEquals(result.componentOf(0), result.componentOf(4));
        assertNotEquals(result.componentOf(3), result.componentOf(4));

        assertEquals(12, result.metrics.edgeScans);
        assertTrue(result.metrics.maxDepth > 0);
    }

    @Test
    void findsSingleVertexComponentsInDag() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_directed.txt"));

        SCCResult result = KosarajuSCC.compute(graph);

        assertEquals(6, result.components);
        assertEquals(14, result.metrics.edgeScans);

        HashSet<Integer> ids = new HashSet<>();

        for (int v = 0; v < graph.vertexCount(); v++) {
            ids.add(result.componentOf(v));
        }

        assertEquals(6, ids.size());
    }

    @Test
    void findsMultipleStronglyConnectedGroups() {
        Graph graph = new Graph(8, true);

        graph.addEdge(0, 1, 4);
        graph.addEdge(1, 0, 2);
        graph.addEdge(1, 2, 1);

        graph.addEdge(2, 3, 1);
        graph.addEdge(3, 2, 1);
        graph.addEdge(3, 4, 1);

        graph.addEdge(5, 6, 1);
        graph.addEdge(6, 5, 1);
        graph.addEdge(5, 5, 1);
        graph.addEdge(6, 7, 1);

        SCCResult result = KosarajuSCC.compute(graph);

        assertEquals(5, result.components);
        assertEquals(20, result.metrics.edgeScans);

        assertEquals(result.componentOf(0), result.componentOf(1));
        assertEquals(result.componentOf(2), result.componentOf(3));
        assertEquals(result.componentOf(5), result.componentOf(6));

        HashSet<Integer> groups = new HashSet<>();

        groups.add(result.componentOf(0));
        groups.add(result.componentOf(2));
        groups.add(result.componentOf(4));
        groups.add(result.componentOf(5));
        groups.add(result.componentOf(7));

        assertEquals(5, groups.size());
    }

    @Test
    void handlesParallelEdgesAndSelfLoops() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 5);
        graph.addEdge(0, 1, 2);
        graph.addEdge(1, 0, 3);
        graph.addEdge(1, 2, 1);
        graph.addEdge(2, 2, -5);

        SCCResult result = KosarajuSCC.compute(graph);

        assertEquals(2, result.components);
        assertEquals(10, result.metrics.edgeScans);

        assertEquals(result.componentOf(0), result.componentOf(1));
        assertNotEquals(result.componentOf(1), result.componentOf(2));
    }

    @Test
    void handlesEmptyAndIsolatedVertices() {
        Graph empty = new Graph(0, true);

        SCCResult emptyResult = KosarajuSCC.compute(empty);

        assertEquals(0, emptyResult.components);
        assertEquals(0, emptyResult.vertexCount());
        assertEquals(0, emptyResult.metrics.edgeScans);
        assertEquals(0, emptyResult.metrics.maxDepth);

        Graph isolated = new Graph(4, true);

        SCCResult isolatedResult = KosarajuSCC.compute(isolated);

        assertEquals(4, isolatedResult.components);
        assertEquals(0, isolatedResult.metrics.edgeScans);
        assertEquals(1, isolatedResult.metrics.maxDepth);

        Graph single = new Graph(1, true);
        single.addEdge(0, 0, 10);

        SCCResult singleResult = KosarajuSCC.compute(single);

        assertEquals(1, singleResult.components);
        assertEquals(2, singleResult.metrics.edgeScans);
        assertEquals(1, singleResult.metrics.maxDepth);
    }

    @Test
    void handlesLongDirectedChainIteratively() {
        int n = 5000;
        Graph graph = new Graph(n, true);

        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, 1);
        }

        SCCResult result = KosarajuSCC.compute(graph);

        assertEquals(n, result.components);
        assertEquals(2L * (n - 1), result.metrics.edgeScans);
        assertEquals(n, result.metrics.maxDepth);

        assertNotEquals(
                result.componentOf(0),
                result.componentOf(n - 1));
    }

    @Test
    void repeatedComputationsGiveSameResult() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_cyclic.txt"));

        SCCResult first = KosarajuSCC.compute(graph);
        SCCResult second = KosarajuSCC.compute(graph);

        assertEquals(first.components, second.components);
        assertEquals(first.metrics.edgeScans, second.metrics.edgeScans);

        for (int v = 0; v < graph.vertexCount(); v++) {
            assertEquals(first.componentOf(v), second.componentOf(v));
        }

        assertEquals(6, graph.edgeCount());
        assertEquals(2, graph.neighbors(1).size());
    }

    @Test
    void rejectsUndirectedGraphs() {
        Graph graph = new Graph(3, false);
        graph.addEdge(0, 1, 1);

        assertThrows(IllegalArgumentException.class,
                () -> KosarajuSCC.compute(graph));
    }
}