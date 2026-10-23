package reference;

import graph.Edge;
import graph.Graph;

import java.util.Arrays;

public final class BellmanFord {

    private BellmanFord() {
    }

    public static long[] compute(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Bellman-Ford reference requires a directed graph");
        }

        int n = graph.vertexCount();
        int[] inDegree = new int[n];

        for (Edge edge : graph.edges()) {
            inDegree[edge.to]++;
        }

        final long INF = Long.MAX_VALUE / 4;

        long[] dist = new long[n];
        Arrays.fill(dist, INF);

        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                dist[v] = 0;
            }
        }

        for (int pass = 1; pass < n; pass++) {
            boolean changed = false;

            for (Edge edge : graph.edges()) {
                if (dist[edge.from] == INF) {
                    continue;
                }

                long candidate = dist[edge.from] + (long) edge.weight;

                if (candidate < dist[edge.to]) {
                    dist[edge.to] = candidate;
                    changed = true;
                }
            }

            if (!changed) {
                break;
            }
        }

        return dist;
    }
}