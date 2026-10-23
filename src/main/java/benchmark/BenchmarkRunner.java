package benchmark;

import algorithms.DagShortestPath;
import algorithms.DagShortestPathResult;
import algorithms.KahnTopologicalSort;
import algorithms.KosarajuSCC;
import algorithms.KruskalMST;
import algorithms.MSTResult;
import algorithms.PrimMST;
import algorithms.SCCResult;
import algorithms.TopologicalResult;
import graph.Graph;
import metrics.OperationMetrics;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public final class BenchmarkRunner {

    private static final int RUNS = 5;

    private static final int[] VERTICES = {
            1000, 1000, 3162, 3162, 10000,
            10000, 31623, 31623, 100000, 100000
    };

    private static final int[] EDGES = {
            4000, 20000, 12648, 63240, 40000,
            200000, 126492, 632460, 400000, 2000000
    };

    private static final long[] MST_REFERENCE = {
            147970, 30363, 468911, 97589, 1486139,
            309550, 4715515, 968517, 15012094, 3056681
    };

    private static final int[] SCC_REFERENCE = {
            27, 1, 137, 1, 373, 1, 1188, 1, 3923, 1
    };

    private static final long[] DAG_REFERENCE = {
            396274, 252411, 1271911, 723157, 3941828,
            2381342, 12551891, 7376612, 40096539, 23512087
    };

    private BenchmarkRunner() {
    }

    public static void main(String[] args) throws IOException {
        Path output = Path.of("results", "results.csv");
        Files.createDirectories(output.getParent());
        Files.deleteIfExists(output);

        List<String> lines = new ArrayList<>();
        lines.add("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");

        for (int i = 0; i < VERTICES.length; i++) {
            int n = VERTICES[i];
            int m = EDGES[i];
            String density = m == 4 * n ? "sparse" : "dense";

            System.out.println("CHECKING " + n + "/" + m);

            benchmarkMst(lines, n, m, density, MST_REFERENCE[i]);
            benchmarkScc(lines, n, m, density, SCC_REFERENCE[i]);
            benchmarkDag(lines, n, m, density, DAG_REFERENCE[i]);

            System.out.println("PASS " + n + "/" + m
                    + " | MST=" + MST_REFERENCE[i]
                    + " | SCC=" + SCC_REFERENCE[i]
                    + " | DAG=" + DAG_REFERENCE[i]);
        }

        if (lines.size() != 51) {
            throw new IllegalStateException(
                    "Expected 50 data rows, found " + (lines.size() - 1));
        }

        Files.write(output, lines, StandardCharsets.UTF_8);

        System.out.println("ALL 10 CONFIGURATIONS PASSED");
        System.out.println("CSV DATA ROWS: " + (lines.size() - 1));
        System.out.println("CSV SAVED: " + output.toAbsolutePath());
    }

    private static void benchmarkMst(List<String> lines, int n, int m,
                                     String density, long expected) {
        Graph graph = GraphGenerator.undirected(n, m);
        String label = "undirected_" + density;

        long kruskal = measure(
                lines, "Kruskal", label, graph, n, m, expected, -1);

        long prim = measure(
                lines, "Prim", label, graph, n, m, expected, 2L * m);

        if (kruskal != prim) {
            throw new IllegalStateException(
                    "Kruskal/Prim mismatch: " + n + "/" + m);
        }
    }

    private static void benchmarkScc(List<String> lines, int n, int m,
                                     String density, int expected) {
        Graph graph = GraphGenerator.directed(n, m);

        measure(lines, "Kosaraju", "directed_" + density,
                graph, n, m, expected, 2L * m);
    }

    private static void benchmarkDag(List<String> lines, int n, int m,
                                     String density, long expected) {
        Graph graph = GraphGenerator.dag(n, m);
        TopologicalResult topological =
                KahnTopologicalSort.compute(graph);

        if (topological.hasCycle
                || !KahnTopologicalSort.isValidOrder(
                graph, topological.order())) {
            throw new IllegalStateException(
                    "Invalid DAG order: " + n + "/" + m);
        }

        String label = "dag_" + density;

        measure(lines, "Kahn", label, graph, n, m, n, m);

        measure(lines, "DagShortestPath", label,
                graph, n, m, expected, m);
    }

    private static long measure(
            List<String> lines,
            String algorithm,
            String graphLabel,
            Graph graph,
            int n,
            int m,
            long expectedResult,
            long expectedScans) {

        long[] durations = new long[RUNS];
        Measurement first = null;

        for (int run = 0; run < RUNS; run++) {
            long start = System.nanoTime();
            Measurement current = runOnce(algorithm, graph);
            durations[run] = System.nanoTime() - start;

            if (first == null) {
                first = current;
            } else if (!sameValues(first, current)) {
                throw new IllegalStateException(
                        "Results or counters changed between runs: "
                                + algorithm);
            }
        }

        if (first.result != expectedResult) {
            throw new IllegalStateException(
                    algorithm + " mismatch at " + n + "/" + m
                            + ": expected " + expectedResult
                            + ", got " + first.result);
        }

        if (expectedScans >= 0
                && first.metrics.edgeScans != expectedScans) {
            throw new IllegalStateException(
                    algorithm + " edgeScans mismatch at "
                            + n + "/" + m);
        }

        if (algorithm.equals("Kruskal")
                && (first.metrics.edgeScans < n - 1
                || first.metrics.edgeScans > m)) {
            throw new IllegalStateException(
                    "Invalid Kruskal edgeScans");
        }

        Arrays.sort(durations);

        String medianMs = String.format(
                Locale.US, "%.3f",
                durations[RUNS / 2] / 1_000_000.0);

        OperationMetrics metrics = first.metrics;

        lines.add(algorithm + "," + graphLabel + "," + n + "," + m
                + "," + medianMs
                + "," + metrics.dsuOps
                + "," + metrics.edgeScans
                + "," + metrics.comparisons
                + "," + metrics.maxDepth
                + "," + first.result);

        System.out.println("  " + algorithm
                + ": result=" + first.result
                + ", median=" + medianMs + " ms");

        return first.result;
    }

    private static boolean sameValues(Measurement a, Measurement b) {
        return a.result == b.result
                && a.metrics.dsuOps == b.metrics.dsuOps
                && a.metrics.edgeScans == b.metrics.edgeScans
                && a.metrics.comparisons == b.metrics.comparisons
                && a.metrics.maxDepth == b.metrics.maxDepth;
    }

    private static Measurement runOnce(String algorithm, Graph graph) {
        switch (algorithm) {
            case "Kruskal": {
                MSTResult result = KruskalMST.compute(graph);
                return new Measurement(result.totalWeight, result.metrics);
            }

            case "Prim": {
                MSTResult result = PrimMST.compute(graph);
                return new Measurement(result.totalWeight, result.metrics);
            }

            case "Kosaraju": {
                SCCResult result = KosarajuSCC.compute(graph);
                return new Measurement(result.components, result.metrics);
            }

            case "Kahn": {
                TopologicalResult result =
                        KahnTopologicalSort.compute(graph);

                if (result.hasCycle) {
                    throw new IllegalStateException(
                            "Cycle in generated DAG");
                }

                return new Measurement(
                        result.processedVertices, result.metrics);
            }

            case "DagShortestPath": {
                DagShortestPathResult result =
                        DagShortestPath.compute(graph);

                return new Measurement(
                        result.sumDistances(), result.metrics);
            }

            default:
                throw new IllegalArgumentException(
                        "Unknown algorithm: " + algorithm);
        }
    }

    private static final class Measurement {
        final long result;
        final OperationMetrics metrics;

        Measurement(long result, OperationMetrics metrics) {
            this.result = result;
            this.metrics = metrics;
        }
    }
}