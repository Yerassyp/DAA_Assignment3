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

import graph.Edge;
import graph.Graph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReferenceValuesTest {

    @Test
    void reproducesFirstReferenceRow() {
        int n = 1000;
        int m = 4000;

        Graph undirected = GraphGenerator.undirected(n, m);

        assertEquals(n, undirected.vertexCount());
        assertEquals(m, undirected.edgeCount());

        MSTResult kruskal = KruskalMST.compute(undirected);
        MSTResult prim = PrimMST.compute(undirected);

        System.out.println(
                "REFERENCE MST: " + kruskal.totalWeight);

        assertEquals(147970L, kruskal.totalWeight);
        assertEquals(kruskal.totalWeight, prim.totalWeight);
        assertEquals(1, kruskal.components);
        assertEquals(1, prim.components);
        assertEquals(n - 1, kruskal.edgeCount());
        assertEquals(n - 1, prim.edgeCount());

        assertEquals(2L * m, prim.metrics.edgeScans);
        assertTrue(kruskal.metrics.dsuOps > 0);
        assertTrue(kruskal.metrics.comparisons > 0);

        Graph directed = GraphGenerator.directed(n, m);

        assertEquals(n, directed.vertexCount());
        assertEquals(m, directed.edgeCount());

        SCCResult scc = KosarajuSCC.compute(directed);

        System.out.println(
                "REFERENCE SCC: " + scc.components);

        assertEquals(27, scc.components);
        assertEquals(2L * m, scc.metrics.edgeScans);

        Graph dag = GraphGenerator.dag(n, m);

        assertEquals(n, dag.vertexCount());
        assertEquals(m, dag.edgeCount());

        TopologicalResult topo =
                KahnTopologicalSort.compute(dag);

        assertFalse(topo.hasCycle);
        assertEquals(n, topo.processedVertices);
        assertEquals(m, topo.metrics.edgeScans);

        assertTrue(KahnTopologicalSort.isValidOrder(
                dag, topo.order()));

        DagShortestPathResult paths =
                DagShortestPath.compute(dag);

        System.out.println(
                "REFERENCE DAG SUM: " + paths.sumDistances());

        assertEquals(396274L, paths.sumDistances());
        assertEquals(m, paths.metrics.edgeScans);

        System.out.println(
                "REFERENCE 1000/4000: ALL VALUES MATCH");
    }

    @Test
    void generatorIsDeterministicAndValidatesInputs() {
        assertSameEdges(
                GraphGenerator.undirected(8, 24),
                GraphGenerator.undirected(8, 24));

        assertSameEdges(
                GraphGenerator.directed(8, 24),
                GraphGenerator.directed(8, 24));

        assertSameEdges(
                GraphGenerator.dag(8, 24),
                GraphGenerator.dag(8, 24));

        assertThrows(IllegalArgumentException.class,
                () -> GraphGenerator.undirected(5, 3));

        assertThrows(IllegalArgumentException.class,
                () -> GraphGenerator.directed(-1, 5));

        assertThrows(IllegalArgumentException.class,
                () -> GraphGenerator.dag(1, 1));

        assertEquals(0,
                GraphGenerator.undirected(0, 0).edgeCount());

        assertEquals(0,
                GraphGenerator.directed(0, 0).edgeCount());

        assertEquals(0,
                GraphGenerator.dag(0, 0).edgeCount());
    }

    private static void assertSameEdges(Graph first, Graph second) {
        assertEquals(first.vertexCount(), second.vertexCount());
        assertEquals(first.edgeCount(), second.edgeCount());
        assertEquals(first.isDirected(), second.isDirected());

        for (int i = 0; i < first.edgeCount(); i++) {
            Edge a = first.edges().get(i);
            Edge b = second.edges().get(i);

            assertEquals(a.from, b.from);
            assertEquals(a.to, b.to);
            assertEquals(a.weight, b.weight);
        }
    }
}