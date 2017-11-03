package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Implementation of Kahn's algorithm for a topological sort of a directed acyclic graph (DAG).
 * Every DAG has at least one topological ordering and this is a algorithm known for constructing a
 * topological ordering in linear time.
 *
 * @reference Kahn, Arthur B. (1962), "Topological sorting of large networks", Communications of the ACM, 5 (11): 558–562
 * @complexity O(VxE) time, where V and E are the number of vertices and edges
 * respectively.
 */
public class TopologicalSort implements Algorithm {

    /**
     * graph to calculate a topological ordering
     */
    private Graph graph;

    /**
     * collection containing sorted nodes after calculation
     */
    private List<Node> sortedNodes;

    /**
     * collection containing all source nodes (inDegree=00)
     */
    private Set<Node> sourceNodes;

    @Override
    public void init(Graph theGraph) {
        graph = getCopyOfGraph(theGraph);
        sortedNodes = new ArrayList<>();
        sourceNodes = calculateSourceNodes();

    }

    /**
     * makes a deep copy of the graph to not modify the original graph
     *
     * @param theGraph graph to calculate topological ordering
     * @return copy of graph
     */
    private Graph getCopyOfGraph(Graph theGraph) {
        Graph aGraphCopy = new SingleGraph("TopoSort");
        
        theGraph.nodes().forEach(aNode -> {
        	aGraphCopy.addNode(aNode.getId());
        });
        
        theGraph.edges().forEach(anEdge -> {
        	if (anEdge.isDirected()) {
                aGraphCopy.addEdge(anEdge.getId(), anEdge.getSourceNode().getId(), anEdge.getTargetNode().getId(), true);
            } else {
                throwExeeption();
            }
        });
        
        return aGraphCopy;
    }

    @Override
    public void compute() {
    	while (!sourceNodes.isEmpty()) {
    		Node aSourceNode = sourceNodes.iterator().next();
    		
    		sourceNodes.remove(aSourceNode);
    		sortedNodes.add(aSourceNode);
    		
    		long count = aSourceNode.leavingEdges().count() ;
    		for ( int i = 0 ; i < count ; i++){
    			Edge aLeavingEdge = aSourceNode.getLeavingEdge(0);
    			Node aTargetNode = aLeavingEdge.getTargetNode();
    			
    			graph.removeEdge(aLeavingEdge);
    			
    			if (aTargetNode.enteringEdges().count() == 0) {
    				sourceNodes.add(aTargetNode);
    			}
    		}
    	}
    	
    	
    	
    	AtomicBoolean hasCycle = new AtomicBoolean(false);
        graph.nodes()
        	.filter(aNode -> aNode.enteringEdges().count() != 0)
        	.forEach(aNode -> hasCycle.set(true));
        
        
        if (hasCycle.get()) {
            throwExeeption();
        } else {
            System.out.println("TopologicalSortedNodes:" + Arrays.toString(sortedNodes.toArray()));
        }
    }

    /**
     * calculates the source nodes
     * @return set of source nodes
     */
    private Set<Node> calculateSourceNodes() {
        Set<Node> aSourceNodeSet = new HashSet<>();
        graph.nodes()
        	.filter(aNode -> aNode.getInDegree() == 0)
        	.forEach(aNode -> aSourceNodeSet.add(aNode));
       
        if (aSourceNodeSet.isEmpty()) {
            throwExeeption();
        }
        return aSourceNodeSet;
    }

    /**
     * throws exception if given graph is no directed acyclic graph (DAG)
     */
    private void throwExeeption() {
        throw new IllegalStateException("graph is no DAG");
    }

    /**
     * gets sorted list of the given graph
     * @return topological sorted list of nodes
     */
    public List<Node> getSortedNodes() {
        return sortedNodes;
    }
}