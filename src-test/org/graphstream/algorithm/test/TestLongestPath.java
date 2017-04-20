package org.graphstream.algorithm.test;

import org.graphstream.algorithm.LongestPath;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestLongestPath {

    private static Graph getTestSmallDigraph() {
        Graph graph = new SingleGraph("Graph");
        graph.addNode("S");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addEdge("S-A", "S", "A", true);
        graph.addEdge("S-C", "S", "C", true);
        graph.addEdge("A-B", "A", "B", true);
        graph.addEdge("B-D", "B", "D", true);
        graph.addEdge("B-E", "B", "E", true);
        graph.addEdge("C-A", "C", "A", true);
        graph.addEdge("C-D", "C", "D", true);
        graph.addEdge("D-E", "D", "E", true);
        return graph;
    }

    private static Graph getTestBiggerDigraph() {
        Graph graph = new SingleGraph("Graph");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addNode("F");
        graph.addNode("G");
        graph.addNode("H");
        graph.addNode("I");
        graph.addEdge("A-B", "A", "B", true);
        graph.addEdge("B-C", "B", "C", true);
        graph.addEdge("B-D", "B", "D", true);
        graph.addEdge("C-H", "C", "H", true);
        graph.addEdge("C-G", "C", "G", true);
        graph.addEdge("D-E", "D", "E", true);
        graph.addEdge("D-F", "D", "F", true);
        graph.addEdge("E-G", "E", "G", true);
        graph.addEdge("E-H", "E", "H", true);
        graph.addEdge("F-I", "F", "I", true);
        graph.addEdge("G-H", "G", "H", true);
        graph.addEdge("H-I", "H", "I", true);
        return graph;
    }

    @Test
    public void testLongestPathSmallGraph() {
        Graph graph = TestLongestPath.getTestSmallDigraph();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        List<Node> longestPath = path.getLongestPath();
        Assert.assertEquals("[S, C, A, B, D, E]", longestPath.toString());
    }

    @Test
    public void testLongestPathBiggerGraph() {
        Graph graph = TestLongestPath.getTestBiggerDigraph();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        List<Node> longestPath = path.getLongestPath();
        Assert.assertEquals("[A, B, D, E, G, H, I]", longestPath.toString());
    }
}
