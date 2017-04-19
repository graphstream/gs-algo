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
    private Graph graph;

    private Map<Node, Integer> distances;

    private List<Node> longestPath;

    public void init(Graph theGraph) {
        graph = Graphs.clone(theGraph);
        distances = new HashMap<>();
        longestPath = new ArrayList<>();
    }

    public void compute() {
        initializeHashMap();
        TopologicalSort sort = new TopologicalSort(TopologicalSort.SortAlgorithm.DEPTH_FIRST);
        sort.init(graph);
        sort.compute();
        Node[] sortedArray = sort.getSortedArray();

        for (Node aNode : sortedArray) {
            for (Edge anEdge : aNode.getEachEnteringEdge()) {
                Node aSourceNode = anEdge.getSourceNode();
                Node aTargetNode = anEdge.getTargetNode();
                int aMaxDistance = Math.max(distances.get(aTargetNode), distances.get(aSourceNode)) + 1;
                distances.put(aTargetNode, aMaxDistance);
            }
        }
        Map.Entry<Node, Integer> maxEntry = null;
        for (Map.Entry<Node, Integer> entry : distances.entrySet()) {
            if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0) {
                maxEntry = entry;
            }
        }
        for (Node aNode : sortedArray) {
            if (aNode.equals(maxEntry.getKey())) {
                longestPath.add(aNode);
                getMaxNeigbourgh(aNode);
            }
        }
        Collections.reverse(longestPath);
        System.out.println(longestPath);
    }

    private void getMaxNeigbourgh(Node theNode) {
        Node aMaxNode = null;
        int aMaxDistance = 0;
        for (Edge anEdge : theNode.getEachEnteringEdge()) {
            Node aSourceNode = anEdge.getSourceNode();
            if (distances.get(aSourceNode) >= aMaxDistance) {
                aMaxDistance = distances.get(aSourceNode);
                aMaxNode = aSourceNode;
            }
        }
        if (aMaxNode != null) {
            longestPath.add(aMaxNode);
            getMaxNeigbourgh(aMaxNode);
        }

    }

    private void initializeHashMap() {
        for (Node aNode : graph.getEachNode()) {
            distances.put(aNode, 0);
        }
    }

    public List<Node> getLongestPath() {
        return longestPath;
    }
}
