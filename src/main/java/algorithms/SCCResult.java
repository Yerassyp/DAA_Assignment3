package algorithms;

import metrics.OperationMetrics;

public final class SCCResult {
    public final int components;
    public final OperationMetrics metrics;

    private final int[] componentId;

    public SCCResult(int[] componentId, int components,
                     OperationMetrics metrics) {
        this.componentId = componentId.clone();
        this.components = components;
        this.metrics = metrics;
    }

    public int componentOf(int vertex) {
        return componentId[vertex];
    }

    public int vertexCount() {
        return componentId.length;
    }
}