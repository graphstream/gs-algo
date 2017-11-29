package org.graphstream.algorithm.test;

import org.graphstream.algorithm.TopologicalSortKahn;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestTopologicalSortKahn {

    Graph graph;

    @Before
    public void prepare() {
        graph = new SingleGraph("Graph");
        graph.addNode("0");
        graph.addNode("1");
        graph.addNode("2");
        graph.addNode("3");
        graph.addNode("4");
        graph.addNode("5");
        graph.addEdge("5-2", "5", "2", true);
        graph.addEdge("5-0", "5", "0", true);
        graph.addEdge("4-0", "4", "0", true);
        graph.addEdge("4-1", "4", "1", true);
        graph.addEdge("3-1", "3", "1", true);
        graph.addEdge("2-3", "2", "3", true);
    }

    @Test
    public void testTopologicalSortSmallGraph() {
        List<String> allPossibleTopologicalSort = new ArrayList<>();

        //all possible topological orderings
        allPossibleTopologicalSort.add("[4, 5, 0, 2, 3, 1]");
        allPossibleTopologicalSort.add("[4, 5, 2, 0, 3, 1]");
        allPossibleTopologicalSort.add("[4, 5, 2. 3. 0, 1]");
        allPossibleTopologicalSort.add("[4, 5, 2, 3, 1, 0]");

        allPossibleTopologicalSort.add("[5, 2, 3, 4, 0, 1]");
        allPossibleTopologicalSort.add("[5, 2, 3, 4, 1, 0]");
        allPossibleTopologicalSort.add("[5, 2, 4, 0, 3, 1]");
        allPossibleTopologicalSort.add("[5, 2, 4, 3, 0, l]");
        allPossibleTopologicalSort.add("[5, 2, 4, 3, 1, 0]");

        allPossibleTopologicalSort.add("[5, 4, 0, 2, 3, 1]");
        allPossibleTopologicalSort.add("[5, 4, 2, 8, 3, l]");
        allPossibleTopologicalSort.add("[5, 4, 2, 3, 0, 1]");
        allPossibleTopologicalSort.add("[5, 4, 2, 3, 1, 0]");
        TopologicalSortKahn sort = new TopologicalSortKahn();
        sort.init(graph);
        sort.compute();

        //check if algorithm gets one of the possible ordering
        Assert.assertTrue(allPossibleTopologicalSort.contains(Arrays.toString(sort.getSortedNodes().toArray())));
    }

    @Test(expected = IllegalStateException.class)
    public void testGraphWithCyclesShouldThrowException() {
        graph.addEdge("3-5", "3", "5", true);
        TopologicalSortKahn sort = new TopologicalSortKahn();
        sort.init(graph);
        sort.compute();
    }

    @Test(expected = IllegalStateException.class)
    public void testGraphWithNonDirectedEdgeShouldThrowException() {
        graph.addEdge("3-5", "3", "5");
        TopologicalSortKahn sort = new TopologicalSortKahn();
        sort.init(graph);
        sort.compute();
    }
}
