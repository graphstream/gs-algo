package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Find a topological sort of the graph, using constant memory and without
 * making a copy of the graph.
 * <p>
 * This algorithm uses a depth-first search to find a topological sort of a
 * graph. Information about the algorithm is available on
 * <a href="https://en.wikipedia.org/wiki/Topological_sorting#Depth-first_search">Wikipedia</a>
 * <p>
 * </p>
 *
 * @complexity O(<em>v</em> + <em>e</em>), where <em>v</em> is the number of
 * vertices in the graph and <em>e</em> is the number of edges.
 */
public class InPlaceTopologicalSort implements Algorithm {
    public class GraphHasCycleException extends RuntimeException {}

    private final static int MARK_UNMARKED = 0;
    private final static int MARK_TEMP = 1;
    private final static int MARK_PERM = 2;

    /**
     * The graph to be operated on.
     */
    private Graph graph;

    /**
     * Array to hold the nodes in topological order.
     */
    private Node[] sortedNodes;

    /**
     * The next index to be filled in the sorted nodes array. This has to be a
     * field as it is updated across recursive calls of visitNode.
     */
    private int index;

    /**
     * Initialise algorithm with a graph to operate on.
     * @param graph Graph to be topologically sorted
     */
    public void init(Graph graph) {
        this.graph = graph;
    }

    /**
     * Find a topological sort.
     */
    public void compute() {
        if (graph == null) {
            throw new NotInitializedException(this);
        }

        int[] marks = new int[graph.getNodeCount()];
        sortedNodes = new Node[graph.getNodeCount()];
        // DFS gives reverse topological order, so it's fastest to just
        // fill the array in reverse
        index = graph.getNodeCount() - 1;
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
     * Return a topological sort of the graph.
     *
     * @return Topologically sorted array of nodes
     */
    public Node[] getTopologicalSort() {
        return sortedNodes;
    }

    /**
     * Return a topological sort of the graph in list form.
     *
     * @return Topologically sorted list of nodes
     */
    public List<Node> getSortedNodes() {
        return new ArrayList<>(Arrays.asList(sortedNodes));
    }
}
