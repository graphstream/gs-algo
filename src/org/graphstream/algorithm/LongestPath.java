package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

import java.util.*;

/**
 * Implementation of longest path algorithm in a DAG (directed acyclic graph) using topologicalSort
 *
 * @complexity yet to be calculated
 */
public class LongestPath implements Algorithm {

    /**
     * Default weight attribute
     */
    public static final String DEFAULT_WEIGHT_ATTRIBUTE = "weight";

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

    /**
     * Attribute where the weights of the edges are stored
     */
    protected String weightAttribute;

    public void init(Graph theGraph) {
        graph = theGraph;
        distanceMap = new HashMap<>();
        longestPath = new ArrayList<>();
    }

    public void compute() {
        initializeAlgorithm();
        TopologicalSortDFS aTopoSortAlgorithm = new TopologicalSortDFS();
        aTopoSortAlgorithm.init(graph);
        aTopoSortAlgorithm.compute();
        List<Node> aSortedArray = aTopoSortAlgorithm.getSortedNodes();
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

    private void fillDistanceMapWeighted(List<Node> theSortedArray) {
        for (Node aNode : theSortedArray) {
            aNode.enteringEdges().forEach(anEdge -> {
                Node aSourceNode = anEdge.getSourceNode();
                Node aTargetNode = anEdge.getTargetNode();
                double aWeight = anEdge.getNumber(getWeightAttribute());
                Double aMaxDistance = Math.max(distanceMap.get(aTargetNode), distanceMap.get(aSourceNode) + aWeight);
                distanceMap.put(aTargetNode, aMaxDistance);
            });
        }
    }

    private void fillDistanceMapUnweighted(List<Node> theSortedArray) {
        for (Node aNode : theSortedArray) {
            aNode.enteringEdges().forEach(anEdge -> {
                Node aSourceNode = anEdge.getSourceNode();
                Node aTargetNode = anEdge.getTargetNode();
                Double aMaxDistance = Math.max(distanceMap.get(aTargetNode), distanceMap.get(aSourceNode)) + 1;
                distanceMap.put(aTargetNode, aMaxDistance);
            });
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
        final Node[] aMaxNode = {null};
        final double[] aMaxDistance = {0.0};
        theNode.enteringEdges().forEach(anEdge -> {
            Node aSourceNode = anEdge.getSourceNode();
            if (distanceMap.get(aSourceNode) >= aMaxDistance[0]) {
                aMaxDistance[0] = distanceMap.get(aSourceNode);
                aMaxNode[0] = aSourceNode;
            }
        });
        if (aMaxNode[0] != null) {
            longestPath.add(aMaxNode[0]);
            getMaxNeigbourgh(aMaxNode[0]);
        }

    }

    private void initializeAlgorithm() {
        graph.nodes().forEach(aNode -> {
            aNode.edges().forEach(anEdge -> {
                double aWeight = anEdge.getNumber(getWeightAttribute());
                if (Double.isNaN(aWeight)) {
                    weighted = false;
                }
            });
            distanceMap.put(aNode, 0.0);
        });
    }

    /**
     * gets sorted list of the longest path
     * @return sorted list of nodes in longest path
     */
    public List<Node> getLongestPathList() {
        return longestPath;
    }

    /**
     * gets longest path
     *
     * @return longest path
     */
    public Path getLongestPath() {
        Path path = new Path();
        for (int i = 0; i < longestPath.size()-1; i++) {
            Node aSourceNode = longestPath.get(i);
            Node aTargetNode = longestPath.get(i+1);
            Optional<Edge> anEdge = graph.edges()
                    .filter(aNode -> aNode.getSourceNode().equals(aSourceNode))
                    .filter(aNode -> aNode.getTargetNode().equals(aTargetNode))
                    .findAny();
            anEdge.ifPresent(edge -> path.add(aSourceNode, edge));
        }

//        longestPath.forEach(aNode ->{
//            path.add(aNode,createPathEdge(aNode));
//        });
//        for (int i = 0; i < longestPath.size(); i++) {
//            int finalI = i;
//            graph.edges()
//                    .filter(anEdge -> !anEdge.getSourceNode().equals(longestPath.get(finalI)))
//                    .filter(anEdge -> !anEdge.getTargetNode().equals(longestPath.get(finalI + 1)))
//                    .forEach(anEdge -> {
//                path.add(anEdge.getSourceNode(), anEdge);
//            });
//        }
        return path;
    }

    /**
     * get value of longest path
     * if unweighted value = hops
     * @return value of longest path
     */
    public Double getLongestPathValue() {
        return longestPathNode.getValue();
    }

    public String getWeightAttribute() {
        return weightAttribute == null ? DEFAULT_WEIGHT_ATTRIBUTE : weightAttribute;
    }

    public void setWeightAttribute(String weightAttribute) {
        this.weightAttribute = weightAttribute;
    }
}