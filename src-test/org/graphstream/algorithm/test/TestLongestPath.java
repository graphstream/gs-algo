package org.graphstream.algorithm.test;

import org.graphstream.algorithm.LongestPath;
import org.graphstream.algorithm.TopologicalSort;
import org.graphstream.graph.Edge;
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
    
        private static Graph getTestSmallDigraphWeighted() {
        Graph graph = new SingleGraph("Graph");
        graph.addNode("S");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        Edge sa = graph.addEdge("S-A", "S", "A", true);
        sa.setAttribute("weight", 1.0);
        Edge sc = graph.addEdge("S-C", "S", "C", true);
        sc.setAttribute("weight", 2.0);
        Edge ab = graph.addEdge("A-B", "A", "B", true);
        ab.setAttribute("weight", 6.0);
        Edge bd = graph.addEdge("B-D", "B", "D", true);
        bd.setAttribute("weight", 1.0);
        Edge be = graph.addEdge("B-E", "B", "E", true);
        be.setAttribute("weight", 2.0);
        Edge ca = graph.addEdge("C-A", "C", "A", true);
        ca.setAttribute("weight", 4.0);
        Edge cd = graph.addEdge("C-D", "C", "D", true);
        cd.setAttribute("weight", 3.0);
        Edge de = graph.addEdge("D-E", "D", "E", true);
        de.setAttribute("weight", 1.0);
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

    @Test(expected = TopologicalSort.GraphHasCycleException.class)
    public void testCycledGraph() {
        Graph graph = TestLongestPath.getTestBiggerDigraph();
        graph.addEdge("H-G", "H", "G", true);
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
    }

    @Test
    public void testLongestPathHops() {
        Graph graph = TestLongestPath.getTestBiggerDigraph();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        Assert.assertEquals(Integer.valueOf(6), path.getLongestPathValue());
    }

    @Test
    public void testLongestPathSmallGraphWeighted() {
        Graph graph = TestLongestPath.getTestSmallDigraphWeighted();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        List<Node> longestPath = path.getLongestPath();
        Assert.assertEquals("[S, C, A, B, D, E]", longestPath.toString());
    }
}
