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
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.Stack;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.graphstream.algorithm.util.FibonacciHeap;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

/**
 * <p>
 * Dijkstra's algorithm computes the shortest paths from a given node called
 * source to all the other nodes in a graph. It produces a shortest path tree
 * rooted in the source. <b>This algorithm works only for nonnegative
 * lengths.</b>
 * </p>
 * 
 * <p>
 * This implementation uses internally Fibonacci Heap, a data structure that
 * makes it run faster for big graphs.
 * </p>
 * 
 * <h3>Length of a path</h3>
 * 
 * <p>
 * Traditionally the length of a path is defined as the sum of the lengths of
 * its edges. This implementation allows to take into account also the "lengths"
 * of the nodes. This is done by a parameter of type {@link Element} passed in
 * the constructors.
 * </p>
 * 
 * <p>
 * The lengths of individual elements (edges or/and nodes) are defined using
 * another constructor parameter called {@code lengthAttribute}. If this
 * parameter is {@code null}, the elements are considered to have unit lengths.
 * In other words, the length of a path is the number of its edges or/and nodes.
 * If the parameter is not null, the elements are supposed to have a numeric
 * attribute named {@code lengthAttribute} used to store their lengths.
 * </p>
 * 
 * <h3>Solutions</h3>
 * 
 * <p>
 * Internal solution data is stored in attributes of the nodes of the underlying
 * graph. The name of this attribute is another constructor parameter called
 * {@code resultAttribute}. This name must be specified in order to avoid
 * conflicts with existing attributes, but also to distinguish between solutions
 * produced by different instances of this class working on the same graph (for
 * example when computing shortest paths from two different sources). If not
 * specified, a unique name is chosen automatically based on the hash code of
 * the Dijkstra instance. The attributes store opaque internal objects and must
 * not be accessed, modified or deleted. The only way to retrieve the solution
 * is using different solution access methods.
 * </p>
 * 
 * <h3>Usage</h3>
 * 
 * <p>
 * A typical usage of this class involves the following steps:
 * </p>
 * <ul>
 * <li>Instantiation using one of the constructors with appropriate parameters</li>
 * <li>Initialization of the algorithm using {@link #init(Graph)}</li>
 * <li>Computation of the shortest paths using {@link #compute()}</li>
 * <li>Retrieving the solution using different solution access methods</li>
 * <li>Cleaning up using {@link #clear()}</li>
 * </ul>
 * 
 * <p>
 * Note that if the graph changes after the call of {@link #compute()} the
 * computed solution is no longer valid. In this case the behavior of the
 * different solution access methods is undefined.
 * </p>
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 * Graph graph = ...;
 * 
 * // Edge lengths are stored in an attribute called "length"
 * // The length of a path is the sum of the lengths of its edges
 * // The algorithm will store its results in attribute called "result"
 * Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.edge, "result", "length");
 * 	
 * // Compute the shortest paths in g from A to all nodes
 * dijkstra.init(graph);
 * dijkstra.setSource(graph.getNode("A"));
 * dijkstra.compute();
 * 	
 * // Print the lengths of all the shortest paths
 * for (Node node : graph)
 *     System.out.printf("%s->%s:%6.2f%n", dijkstra.getSource(), node, dijkstra.getPathLength(node));
 * 	
 * // Color in blue all the nodes on the shortest path form A to B
 * for (Node node : dijkstra.getPathNodes(graph.getNode("B")))
 *     node.addAttribute("ui.style", "fill-color: blue;");
 * 	
 * // Color in red all the edges in the shortest path tree
 * for (Edge edge : dijkstra.getTreeEdges())
 *     edge.addAttribute("ui.style", "fill-color: red;");
 * 
 * // Print the shortest path from A to B
 * System.out.println(dijkstra.getPath(graph.getNode("B"));
 * 
 * // Build a list containing the nodes in the shortest path from A to B
 * // Note that nodes are added at the beginning of the list
 * // because the iterator traverses them in reverse order, from B to A
 * List &lt;Node&gt; list1 = new ArrayList&lt;Node&gt;();
 * for (Node node : dijkstra.getPathNodes(graph.getNode("B")))
 *     list1.add(0, node);
 * 
 * // A shorter but less efficient way to do the same thing
 * List&lt;Node&gt; list2 = dijkstra.getPath(graph.getNode("B")).getNodePath();
 * </pre>
 * 
 * @author Stefan Balev
 */
public class Dijkstra extends AbstractSpanningTree {
	protected static class Data {
		FibonacciHeap<Double, Node>.Node fn;
		Edge edgeFromParent;
		double distance;
	}

	/**
	 * This enumeration is used to specify how the length of a path is computed
	 * 
	 * @author Stefan Balev
	 */
	public static enum Element {
		/**
		 * The length of a path is the sum of the lengths of its edges.
		 */
		EDGE,
		/**
		 * The length of a path is the sum of the lengths of its nodes.
		 */
		NODE,
		/**
		 * The length of a path is the sum of the lengths of its edges and
		 * nodes.
		 */
		EDGE_AND_NODE;
	}

	protected Element element;
	protected String resultAttribute;
	protected String lengthAttribute;
	protected Node source;
	
	// Used by default result
	private String sourceId = null;
	private String target;
	// *** Helpers ***

	protected double getLength(Edge edge, Node dest) {
		double lenght = 0;
		if (element != Element.NODE)
			lenght += lengthAttribute == null ? 1 : edge
					.getNumber(lengthAttribute);
		if (element != Element.EDGE)
			lenght += lengthAttribute == null ? 1 : dest
					.getNumber(lengthAttribute);
		if (lenght < 0)
			throw new IllegalStateException("Edge " + edge.getId()
					+ " has negative lenght " + lenght);
		return lenght;
	}

	protected double getSourceLength() {
		if (element == Element.EDGE)
			return 0;
		return lengthAttribute == null ? 1 : source.getNumber(lengthAttribute);
	}

	// *** Constructors ***

	/**
	 * Constructs an instance with the specified parameters. The edges of the shortest path tree are not tagged.
	 * 
	 * @param element
	 *            Graph elements (edges or/and nodes) used to compute the path
	 *            lengths. If {@code null}, the length of the path is computed
	 *            using edges.
	 * @param resultAttribute
	 *            Attribute name used to store internal solution data in the
	 *            nodes of the graph. If {@code null}, a unique name is chosen
	 *            automatically.
	 * @param lengthAttribute
	 *            Attribute name used to define individual element lengths. If
	 *            {@code null} the length of the elements is considered to be
	 *            one.
	 */
	public Dijkstra(Element element, String resultAttribute,
			String lengthAttribute) {
		this(element, resultAttribute, lengthAttribute, null, null, null);
	}

	/**
	 * Constructs an instance in which the length of the path is considered to
	 * be the number of edges. Unique result attribute is chosen automatically. The edges of the shortest path tree are not tagged.
	 */
	public Dijkstra() {
		this(null, null, null, null, null, null);
	}
	
	/**
	 * Constructs an instance with the specified parameters.
	 * 
	 * @param element
	 *            Graph elements (edges or/and nodes) used to compute the path
	 *            lengths. If {@code null}, the length of the path is computed
	 *            using edges.
	 * @param resultAttribute
	 *            Attribute name used to store internal solution data in the
	 *            nodes of the graph. If {@code null}, a unique name is chosen
	 *            automatically.
	 * @param lengthAttribute
	 *            Attribute name used to define individual element lengths. If
	 *            {@code null} the length of the elements is considered to be
	 *            one.
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 * @param flagOn
	 *            value of the <i>flagAttribute</i> if edge is in the spanning
	 *            tree
	 * @param flagOff
	 *            value of the <i>flagAttribute</i> if edge is not in the
	 *            spanning tree
	 */
	public Dijkstra(Element element, String resultAttribute, String lengthAttribute, String flagAttribute, Object flagOn, Object flagOff) {
		super(flagAttribute, flagOn, flagOff);
		this.element = element == null ? Element.EDGE : element;
		this.resultAttribute = resultAttribute == null ? toString()
				+ "_result_" : resultAttribute;
		this.lengthAttribute = lengthAttribute;
		graph = null;
		source = null;
	}

	// *** Some basic methods ***

	/**
	 * Dijkstra's algorithm computes shortest paths from a given source node to
	 * all nodes in a graph. This method returns the source node.
	 * 
	 * @return the source node
	 * @see #setSource(Node)
	 */
	@SuppressWarnings("unchecked")
	public <T extends Node> T getSource() {
		return (T) source;
	}
	
	/**
	 * Dijkstra's algorithm computes shortest paths from a given source node to
	 * all nodes in a graph. This method sets the source node.
	 * 
	 * @param source
	 *            The new source node.
	 * @see #getSource()
	 */
	public void setSource(Node source) {
		this.source = source;
	}
	
	@Parameter(true)
	public void setSource(String source) {
		this.sourceId = source;
	}
	
	@Parameter(true)
	public void setTarget(String target) {
		this.target = target;
	}

	/**
	 * Removes the attributes used to store internal solution data in the nodes
	 * of the graph. Use this method to free memory. Solution access methods
	 * must not be used after calling this method.
	 */
	@Override
	public void clear() {
		super.clear();
		graph.nodes().forEach(node -> {
			Data data = (Data) node.getAttribute(resultAttribute);
			if (data != null) {
				data.fn = null;
				data.edgeFromParent = null;
			}
			node.removeAttribute(resultAttribute);
		});
	}

	// *** Methods of Algorithm interface ***


	/**
	 * Computes the shortest paths from the source node to all nodes in the
	 * graph.
	 * 
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} or {@link #setSource(Node)} have not
	 *             been called before or if elements with negative lengths are
	 *             discovered.
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 * @complexity O(<em>m</em> + <em>n</em>log<em>n</em>) where <em>m</em> is
	 *             the number of edges and <em>n</em> is the number of nodes in
	 *             the graph.
	 */
	@Override
	public void compute() {
		// check if computation can start
		if (graph == null)
			throw new IllegalStateException(
					"No graph specified. Call init() first.");
		if(sourceId != null)
			this.source = graph.getNode(sourceId);
		else if (source == null)
			throw new IllegalStateException(
					"No source specified. Call setSource() first.");
		
		
		resetFlags();
		makeTree();
	}
	
	@Override
	protected void makeTree() {
		// initialization
		FibonacciHeap<Double, Node> heap = new FibonacciHeap<Double, Node>();
		
		graph.nodes().forEach(node -> {
			Data data = new Data();
			double v = node == source ? getSourceLength()
					: Double.POSITIVE_INFINITY;
			data.fn = heap.add(v, node);
			data.edgeFromParent = null;
			node.setAttribute(resultAttribute, data);
		});

		// main loop
		while (!heap.isEmpty()) {
			Node u = heap.extractMin();
			Data dataU = (Data) u.getAttribute(resultAttribute);
			dataU.distance = dataU.fn.getKey();
			dataU.fn = null;
			if (dataU.edgeFromParent != null)
				edgeOn(dataU.edgeFromParent);
			
			u.leavingEdges()
				.filter(e -> ((Data) e.getOpposite(u).getAttribute(resultAttribute)).fn != null)
				.forEach(e -> {
				Node v = e.getOpposite(u);
				Data dataV = (Data) v.getAttribute(resultAttribute);
			
				double tryDist = dataU.distance + getLength(e, v);
				if (tryDist < dataV.fn.getKey()) {
					dataV.edgeFromParent = e;
					heap.decreaseKey(dataV.fn, tryDist);
				}
			});
		}		
	}

	// *** Iterators ***

	protected class NodeIterator implements Iterator<Node> {
		protected Node nextNode;

		protected NodeIterator(Node target) {
			nextNode = Double.isInfinite(getPathLength(target)) ? null : target;
		}

		public boolean hasNext() {
			return nextNode != null;
		}

		public Node next() {
			if (nextNode == null)
				throw new NoSuchElementException();
			Node node = nextNode;
			nextNode = getParent(nextNode);
			return node;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"remove is not supported by this iterator");
		}
	}

	protected class EdgeIterator implements Iterator<Edge> {
		protected Node nextNode;
		protected Edge nextEdge;

		protected EdgeIterator(Node target) {
			nextNode = target;
			nextEdge = getEdgeFromParent(nextNode);
		}

		public boolean hasNext() {
			return nextEdge != null;
		}

		public Edge next() {
			if (nextEdge == null)
				throw new NoSuchElementException();
			Edge edge = nextEdge;
			nextNode = getParent(nextNode);
			nextEdge = getEdgeFromParent(nextNode);
			return edge;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"remove is not supported by this iterator");
		}
	}

	protected class PathIterator implements Iterator<Path> {
		protected List<Node> nodes;
		protected List<Iterator<Edge>> iterators;
		protected Path nextPath;

		protected void extendPathStep() {
			int last = nodes.size() - 1;
			Node v = nodes.get(last);
			double lengthV = getPathLength(v);
			Iterator<Edge> it = iterators.get(last);
			while (it.hasNext()) {
				Edge e = it.next();
				Node u = e.getOpposite(v);
				if (getPathLength(u) + getLength(e, v) == lengthV) {
					nodes.add(u);
					iterators.add(u.enteringEdges().iterator());
					return;
				}
			}
			nodes.remove(last);
			iterators.remove(last);
		}

		protected void extendPath() {
			while (!nodes.isEmpty() && nodes.get(nodes.size() - 1) != source)
				extendPathStep();
		}

		protected void constructNextPath() {
			if (nodes.isEmpty()) {
				nextPath = null;
				return;
			}
			nextPath = new Path();
			nextPath.setRoot(source);
			for (int i = nodes.size() - 1; i > 0; i--)
				nextPath.add(nodes.get(i).getEdgeToward(
						nodes.get(i - 1).getId()));
		}

		public PathIterator(Node target) {
			nodes = new ArrayList<Node>();
			iterators = new ArrayList<Iterator<Edge>>();
			if (Double.isInfinite(getPathLength(target))) {
				nextPath = null;
				return;
			}
			nodes.add(target);
			iterators.add(target.enteringEdges().iterator());
			extendPath();
			constructNextPath();
		}

		public boolean hasNext() {
			return nextPath != null;
		}

		public Path next() {
			if (nextPath == null)
				throw new NoSuchElementException();
			nodes.remove(nodes.size() - 1);
			iterators.remove(iterators.size() - 1);
			extendPath();
			Path path = nextPath;
			constructNextPath();
			return path;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"remove is not supported by this iterator");
		}
	}

	protected class TreeIterator implements Iterator<Edge> {
		Iterator<Node> nodeIt;
		Edge nextEdge;

		protected void findNextEdge() {
			nextEdge = null;
			while (nodeIt.hasNext() && nextEdge == null)
				nextEdge = getEdgeFromParent(nodeIt.next());
		}

		protected TreeIterator() {
			nodeIt = graph.nodes().iterator();
			findNextEdge();
		}

		public boolean hasNext() {
			return nextEdge != null;
		}

		public Edge next() {
			if (nextEdge == null)
				throw new NoSuchElementException();
			Edge edge = nextEdge;
			findNextEdge();
			return edge;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"remove is not supported by this iterator");
		}
	}

	// *** Methods to access the solution ***

	/**
	 * Returns the length of the shortest path from the source node to a given
	 * target node.
	 * 
	 * @param target
	 *            A node
	 * @return the length of the shortest path or
	 *         {@link java.lang.Double#POSITIVE_INFINITY} if there is no path
	 *         from the source to the target
	 * @complexity O(1)
	 */
	public double getPathLength(Node target) {
		return ((Data)target.getAttribute(resultAttribute)).distance;
	}

	/**
	 * Dijkstra's algorithm produces a shortest path tree rooted in the source
	 * node. This method returns the total length of the tree.
	 * 
	 * @return the length of the shortest path tree
	 * @complexity O(<em>n</em>) where <em>n</em> is the number of nodes is the
	 *             graph.
	 */
	public double getTreeLength() {
		double length = getSourceLength();
		for (Edge edge : getTreeEdges()) {
			Node node = edge.getNode0();
			if (getEdgeFromParent(node) != edge)
				node = edge.getNode1();
			length += getLength(edge, node);
		}
		return length;
	}

	/**
	 * Returns the edge between the target node and the previous node in the
	 * shortest path from the source to the target. This is also the edge
	 * connecting the target to its parent in the shortest path tree.
	 * 
	 * @param target
	 *            a node
	 * @return the edge between the target and its predecessor in the shortest
	 *         path, {@code null} if there is no path from the source to the
	 *         target or if the target and the source are the same node.
	 * @see #getParent(Node)
	 * @complexity O(1)
	 */
	public Edge getEdgeFromParent(Node target) {
		return ((Data) target.getAttribute(resultAttribute)).edgeFromParent;
	}

	/**
	 * Returns the node preceding the target in the shortest path from the
	 * source to the target. This node is the parent of the target in the
	 * shortest path tree.
	 * 
	 * @param target
	 *            a node
	 * @return the predecessor of the target in the shortest path, {@code null}
	 *         if there is no path from the source to the target or if the
	 *         target and the source are the same node.
	 * @see #getEdgeFromParent(Node)
	 * @complexity O(1)
	 */
	public Node getParent(Node target) {
		Edge edge = getEdgeFromParent(target);
		if (edge == null)
			return null;
		return edge.getOpposite(target);
	}

	/**
	 * This iterator traverses the nodes on the shortest path from the source
	 * node to a given target node. The nodes are traversed in reverse order:
	 * the target node first, then its predecessor, ... and finally the source
	 * node. If there is no path from the source to the target, no nodes are
	 * traversed. This iterator does not support
	 * {@link java.util.Iterator#remove()}.
	 * 
	 * @param target
	 *            a node
	 * @return an iterator on the nodes of the shortest path from the source to
	 *         the target
	 * @see #getPathNodes(Node)
	 * @complexity Each call of {@link java.util.Iterator#next()} of this
	 *             iterator takes O(1) time
	 */
	public Stream<Node> getPathNodesStream(Node target) {
		return StreamSupport.stream(
		    	Spliterators.spliteratorUnknownSize(
		    			new NodeIterator(target),
		                Spliterator.DISTINCT |
		                Spliterator.IMMUTABLE |
		                Spliterator.NONNULL), false);
	}

	/**
	 * An iterable view of the nodes on the shortest path from the source node
	 * to a given target node. Uses {@link #getPathNodesStream(Node)}.
	 * 
	 * @param target
	 *            a node
	 * @return an iterable view of the nodes on the shortest path from the
	 *         source to the target
	 * @see #getPathNodesStream(Node)
	 */
	public Iterable<Node> getPathNodes(final Node target) {
		return new Iterable<Node>() {
			public Iterator<Node> iterator() {
				return new NodeIterator(target);
			}
		};
	}

	/**
	 * This iterator traverses the edges on the shortest path from the source
	 * node to a given target node. The edges are traversed in reverse order:
	 * first the edge between the target and its predecessor, ... and finally
	 * the edge between the source end its successor. If there is no path from
	 * the source to the target or if he source and the target are the same
	 * node, no edges are traversed. This iterator does not support
	 * {@link java.util.Iterator#remove()}.
	 * 
	 * @param target
	 *            a node
	 * @return an iterator on the edges of the shortest path from the source to
	 *         the target
	 * @see #getPathEdges(Node)
	 * @complexity Each call of {@link java.util.Iterator#next()} of this
	 *             iterator takes O(1) time
	 */
	public Stream<Edge> getPathEdgesStream(Node target) {
		return StreamSupport.stream(
		    	Spliterators.spliteratorUnknownSize(
		    			new EdgeIterator(target),
		                Spliterator.DISTINCT |
		                Spliterator.IMMUTABLE |
		                Spliterator.NONNULL), false);
		
	}

	/**
	 * An iterable view of the edges on the shortest path from the source node
	 * to a given target node. Uses {@link #getPathEdgesStream(Node)}.
	 * 
	 * @param target
	 *            a node
	 * @return an iterable view of the edges on the shortest path from the
	 *         source to the target
	 * @see #getPathEdgesStream(Node)
	 */
	public Iterable<Edge> getPathEdges(final Node target) {
		return new Iterable<Edge>() {
			public Iterator<Edge> iterator() {
				return new EdgeIterator(target);
			}

		};
	}

	/**
	 * This iterator traverses <em>all</em> the shortest paths from the source
	 * node to a given target node. If there is more than one shortest paths
	 * between the source and the target, other solution access methods choose
	 * one of them (the one from the shortest path tree). This iterator can be
	 * used if one needs to know all the paths. Each call to
	 * {@link java.util.Iterator#next()} method of this iterator returns a
	 * shortest path in the form of {@link org.graphstream.graph.Path} object.
	 * This iterator does not support {@link java.util.Iterator#remove()}.
	 * 
	 * @param target
	 *            a node
	 * @return an iterator on all the shortest paths from the source to the
	 *         target
	 * @see #getAllPaths(Node)
	 * @complexity Each call of {@link java.util.Iterator#next()} of this
	 *             iterator takes O(<em>m</em>) time in the worst case, where
	 *             <em>m</em> is the number of edges in the graph
	 */
	public Stream<Path> getAllPathsStream(Node target) {
		return StreamSupport.stream(
		    	Spliterators.spliteratorUnknownSize(
		    			new PathIterator(target),
		                Spliterator.DISTINCT |
		                Spliterator.IMMUTABLE |
		                Spliterator.NONNULL), false);
	}

	/**
	 * An iterable view of of <em>all</em> the shortest paths from the source
	 * node to a given target node. Uses {@link #getAllPathsStream(Node)}
	 * 
	 * @param target
	 *            a node
	 * @return an iterable view of all the shortest paths from the source to the
	 *         target
	 * @see #getAllPathsStream(Node)
	 */
	public Iterable<Path> getAllPaths(final Node target) {
		return new Iterable<Path>() {
			public Iterator<Path> iterator() {
				return new PathIterator(target);
			}
		};
	}

	/**
	 * Dijkstra's algorithm produces a shortest path tree rooted in the source
	 * node. This iterator traverses the edges of this tree. The edges are
	 * traversed in no particular order.
	 * 
	 * @return an iterator on the edges of the shortest path tree
	 * @see #getTreeEdges()
	 * @complexity Each call of {@link java.util.Iterator#next()} of this
	 *             iterator takes O(1) time
	 */
	@Override
	public Stream<Edge> getTreeEdgesStream() {
		return StreamSupport.stream(
		    	Spliterators.spliteratorUnknownSize(
		    			new TreeIterator(),
		                Spliterator.DISTINCT |
		                Spliterator.IMMUTABLE |
		                Spliterator.NONNULL), false);
	}


	/**
	 * Returns the shortest path from the source node to a given target node. If
	 * there is no path from the source to the target returns an empty path.
	 * This method constructs a {@link org.graphstream.graph.Path} object which
	 * consumes heap memory proportional to the number of edges and nodes in the
	 * path. When possible, prefer using {@link #getPathNodes(Node)} and
	 * {@link #getPathEdges(Node)} which are more memory- and time-efficient.
	 * 
	 * @param target
	 *            a node
	 * @return the shortest path from the source to the target
	 * @complexity O(<em>p</em>) where <em>p</em> is the number of the nodes in
	 *             the path
	 */
	public Path getPath(Node target) {
		Path path = new Path();
		if (Double.isInfinite(getPathLength(target)))
			return path;
		Stack<Edge> stack = new Stack<Edge>();
		
		getPathEdges(target).forEach(e -> stack.push(e));
			
		path.setRoot(source);
		while (!stack.isEmpty())
			path.add(stack.pop());
		return path;
	}
	
	@Result
	public String defaultResult() {
		return getPath(graph.getNode(target)).toString();
	}
}
