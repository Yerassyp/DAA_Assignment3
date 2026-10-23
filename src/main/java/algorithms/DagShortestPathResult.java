package algorithms;

import metrics.OperationMetrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DagShortestPathResult {
    private final long[] distances;
    private final int[] parents;

    public final OperationMetrics metrics;

    DagShortestPathResult(long[] distances, int[] parents,
                          OperationMetrics metrics) {
        this.distances = distances.clone();
        this.parents = parents.clone();
        this.metrics = metrics;
    }

    public int vertexCount() {
        return distances.length;
    }

    public long distanceTo(int vertex) {
        checkVertex(vertex);
        return distances[vertex];
    }

    public int parentOf(int vertex) {
        checkVertex(vertex);
        return parents[vertex];
    }

    public long[] distances() {
        return distances.clone();
    }

    public int[] parents() {
        return parents.clone();
    }

    public long sumDistances() {
        long sum = 0;

        for (long distance : distances) {
            sum += distance;
        }

        return sum;
    }

    public List<Integer> pathTo(int vertex) {
        checkVertex(vertex);

        List<Integer> path = new ArrayList<>();
        int current = vertex;

        while (current != -1) {
            path.add(current);
            current = parents[current];
        }

        Collections.reverse(path);
        return List.copyOf(path);
    }

    private void checkVertex(int vertex) {
        if (vertex < 0 || vertex >= distances.length) {
            throw new IllegalArgumentException(
                    "Vertex out of range: " + vertex);
        }
    }
}