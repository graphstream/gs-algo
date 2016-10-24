package org.graphstream.algorithm.test;

import org.graphstream.algorithm.InPlaceTopologicalSort;
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

        InPlaceTopologicalSort sort = new InPlaceTopologicalSort();
        sort.init(graph);
        sort.compute();

        //check if algorithm gets one of the possible ordering
        Assert.assertTrue(allPossibleTopologicalSort.contains(Arrays.toString(sort.getTopologicalSort())));
    }

    @Test(expected = InPlaceTopologicalSort.GraphHasCycleException.class)
    public void testGraphWithCyclesShouldThrowException() {
        Graph graph = TestTopologicalSort.getTestCycleDigraph();

        InPlaceTopologicalSort sort = new InPlaceTopologicalSort();
        sort.init(graph);
        sort.compute();
    }

    @Test(expected = InPlaceTopologicalSort.GraphHasCycleException.class)
    public void testGraphWithNonDirectedEdgeShouldThrowException() {
        Graph graph = TestTopologicalSort.getTestNondirectedGraph();

        InPlaceTopologicalSort sort = new InPlaceTopologicalSort();
        sort.init(graph);
        sort.compute();
    }
}
