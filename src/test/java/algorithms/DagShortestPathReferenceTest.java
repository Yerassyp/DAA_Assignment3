package algorithms;

import graph.Edge;
import graph.Graph;
import reference.BellmanFord;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathReferenceTest {

    @Test
    void matchesProvidedDag() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_directed.txt"));

        assertMatchesReference(graph);

        assertArrayEquals(
                new long[]{0, 2, -1, 6, 2, 0},
                BellmanFord.compute(graph)
        );
    }

    @Test
    void matchesOneHundredRandomDags() {
        Random random = new Random(2026);

        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + random.nextInt(200);
            int[] order = new int[n];

            for (int i = 0; i < n; i++) {
                order[i] = i;
            }

            for (int i = n - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);

                int temp = order[i];
                order[i] = order[j];
                order[j] = temp;
            }

            Graph graph = new Graph(n, true);

            if (n > 1) {
                int m = random.nextInt(6 * n + 1);

                for (int e = 0; e < m; e++) {
                    int a = random.nextInt(n);
                    int b = random.nextInt(n - 1);

                    if (b >= a) {
                        b++;
                    }

                    int from = order[Math.min(a, b)];
                    int to = order[Math.max(a, b)];
                    int weight = random.nextInt(2001) - 1000;

                    graph.addEdge(from, to, weight);
                }
            }

            assertMatchesReference(graph);
        }
    }

    @Test
    void matchesDenseDagWithNegativeWeights() {
        int n = 200;

        Graph graph = new Graph(n, true);
        Random random = new Random(77);

        for (int from = 0; from < n; from++) {
            for (int to = from + 1; to < n; to++) {
                if (random.nextInt(3) == 0) {
                    int weight = random.nextInt(2001) - 1000;
                    graph.addEdge(from, to, weight);
                }
            }
        }

        assertMatchesReference(graph);
    }

    @Test
    void handlesMultipleSourcesAndIsolatedVertices() {
        Graph graph = new Graph(8, true);

        graph.addEdge(0, 3, 8);
        graph.addEdge(1, 3, -4);
        graph.addEdge(2, 4, 7);
        graph.addEdge(3, 5, -2);
        graph.addEdge(4, 5, 1);
        graph.addEdge(6, 5, -10);

        assertMatchesReference(graph);

        DagShortestPathResult result =
                DagShortestPath.compute(graph);

        assertArrayEquals(
                new long[]{0, 0, 0, -4, 7, -10, 0, 0},
                result.distances()
        );

        assertEquals(-7L, result.sumDistances());
        assertEquals(List.of(6, 5), result.pathTo(5));
        assertEquals(List.of(7), result.pathTo(7));
    }

    @Test
    void handlesParallelEdgesAndEqualDistances() {
        Graph graph = new Graph(4, true);

        graph.addEdge(0, 2, 10);
        graph.addEdge(0, 2, -6);
        graph.addEdge(1, 2, -6);
        graph.addEdge(2, 3, 4);
        graph.addEdge(0, 3, 100);
        graph.addEdge(1, 3, -2);

        assertMatchesReference(graph);

        DagShortestPathResult result =
                DagShortestPath.compute(graph);

        assertArrayEquals(
                new long[]{0, 0, -6, -2},
                result.distances()
        );

        assertEquals(-8L, result.sumDistances());
        assertEquals(6, result.metrics.edgeScans);
    }

    @Test
    void handlesReverseOrderedChain() {
        int n = 200;

        Graph graph = new Graph(n, true);

        for (int i = n - 2; i >= 0; i--) {
            graph.addEdge(i, i + 1, -1);
        }

        assertMatchesReference(graph);

        DagShortestPathResult result =
                DagShortestPath.compute(graph);

        assertEquals(-199L, result.distanceTo(199));
        assertEquals(-19_900L, result.sumDistances());
        assertEquals(199, result.metrics.edgeScans);
    }

    @Test
    void handlesEmptyAndRejectsUndirectedGraphs() {
        Graph empty = new Graph(0, true);

        assertMatchesReference(empty);

        assertArrayEquals(
                new long[0],
                BellmanFord.compute(empty)
        );

        Graph single = new Graph(1, true);

        assertMatchesReference(single);

        assertArrayEquals(
                new long[]{0},
                BellmanFord.compute(single)
        );

        Graph undirected = new Graph(3, false);
        undirected.addEdge(0, 1, 4);

        assertThrows(IllegalArgumentException.class,
                () -> BellmanFord.compute(undirected));

        assertThrows(IllegalArgumentException.class,
                () -> DagShortestPath.compute(undirected));
    }

    private static void assertMatchesReference(Graph graph) {
        long[] expected = BellmanFord.compute(graph);

        DagShortestPathResult result =
                DagShortestPath.compute(graph);

        assertArrayEquals(expected, result.distances());

        assertEquals(
                (long) graph.edgeCount(),
                result.metrics.edgeScans
        );

        int n = graph.vertexCount();
        int[] inDegree = new int[n];

        for (Edge edge : graph.edges()) {
            inDegree[edge.to]++;
        }

        long expectedSum = 0;

        for (int vertex = 0; vertex < n; vertex++) {
            expectedSum += expected[vertex];

            List<Integer> path = result.pathTo(vertex);

            assertFalse(path.isEmpty());
            assertTrue(path.size() <= n);

            assertEquals(
                    vertex,
                    path.get(path.size() - 1).intValue()
            );

            assertEquals(0, inDegree[path.get(0)]);

            if (inDegree[vertex] == 0) {
                assertEquals(0L, result.distanceTo(vertex));
                assertEquals(-1, result.parentOf(vertex));
                assertEquals(List.of(vertex), path);
            } else {
                assertTrue(result.parentOf(vertex) >= 0);
            }

            for (int i = 1; i < path.size(); i++) {
                int from = path.get(i - 1);
                int to = path.get(i);

                assertEquals(from, result.parentOf(to));

                boolean validEdge = false;

                for (Edge edge : graph.neighbors(from)) {
                    if (edge.to == to
                            && result.distanceTo(from)
                            + (long) edge.weight
                            == result.distanceTo(to)) {

                        validEdge = true;
                        break;
                    }
                }

                assertTrue(
                        validEdge,
                        "Invalid shortest path edge: "
                                + from + " -> " + to
                );
            }
        }

        assertEquals(expectedSum, result.sumDistances());
    }
}