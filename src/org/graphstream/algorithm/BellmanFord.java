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
 * @since 2009-02-19
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

/**
 * Implementation of the Bellman-Ford algorithm that computes single-source
 * shortest paths in a weighted digraph
 * <p>
 * The Bellman-Ford algorithm computes single-source shortest paths in a
 * weighted digraph (where some of the edge weights may be negative). Dijkstra's
 * algorithm accomplishes the same problem with a lower running time, but
 * requires edge weights to be non-negative. Thus, Bellman-Ford is usually used
 * only when there are negative edge weights (from the <a
 * href="http://en.wikipedia.org/wiki/Bellman-Ford_algorithm">Wikipedia</a>).
 * </p>
 * 
 * <h2>Example</h2>
 * <pre>
 * import java.io.IOException;
 * import java.io.StringReader;
 * 
 * import org.graphstream.algorithm.BellmanFord;
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * import org.graphstream.stream.file.FileSourceDGS;
 * 
 * public class BellmanFordTest {
 * 	
 * 	//     B-(1)-C
 * 	//    /       \
 * 	//  (1)       (10)
 * 	//  /           \
 * 	// A             F
 * 	//  \           /
 * 	//  (1)       (1)
 * 	//    \       /
 * 	//     D-(1)-E
 * 	static String my_graph = 
 * 		"DGS004\n" 
 * 		+ "my 0 0\n" 
 * 		+ "an A \n" 
 * 		+ "an B \n"
 * 		+ "an C \n"
 * 		+ "an D \n"
 * 		+ "an E \n"
 * 		+ "an F \n"
 * 		+ "ae AB A B weight:1 \n"
 * 		+ "ae AD A D weight:1 \n"
 * 		+ "ae BC B C weight:1 \n"
 * 		+ "ae CF C F weight:10 \n"
 * 		+ "ae DE D E weight:1 \n"
 * 		+ "ae EF E F weight:1 \n"
 * 		;
 * 	
 * 	public static void main(String[] args) throws IOException {
 * 		Graph graph = new DefaultGraph("Bellman-Ford Test");
 * 		StringReader reader  = new StringReader(my_graph);
 * 		
 * 		FileSourceDGS source = new FileSourceDGS();
 * 		source.addSink(graph);
 * 		source.readAll(reader);
 * 
 * 		BellmanFord bf = new BellmanFord("weight","A");
 * 		bf.init(graph);
 * 		bf.compute();
 * 
 * 		System.out.println(bf.getShortestPath(graph.getNode("F")));
 * 	}
 * }
 * </pre>
 * <h3>Warning</h3>
 * <p>
 * This Implementation is only a stub. For the moment only attributes located on
 * the edges are supported. If you need more features, consider using the
 * Dijkstra implementation. If you really need that algorithm, please contact
 * the team members through the mailing list.
 * </p>
 * 
 * @reference Bellman, Richard "On a routing problem", Quarterly of Applied
 *            Mathematics 16: 87–90. 1958.
 * 
 * @complexity O(VxE) time, where V and E are the number of vertices and edges
 *             respectively.
 * 
 * @author Antoine Dutot
 * @author Yoann Pigné
 * 
 */
public class BellmanFord implements Algorithm {

	/**
	 * The graph to be computed for shortest path.
	 */
	protected Graph graph;

	/**
	 * ID of the source node.
	 */
	protected String source_id;

	protected Node source;
	
	// Used by default result
	protected String target = "";
	
	/**
	 * object-level unique string that identifies tags of this instance on a
	 * graph.
	 */
	protected String identifier;
	
	/**
	 * Name of attribute used to get weight of edges.
	 */
	protected String weightAttribute;
	
	/**
	 * Default weight attribute
	 */
	public static final String DEFAULT_WEIGHT_ATTRIBUTE = "weight";
	
	/**
	 * Build a new BellmanFord algorithm with default parameters.
	 */
	public BellmanFord() {
		this(DEFAULT_WEIGHT_ATTRIBUTE);
	}
	
	/**
	 * Build a new BellmanFord algorithm giving the name of the weight attribute
	 * for edges.
	 * 
	 * @param attribute
	 *            weight attribute of edges
	 */
	public BellmanFord(String attribute) {
		this(attribute, null);
	}

	/**
	 * Same that {@link #BellmanFord(String)} but setting the id of the source
	 * node.
	 * 
	 * @param attribute
	 *            weight attribute of edges
	 * @param sourceNode
	 *            id of the source node
	 */
	public BellmanFord(String attribute, String sourceNode) {
		this.identifier = this.toString() + "/BellmanFord";
		this.source_id = sourceNode;
		this.weightAttribute = attribute;
	}

	/**
	 * Set the id of the node used as source.
	 * 
	 * @param nodeId
	 *            id of the source node
	 */
	@Parameter(true)
	public void setSource(String nodeId) {
		if((source_id == null || ! source_id.equals(nodeId)) && graph!=null){
			source = graph.getNode(nodeId);
		}
		this.source_id = nodeId;	
	}
	
	@Parameter(true)
	public void setTarget(String target) {
		this.target = target;
	}
	
	@Parameter
	public void setWeightAttribute(String weightAttribute) {
		this.weightAttribute = weightAttribute;
	}

	/**
	 * Get the id of node used as source.
	 * 
	 * @return id of the source node
	 */
	public String getSource() {
		return source_id;
	}

	/**
	 * Constructs all the possible shortest paths from the source node to the
	 * destination (end). Warning: this construction is VERY HEAVY !
	 * 
	 * @param end
	 *            The destination to which shortest paths are computed.
	 * @return a list of shortest paths given with
	 *         {@link org.graphstream.graph.Path} objects.
	 */
	public List<Path> getPathSetShortestPaths(Node end) {
		ArrayList<Path> paths = new ArrayList<Path>();
		pathSetShortestPath_facilitate(end, new Path(), paths);
		return paths;
	}

	@SuppressWarnings("unchecked")
	private void pathSetShortestPath_facilitate(Node current, Path path,
			List<Path> paths) {
		Node source = graph.getNode(this.source_id);

		if (current != source) {
			Node next = null;
			ArrayList<? extends Edge> predecessors = (ArrayList<? extends Edge>) current
					.getAttribute(identifier+".predecessors");
			while (current != source && predecessors.size() == 1) {
				Edge e = predecessors.get(0);
				next = e.getOpposite(current);
				path.add(current, e);
				current = next;
				predecessors = (ArrayList<? extends Edge>) current
						.getAttribute(identifier+".predecessors");
			}
			if (current != source) {
				final Node c = current ;
				predecessors.forEach(e -> {
					Path p = path.getACopy();
					p.add(c, e);
					pathSetShortestPath_facilitate(e.getOpposite(c), p, paths);
				});
			}
		}
		if (current == source) {
			paths.add(path);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
		if (getSource() != null){
			source = graph.getNode(getSource());
		}
	}
	
	
	
	/**
	 * Set the unique identifier for this instance.
	 * 
	 * @see #getIdentifier()
	 * 
	 * @param identifier
	 *            the unique identifier to set
	 */
	@Parameter
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	/**
	 * The unique identifier of this instance. Used to tag attributes in the graph.
	 * @return the unique identifier of this graph.
	 */
	public String getIdentifier() {
		return this.identifier; 
	}

	/**
	 * Returns the value of the shortest path between the source node and the
	 * given target according to the attribute specified in the constructor. If
	 * <code>target</code> is not in the same connected component as the source
	 * node, then the method returns <code>Double.POSITIVE_INFINITY</code>
	 * (Infinity).
	 * 
	 * @param target
	 *            The endpoint of the path to compute from the source node given
	 *            in the constructor.
	 * @return A numerical value that represent the distance of the shortest
	 *         path.
	 */
	public double getShortestPathValue(Node target) {
		Double d = (double) target.getAttribute(identifier+".distance");
		if (d != null)
			return d;
		return Double.POSITIVE_INFINITY;
	}
	
	/**
	 * Returns the shortest path between the source node and one given target. 
	 * If multiple shortest paths exist, one of them is returned at random.
	 * 
	 * @param target
	 *            the target of the shortest path starting at the source node
	 *            given in the constructor.
	 * @return A {@link org.graphstream.graph.Path} object that constrains the
	 *         list of nodes and edges that constitute it.
	 */
	@SuppressWarnings("unchecked")
	public Path getShortestPath(Node target) {
		Path p = new Path();
		if (target == source ) {
			return p;
		}
		boolean noPath = false;
		Node v = target;
		while (v != source && !noPath) {
			ArrayList<? extends Edge> list = (ArrayList<? extends Edge>) v
					.getAttribute(identifier+".predecessors");
			if (list == null) {
				noPath = true;
			} else {
				Edge parentEdge = list.get(0);
				p.add(v, parentEdge);
				v = parentEdge.getOpposite(v);
			}
		}
		return p;
	}
	
	/**
	 * @see #getShortestPath(Node target)
	 */
	public Path getShortestPath() {
		return getShortestPath(graph.getNode(target));
	}
	
	@Result
    public String defaultResult() {
    	return getShortestPath().toString() ;
    }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	@SuppressWarnings("unchecked")
	public void compute() {
		Node source = graph.getNode(this.source_id);

		// Step 1: Initialize graph
		graph.nodes().forEach(n -> {
			if (n == source)
				n.setAttribute(identifier+".distance", 0.0);
			else
				n.setAttribute(identifier+".distance", Double.POSITIVE_INFINITY);
		});
				
		// Step 2: relax edges repeatedly
		graph.nodes().forEach(n -> {
			graph.edges().forEach(e -> {
				Node n0 = e.getNode0();
				Node n1 = e.getNode1();
				Double d0 = (Double) n0.getAttribute(identifier+".distance");
				Double d1 = (Double) n1.getAttribute(identifier+".distance");
				Double we = (Double) e.getAttribute(weightAttribute);
				if (we == null)
					throw new NumberFormatException(
							"org.graphstream.algorithm.BellmanFord: Problem with attribute \""
									+ weightAttribute + "\" on edge " + e);

				if (d0 != null) {
					if (d1 == null || d1 >= d0 + we) {
						n1.setAttribute(identifier+".distance", d0 + we);
						ArrayList<Edge> predecessors = (ArrayList<Edge>) n1
								.getAttribute(identifier+".predecessors");

						if (d1 != null && d1 == d0 + we) {
							if (predecessors == null) {
								predecessors = new ArrayList<Edge>();
							}
						} else {
							predecessors = new ArrayList<Edge>();
						}
						if (!predecessors.contains(e)) {
							predecessors.add(e);
						}

						n1.setAttribute(identifier+".predecessors",
								predecessors);
					}
				}
			});
		});
		

		// Step 3: check for negative-weight cycles
		graph.edges().forEach(e -> {
			Node n0 = e.getNode0();
			Node n1 = e.getNode1();
			Double d0 = (Double) n0.getAttribute(identifier+".distance");
			Double d1 = (Double) n1.getAttribute(identifier+".distance");

			Double we = (Double) e.getAttribute(weightAttribute);

			if (we == null) {
				throw new NumberFormatException(
						String.format(
								"%s: Problem with attribute \"%s\" on edge \"%s\"",
								BellmanFord.class.getName(), weightAttribute,
								e.getId()));
			}

			if (d1 > d0 + we) {
				throw new NumberFormatException(
						String.format(
								"%s: Problem: negative weight, cycle detected on edge \"%s\"",
								BellmanFord.class.getName(), e.getId()));
			}
		});
	}
}
