/*
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 *
 *
 * @since 2016-10-14
 * 
 * @author jordilaforge <8899ph@web.de>
 */
package org.graphstream.algorithm;

import java.util.*;
import java.util.stream.Collectors;

import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

/**
 * Implementation of Kahn's algorithm for a topological sorting of a directed acyclic graph (DAG).
 * Every DAG has at least one topological ordering and this is a algorithm known for constructing a
 * topological ordering in linear time without considering initial copying the graph and searching for source nodes.
 *
 * @reference Kahn, Arthur B. (1962), "Topological sorting of large networks", Communications of the ACM, 5 (11): 558–562
 * @complexity O(VxE) time, where V and E are the number of vertices and edges
 * respectively.
 */
public class TopologicalSortKahn implements Algorithm {

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
        Graph aGraphCopy = new SingleGraph("TopoSortKahn");
        
        theGraph.nodes().forEach(aNode -> aGraphCopy.addNode(aNode.getId()));
        
        theGraph.edges().forEach(anEdge -> {
            if(!anEdge.isDirected()){
               throwException();
            }
            aGraphCopy.addEdge(anEdge.getId(), anEdge.getSourceNode().getId(), anEdge.getTargetNode().getId(), true);
        });

        return aGraphCopy;
    }

    @Override
    public void compute() {
    	while (!sourceNodes.isEmpty()) {
    		Node aSourceNode = sourceNodes.iterator().next();

    		sourceNodes.remove(aSourceNode);
    		sortedNodes.add(aSourceNode);

    		aSourceNode.leavingEdges().forEach(anEdge -> removeEdge(aSourceNode));
    	}


        if(hasCycle()){
    	    throwException();
        }
        System.out.println("TopologicalSortedNodes:" + Arrays.toString(sortedNodes.toArray()));
    }

    /**
     * Checks graph for cycles
     * @return true if graph has a cycle
     */
    private boolean hasCycle() {
        return graph.nodes()
        	.anyMatch(aNode -> aNode.enteringEdges().count() != 0);
    }

    /**
     * removes edge
     * @param aSourceNode source nodes
     */
    private void removeEdge(Node aSourceNode) {
        Edge aLeavingEdge = aSourceNode.getLeavingEdge(0);
        Node aTargetNode = aLeavingEdge.getTargetNode();

        graph.removeEdge(aLeavingEdge);

        if (aTargetNode.enteringEdges().count() == 0) {
            sourceNodes.add(aTargetNode);
        }
    }

    /**
     * calculates the source nodes
     * @return set of source nodes
     */
    private Set<Node> calculateSourceNodes() {
        Set<Node> aSourceNodeSet = graph.nodes()
        	.filter(aNode -> aNode.getInDegree() == 0)
        	.collect(Collectors.toSet());
       
        if (aSourceNodeSet.isEmpty()) {
            throwException();
        }
        return aSourceNodeSet;
    }

    /**
     * throws exception if given graph is no directed acyclic graph (DAG)
     */
    private void throwException() {
        throw new IllegalStateException("graph is no DAG");
    }

    /**
     * gets sorted list of the given graph
     * @return topological sorted list of nodes
     */
    public List<Node> getSortedNodes() {
        return sortedNodes;
    }

    @Result
    public String defaultResult() {
    	return getSortedNodes().toString() ;
    }

}