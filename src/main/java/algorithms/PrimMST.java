package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.OperationMetrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class PrimMST {

    private PrimMST() {
    }

    public static MSTResult compute(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Prim requires an undirected graph");
        }

        int n = graph.vertexCount();
        boolean[] visited = new boolean[n];

        OperationMetrics metrics = new OperationMetrics();

        PriorityQueue<Edge> queue = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });

        List<Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;
        int components = 0;

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            components++;
            visited[start] = true;

            addOutgoingEdges(graph, start, visited, queue, metrics);

            while (!queue.isEmpty()) {
                Edge edge = queue.poll();

                if (visited[edge.to]) {
                    continue;
                }

                visited[edge.to] = true;
                selectedEdges.add(edge);
                totalWeight += edge.weight;

                addOutgoingEdges(
                        graph, edge.to, visited, queue, metrics);
            }
        }

        return new MSTResult(
                totalWeight,
                components,
                selectedEdges,
                metrics
        );
    }

    private static void addOutgoingEdges(
            Graph graph,
            int vertex,
            boolean[] visited,
            PriorityQueue<Edge> queue,
            OperationMetrics metrics) {

        for (Edge edge : graph.neighbors(vertex)) {
            metrics.edgeScans++;

            if (!visited[edge.to]) {
                queue.offer(edge);
            }
        }
    }
}