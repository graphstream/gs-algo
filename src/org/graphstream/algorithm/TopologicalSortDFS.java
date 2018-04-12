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
 * @since 2017-11-26
 * 
 * @author jordilaforge <8899ph@web.de>
 */
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Implementation of depth first search algorithm for a topological sorting of a directed acyclic graph (DAG).
 * Every DAG has at least one topological ordering and this is a algorithm known for constructing a
 * topological ordering in linear time.
 *
 * @reference Tarjan, Robert E. (1976), "Edge-disjoint spanning trees and depth-first search", Acta Informatica, 6 (2): 171–185
 * @complexity O(VxE) time, where V and E are the number of vertices and edges
 * respectively.
 */
public class TopologicalSortDFS implements Algorithm{

    /**
     * graph to calculate a topological ordering
     */
    private Graph graph;

    /**
     * collection containing sorted nodes after calculation
     */
    private List<Node> sortedNodes;

    /**
     * collection to mark visited nodes
     */
    private HashSet<Node> markedNodeList;

    /**
     * collection to mark temporary visited node needed to throw exception
     */
    private HashSet<Node> tempMarkedNodeList;

    @Override
    public void init(Graph theGraph) {
        graph = theGraph;
        sortedNodes = new ArrayList<>();
        markedNodeList = new HashSet<>();
        tempMarkedNodeList = new HashSet<>();
    }

    @Override
    public void compute() {
        while (markedNodeList.size() != graph.nodes().count()){
            graph.nodes().forEach(this::visit);
        }
        Collections.reverse(sortedNodes);
    }

    /**
     * Recursive function to compute topo sort
     * @param theNode start node of recursive computation
     */
    private void visit(Node theNode) {
        if(markedNodeList.contains(theNode)){
            return;
        }
        if(tempMarkedNodeList.contains(theNode)){
            throwException();
        }
        tempMarkedNodeList.add(theNode);
        theNode.leavingEdges().forEach(anEdge -> visit(anEdge.getTargetNode()));
        markedNodeList.add(theNode);
        sortedNodes.add(theNode);
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
