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

import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.edgeLength;
import static org.graphstream.ui.graphicGraph.GraphPosLengthUtils.nodePosition;

import java.util.ArrayList;
import java.util.HashMap;

import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

/**
 * An implementation of the A* algorithm.
 * 
 * <p>
 * A* computes the shortest path from a node to another in a graph. It guarantees
 * that the path found is the shortest one, given its heuristic is admissible,
 * and a path exists between the two nodes. It will fail if the two nodes are in
 * two distinct connected components.
 * </p>
 * 
 * <p>
 * In this A* implementation, the various costs (often called g, h and f) are
 * given by a {@link org.graphstream.algorithm.AStar.Costs} class. This class
 * must provide a way to compute:
 * <ul>
 * <li>The cost of moving from a node to another, often called g;</li>
 * <li>The estimated cost from a node to the destination, the heuristic, often
 * noted h;</li>
 * <li>f is the sum of g and h and is computed automatically.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * By default the {@link org.graphstream.algorithm.AStar.Costs} implementation
 * used uses a heuristic that always returns 0. This makes A* an * equivalent of
 * the Dijkstra algorithm, but also makes it less efficient.
 * </p>
 * 
 * <p>
 * If there are several equivalent shortest paths between the two nodes, the returned
 * one is arbitrary. Therefore this AStar algorithm works with multi-graphs but if two
 * edges between two nodes have the same properties, the one that will be chosen will
 * be arbitrary. 
 * </p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>The basic usage is to create an instance of A* (optionally specify a {@link Costs}
 * object), then to ask it to compute from a shortest path from one target to one
 * destination, and finally to ask for that path:
 * </p>
 * <pre>
 * AStart astar = new AStar(graph); 
 * astar.compute("A", "Z"); // with A and Z node identifiers in the graph. 
 * Path path = astar.getShortestPath();
 * </pre>
 * <p>
 * The advantage of A* is that it can consider any cost function to drive the
 * search. You can (and should) create your own cost functions implementing the
 * {@link org.graphstream.algorithm.AStar.Costs} interface.
 * </p>
 * <p>
 * You can also test the default euclidean "distance" cost function on a graph that has
 * "x" and "y" values. You specify the {@link Costs} function before calling the
 * {@link #compute(String,String)} method:
 * </p>
 * <pre>
 * AStart astar = new AStar(graph); 
 * astar.setCosts(new DistanceCosts());
 * astar.compute("A", "Z"); 
 * Path path = astar.getShortestPath();
 * </pre>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * import java.io.IOException;
 * import java.io.StringReader;
 * 
 * import org.graphstream.algorithm.AStar;
 * import org.graphstream.algorithm.AStar.DistanceCosts;
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * import org.graphstream.stream.file.FileSourceDGS;
 * 
 * public class AStarTest {
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
 * 		+ "an A xy: 0,1\n" 
 * 		+ "an B xy: 1,2\n"
 * 		+ "an C xy: 2,2\n"
 * 		+ "an D xy: 1,0\n"
 * 		+ "an E xy: 2,0\n"
 * 		+ "an F xy: 3,1\n"
 * 		+ "ae AB A B weight:1 \n"
 * 		+ "ae AD A D weight:1 \n"
 * 		+ "ae BC B C weight:1 \n"
 * 		+ "ae CF C F weight:10 \n"
 * 		+ "ae DE D E weight:1 \n"
 * 		+ "ae EF E F weight:1 \n"
 * 		;
 * 
 * 	public static void main(String[] args) throws IOException {
 * 		Graph graph = new DefaultGraph("A* Test");
 * 		StringReader reader = new StringReader(my_graph);
 * 
 * 		FileSourceDGS source = new FileSourceDGS();
 * 		source.addSink(graph);
 * 		source.readAll(reader);
 * 
 * 		AStar astar = new AStar(graph);
 * 		//astar.setCosts(new DistanceCosts());
 * 		astar.compute("C", "F");
 * 
 * 		System.out.println(astar.getShortestPath());
 * 	}
 * }
 * </pre>
 *
 * @complexity The complexity of A* depends on the heuristic.
 */
public class AStar implements Algorithm {
	/**
	 * The graph.
	 */
	protected Graph graph;

	/**
	 * The source node id.
	 */
	protected String source;

	/**
	 * The target node id.
	 */
	protected String target;

	/**
	 * How to compute the path cost, the cost between two nodes and the
	 * heuristic. The heuristic to estimate the distance from the current
	 * position to the target.
	 */
	protected Costs costs = new DefaultCosts();

	/**
	 * The open set.
	 */
	protected HashMap<Node, AStarNode> open = new HashMap<Node, AStarNode>();

	/**
	 * The closed set.
	 */
	protected HashMap<Node, AStarNode> closed = new HashMap<Node, AStarNode>();

	/**
	 * If found the shortest path is stored here.
	 */
	protected Path result;

	/**
	 * Set to false if the algorithm ran, but did not found any path from the
	 * source to the target, or if the algorithm did not run yet.
	 */
	protected boolean pathFound = false;

	/**
	 * New A* algorithm.
	 */
	public AStar() {
	}

	/**
	 * New A* algorithm on a given graph.
	 * 
	 * @param graph
	 *            The graph where the algorithm will compute paths.
	 */
	public AStar(Graph graph) {
		init(graph);
	}

	/**
	 * New A* algorithm on the given graph.
	 * 
	 * @param graph
	 *            The graph where the algorithm will compute paths.
	 * @param src
	 *            The start node.
	 * @param trg
	 *            The destination node.
	 */
	public AStar(Graph graph, String src, String trg) {
		this(graph);
		setSource(src);
		setTarget(trg);
	}

	/**
	 * Change the source node. This clears the already computed path, but
	 * preserves the target node name.
	 * 
	 * @param nodeName
	 *            Identifier of the source node.
	 */
	@Parameter(true)
	public void setSource(String nodeName) {
		clearAll();
		source = nodeName;
	}

	/**
	 * Change the target node. This clears the already computed path, but
	 * preserves the source node name.
	 * 
	 * @param nodeName
	 *            Identifier of the target node.
	 */
	@Parameter(true)
	public void setTarget(String nodeName) {
		clearAll();
		target = nodeName;
	}

	/**
	 * Specify how various costs are computed. The costs object is in charge of
	 * computing the cost of displacement from one node to another (and
	 * therefore allows to compute the cost from the source node to any node).
	 * It also allows to compute the heuristic to use for evaluating the cost
	 * from the current position to the target node. Calling this DOES NOT clear
	 * the currently computed paths.
	 * 
	 * @param costs
	 *            The cost method to use.
	 */
	@Parameter
	public void setCosts(Costs costs) {
		this.costs = costs;
	}

	/*
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		clearAll();
		this.graph = graph;
	}

	/*
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		if (source != null && target != null) {
			Node sourceNode = graph.getNode(source);
			Node targetNode = graph.getNode(target);

			if (sourceNode == null)
				throw new RuntimeException("source node '" + source
						+ "' does not exist in the graph");

			if (targetNode == null)
				throw new RuntimeException("target node '" + target
						+ "' does not exist in the graph");

			aStar(sourceNode, targetNode);
		}
	}

	/**
	 * The computed path, or null if nor result was found.
	 * 
	 * @return The computed path, or null if no path was found.
	 */
	@Result
	public Path getShortestPath() {
		return result;
	}

	/**
	 * After having called {@link #compute()} or
	 * {@link #compute(String, String)}, if the {@link #getShortestPath()}
	 * returns null, or this method return true, there is no path from the given
	 * source node to the given target node. In other words, the graph has
	 * several connected components. It also return true if the algorithm did
	 * not run.
	 * 
	 * @return True if there is no possible path from the source to the
	 *         destination or if the algorithm did not run.
	 */
	public boolean noPathFound() {
		return (! pathFound);
	}

	/**
	 * Build the shortest path from the target/destination node, following the
	 * parent links.
	 * 
	 * @param target
	 *            The destination node.
	 * @return The path.
	 */
	public Path buildPath(AStarNode target) {
		Path path = new Path();

		ArrayList<AStarNode> thePath = new ArrayList<AStarNode>();
		AStarNode node = target;

		while (node != null) {
			thePath.add(node);
			node = node.parent;
		}

		int n = thePath.size();

		if (n > 1) {
			AStarNode current = thePath.get(n - 1);
			AStarNode follow = thePath.get(n - 2);

			path.add(current.node, follow.edge);

			current = follow;

			for (int i = n - 3; i >= 0; i--) {
				follow = thePath.get(i);
				path.add(follow.edge);
				current = follow;
			}
		}

		return path;
	}

	/**
	 * Call {@link #compute()} after having called {@link #setSource(String)}
	 * and {@link #setTarget(String)}.
	 * 
	 * @param source
	 *            Identifier of the source node.
	 * @param target
	 *            Identifier of the target node.
	 */
	public void compute(String source, String target) {
		setSource(source);
		setTarget(target);
		compute();
	}

	/**
	 * Clear the already computed path. This does not clear the source node
	 * name, the target node name and the weight attribute name.
	 */
	protected void clearAll() {
		open.clear();
		closed.clear();

		result = null;
		pathFound = false;
	}

	/**
	 * The A* algorithm proper.
	 * 
	 * @param sourceNode
	 *            The source node.
	 * @param targetNode
	 *            The target node.
	 */
	protected void aStar(Node sourceNode, Node targetNode) {
		clearAll();
		open.put(
				sourceNode,
				new AStarNode(sourceNode, null, null, 0, costs.heuristic(
						sourceNode, targetNode)));

		pathFound = false;

		while (!open.isEmpty()) {
			AStarNode current = getNextBetterNode();

			assert (current != null);

			if (current.node == targetNode) {
				// We found it !
				assert current.edge != null;
				pathFound = true;
				result = buildPath(current);
				return;
			} else {
				open.remove(current.node);
				closed.put(current.node, current);

				// For each successor of the current node :
				
				current.node.leavingEdges().forEach(edge -> {
					Node next = edge.getOpposite(current.node);
					double h = costs.heuristic(next, targetNode);
					double g = current.g + costs.cost(current.node, edge, next);
					double f = g + h;

					// If the node is already in open with a better rank, we
					// skip it.

					AStarNode alreadyInOpen = open.get(next);

					if (!(alreadyInOpen != null && alreadyInOpen.rank <= f)) {
						
						// If the node is already in closed with a better rank; we
						// skip it.
						AStarNode alreadyInClosed = closed.get(next);

						if (!(alreadyInClosed != null && alreadyInClosed.rank <= f)){

							closed.remove(next);
							open.put(next, new AStarNode(next, edge, current, g, h));
						}
					}
				});
			}
		}
	}

	/**
	 * Find the node with the lowest rank in the open list.
	 * 
	 * @return The node of open that has the lowest rank.
	 */
	protected AStarNode getNextBetterNode() {
		// TODO: consider using a priority queue here ?
		// The problem is that we use open has a hash to ensure
		// a node we will add to to open is not yet in it.

		AStarNode theChosenOne = null;
		
		theChosenOne = open.values().stream()
				.min((n,m) -> Double.compare(n.rank, m.rank))
				.get();
		
		return theChosenOne;
	}

	// Nested classes

	/**
	 * the distance between the current position and the target.
	 */
	public interface Costs {
		/**
		 * Estimate cost from the given node to the target node.
		 * 
		 * @param node
		 *            A node.
		 * @param target
		 *            The target node.
		 * @return The estimated cost between a node and a target node.
		 */
		double heuristic(Node node, Node target);
		
		/**
		 * Cost of displacement from parent to next. The next node must be
		 * directly connected to parent, or -1 is returned.
		 * 
		 * @param parent
		 *            The node we come from.
		 * @param from
	 * The definition of an heuristic. The heuristic is in charge of evaluating
		 *            The edge used between the two nodes (in case this is a
		 *            multi-graph).
		 * @param next
		 *            The node we go to.
		 * @return The real cost of moving from parent to next, or -1 if next is
		 *         not directly connected to parent by an edge.
		 */
		double cost(Node parent, Edge from, Node next);
	}

	/**
	 * An implementation of the Costs interface that provides a default
	 * heuristic. It computes the G part using "weights" on edges. These weights
	 * must be stored in an attribute on edges. By default this attribute must
	 * be named "weight", but this can be changed. The weight attribute must be
	 * a {@link Number} an must be translatable to a double value. This implementation
	 * always return 0 for the H value. This makes the A* algorithm an
	 * equivalent of the Dijkstra algorithm.
	 */
	public static class DefaultCosts implements Costs {
		/**
		 * The attribute used to retrieve the cost of an edge cross.
		 */
		protected String weightAttribute = "weight";

		/**
		 * New default costs for the A* algorithm. The cost of each edge is
		 * obtained from a numerical attribute stored under the name "weight".
		 * This attribute must be a descendant of Number (Double, Float,
		 * Integer, etc.).
		 */
		public DefaultCosts() {
		}

		/**
		 * New default costs for the A* algorithm. The cost of each edge is
		 * obtained from the attribute stored on each edge under the
		 * "weightAttributeName". This attribute must be a descendant of Number
		 * (Double, Float, Integer, etc.).
		 * 
		 * @param weightAttributeName
		 *            The name of cost attributes on edges.
		 */
		public DefaultCosts(String weightAttributeName) {
			weightAttribute = weightAttributeName;
		}

		/**
		 * The heuristic. This one always returns zero, therefore transforming
		 * this A* into the Dijkstra algorithm.
		 * 
		 * @return The estimation.
		 */
		public double heuristic(Node node, Node target) {
			return 0;
		}

		/**
		 * The cost of moving from parent to next. If there is no cost
		 * attribute, the edge is considered to cost value "1".
		 * 
		 * @param parent
		 *            The node we come from.
		 * @param edge
		 *            The edge between parent and next.
		 * @param next
		 *            The node we go to.
		 * @return The movement cost.
		 */
		public double cost(Node parent, Edge edge, Node next) {
			// Edge choice = parent.getEdgeToward( next.getId() );

			if (edge != null && edge.hasNumber(weightAttribute))
				return ((Number) edge.getNumber(weightAttribute)).doubleValue();

			return 1;
		}
	}

	/**
	 * An implementation of the Costs interface that assume that the weight of
	 * edges is an Euclidean distance in 2D or 3D. No weight attribute is used.
	 * Instead, for the G value, the edge weights are used. For the H value the
	 * Euclidean distance in 2D or 3D between the current node and the target
	 * node is used. For this Costs implementation to work, the graph nodes must
	 * have a position (either individual "x", "y" and "z" attribute, or "xy"
	 * attribute or even "xyz" attributes. If there are only "x" and "y" or "xy"
	 * attribute this works in 2D, else the third coordinate is taken into
	 * account.
	 */
	public static class DistanceCosts implements AStar.Costs {
		public double heuristic(Node node, Node target) {
			double xy1[] = nodePosition(node);
			double xy2[] = nodePosition(target);

			double x = xy2[0] - xy1[0];
			double y = xy2[1] - xy1[1];
			double z = (xy1.length > 2 && xy2.length > 2) ? (xy2[2] - xy1[2])
					: 0;

			return Math.sqrt((x * x) + (y * y) + (z * z));
		}

		public double cost(Node parent, Edge edge, Node next) {
			return edgeLength(edge);// parent.getEdgeToward( next.getId() ) );
		}
	}

	/**
	 * Representation of a node in the A* algorithm.
	 * 
	 * <p>
	 * This representation contains :
	 * <ul>
	 * <li>the node itself;</li>
	 * <li>its parent node (to reconstruct the path);</li>
	 * <li>the g value (cost from the source to this node);</li>
	 * <li>the h value (estimated cost from this node to the target);</li>
	 * <li>the f value or rank, the sum of g and h.</li>
	 * </ul>
	 * </p>
	 */
	protected class AStarNode {
		/**
		 * The node.
		 */
		public Node node;

		/**
		 * The node's parent.
		 */
		public AStarNode parent;

		/**
		 * The edge used to go from parent to node.
		 */
		public Edge edge;

		/**
		 * Cost from the source node to this one.
		 */
		public double g;

		/**
		 * Estimated cost from this node to the destination.
		 */
		public double h;

		/**
		 * Sum of g and h.
		 */
		public double rank;

		/**
		 * New A* node.
		 * 
		 * @param node
		 *            The node.
		 * @param edge
		 *            The edge used to go from parent to node (useful for
		 *            multi-graphs).
		 * @param parent
		 *            It's parent node.
		 * @param g
		 *            The cost from the source to this node.
		 * @param h
		 *            The estimated cost from this node to the target.
		 */
		public AStarNode(Node node, Edge edge, AStarNode parent, double g,
				double h) {
			this.node = node;
			this.edge = edge;
			this.parent = parent;
			this.g = g;
			this.h = h;
			this.rank = g + h;
		}
	}
}