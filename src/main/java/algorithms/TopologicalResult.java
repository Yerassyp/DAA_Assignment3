package algorithms;

import metrics.OperationMetrics;

public final class TopologicalResult {
    public final boolean hasCycle;
    public final int processedVertices;
    public final OperationMetrics metrics;

    private final int[] order;

    TopologicalResult(int[] order, boolean hasCycle,
                      int processedVertices, OperationMetrics metrics) {
        this.order = order.clone();
        this.hasCycle = hasCycle;
        this.processedVertices = processedVertices;
        this.metrics = metrics;
    }

    public int[] order() {
        return order.clone();
    }

    public int orderLength() {
        return order.length;
    }
}