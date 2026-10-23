package benchmark;

import graph.Graph;

import java.util.Random;

public final class GraphGenerator {

    private GraphGenerator() {
    }

    public static Graph undirected(int n, int m) {
        validate(n, m);

        if (m < Math.max(0, n - 1)) {
            throw new IllegalArgumentException(
                    "Undirected connected graph needs at least n - 1 edges");
        }

        Random random = new Random(42);
        Graph graph = new Graph(n, false);
        int[] perm = shuffledVertices(n, random);

        for (int i = 1; i < n; i++) {
            int from = perm[random.nextInt(i)];
            int to = perm[i];
            int weight = 1 + random.nextInt(1000);

            graph.addEdge(from, to, weight);
        }

        while (graph.edgeCount() < m) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);

            while (to == from) {
                to = random.nextInt(n);
            }

            int weight = 1 + random.nextInt(1000);
            graph.addEdge(from, to, weight);
        }

        return graph;
    }

    public static Graph directed(int n, int m) {
        validate(n, m);

        Random random = new Random(42);
        Graph graph = new Graph(n, true);

        for (int i = 0; i < m; i++) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);

            while (to == from) {
                to = random.nextInt(n);
            }

            int weight = 1 + random.nextInt(1000);
            graph.addEdge(from, to, weight);
        }

        return graph;
    }

    public static Graph dag(int n, int m) {
        validate(n, m);

        Random random = new Random(42);
        Graph graph = new Graph(n, true);
        int[] rank = shuffledVertices(n, random);

        for (int i = 0; i < m; i++) {
            int from = random.nextInt(n);
            int to = random.nextInt(n);

            while (to == from) {
                to = random.nextInt(n);
            }

            if (rank[from] > rank[to]) {
                int temp = from;
                from = to;
                to = temp;
            }

            int weight = 1 + random.nextInt(1000);
            graph.addEdge(from, to, weight);
        }

        return graph;
    }

    private static int[] shuffledVertices(int n, Random random) {
        int[] values = new int[n];

        for (int i = 0; i < n; i++) {
            values[i] = i;
        }

        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = values[i];
            values[i] = values[j];
            values[j] = temp;
        }

        return values;
    }

    private static void validate(int n, int m) {
        if (n < 0 || m < 0) {
            throw new IllegalArgumentException(
                    "Vertex and edge counts cannot be negative");
        }

        if (n < 2 && m > 0) {
            throw new IllegalArgumentException(
                    "Cannot generate non-loop edges with fewer than two vertices");
        }
    }
}