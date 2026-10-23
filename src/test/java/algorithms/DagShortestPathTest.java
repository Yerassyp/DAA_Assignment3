package algorithms;

import graph.Graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathTest {

    @Test
    void findsShortestPathsInSampleDag() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_directed.txt"));

        DagShortestPathResult result = DagShortestPath.compute(graph);

        assertEquals(6, result.vertexCount());

        assertArrayEquals(
                new long[]{0, 2, -1, 6, 2, 0},
                result.distances()
        );

        assertEquals(9L, result.sumDistances());
        assertEquals(7, result.metrics.edgeScans);
        assertEquals(7, result.metrics.comparisons);

        assertEquals(List.of(0, 1, 2, 4), result.pathTo(4));
        assertEquals(List.of(0, 1, 3), result.pathTo(3));
        assertEquals(List.of(5), result.pathTo(5));

        assertEquals(2, result.parentOf(4));
        assertEquals(-1, result.parentOf(5));
    }

    @Test
    void handlesMultipleSourcesAndNegativeEdges() {
        Graph graph = new Graph(5, true);

        graph.addEdge(0, 2, 10);
        graph.addEdge(1, 2, 3);
        graph.addEdge(2, 3, -8);
        graph.addEdge(4, 3, 2);

        DagShortestPathResult result = DagShortestPath.compute(graph);

        assertArrayEquals(
                new long[]{0, 0, 3, -5, 0},
                result.distances()
        );

        assertEquals(-2L, result.sumDistances());
        assertEquals(4, result.metrics.edgeScans);

        assertEquals(List.of(1, 2, 3), result.pathTo(3));
        assertEquals(List.of(0), result.pathTo(0));
        assertEquals(List.of(4), result.pathTo(4));

        assertEquals(1, result.parentOf(2));
        assertEquals(2, result.parentOf(3));
    }

    @Test
    void doesNotTreatInternalVerticesAsSources() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 7);
        graph.addEdge(1, 2, 3);

        DagShortestPathResult result = DagShortestPath.compute(graph);

        assertArrayEquals(
                new long[]{0, 7, 10},
                result.distances()
        );

        assertEquals(17L, result.sumDistances());
        assertEquals(List.of(0, 1, 2), result.pathTo(2));
    }

    @Test
    void supportsParallelEdgesAndNegativeWeights() {
        Graph graph = new Graph(3, true);

        graph.addEdge(0, 1, 8);
        graph.addEdge(0, 1, -2);
        graph.addEdge(1, 2, 5);
        graph.addEdge(0, 2, 10);

        DagShortestPathResult result = DagShortestPath.compute(graph);

        assertArrayEquals(
                new long[]{0, -2, 3},
                result.distances()
        );

        assertEquals(1L, result.sumDistances());
        assertEquals(4, result.metrics.edgeScans);
        assertEquals(List.of(0, 1, 2), result.pathTo(2));
    }

    @Test
    void handlesEmptyAndIsolatedVertices() {
        Graph empty = new Graph(0, true);

        DagShortestPathResult emptyResult =
                DagShortestPath.compute(empty);

        assertEquals(0, emptyResult.vertexCount());
        assertEquals(0L, emptyResult.sumDistances());
        assertEquals(0, emptyResult.metrics.edgeScans);

        assertThrows(IllegalArgumentException.class,
                () -> emptyResult.pathTo(0));

        Graph isolated = new Graph(4, true);

        DagShortestPathResult result =
                DagShortestPath.compute(isolated);

        assertArrayEquals(
                new long[]{0, 0, 0, 0},
                result.distances()
        );

        assertEquals(0L, result.sumDistances());
        assertEquals(0, result.metrics.edgeScans);

        for (int v = 0; v < 4; v++) {
            assertEquals(-1, result.parentOf(v));
            assertEquals(List.of(v), result.pathTo(v));
        }
    }

    @Test
    void rejectsCyclesAndSelfLoops() throws IOException {
        Graph cyclic = Graph.fromFile(
                Path.of("data", "small_cyclic.txt"));

        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPath.compute(cyclic));

        Graph selfLoop = new Graph(2, true);

        selfLoop.addEdge(0, 0, -5);
        selfLoop.addEdge(0, 1, 3);

        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPath.compute(selfLoop));
    }

    @Test
    void rejectsUndirectedGraphs() {
        Graph graph = new Graph(3, false);

        graph.addEdge(0, 1, 5);

        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPath.compute(graph));
    }

    @Test
    void protectsResultsAndRejectsInvalidVertices() {
        Graph graph = new Graph(2, true);

        graph.addEdge(0, 1, 3);

        DagShortestPathResult result = DagShortestPath.compute(graph);

        long[] distances = result.distances();
        distances[1] = -999;

        int[] parents = result.parents();
        parents[1] = -1;

        assertEquals(3L, result.distanceTo(1));
        assertEquals(0, result.parentOf(1));

        List<Integer> path = result.pathTo(1);

        assertEquals(List.of(0, 1), path);

        assertThrows(UnsupportedOperationException.class,
                () -> path.clear());

        assertThrows(IllegalArgumentException.class,
                () -> result.distanceTo(-1));

        assertThrows(IllegalArgumentException.class,
                () -> result.parentOf(2));

        assertThrows(IllegalArgumentException.class,
                () -> result.pathTo(2));
    }

    @Test
    void handlesOneHundredThousandVertexDag() {
        int n = 100_000;

        Graph graph = new Graph(n, true);

        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, -1);
        }

        DagShortestPathResult result = DagShortestPath.compute(graph);

        assertEquals(n, result.vertexCount());
        assertEquals(0L, result.distanceTo(0));
        assertEquals(-99_999L, result.distanceTo(n - 1));

        assertEquals(
                -4_999_950_000L,
                result.sumDistances()
        );

        assertEquals(n - 1, result.metrics.edgeScans);
        assertEquals(n - 2, result.parentOf(n - 1));

        List<Integer> path = result.pathTo(n - 1);

        assertEquals(n, path.size());
        assertEquals(0, path.get(0));
        assertEquals(n - 1, path.get(n - 1));
    }

    @Test
    void usesLongForLargePathWeights() {
        Graph graph = new Graph(4, true);

        graph.addEdge(0, 1, 2_000_000_000);
        graph.addEdge(1, 2, 2_000_000_000);
        graph.addEdge(2, 3, 2_000_000_000);

        DagShortestPathResult result = DagShortestPath.compute(graph);

        assertArrayEquals(
                new long[]{
                        0L,
                        2_000_000_000L,
                        4_000_000_000L,
                        6_000_000_000L
                },
                result.distances()
        );

        assertEquals(12_000_000_000L, result.sumDistances());
        assertEquals(3, result.metrics.edgeScans);

        assertEquals(List.of(0, 1, 2, 3), result.pathTo(3));
    }
}