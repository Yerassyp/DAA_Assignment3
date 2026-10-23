package benchmark;

import algorithms.DagShortestPath;
import algorithms.DagShortestPathResult;
import algorithms.KahnTopologicalSort;
import algorithms.KosarajuSCC;
import algorithms.KruskalMST;
import algorithms.MSTResult;
import algorithms.PrimMST;
import algorithms.SCCResult;
import algorithms.TopologicalResult;

import graph.Graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FullReferenceValuesTest {

    @Test
    void matchesAllTenReferenceRows() {
        int[] vertexCounts = {
                1000, 1000,
                3162, 3162,
                10000, 10000,
                31623, 31623,
                100000, 100000
        };

        int[] edgeCounts = {
                4000, 20000,
                12648, 63240,
                40000, 200000,
                126492, 632460,
                400000, 2000000
        };

        long[] expectedMst = {
                147970, 30363,
                468911, 97589,
                1486139, 309550,
                4715515, 968517,
                15012094, 3056681
        };

        int[] expectedScc = {
                27, 1,
                137, 1,
                373, 1,
                1188, 1,
                3923, 1
        };

        long[] expectedDag = {
                396274, 252411,
                1271911, 723157,
                3941828, 2381342,
                12551891, 7376612,
                40096539, 23512087
        };

        for (int i = 0; i < vertexCounts.length; i++) {
            int n = vertexCounts[i];
            int m = edgeCounts[i];

            String label = n + "/" + m;

            System.out.println("CHECKING: " + label);

            long mst = verifyMst(
                    n, m, expectedMst[i], label);

            int scc = verifyScc(
                    n, m, expectedScc[i], label);

            long dag = verifyDag(
                    n, m, expectedDag[i], label);

            System.out.println(
                    "PASS " + label
                            + " | MST=" + mst
                            + " | SCC=" + scc
                            + " | DAG=" + dag
            );
        }

        System.out.println(
                "ALL 10 REFERENCE ROWS MATCHED (30 VALUES)");
    }

    private static long verifyMst(
            int n, int m, long expected, String label) {

        Graph graph = GraphGenerator.undirected(n, m);

        assertEquals(n, graph.vertexCount());
        assertEquals(m, graph.edgeCount());

        MSTResult kruskal = KruskalMST.compute(graph);
        MSTResult prim = PrimMST.compute(graph);

        assertEquals(
                expected,
                kruskal.totalWeight,
                "Kruskal reference mismatch: " + label
        );

        assertEquals(
                expected,
                prim.totalWeight,
                "Prim reference mismatch: " + label
        );

        assertEquals(
                kruskal.totalWeight,
                prim.totalWeight,
                "MST algorithms disagree: " + label
        );

        assertEquals(1, kruskal.components);
        assertEquals(1, prim.components);

        assertEquals(n - 1, kruskal.edgeCount());
        assertEquals(n - 1, prim.edgeCount());

        assertEquals(
                2L * m,
                prim.metrics.edgeScans,
                "Prim edge scans mismatch: " + label
        );

        assertTrue(kruskal.metrics.edgeScans >= n - 1);
        assertTrue(kruskal.metrics.edgeScans <= m);

        assertTrue(kruskal.metrics.dsuOps > 0);
        assertTrue(kruskal.metrics.comparisons > 0);

        return kruskal.totalWeight;
    }

    private static int verifyScc(
            int n, int m, int expected, String label) {

        Graph graph = GraphGenerator.directed(n, m);

        assertEquals(n, graph.vertexCount());
        assertEquals(m, graph.edgeCount());

        SCCResult result = KosarajuSCC.compute(graph);

        assertEquals(
                expected,
                result.components,
                "SCC reference mismatch: " + label
        );

        assertEquals(n, result.vertexCount());

        assertEquals(
                2L * m,
                result.metrics.edgeScans,
                "Kosaraju edge scans mismatch: " + label
        );

        assertTrue(result.metrics.maxDepth > 0);

        return result.components;
    }

    private static long verifyDag(
            int n, int m, long expected, String label) {

        Graph graph = GraphGenerator.dag(n, m);

        assertEquals(n, graph.vertexCount());
        assertEquals(m, graph.edgeCount());

        TopologicalResult topo =
                KahnTopologicalSort.compute(graph);

        assertFalse(
                topo.hasCycle,
                "Generated DAG contains a cycle: " + label
        );

        assertEquals(n, topo.processedVertices);
        assertEquals(n, topo.orderLength());

        assertTrue(
                KahnTopologicalSort.isValidOrder(
                        graph, topo.order()),
                "Invalid topological order: " + label
        );

        assertEquals(
                (long) m,
                topo.metrics.edgeScans,
                "Kahn edge scans mismatch: " + label
        );

        DagShortestPathResult paths =
                DagShortestPath.compute(graph);

        assertEquals(
                expected,
                paths.sumDistances(),
                "DAG distance sum mismatch: " + label
        );

        assertEquals(
                (long) m,
                paths.metrics.edgeScans,
                "DAG edge scans mismatch: " + label
        );

        return paths.sumDistances();
    }
}