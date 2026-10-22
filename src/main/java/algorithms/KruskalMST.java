package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.OperationMetrics;

import java.util.ArrayList;
import java.util.List;

public final class KruskalMST {

    private KruskalMST() {
    }

    public static MSTResult compute(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException(
                    "Kruskal requires an undirected graph");
        }

        OperationMetrics metrics = new OperationMetrics();

        List<Edge> sortedEdges = new ArrayList<>(graph.edges());

        sortedEdges.sort((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.weight, b.weight);
        });

        DisjointSetUnion dsu = new DisjointSetUnion(graph.vertexCount());

        List<Edge> selectedEdges = new ArrayList<>();
        long totalWeight = 0;

        for (Edge edge : sortedEdges) {
            if (dsu.components() == 1) {
                break;
            }

            metrics.edgeScans++;

            if (dsu.find(edge.from) != dsu.find(edge.to)) {
                dsu.union(edge.from, edge.to);

                selectedEdges.add(edge);
                totalWeight += edge.weight;
            }
        }

        metrics.dsuOps = dsu.dsuOps();

        return new MSTResult(
                totalWeight,
                dsu.components(),
                selectedEdges,
                metrics
        );
    }
}