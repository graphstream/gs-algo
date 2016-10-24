package org.graphstream.algorithm.test;

import org.graphstream.algorithm.InPlaceTopologicalSort;
import org.graphstream.algorithm.TopologicalSort;
import org.graphstream.graph.Graph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class TestInPlaceTopologicalSort {
    @Test
    public void testTopologicalSortSmallGraph() {
        Graph graph = TestTopologicalSort.getTestDigraph();
        List<String> allPossibleTopologicalSort = TestTopologicalSort.getTestDigraphPossibleTopologicalSorts();

        TopologicalSort sort = new InPlaceTopologicalSort();
        sort.init(graph);
        sort.compute();

        //check if algorithm gets one of the possible ordering
        Assert.assertTrue(allPossibleTopologicalSort.contains(Arrays.toString(sort.getSortedNodes().toArray())));
    }

    @Test(expected = IllegalStateException.class)
    public void testGraphWithCyclesShouldThrowException() {
        Graph graph = TestTopologicalSort.getTestCycleDigraph();

        TopologicalSort sort = new InPlaceTopologicalSort();
        sort.init(graph);
        sort.compute();
    }

    @Test(expected = IllegalStateException.class)
    public void testGraphWithNonDirectedEdgeShouldThrowException() {
        Graph graph = TestTopologicalSort.getTestNondirectedGraph();

        TopologicalSort sort = new InPlaceTopologicalSort();
        sort.init(graph);
        sort.compute();
    }
}
