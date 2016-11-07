package org.graphstream.algorithm.test;

import org.graphstream.algorithm.TopologicalSort;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestTopologicalSort {
    public static Graph getTestDigraph() {
        Graph graph = new SingleGraph("Graph");
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
        return graph;
    }

    public static Graph getTestCycleDigraph() {
        Graph graph = getTestDigraph();
        graph.addEdge("3-5", "3", "5", true);
        return graph;
    }

    public static Graph getTestNondirectedGraph() {
        Graph graph = getTestDigraph();
        graph.addEdge("3-5", "3", "5");
        return graph;
    }

    public static List<String> getTestDigraphPossibleTopologicalSorts() {
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

        return allPossibleTopologicalSort;
    }

    @Test
    public void testKahnTopologicalSortSmallGraph() {
        Graph graph = getTestDigraph();
        List<String> allPossibleTopologicalSort = getTestDigraphPossibleTopologicalSorts();

        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.KAHN);
        sort.init(graph);
        sort.compute();

        //check if algorithm gets one of the possible ordering
        Assert.assertTrue(allPossibleTopologicalSort.contains(Arrays.toString(sort.getSortedArray())));
    }

    @Test(expected = TopologicalSort.GraphHasCycleException.class)
    public void testKahnGraphWithCyclesShouldThrowException() {
        Graph graph = getTestCycleDigraph();

        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.KAHN);
        sort.init(graph);
        sort.compute();
    }

    @Test(expected = TopologicalSort.GraphHasCycleException.class)
    public void testKahnGraphWithNonDirectedEdgeShouldThrowException() {
        Graph graph = getTestNondirectedGraph();

        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.KAHN);
        sort.init(graph);
        sort.compute();
    }

    @Test
    public void testDFSTopologicalSortSmallGraph() {
        Graph graph = TestTopologicalSort.getTestDigraph();
        List<String> allPossibleTopologicalSort = TestTopologicalSort.getTestDigraphPossibleTopologicalSorts();

        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.DEPTH_FIRST);
        sort.init(graph);
        sort.compute();

        //check if algorithm gets one of the possible ordering
        Assert.assertTrue(allPossibleTopologicalSort.contains(Arrays.toString(sort.getSortedArray())));
    }

    @Test(expected = TopologicalSort.GraphHasCycleException.class)
    public void testDFSGraphWithCyclesShouldThrowException() {
        Graph graph = TestTopologicalSort.getTestCycleDigraph();

        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.DEPTH_FIRST);
        sort.init(graph);
        sort.compute();
    }

    @Test(expected = TopologicalSort.GraphHasCycleException.class)
    public void testDFSGraphWithNonDirectedEdgeShouldThrowException() {
        Graph graph = TestTopologicalSort.getTestNondirectedGraph();

        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.DEPTH_FIRST);
        sort.init(graph);
        sort.compute();
    }
}
