package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

import java.util.*;

/**
 * Implementation of DFS and Kahn's algorithms for topological sorting of a directed acyclic graph (DAG).
 * Every DAG has at least one topological ordering and these are algorithms known for constructing a
 * topological ordering in linear time.
 *
 * DFS sorting is the default, as Kahn's algorithm requires the graph to be copied or for the original to
 * be modified, which can be expensive. DFS works in place and with constant memory.
 *
 * @reference Kahn, Arthur B. (1962), "Topological sorting of large networks", Communications of the ACM, 5 (11): 558–562
 *
 * @complexity O(V+E) time, where V and E are the number of vertices and edges
 * respectively.
 */
public class TopologicalSort implements Algorithm {
    public class GraphHasCycleException extends IllegalStateException {}

    public enum SortAlgorithm {
        KAHN,
        DEPTH_FIRST
    }

    private final static int MARK_UNMARKED = 0;
    private final static int MARK_TEMP = 1;
    private final static int MARK_PERM = 2;

    /**
     * The algorithm that will be used for topological sorting
     */
    private final SortAlgorithm algorithm;

    /**
     * graph to calculate a topological ordering
     */
    private Graph graph;

    /**
     * collection containing sorted nodes after calculation
     */
    private Node[] sortedNodes;

    /**
     * Next index to populated in sortedNodes
     */
    private int index;

    /**
     * collection containing all source nodes (inDegree=00)
     */
    private Set<Node> sourceNodes;

    public TopologicalSort() {
        this(SortAlgorithm.DEPTH_FIRST);
    }

    public TopologicalSort(SortAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public void init(Graph theGraph) {
        sortedNodes = new Node[theGraph.getNodeCount()];

        if (algorithm == SortAlgorithm.KAHN) {
            graph = Graphs.clone(theGraph);
            sourceNodes = calculateSourceNodes();
        } else {
            graph = theGraph;
        }
    }

    @Override
    public void compute() {
        if (algorithm == SortAlgorithm.KAHN) {
            index = 0;
            computeKahns();
        } else {
            // DFS gives reverse topological order, so it's fastest to just
            // fill the array in reverse
            index = sortedNodes.length - 1;
            computeDFS();
        }
    }

    private void computeKahns() {
        while (!sourceNodes.isEmpty()) {
            Node aSourceNode = sourceNodes.iterator().next();
            sourceNodes.remove(aSourceNode);

            sortedNodes[index] = aSourceNode;
            index++;

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
            throw new GraphHasCycleException();
        }
    }

    /**
     * calculates the source nodes
     * @return set of source nodes
     */
    private Set<Node> calculateSourceNodes() {
        Set<Node> aSourceNodeSet = new HashSet<>();
        for (Node aNode : graph.getEachNode()) {
            if (aNode.getInDegree() == 0) {
                aSourceNodeSet.add(aNode);
            }
        }
        if (aSourceNodeSet.isEmpty()) {
            throw new GraphHasCycleException();
        }
        return aSourceNodeSet;
    }

    private void computeDFS() {
        if (graph == null) {
            throw new NotInitializedException(this);
        }

        int[] marks = new int[graph.getNodeCount()];
        Node n;

        while ((n = getUnmarkedNode(marks)) != null) {
            visitNode(n, marks);
        }
    }

    private Node getUnmarkedNode(int[] marks) {
        for (int i = 0; i < marks.length; i++) {
            if (marks[i] == MARK_UNMARKED) {
                return graph.getNode(i);
            }
        }

        return null;
    }

    private void visitNode(Node node, int[] marks) {
        int mark = marks[node.getIndex()];

        if (mark == MARK_TEMP) {
            throw new GraphHasCycleException();
        } else if (mark == MARK_UNMARKED) {
            marks[node.getIndex()] = MARK_TEMP;

            for (Edge edge : node.getEachLeavingEdge()) {
                visitNode(edge.getOpposite(node), marks);
            }

            marks[node.getIndex()] = MARK_PERM;

            sortedNodes[index] = node;
            index--;
        }
    }

    /**
     * gets sorted list of the given graph
     * @return topological sorted list of nodes
     */
    public List<Node> getSortedNodes() {
        return Arrays.asList(sortedNodes);
    }
}
