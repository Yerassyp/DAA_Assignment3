package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.OperationMetrics;

import java.util.ArrayDeque;
import java.util.Arrays;

public final class KahnTopologicalSort {

    private KahnTopologicalSort() {
    }

    public static TopologicalResult compute(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Topological sorting requires a directed graph");
        }

        int n = graph.vertexCount();
        int[] inDegree = new int[n];

        OperationMetrics metrics = new OperationMetrics();

        for (Edge edge : graph.edges()) {
            inDegree[edge.to]++;
        }

        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue.addLast(v);
            }
        }

        int[] order = new int[n];
        int processed = 0;

        while (!queue.isEmpty()) {
            int vertex = queue.removeFirst();
            order[processed++] = vertex;

            for (Edge edge : graph.neighbors(vertex)) {
                metrics.edgeScans++;

                inDegree[edge.to]--;

                if (inDegree[edge.to] == 0) {
                    queue.addLast(edge.to);
                }
            }
        }

        if (processed != n) {
            return new TopologicalResult(
                    new int[0], true, processed, metrics);
        }

        return new TopologicalResult(
                order, false, processed, metrics);
    }

    public static boolean isValidOrder(Graph graph, int[] order) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Topological order requires a directed graph");
        }

        int n = graph.vertexCount();

        if (order.length != n) {
            return false;
        }

        int[] position = new int[n];
        Arrays.fill(position, -1);

        for (int i = 0; i < n; i++) {
            int vertex = order[i];

            if (vertex < 0 || vertex >= n) {
                return false;
            }

            if (position[vertex] != -1) {
                return false;
            }

            position[vertex] = i;
        }

        for (Edge edge : graph.edges()) {
            if (position[edge.from] >= position[edge.to]) {
                return false;
            }
        }

        return true;
    }
}