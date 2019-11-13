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
 * @since 2017-11-30
 * 
 * @author jordilaforge <8899ph@web.de>
 */
package org.graphstream.algorithm;

import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
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
     * map with all nodes and there distance from starting point
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
    private String weightAttribute;

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
        fillDistanceMap(aTopoSortAlgorithm.getSortedNodes());
        longestPathNode = distanceMap.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        if(longestPathNode == null){
            throw new IllegalStateException("No max node found!");
        }
        longestPath.add(longestPathNode.getKey());
        getMaxNeighbor(longestPathNode.getKey());
        Collections.reverse(longestPath);
    }

    private void fillDistanceMap(List<Node> theSortedArray) {
        for (Node aNode : theSortedArray) {
            aNode.enteringEdges().forEach(anEdge -> {
                Node aSourceNode = anEdge.getSourceNode();
                Node aTargetNode = anEdge.getTargetNode();
                double aWeight = weighted ? anEdge.getNumber(getWeightAttribute()) : 1;
                Double aMaxDistance = Math.max(distanceMap.get(aTargetNode), distanceMap.get(aSourceNode) + aWeight);
                distanceMap.put(aTargetNode, aMaxDistance);
            });
        }
    }

    private void getMaxNeighbor(Node theNode) {
        Optional<Edge> optionalEdge = theNode.enteringEdges()
                .max(Comparator.comparingDouble(anEdge -> distanceMap.get(anEdge.getSourceNode())));
        optionalEdge.ifPresent(edge -> {
            longestPath.add(edge.getSourceNode());
            getMaxNeighbor(edge.getSourceNode());
        });
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
        return path;
    }
    
    @Result
    public String defaultResult() {
    	return getLongestPath().toString() ;
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
    
    @Parameter
    public void setWeightAttribute(String weightAttribute) {
        this.weightAttribute = weightAttribute;
    }
}