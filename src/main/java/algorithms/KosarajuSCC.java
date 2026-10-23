package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.OperationMetrics;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;

public final class KosarajuSCC {

    private KosarajuSCC() {
    }

    private static final class DfsFrame {
        final int vertex;
        final List<Edge> neighbors;
        int nextEdge;

        DfsFrame(int vertex, List<Edge> neighbors) {
            this.vertex = vertex;
            this.neighbors = neighbors;
            this.nextEdge = 0;
        }
    }

    public static SCCResult compute(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kosaraju requires a directed graph");
        }

        int n = graph.vertexCount();
        OperationMetrics metrics = new OperationMetrics();

        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finished = 0;

        ArrayDeque<DfsFrame> dfsStack = new ArrayDeque<>();

        for (int start = 0; start < n; start++) {
            if (visited[start]) {
                continue;
            }

            visited[start] = true;
            dfsStack.push(
                    new DfsFrame(start, graph.neighbors(start)));

            metrics.maxDepth = Math.max(
                    metrics.maxDepth, dfsStack.size());

            while (!dfsStack.isEmpty()) {
                DfsFrame frame = dfsStack.peek();

                if (frame.nextEdge == frame.neighbors.size()) {
                    // Record the vertex only after all outgoing edges are processed.
                    finishOrder[finished++] = frame.vertex;
                    dfsStack.pop();
                    continue;
                }

                Edge edge = frame.neighbors.get(frame.nextEdge++);
                metrics.edgeScans++;

                if (!visited[edge.to]) {
                    visited[edge.to] = true;

                    dfsStack.push(
                            new DfsFrame(
                                    edge.to,
                                    graph.neighbors(edge.to)));

                    metrics.maxDepth = Math.max(
                            metrics.maxDepth, dfsStack.size());
                }
            }
        }

        Graph reversed = new Graph(n, true);

        for (Edge edge : graph.edges()) {
            reversed.addEdge(
                    edge.to, edge.from, edge.weight);
        }

        int[] componentId = new int[n];
        Arrays.fill(componentId, -1);

        int components = 0;
        ArrayDeque<Integer> stack = new ArrayDeque<>();

        for (int i = finished - 1; i >= 0; i--) {
            int start = finishOrder[i];

            if (componentId[start] != -1) {
                continue;
            }

            componentId[start] = components;
            stack.push(start);

            metrics.maxDepth = Math.max(
                    metrics.maxDepth, stack.size());

            while (!stack.isEmpty()) {
                int vertex = stack.pop();

                for (Edge edge : reversed.neighbors(vertex)) {
                    metrics.edgeScans++;

                    if (componentId[edge.to] == -1) {
                        componentId[edge.to] = components;
                        stack.push(edge.to);

                        metrics.maxDepth = Math.max(
                                metrics.maxDepth, stack.size());
                    }
                }
            }

            components++;
        }

        return new SCCResult(
                componentId,
                components,
                metrics
        );
    }
}