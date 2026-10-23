package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.OperationMetrics;

import java.util.Arrays;

public final class DagShortestPath {

    private DagShortestPath() {
    }

    public static DagShortestPathResult compute(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "DAG shortest paths require a directed graph");
        }

        TopologicalResult topological =
                KahnTopologicalSort.compute(graph);

        if (topological.hasCycle) {
            throw new IllegalArgumentException(
                    "Shortest paths require an acyclic graph");
        }

        int n = graph.vertexCount();
        int[] inDegree = new int[n];

        for (Edge edge : graph.edges()) {
            inDegree[edge.to]++;
        }

        final long INF = Long.MAX_VALUE / 4;

        long[] dist = new long[n];
        int[] parent = new int[n];

        Arrays.fill(dist, INF);
        Arrays.fill(parent, -1);

        for (int vertex = 0; vertex < n; vertex++) {
            if (inDegree[vertex] == 0) {
                dist[vertex] = 0;
            }
        }

        OperationMetrics metrics = new OperationMetrics();

        for (int vertex : topological.order()) {
            for (Edge edge : graph.neighbors(vertex)) {
                metrics.edgeScans++;

                if (dist[vertex] == INF) {
                    continue;
                }

                metrics.comparisons++;

                long candidate = dist[vertex] + (long) edge.weight;

                if (candidate < dist[edge.to]) {
                    dist[edge.to] = candidate;
                    parent[edge.to] = vertex;
                }
            }
        }

        return new DagShortestPathResult(
                dist,
                parent,
                metrics
        );
    }
}