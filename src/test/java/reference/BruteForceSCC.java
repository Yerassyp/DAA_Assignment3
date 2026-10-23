package reference;

import graph.Edge;
import graph.Graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class BruteForceSCC {

    private BruteForceSCC() {
    }

    public static int[] componentIds(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Brute-force SCC requires a directed graph");
        }

        int n = graph.vertexCount();

        if (n > 200) {
            throw new IllegalArgumentException(
                    "Brute-force SCC supports at most 200 vertices");
        }

        List<List<Integer>> reversed = new ArrayList<>(n);

        for (int v = 0; v < n; v++) {
            reversed.add(new ArrayList<>());
        }

        for (Edge edge : graph.edges()) {
            reversed.get(edge.to).add(edge.from);
        }

        boolean[][] forward = new boolean[n][];
        boolean[][] backward = new boolean[n][];

        for (int start = 0; start < n; start++) {
            forward[start] = bfsForward(graph, start);
            backward[start] = bfsBackward(reversed, start);
        }

        int[] componentId = new int[n];
        Arrays.fill(componentId, -1);

        int components = 0;

        for (int start = 0; start < n; start++) {
            if (componentId[start] != -1) {
                continue;
            }

            for (int v = 0; v < n; v++) {
                if (componentId[v] == -1
                        && forward[start][v]
                        && backward[start][v]) {

                    componentId[v] = components;
                }
            }

            components++;
        }

        return componentId;
    }

    private static boolean[] bfsForward(Graph graph, int start) {
        int n = graph.vertexCount();

        boolean[] visited = new boolean[n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        visited[start] = true;
        queue.add(start);

        while (!queue.isEmpty()) {
            int vertex = queue.remove();

            for (Edge edge : graph.neighbors(vertex)) {
                if (!visited[edge.to]) {
                    visited[edge.to] = true;
                    queue.add(edge.to);
                }
            }
        }

        return visited;
    }

    private static boolean[] bfsBackward(
            List<List<Integer>> reversed, int start) {

        int n = reversed.size();

        boolean[] visited = new boolean[n];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        visited[start] = true;
        queue.add(start);

        while (!queue.isEmpty()) {
            int vertex = queue.remove();

            for (int neighbor : reversed.get(vertex)) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.add(neighbor);
                }
            }
        }

        return visited;
    }
}