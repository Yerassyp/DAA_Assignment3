package graph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class GraphCoreTest {

    @Test
    void readsDirectedGraphWithParallelEdgesAndNegativeWeights() throws IOException {
        Graph graph = Graph.fromFile(Path.of("data", "small_directed.txt"));

        assertTrue(graph.isDirected());
        assertEquals(6, graph.vertexCount());
        assertEquals(7, graph.edgeCount());
        assertEquals(3, graph.neighbors(0).size());
        assertEquals(5, graph.neighbors(0).get(0).weight);
        assertEquals(2, graph.neighbors(0).get(1).weight);
        assertEquals(-3, graph.neighbors(1).get(0).weight);
        assertTrue(graph.neighbors(5).isEmpty());
    }

    @Test
    void readsCyclicGraphWithSelfLoop() throws IOException {
        Graph graph = Graph.fromFile(Path.of("data", "small_cyclic.txt"));

        assertTrue(graph.isDirected());
        assertEquals(5, graph.vertexCount());
        assertEquals(6, graph.edgeCount());
        assertEquals(2, graph.neighbors(1).size());
        assertEquals(1, graph.neighbors(3).size());
        assertEquals(3, graph.neighbors(3).get(0).to);
        assertEquals(-2, graph.neighbors(3).get(0).weight);
        assertTrue(graph.neighbors(4).isEmpty());
    }

    @Test
    void keepsUndirectedEdgesInBothAdjacencyLists() throws IOException {
        Graph graph = Graph.fromFile(Path.of("data", "small_disconnected.txt"));

        assertFalse(graph.isDirected());
        assertEquals(7, graph.vertexCount());
        assertEquals(6, graph.edgeCount());
        assertEquals(1, graph.neighbors(0).get(0).to);
        assertEquals(0, graph.neighbors(1).get(0).to);
        assertEquals(2, graph.neighbors(3).size());
        assertEquals(2, graph.neighbors(4).size());
        assertEquals(2, graph.neighbors(5).size());
        assertTrue(graph.neighbors(6).isEmpty());
    }

    @Test
    void acceptsAnEmptyGraphAndRejectsInvalidVertices() {
        Graph empty = new Graph(0, true);

        assertEquals(0, empty.vertexCount());
        assertTrue(empty.edges().isEmpty());

        Graph graph = new Graph(3, false);

        assertThrows(IllegalArgumentException.class,
                () -> graph.addEdge(-1, 0, 1));

        assertThrows(IllegalArgumentException.class,
                () -> graph.addEdge(0, 3, 1));

        assertThrows(IllegalArgumentException.class,
                () -> graph.neighbors(3));

        assertThrows(IllegalArgumentException.class,
                () -> new Graph(-1, true));

        assertEquals(0, graph.edgeCount());
    }

    @Test
    void rejectsMalformedInputFiles(@TempDir Path directory) throws IOException {
        Path file = directory.resolve("invalid.txt");

        Files.writeString(file, "");
        assertThrows(IOException.class,
                () -> Graph.fromFile(file));

        Files.writeString(file, "2 2 directed\n0 1 4\n");
        assertThrows(IOException.class,
                () -> Graph.fromFile(file));

        Files.writeString(file, "2 1 sideways\n0 1 4\n");
        assertThrows(IllegalArgumentException.class,
                () -> Graph.fromFile(file));

        Files.writeString(file, "2 1 directed\n0 2 4\n");
        assertThrows(IllegalArgumentException.class,
                () -> Graph.fromFile(file));

        Files.writeString(file, "2 1 directed\n0 1 4\n1 0 4\n");
        assertThrows(IOException.class,
                () -> Graph.fromFile(file));
    }
}