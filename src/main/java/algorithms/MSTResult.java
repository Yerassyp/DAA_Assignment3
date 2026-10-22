package algorithms;

import graph.Edge;
import metrics.OperationMetrics;

import java.util.List;

public final class MSTResult {
    public final long totalWeight;
    public final int components;
    public final List<Edge> edges;
    public final OperationMetrics metrics;

    public MSTResult(long totalWeight, int components,
                     List<Edge> edges, OperationMetrics metrics) {
        this.totalWeight = totalWeight;
        this.components = components;
        this.edges = List.copyOf(edges);
        this.metrics = metrics;
    }

    public int edgeCount() {
        return edges.size();
    }
}