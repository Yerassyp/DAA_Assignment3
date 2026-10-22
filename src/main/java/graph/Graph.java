package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adjacency;
    private final List<Edge> edges;

    public Graph(int n, boolean directed) {
        if (n < 0) {
            throw new IllegalArgumentException("Vertex count cannot be negative");
        }

        this.n = n;
        this.directed = directed;
        this.adjacency = new ArrayList<>(n);
        this.edges = new ArrayList<>();

        for (int v = 0; v < n; v++) {
            adjacency.add(new ArrayList<>());
        }
    }

    public int vertexCount() {
        return n;
    }

    public int edgeCount() {
        return edges.size();
    }

    public boolean isDirected() {
        return directed;
    }

    public void addEdge(int from, int to, int weight) {
        checkVertex(from);
        checkVertex(to);

        Edge edge = new Edge(from, to, weight);
        edges.add(edge);
        adjacency.get(from).add(edge);

        if (!directed) {
            adjacency.get(to).add(new Edge(to, from, weight));
        }
    }

    public List<Edge> neighbors(int vertex) {
        checkVertex(vertex);
        return Collections.unmodifiableList(adjacency.get(vertex));
    }

    public List<Edge> edges() {
        return Collections.unmodifiableList(edges);
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= n) {
            throw new IllegalArgumentException("Vertex out of range: " + vertex);
        }
    }

    public static Graph fromFile(Path path) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IOException("Missing graph header");
            }

            String[] header = headerLine.trim().split("\\s+");
            if (header.length != 3) {
                throw new IOException("Expected header: n m directed|undirected");
            }

            int n = Integer.parseInt(header[0]);
            int m = Integer.parseInt(header[1]);

            if (m < 0) {
                throw new IllegalArgumentException("Edge count cannot be negative");
            }

            boolean directed;
            if (header[2].equals("directed")) {
                directed = true;
            } else if (header[2].equals("undirected")) {
                directed = false;
            } else {
                throw new IllegalArgumentException("Unknown graph type: " + header[2]);
            }

            Graph graph = new Graph(n, directed);

            for (int i = 0; i < m; i++) {
                String line = reader.readLine();

                if (line == null) {
                    throw new IOException("Expected " + m + " edges, found " + i);
                }

                String[] parts = line.trim().split("\\s+");
                if (parts.length != 3) {
                    throw new IOException("Invalid edge at line " + (i + 2));
                }

                int from = Integer.parseInt(parts[0]);
                int to = Integer.parseInt(parts[1]);
                int weight = Integer.parseInt(parts[2]);

                graph.addEdge(from, to, weight);
            }

            String trailing;
            while ((trailing = reader.readLine()) != null) {
                if (!trailing.isBlank()) {
                    throw new IOException("Unexpected data after " + m + " edges");
                }
            }

            return graph;
        }
    }
}