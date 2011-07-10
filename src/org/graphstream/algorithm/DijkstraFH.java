package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

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
 * This implementation uses internally Fibonacci Heap (which explains the "FH"
 * in the name of the class), a data structure that makes it run faster for big
 * graphs.
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
 * example when computing shortest paths from two different sources). The
 * attributes store opaque internal objects and must not be accessed, modified
 * or deleted. The only way to retrieve the solution is using different solution
 * access methods.
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
 * <li>Retrieving the solution using differernt solution access methods</li>
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
 * DijkstraFH dijkstra = new DijkstraFH(DijkstraFH.Element.edge, "result", "length");
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
public class DijkstraFH implements Algorithm {
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
	protected Graph graph;
	protected Node source;

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
	 * Constructs an instance with the specified parameters.
	 * 
	 * @param element
	 *            Graph elements (edges or/and nodes) used to compute the path
	 *            lengths.
	 * @param resultAttribute
	 *            Attribute name used to store internal solution data in the
	 *            nodes of the graph.
	 * @param lengthAttribute
	 *            Attribute name used to define individual element lengths. If
	 *            {@code null} the length of the elements is considered to be
	 *            one.
	 * @throws IllegalArgumentException
	 *             if {@code element} or {@code resultAttribute} are {@code
	 *             null}
	 */
	public DijkstraFH(Element element, String resultAttribute,
			String lengthAttribute) {
		if (element == null)
			throw new IllegalArgumentException("element must not be null");
		if (resultAttribute == null)
			throw new IllegalArgumentException(
					"resultAttribute must not be null");
		this.element = element;
		this.resultAttribute = resultAttribute;
		this.lengthAttribute = lengthAttribute;
		graph = null;
		source = null;
	}

	/**
	 * Constructs an instance with the specified parameters. Individual element
	 * lengths are considered to be one.
	 * 
	 * @param element
	 *            Graph elements (edges or/and nodes) used to compute the path
	 *            lengths.
	 * @param resultAttribute
	 *            Attribute name used to store internal solution data in the
	 *            nodes of the graph.
	 * @throws IllegalArgumentException
	 *             if {@code element} or {@code resultAttribute} are {@code
	 *             null}
	 */
	public DijkstraFH(Element element, String resultAttribute) {
		this(element, resultAttribute, null);
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

	/**
	 * Removes the attributes used to store internal solution data in the nodes
	 * of the graph. Use this method to free memory. Solution access methods
	 * must not be used after calling this method.
	 */
	public void clear() {
		for (Node node : graph) {
			Data data = node.getAttribute(resultAttribute);
			if (data != null) {
				data.fn = null;
				data.edgeFromParent = null;
			}
			node.removeAttribute(resultAttribute);
		}
	}

	// *** Methods of Algorithm interface ***

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
	}

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
	public void compute() {
		// check if computation can start
		if (graph == null)
			throw new IllegalStateException(
					"No graph specified. Call init() first.");
		if (source == null)
			throw new IllegalStateException(
					"No source specified. Call setSource() first.");

		// initialization
		FibonacciHeap<Double, Node> heap = new FibonacciHeap<Double, Node>();
		for (Node node : graph) {
			Data data = new Data();
			double v = node == source ? getSourceLength()
					: Double.POSITIVE_INFINITY;
			data.fn = heap.add(v, node);
			data.edgeFromParent = null;
			node.addAttribute(resultAttribute, data);
		}

		// main loop
		while (!heap.isEmpty()) {
			Node u = heap.extractMin();
			Data dataU = u.getAttribute(resultAttribute);
			dataU.distance = dataU.fn.getKey();
			dataU.fn = null;
			for (Edge e : u.getEachLeavingEdge()) {
				Node v = e.getOpposite(u);
				Data dataV = v.getAttribute(resultAttribute);
				if (dataV.fn == null)
					continue;
				double tryDist = dataU.distance + getLength(e, v);
				if (tryDist < dataV.fn.getKey()) {
					dataV.edgeFromParent = e;
					heap.decreaseKey(dataV.fn, tryDist);
				}
			}
		}
	}

	// *** Iterators ***

	protected class NodeIterator<T extends Node> implements Iterator<T> {
		protected Node nextNode;

		protected NodeIterator(Node target) {
			nextNode = Double.isInfinite(getPathLength(target)) ? null : target;
		}

		public boolean hasNext() {
			return nextNode != null;
		}

		@SuppressWarnings("unchecked")
		public T next() {
			if (nextNode == null)
				throw new NoSuchElementException();
			Node node = nextNode;
			nextNode = getParent(nextNode);
			return (T) node;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"remove is not supported by this iterator");
		}
	}

	protected class EdgeIterator<T extends Edge> implements Iterator<T> {
		protected Node nextNode;
		protected T nextEdge;

		protected EdgeIterator(Node target) {
			nextNode = target;
			nextEdge = getEdgeFromParent(nextNode);
		}

		public boolean hasNext() {
			return nextEdge != null;
		}

		public T next() {
			if (nextEdge == null)
				throw new NoSuchElementException();
			T edge = nextEdge;
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
					iterators.add(u.getEnteringEdgeIterator());
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
			iterators.add(target.getEnteringEdgeIterator());
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

	protected class TreeIterator<T extends Edge> implements Iterator<T> {
		Iterator<Node> nodeIt;
		T nextEdge;

		protected void findNextEdge() {
			nextEdge = null;
			while (nodeIt.hasNext() && nextEdge == null)
				nextEdge = getEdgeFromParent(nodeIt.next());
		}

		protected TreeIterator() {
			nodeIt = graph.getNodeIterator();
			findNextEdge();
		}

		public boolean hasNext() {
			return nextEdge != null;
		}

		public T next() {
			if (nextEdge == null)
				throw new NoSuchElementException();
			T edge = nextEdge;
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
		return target.<Data> getAttribute(resultAttribute).distance;
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
	@SuppressWarnings("unchecked")
	public <T extends Edge> T getEdgeFromParent(Node target) {
		return (T) target.<Data> getAttribute(resultAttribute).edgeFromParent;
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
	public <T extends Node> T getParent(Node target) {
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
	public <T extends Node> Iterator<T> getPathNodesIterator(Node target) {
		return new NodeIterator<T>(target);
	}

	/**
	 * An iterable view of the nodes on the shortest path from the source node
	 * to a given target node. Uses {@link #getPathNodesIterator(Node)}.
	 * 
	 * @param target
	 *            a node
	 * @return an iterable view of the nodes on the shortest path from the
	 *         source to the target
	 * @see #getPathNodesIterator(Node)
	 */
	public <T extends Node> Iterable<T> getPathNodes(final Node target) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return getPathNodesIterator(target);
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
	public <T extends Edge> Iterator<T> getPathEdgesIterator(Node target) {
		return new EdgeIterator<T>(target);
	}

	/**
	 * An iterable view of the edges on the shortest path from the source node
	 * to a given target node. Uses {@link #getPathEdgesIterator(Node)}.
	 * 
	 * @param target
	 *            a node
	 * @return an iterable view of the edges on the shortest path from the
	 *         source to the target
	 * @see #getPathEdgesIterator(Node)
	 */
	public <T extends Edge> Iterable<T> getPathEdges(final Node target) {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return getPathEdgesIterator(target);
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
	public Iterator<Path> getAllPathsIterator(Node target) {
		return new PathIterator(target);
	}

	/**
	 * An iterable view of of <em>all</em> the shortest paths from the source
	 * node to a given target node. Uses {@link #getAllPathsIterator(Node)}
	 * 
	 * @param target
	 *            a node
	 * @return an iterable view of all the shortest paths from the source to the
	 *         target
	 * @see #getAllPathsIterator(Node)
	 */
	public Iterable<Path> getAllPaths(final Node target) {
		return new Iterable<Path>() {
			public Iterator<Path> iterator() {
				return getAllPathsIterator(target);
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
	public <T extends Edge> Iterator<T> getTreeEdgesIterator() {
		return new TreeIterator<T>();
	}

	/**
	 * Dijkstra's algorithm produces a shortest path tree rooted in the source
	 * node. This method provides an iterable view of the edges of this tree.
	 * Uses {@link #getTreeEdgesIterator()}
	 * 
	 * @return an iterable view of the edges of the shortest path tree
	 * @see #getTreeEdgesIterator()
	 */
	public <T extends Edge> Iterable<T> getTreeEdges() {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return getTreeEdgesIterator();
			}
		};
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
		for (Edge e : getPathEdges(target))
			stack.push(e);
		path.setRoot(source);
		while (!stack.isEmpty())
			path.add(stack.pop());
		return path;
	}
}
