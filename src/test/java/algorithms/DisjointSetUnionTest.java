package algorithms;

import java.util.HashSet;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisjointSetUnionTest {

    @Test
    void startsWithSeparateSets() {
        DisjointSetUnion dsu = new DisjointSetUnion(5);

        assertEquals(5, dsu.components());

        for (int i = 0; i < 5; i++) {
            assertEquals(i, dsu.find(i));
        }

        assertEquals(5, dsu.findCalls());
        assertEquals(0, dsu.successfulUnions());
        assertEquals(5, dsu.dsuOps());
    }

    @Test
    void ignoresDuplicateAndSelfUnions() {
        DisjointSetUnion dsu = new DisjointSetUnion(5);

        assertTrue(dsu.union(0, 1));
        assertFalse(dsu.union(0, 1));
        assertFalse(dsu.union(2, 2));

        assertEquals(4, dsu.components());
        assertEquals(1, dsu.successfulUnions());

        assertEquals(dsu.find(0), dsu.find(1));
        assertNotEquals(dsu.find(0), dsu.find(2));
    }

    @Test
    void mergesBalancedTreesCorrectly() {
        DisjointSetUnion dsu = new DisjointSetUnion(8);

        assertTrue(dsu.union(0, 1));
        assertTrue(dsu.union(2, 3));
        assertTrue(dsu.union(0, 2));

        assertTrue(dsu.union(4, 5));
        assertTrue(dsu.union(6, 7));
        assertTrue(dsu.union(4, 6));

        assertEquals(2, dsu.components());

        assertTrue(dsu.union(0, 4));
        assertEquals(1, dsu.components());

        int root = dsu.find(0);

        for (int i = 1; i < 8; i++) {
            assertEquals(root, dsu.find(i));
        }

        assertEquals(7, dsu.successfulUnions());
    }

    @Test
    void countsOperationsAndResetsCounters() {
        DisjointSetUnion dsu = new DisjointSetUnion(4);

        assertEquals(0, dsu.dsuOps());

        assertTrue(dsu.union(0, 1));

        assertEquals(2, dsu.findCalls());
        assertEquals(1, dsu.successfulUnions());
        assertEquals(3, dsu.dsuOps());

        assertFalse(dsu.union(0, 1));

        assertEquals(4, dsu.findCalls());
        assertEquals(1, dsu.successfulUnions());
        assertEquals(5, dsu.dsuOps());

        dsu.find(1);

        assertEquals(5, dsu.findCalls());
        assertEquals(6, dsu.dsuOps());

        dsu.resetCounters();

        assertEquals(0, dsu.findCalls());
        assertEquals(0, dsu.successfulUnions());
        assertEquals(0, dsu.dsuOps());
        assertEquals(3, dsu.components());
    }

    @Test
    void handlesInvalidIndicesAndMakeSet() {
        assertThrows(IllegalArgumentException.class,
                () -> new DisjointSetUnion(-1));

        DisjointSetUnion dsu = new DisjointSetUnion(3);

        assertThrows(IllegalArgumentException.class,
                () -> dsu.find(-1));

        assertThrows(IllegalArgumentException.class,
                () -> dsu.find(3));

        assertThrows(IllegalArgumentException.class,
                () -> dsu.union(0, 3));

        assertTrue(dsu.union(0, 1));

        dsu.makeSet(2);

        assertEquals(2, dsu.components());
        assertEquals(0, dsu.findCalls());
        assertEquals(0, dsu.successfulUnions());

        assertEquals(0, dsu.find(0));
        assertEquals(1, dsu.find(1));

        dsu.makeSet(0);

        assertEquals(0, dsu.components());

        assertThrows(IllegalArgumentException.class,
                () -> dsu.find(0));
    }

    @Test
    void agreesWithReferenceAfterRandomUnions() {
        int n = 70;

        DisjointSetUnion dsu = new DisjointSetUnion(n);
        int[] labels = new int[n];
        Random random = new Random(42);

        for (int i = 0; i < n; i++) {
            labels[i] = i;
        }

        for (int step = 0; step < 400; step++) {
            int x = random.nextInt(n);
            int y = random.nextInt(n);

            int labelX = labels[x];
            int labelY = labels[y];

            boolean shouldMerge = labelX != labelY;

            assertEquals(shouldMerge, dsu.union(x, y));

            if (shouldMerge) {
                for (int i = 0; i < n; i++) {
                    if (labels[i] == labelY) {
                        labels[i] = labelX;
                    }
                }
            }

            HashSet<Integer> groups = new HashSet<>();

            for (int label : labels) {
                groups.add(label);
            }

            assertEquals(groups.size(), dsu.components());

            if (step % 40 == 0 || step == 399) {
                for (int i = 0; i < n; i++) {
                    for (int j = 0; j < n; j++) {
                        boolean expected = labels[i] == labels[j];
                        boolean actual = dsu.find(i) == dsu.find(j);

                        assertEquals(expected, actual);
                    }
                }
            }
        }
    }

    @Test
    void handlesOneHundredThousandElements() {
        int n = 100_000;

        DisjointSetUnion dsu = new DisjointSetUnion(n);

        for (int i = 1; i < n; i++) {
            assertTrue(dsu.union(i - 1, i));
        }

        assertEquals(1, dsu.components());
        assertEquals(n - 1, dsu.successfulUnions());
        assertEquals(dsu.find(0), dsu.find(n - 1));
    }
}