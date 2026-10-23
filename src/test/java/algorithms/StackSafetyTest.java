package algorithms;

import graph.Graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StackSafetyTest {

    @Test
    void handlesChainOfOneHundredThousandVertices() {
        int n = 100_000;

        Graph graph = new Graph(n, true);

        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, 1);
        }

        SCCResult result = assertDoesNotThrow(
                () -> KosarajuSCC.compute(graph));

        assertEquals(n, result.components);
        assertEquals(n, result.vertexCount());

        assertEquals(
                2L * (n - 1),
                result.metrics.edgeScans
        );

        assertEquals(n, result.metrics.maxDepth);

        assertNotEquals(
                result.componentOf(0),
                result.componentOf(n - 1)
        );
    }

    @Test
    void handlesLargeSingleStronglyConnectedComponent() {
        int n = 100_000;

        Graph graph = new Graph(n, true);

        for (int i = 0; i < n - 1; i++) {
            graph.addEdge(i, i + 1, 1);
        }

        graph.addEdge(n - 1, 0, 1);

        SCCResult result = assertDoesNotThrow(
                () -> KosarajuSCC.compute(graph));

        assertEquals(1, result.components);
        assertEquals(n, result.vertexCount());

        assertEquals(
                2L * n,
                result.metrics.edgeScans
        );

        assertEquals(n, result.metrics.maxDepth);

        assertEquals(
                result.componentOf(0),
                result.componentOf(n - 1)
        );
    }
}