package algorithms;

import graph.Graph;
import reference.BruteForceSCC;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SCCReferenceTest {

    @Test
    void matchesReferenceOnCyclicInputFile() throws IOException {
        Graph graph = Graph.fromFile(
                Path.of("data", "small_cyclic.txt"));

        SCCResult result = KosarajuSCC.compute(graph);

        assertMatchesReference(graph);
        assertEquals(3, result.components);
    }

    @Test
    void matchesReferenceOnOneHundredRandomGraphs() {
        Random random = new Random(2026);

        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + random.nextInt(200);
            int m = random.nextInt(5 * n + 1);

            Graph graph = new Graph(n, true);

            for (int i = 0; i < m; i++) {
                int from = random.nextInt(n);
                int to = random.nextInt(n);
                int weight = random.nextInt(201) - 100;

                graph.addEdge(from, to, weight);
            }

            assertMatchesReference(graph);
        }
    }

    @Test
    void matchesReferenceAtTwoHundredVertices() {
        Graph graph = new Graph(200, true);

        for (int i = 0; i < 99; i++) {
            graph.addEdge(i, i + 1, 1);
        }

        graph.addEdge(99, 0, 1);

        for (int i = 100; i < 149; i++) {
            graph.addEdge(i, i + 1, 1);
        }

        graph.addEdge(149, 100, 1);

        graph.addEdge(99, 100, 1);
        graph.addEdge(149, 150, 1);

        SCCResult result = KosarajuSCC.compute(graph);

        assertMatchesReference(graph);
        assertEquals(52, result.components);
    }

    @Test
    void referenceRejectsUnsupportedGraphs() {
        Graph tooLarge = new Graph(201, true);

        assertThrows(IllegalArgumentException.class,
                () -> BruteForceSCC.componentIds(tooLarge));

        Graph undirected = new Graph(3, false);

        assertThrows(IllegalArgumentException.class,
                () -> BruteForceSCC.componentIds(undirected));

        Graph empty = new Graph(0, true);

        assertEquals(0, BruteForceSCC.componentIds(empty).length);
    }

    private static void assertMatchesReference(Graph graph) {
        int n = graph.vertexCount();

        int[] expected = BruteForceSCC.componentIds(graph);
        SCCResult actual = KosarajuSCC.compute(graph);

        HashSet<Integer> referenceGroups = new HashSet<>();
        HashSet<Integer> kosarajuGroups = new HashSet<>();

        for (int v = 0; v < n; v++) {
            referenceGroups.add(expected[v]);
            kosarajuGroups.add(actual.componentOf(v));
        }

        assertEquals(referenceGroups.size(), actual.components);
        assertEquals(referenceGroups.size(), kosarajuGroups.size());

        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                boolean expectedSame = expected[u] == expected[v];

                boolean actualSame =
                        actual.componentOf(u) == actual.componentOf(v);

                assertEquals(
                        expectedSame,
                        actualSame,
                        "SCC mismatch for vertices " + u + " and " + v
                );
            }
        }

        assertEquals(
                2L * graph.edgeCount(),
                actual.metrics.edgeScans
        );
    }
}