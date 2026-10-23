# DAA Assignment 3: Graph Algorithms

| Item | Value |
|---|---|
| Course | Design and Analysis of Algorithms |
| Student | Irangait Yerassyl |
| Group | SE-2523 |
| Language | Java 17 |
| Build and tests | Maven, JUnit 5 |

## Overview

This project implements weighted graphs and algorithms for connectivity, minimum spanning forests, strongly connected components, topological ordering, and shortest paths in a DAG. The graph uses adjacency lists and supports both directed and undirected edges. Algorithm code uses standard Java collections, without external graph libraries.

Implemented algorithms:

- **Disjoint Set Union (DSU):** union by rank and path compression, with operation counters.
- **Kruskal:** minimum spanning forest using sorted edges and DSU.
- **Prim:** lazy minimum spanning forest using `PriorityQueue`, starting at vertex 0 and continuing through disconnected components.
- **Kosaraju:** strongly connected components with two iterative DFS passes; no recursive DFS.
- **Kahn:** topological ordering and cycle detection.
- **DAG shortest paths:** one relaxation pass in topological order, virtual super-source for all zero-in-degree vertices, negative edge weights, and path reconstruction through `parent[]`.

## Requirements

- JDK 17
- Maven 3.x (or the Maven bundled with IntelliJ IDEA)
- Python 3 and `matplotlib` **only** for generating the plots

JUnit 5 is downloaded by Maven as a test dependency. There are no external graph algorithm dependencies.

## Project structure

```text
DAA_Assignment3/
├── pom.xml
├── README.md
├── REPORT.md
├── data/
│   ├── small_directed.txt
│   ├── small_cyclic.txt
│   └── small_disconnected.txt
├── src/
│   ├── main/java/
│   │   ├── graph/          (Edge, Graph)
│   │   ├── algorithms/     (DSU, MST, SCC, Kahn, DAG paths)
│   │   ├── metrics/        (OperationMetrics)
│   │   └── benchmark/      (GraphGenerator, BenchmarkRunner)
│   └── test/java/
│       ├── graph/
│       ├── algorithms/
│       ├── reference/      (BruteForceSCC, BellmanFord)
│       └── benchmark/
├── scripts/
│   └── generate_plots.py
└── results/
    ├── results.csv
    └── plots/
        ├── operations_vs_n.png
        ├── scans_per_edge.png
        └── kruskal_edge_share.png
```

## Build and run tests

Open a terminal **in the project root** (the folder containing `pom.xml`). With Maven available on `PATH`, run:

```powershell
mvn -B clean verify
```

On Windows, if `mvn` is not recognized but you use IntelliJ IDEA, open **View > Tool Windows > Maven > Lifecycle** and double-click **verify**. IntelliJ can run its bundled Maven without a separate command-line installation.

A successful build prints `BUILD SUCCESS`. The final verified project has **70 JUnit tests**, including randomized comparisons, reference values, and a 100,000-vertex stack-safety test. For the reference generator, all ten configurations must match the assignment table.

## Input graph files

Files in `data/` have this format:

```text
n m directed|undirected
u v w
u v w
```

The first line contains vertex count `n`, edge count `m`, and graph type. The following `m` lines contain vertices `u`, `v` and integer weight `w`. Vertices are numbered from `0` to `n - 1`. The graph implementation accepts parallel edges, self-loops, isolated vertices, and disconnected graphs.

## Run the benchmark

**In IntelliJ IDEA:** Run `benchmark.BenchmarkRunner.main()` with these settings:

- Main class: `benchmark.BenchmarkRunner`
- Working directory: project root (`C:\CodeFiles\DAA\DAA_Assignment3` on the development machine)
- VM options: `-Xmx4G`
- Program arguments: empty

**From a terminal**, after a successful Maven build, run from the project root:

```powershell
java -Xmx4G -cp target/classes benchmark.BenchmarkRunner
```

The generator uses a separate `new Random(42)` for each graph type. It creates five sizes (`1000`, `3162`, `10000`, `31623`, `100000`), each with `m = 4n` or `m = 20n`. For each configuration, it generates an undirected graph, a directed graph, and a DAG.

The runner measures **Kruskal, Prim, Kosaraju, Kahn, and DagShortestPath** five times each and writes the median time to `results/results.csv`. The CSV contains **50 data rows** with these columns:

```text
algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result
```

The run stops with an error if the results disagree with the reference values, Kruskal and Prim return different weights, or an expected scan invariant fails. A successful run ends with:

```text
ALL 10 CONFIGURATIONS PASSED
CSV DATA ROWS: 50
```

For the first configuration (`1000 / 4000`), the reference values are MST weight **147970**, SCC count **27**, and DAG distance sum **396274**.

## Generate plots

Install Matplotlib once:

```powershell
py -3 -m pip install matplotlib
```

Then run from the project root:

```powershell
py -3 scripts/generate_plots.py
```

If the Windows `py` launcher is unavailable, use `python` in its place. The script reads `results/results.csv` and writes:

- `results/plots/operations_vs_n.png` — counted operations against vertex count, on log-log axes.
- `results/plots/scans_per_edge.png` — `edgeScans / m` against vertex count.
- `results/plots/kruskal_edge_share.png` — share of edges scanned by Kruskal for sparse and dense graphs.

It also prints log-log slope estimates used in the report. Do not rerun the Java benchmark just to redraw the plots.

## Metrics and correctness checks

- DSU tracks find calls and successful unions; `dsuOps` is their sum.
- Prim and Kosaraju scan `2m` adjacency entries for the generated graphs.
- Kahn and DAG shortest-path relaxation scan `m` edges on a DAG.
- Kruskal may stop examining edges early when the minimum spanning tree is complete.
- The benchmark automatically compares Kruskal and Prim and checks the reference table.
- JUnit compares SCC results against brute-force BFS and DAG distances against Bellman-Ford, including negative edge weights.

The measured operation counters are the main evidence for complexity. Timing can vary with hardware, JVM warm-up, and background processes. See [REPORT.md](REPORT.md) for the complexity table, correctness arguments, counter ratios, plots, and algorithm-selection discussion.

### AI-Use Acknowledgement

Generative AI was used as an interactive programming assistant for the following tasks:
- Drafting the Maven project configuration (`pom.xml`).
- Generating the Python script (`scripts/generate_plots.py`) for benchmark data visualization.
- Assisting in debugging boundary conditions and iterative stack traversal logic for Kosaraju's algorithm.
- Proofreading and formatting the Markdown report structure (`REPORT.md`).

All core algorithm implementations, local test executions, and benchmark data collections were independently verified and analyzed by the student.