package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

import java.util.*;

/**
 * Implementation of longest path algorithm in a DAG (directed acyclic graph) using topologicalSort
 *
 * @complexity yet to be calculated
 */
public class LongestPath implements Algorithm {

    /**
     * graph to calculate longest path
     */
    private Graph graph;

    /**
     * map with all disctances from starting point
     */
    private Map<Node, Double> distanceMap;

    /**
     * calculated longest path
     */
    private List<Node> longestPath;

    /**
     * node and value at the end of the longest path
     */
    private Map.Entry<Node, Double> longestPathNode;

    /**
     * weighted or unweighted graph
     */
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
        Map.Entry<Node, Double> maxEntry = getMaxEntryOfMap();
        longestPathNode = maxEntry;
        longestPath.add(maxEntry.getKey());
        getMaxNeigbourgh(maxEntry.getKey());
        Collections.reverse(longestPath);
    }

    private void fillDistanceMapWeighted(Node[] theSortedArray) {
        for (Node aNode : theSortedArray) {
            for (Edge anEdge : aNode.getEachEnteringEdge()) {
                Node aSourceNode = anEdge.getSourceNode();
                Node aTargetNode = anEdge.getTargetNode();
                double aWeight = anEdge.getNumber("weight");
                Double aMaxDistance = Math.max(distanceMap.get(aTargetNode), distanceMap.get(aSourceNode) + aWeight);
                distanceMap.put(aTargetNode, aMaxDistance);
            }
        }
    }

    private void fillDistanceMapUnweighted(Node[] theSortedArray) {
        for (Node aNode : theSortedArray) {
            for (Edge anEdge : aNode.getEachEnteringEdge()) {
                Node aSourceNode = anEdge.getSourceNode();
                Node aTargetNode = anEdge.getTargetNode();
                Double aMaxDistance = Math.max(distanceMap.get(aTargetNode), distanceMap.get(aSourceNode)) + 1;
                distanceMap.put(aTargetNode, aMaxDistance);
            }
        }
    }

    private Map.Entry<Node, Double> getMaxEntryOfMap() {
        Map.Entry<Node, Double> maxEntry = null;
        for (Map.Entry<Node, Double> entry : distanceMap.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        return maxEntry;
    }

    private void getMaxNeigbourgh(Node theNode) {
        Node aMaxNode = null;
        double aMaxDistance = 0.0;
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
            distanceMap.put(aNode, 0.0);
        }
    }

    /**
     * gets sorted list of the longest path
     * @return sorted list of nodes in longest path
     */
    public List<Node> getLongestPath() {
        return longestPath;
    }

    /**
     * get value of longest path
     * if unweighted value = hops
     * @return value of longest path
     */
    public Double getLongestPathValue() {
        return longestPathNode.getValue();
    }
}
