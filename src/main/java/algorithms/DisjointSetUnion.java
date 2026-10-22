package algorithms;

public final class DisjointSetUnion {
    private int[] parent;
    private int[] rank;
    private int componentCount;

    private long findCalls;
    private long successfulUnions;

    public DisjointSetUnion(int n) {
        makeSet(n);
    }

    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Size cannot be negative");
        }

        parent = new int[n];
        rank = new int[n];
        componentCount = n;

        findCalls = 0;
        successfulUnions = 0;

        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
    }

    public int find(int x) {
        checkIndex(x);
        findCalls++;

        int root = x;

        while (parent[root] != root) {
            root = parent[root];
        }

        while (x != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }

        return root;
    }

    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);

        if (rootX == rootY) {
            return false;
        }

        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }

        componentCount--;
        successfulUnions++;

        return true;
    }

    public int components() {
        return componentCount;
    }

    public long findCalls() {
        return findCalls;
    }

    public long successfulUnions() {
        return successfulUnions;
    }

    public long dsuOps() {
        return findCalls + successfulUnions;
    }

    public void resetCounters() {
        findCalls = 0;
        successfulUnions = 0;
    }

    private void checkIndex(int x) {
        if (x < 0 || x >= parent.length) {
            throw new IllegalArgumentException("Vertex out of range: " + x);
        }
    }
}