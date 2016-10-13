package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

import java.util.*;

public class TopologicalSort implements Algorithm {

    private Graph graph;
    private List<Node> sortedNodes;
    private Set<Node> sourceNodes;


    @Override
    public void init(Graph theGraph) {
        graph = getCopyOfGraph(theGraph);
        sortedNodes = new ArrayList<>();
        sourceNodes = calculateSourceNodes();

    }

    private Graph getCopyOfGraph(Graph theGraph) {
        Graph aGraphCopy = new SingleGraph("TopoSort");
        for (Node aNode : theGraph.getEachNode()) {
            aGraphCopy.addNode(aNode.getId());
        }
        for (Edge anEdge : theGraph.getEachEdge()) {
            if (anEdge.isDirected()) {
                aGraphCopy.addEdge(anEdge.getId(), anEdge.getSourceNode().getId(), anEdge.getTargetNode().getId(), true);
            } else {
                throwExeeption();
            }
        }
        return aGraphCopy;
    }


    @Override
    public void compute() {
        while (!sourceNodes.isEmpty()) {
            Node aSourceNode = sourceNodes.iterator().next();
            sourceNodes.remove(aSourceNode);
            sortedNodes.add(aSourceNode);
            for (Iterator<Edge> it = aSourceNode.getLeavingEdgeIterator(); it.hasNext(); ) {
                Edge aLeavingEdge = it.next();
                Node aTargetNode = aLeavingEdge.getTargetNode();
                it.remove();
                aTargetNode.getEnteringEdgeSet().remove(aLeavingEdge);
                if (aTargetNode.getEnteringEdgeSet().isEmpty()) {
                    sourceNodes.add(aTargetNode);
                }
            }
        }
        boolean hasCycle = false;
        for (Node aNode : graph.getEachNode()) {
            if (!aNode.getEnteringEdgeSet().isEmpty()) {
                hasCycle = true;
                break;
            }
        }
        if (hasCycle) {
            throwExeeption();
        } else {
            System.out.println("TopologicalSortedNodes:" + Arrays.toString(sortedNodes.toArray()));
        }
    }

    private Set<Node> calculateSourceNodes() {
        Set<Node> aSourceNodeSet = new HashSet<>();
        for (Node aNode : graph.getEachNode()) {
            if (aNode.getInDegree() == 0) {
                aSourceNodeSet.add(aNode);
            }
        }
        if (aSourceNodeSet.isEmpty()) {
            throwExeeption();
        }
        return aSourceNodeSet;
    }

    private void throwExeeption() {
        throw new IllegalStateException("graph is no DAG");
    }

    public List<Node> getSortedNodes() {
        return sortedNodes;
    }
}
