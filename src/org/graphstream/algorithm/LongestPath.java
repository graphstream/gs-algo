package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

import java.util.*;

/**
 * Implementation of longest path algorithm in a DAG (directed acyclic graph) using topologicalSort
 *
 * @complexity O(V+E) time, where V and E are the number of vertices and edges
 * respectively.
 */
public class LongestPath implements Algorithm {

    /**
     * graph to calculate longest path
     */
    private Graph              graph;

    private Map<Node, Integer> distanceMap;

    private List<Node>         longestPath;
    
    private Map.Entry<Node, Integer> longestPathNode;

    private boolean weighted = true;

    public void init(Graph theGraph) {
        graph = Graphs.clone(theGraph);
        distanceMap = new HashMap<>();
        longestPath = new ArrayList<>();
    }

    public void compute() {
        initializeAlgorithm();
        TopologicalSort aTopoSortAlgorithm = new TopologicalSort(TopologicalSort.SortAlgorithm.DEPTH_FIRST);
        aTopoSortAlgorithm.init(graph);
        aTopoSortAlgorithm.compute();
        Node[] aSortedArray = aTopoSortAlgorithm.getSortedArray();
        if (weighted) {
            fillDistanceMapWeighted(aSortedArray);
        } else {
            fillDistanceMapUnweighted(aSortedArray);
        }
        Map.Entry<Node, Integer> maxEntry = getMaxEntryOfMap();
        longestPathNode = maxEntry;
        longestPath.add(maxEntry.getKey());
        getMaxNeigbourgh(maxEntry.getKey());
        Collections.reverse(longestPath);
    }

    private void fillDistanceMapWeighted(Node[] aSortedArray) {
        System.out.println("weighted version");
    }

    private void fillDistanceMapUnweighted(Node[] theSortedArray) {
        for (Node aNode : theSortedArray) {
            for (Edge anEdge : aNode.getEachEnteringEdge()) {
                Node aSourceNode = anEdge.getSourceNode();
                Node aTargetNode = anEdge.getTargetNode();
                int aMaxDistance = Math.max(distanceMap.get(aTargetNode), distanceMap.get(aSourceNode)) + 1;
                distanceMap.put(aTargetNode, aMaxDistance);
            }
        }
    }

    private Map.Entry<Node, Integer> getMaxEntryOfMap() {
        Map.Entry<Node, Integer> maxEntry = null;
        for (Map.Entry<Node, Integer> entry : distanceMap.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }

    private void getMaxNeigbourgh(Node theNode) {
        Node aMaxNode = null;
        int aMaxDistance = 0;
        for (Edge anEdge : theNode.getEachEnteringEdge()) {
            Node aSourceNode = anEdge.getSourceNode();
            if (distanceMap.get(aSourceNode) >= aMaxDistance) {
                aMaxDistance = distanceMap.get(aSourceNode);
                aMaxNode = aSourceNode;
            }
        }
        if (aMaxNode != null) {
            longestPath.add(aMaxNode);
            getMaxNeigbourgh(aMaxNode);
        }

    }

    private void initializeAlgorithm() {
        for (Node aNode : graph.getEachNode()) {
            for (Edge anEdge : aNode.getEachEdge()) {
                double aWeight = anEdge.getNumber("weight");
                if (Double.isNaN(aWeight)) {
                    weighted = false;
                }
            }
            distanceMap.put(aNode, 0);
        }
    }

    public List<Node> getLongestPath() {
        return longestPath;
    }
    
    public Integer getLongestPathValue() {
        return longestPathNode.getValue();
    }
}
