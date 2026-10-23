import csv
import math
from pathlib import Path

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parent.parent
CSV_PATH = ROOT / "results" / "results.csv"
PLOT_DIR = ROOT / "results" / "plots"

SIZES = [1000, 3162, 10000, 31623, 100000]
ALGORITHMS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
DENSITIES = ["sparse", "dense"]
MARKERS = ["o", "s", "^", "D", "v"]
KINDS = {
    "Kruskal": "undirected", "Prim": "undirected",
    "Kosaraju": "directed", "Kahn": "dag", "DagShortestPath": "dag"
}


def read_results():
    if not CSV_PATH.is_file():
        raise FileNotFoundError(f"CSV not found: {CSV_PATH}")

    rows = []
    with CSV_PATH.open(newline="", encoding="utf-8-sig") as file:
        reader = csv.DictReader(file)
        expected = ["algorithm", "graph", "n", "m", "time_ms",
                    "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"]
        if reader.fieldnames != expected:
            raise ValueError("Unexpected CSV columns: " + str(reader.fieldnames))

        for row in reader:
            for name in ("n", "m", "dsu_ops", "edge_scans", "comparisons", "max_depth", "result"):
                row[name] = int(row[name])
            row["time_ms"] = float(row["time_ms"])
            rows.append(row)

    if len(rows) != 50:
        raise ValueError(f"Expected 50 data rows, found {len(rows)}")

    for algorithm in ALGORITHMS:
        for density in DENSITIES:
            group = select_rows(rows, algorithm, density)
            if [row["n"] for row in group] != SIZES:
                raise ValueError(f"Missing sizes for {algorithm} {density}")
            for row in group:
                expected_m = row["n"] * (4 if density == "sparse" else 20)
                expected_graph = f"{KINDS[algorithm]}_{density}"
                if row["m"] != expected_m or row["graph"] != expected_graph:
                    raise ValueError(f"Unexpected graph settings: {row}")
    return rows


def select_rows(rows, algorithm, density):
    return sorted(
        (row for row in rows
         if row["algorithm"] == algorithm
         and row["graph"].endswith("_" + density)),
        key=lambda row: row["n"]
    )


def setup_x_axis(ax):
    ax.set_xscale("log")
    ax.set_xticks(SIZES)
    ax.set_xticklabels(["1,000", "3,162", "10,000", "31,623", "100,000"])
    ax.set_xlabel("Number of vertices (n)")
    ax.grid(True, which="both", alpha=0.25)


def plot_operations(rows):
    fig, ax = plt.subplots(figsize=(13, 7.5))
    for index, algorithm in enumerate(ALGORITHMS):
        for density in DENSITIES:
            group = select_rows(rows, algorithm, density)
            ops = [r["dsu_ops"] + r["edge_scans"] + r["comparisons"] for r in group]
            ax.plot([r["n"] for r in group], ops,
                    marker=MARKERS[index], markersize=5,
                    linestyle="-" if density == "sparse" else "--",
                    linewidth=1.8, label=f"{algorithm} ({density})")

    setup_x_axis(ax)
    ax.set_yscale("log")
    ax.set_ylabel("Counted operations (log scale)")
    ax.set_title("Operations vs n (log-log)")
    ax.legend(loc="center left", bbox_to_anchor=(1.02, 0.5), fontsize=9)
    fig.tight_layout()
    fig.savefig(PLOT_DIR / "operations_vs_n.png", dpi=180)
    plt.close(fig)


def plot_scans_per_edge(rows):
    fig, ax = plt.subplots(figsize=(13, 7.5))
    for index, algorithm in enumerate(ALGORITHMS):
        for density in DENSITIES:
            group = select_rows(rows, algorithm, density)
            ratios = [r["edge_scans"] / r["m"] for r in group]
            ax.plot([r["n"] for r in group], ratios,
                    marker=MARKERS[index], markersize=5,
                    linestyle="-" if density == "sparse" else "--",
                    linewidth=1.8, label=f"{algorithm} ({density})")

    setup_x_axis(ax)
    ax.set_ylabel("edgeScans / m")
    ax.set_ylim(0, 2.2)
    ax.set_title("Scans per edge vs n")
    ax.legend(loc="center left", bbox_to_anchor=(1.02, 0.5), fontsize=9)
    fig.tight_layout()
    fig.savefig(PLOT_DIR / "scans_per_edge.png", dpi=180)
    plt.close(fig)


def plot_kruskal_share(rows):
    fig, ax = plt.subplots(figsize=(10.5, 6.5))
    for density, marker in (("sparse", "o"), ("dense", "s")):
        group = select_rows(rows, "Kruskal", density)
        shares = [r["edge_scans"] / r["m"] for r in group]
        ax.plot([r["n"] for r in group], shares, marker=marker,
                linewidth=2, markersize=6, label=f"Kruskal ({density})")

    setup_x_axis(ax)
    ax.set_ylim(0, 1.05)
    ax.set_ylabel("Share of edges scanned (edgeScans / m)")
    ax.set_title("Share of edges examined by Kruskal")
    ax.legend()
    fig.tight_layout()
    fig.savefig(PLOT_DIR / "kruskal_edge_share.png", dpi=180)
    plt.close(fig)


def log_log_slope(group, column):
    xs = [math.log(r["n"]) for r in group]
    ys = [math.log(r[column]) for r in group]
    x_mean = sum(xs) / len(xs)
    y_mean = sum(ys) / len(ys)
    numerator = sum((x - x_mean) * (y - y_mean) for x, y in zip(xs, ys))
    denominator = sum((x - x_mean) ** 2 for x in xs)
    return numerator / denominator


def main():
    rows = read_results()
    PLOT_DIR.mkdir(parents=True, exist_ok=True)

    plot_operations(rows)
    plot_scans_per_edge(rows)
    plot_kruskal_share(rows)

    print("SLOPE CHECK: edgeScans vs n (log-log)")
    for algorithm in ALGORITHMS:
        for density in DENSITIES:
            group = select_rows(rows, algorithm, density)
            slope = log_log_slope(group, "edge_scans")
            print(f"  {algorithm:15s} {density:6s} slope = {slope:.3f}")

    print("KRUSKAL SORTING COMPARISONS: log-log slope")
    for density in DENSITIES:
        group = select_rows(rows, "Kruskal", density)
        print(f"  {density}: {log_log_slope(group, 'comparisons'):.3f}")

    for filename in ("operations_vs_n.png", "scans_per_edge.png", "kruskal_edge_share.png"):
        path = PLOT_DIR / filename
        if not path.is_file() or path.stat().st_size == 0:
            raise RuntimeError("Missing or empty plot: " + str(path))
        print("SAVED:", path)
    print("PASS: 3 PNG plots generated from 50 CSV rows")


if __name__ == "__main__":
    main()