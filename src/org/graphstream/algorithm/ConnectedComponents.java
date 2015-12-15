package org.graphstream.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.util.Filter;
import org.graphstream.util.FilteredEdgeIterator;
import org.graphstream.util.FilteredNodeIterator;

/**
 * Compute and update the number of connected components of a dynamic graph.
 * 
 * <p>
 * This algorithm computes the connected components for a given graph. Connected
 * components are the set of its connected subgraphs. Two nodes belong to the
 * same connected component when there exists a path (without considering the
 * direction of the edges) between them. Therefore, the algorithm does not
 * consider the direction of the edges. The number of connected components of an
 * undirected graph is equal to the number of connected components of the same
 * directed graph. See
 * <a href="http://en.wikipedia.org/wiki/Connected_component_%28graph_theory%29"
 * >wikipedia</a> for details.
 * </p>
 * 
 * <h2>Dynamics</h2>
 * 
 * <p>
 * This algorithm tries to handle the dynamics of the graph, trying not to
 * recompute all from scratch at each change (kind of re-optimization). In this
 * way, each instance of the algorithm is registered as a graph sink. Each
 * change in the graph topology may affect the algorithm.
 * </p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>
 * To start using the algorithm, you first need an instance of
 * {@link org.graphstream.graph.Graph}, then you only have to instantiate the
 * algorithm class. Whether you specify a reference to the graph in the
 * constructor or you set it with the {@link #init(Graph)} method.
 * </p>
 * 
 * <p>
 * The computation of the algorithm starts only when the graph is specified with
 * the {@link #init(Graph)} method or with the appropriated constructor. In case
 * of a static graph, you may call the {@link #compute()} method. In case of a
 * dynamic graph, the algorithm will compute itself automatically when an event
 * (node or edge added or removed) occurs.
 * </p>
 * 
 * <p>
 * Finally you may ask the algorithm for the number of connected components at
 * any moment with a call to the {@link #getConnectedComponentsCount()} method.
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * import org.graphstream.algorithm.ConnectedComponents;
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * 
 * public class CCTest {
 * 	public static void main(String[] args) {
 * 
 * 		Graph graph = new DefaultGraph(&quot;CC Test&quot;);
 * 
 * 		graph.addNode(&quot;A&quot;);
 * 		graph.addNode(&quot;B&quot;);
 * 		graph.addNode(&quot;C&quot;);
 * 		graph.addEdge(&quot;AB&quot;, &quot;A&quot;, &quot;B&quot;);
 * 		graph.addEdge(&quot;AC&quot;, &quot;A&quot;, &quot;C&quot;);
 * 
 * 		ConnectedComponents cc = new ConnectedComponents();
 * 		cc.init(graph);
 * 
 * 		System.out.printf(&quot;%d connected component(s) in this graph, so far.%n&quot;, cc.getConnectedComponentsCount());
 * 
 * 		graph.removeEdge(&quot;AC&quot;);
 * 
 * 		System.out.printf(&quot;Eventually, there are %d.%n&quot;, cc.getConnectedComponentsCount());
 * 
 * 	}
 * }
 * </pre>
 * 
 * <h2>Additional features</h2>
 * 
 * 
 * <h3>Threshold and Ceiling</h3>
 * <p>
 * It is possible to get rid of connected components belong a size threshold
 * when counting the overall number of connected components. It is also possible
 * to define a ceiling size for the connected component. Above that size
 * ceiling, connected components will not be counted. Use the
 * {@link #getConnectedComponentsCount(int)} or
 * {@link #getConnectedComponentsCount(int, int)} methods.
 * </p>
 * 
 * <h3>Components Identifiers</h3>
 * <p>
 * You can tag each node with an integer that identifies the component it
 * pertains to using {@link #setCountAttribute(String)}. The argument of this
 * method is an arbitrary name that will be used as attribute on each node of
 * the graph. The value of this attribute will be an integer (counting from
 * zero) that is different for each connected component.
 * </p>
 * 
 * <h3>Giant component</h3>
 * <p>
 * The {@link #getGiantComponent()} method gives you a list of nodes belonging
 * to the biggest connected component of the graph.
 * </p>
 * 
 * <h3>Cut Attribute</h3>
 * <p>
 * The cut attribute is a feature that can optionally simulate a given edge to
 * be invisible (as if the edge did not exist). In other words if an edge is
 * given such a cut attribute, it will be ignored by the algorithm when
 * counting. You can enable (or disable by passing null) the cut attribute by
 * specifying it with the {@link #setCutAttribute(String)} method, and by giving
 * the special edges the same attribute.
 * </p>
 * <p>
 * What is it useful for? Well you may want to simulate the removal of a given
 * edge and see if it increases the number of connected components. You may not
 * want to really remove and then re-add that edge in the graph, because such
 * removal event may have consequences on other algorithms, viewer, writers...
 * </p>
 * <p>
 * Note that setting the cut attribute will trigger a new computation of the
 * algorithm.
 * </p>
 * 
 * @author Yoann Pigné
 * @author Antoine Dutot
 * @author Guillaume-Jean Herbiet
 * 
 * @since June 26 2007
 * 
 * @complexity For the initial computation, let n be the number of nodes, then
 *             the complexity is 0(n). For the re-optimization steps, let k be
 *             the number of nodes concerned by the changes (k <= n), the
 *             complexity is O(k).
 */
public class ConnectedComponents extends SinkAdapter
		implements DynamicAlgorithm, Iterable<ConnectedComponents.ConnectedComponent> {
	protected HashSet<ConnectedComponent> components;
	protected HashMap<Node, ConnectedComponent> componentsMap;
	protected Graph graph;

	/**
	 * Optional attribute to set on each node of a given component. This
	 * attribute will have for value an index different for each component.
	 */
	protected String countAttribute;
	/**
	 * Optional edge attribute that make it "invisible". The algorithm will find
	 * two connected components if such an edge is the only link between two
	 * node groups.
	 */
	protected String cutAttribute;

	protected boolean started;

	protected int currentComponentId;

	/**
	 * Construction of an instance with no parameter. The process is not
	 * initialized and the algorithm will not receive any event from any graph.
	 * You will have to call the {@link #init(Graph)} method with a reference to
	 * a graph so that the computation is able to start.
	 * 
	 * After the {@link #init(Graph)} method is invoked, the computation starts
	 * as soon as and event is received or if the {@link #compute()} method is
	 * invoked.
	 */
	public ConnectedComponents() {
		this.started = false;
	}

	/**
	 * Constructor with the given graph. The computation of the algorithm start
	 * only when the {@link #init(Graph)} method is invoked. This Constructor
	 * will call the {@link #init(Graph)} method anyway.
	 * 
	 * @param graph
	 *            The graph who's connected components will be computed.
	 */
	public ConnectedComponents(Graph graph) {
		this();

		if (graph != null) {
			init(graph);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	@Override
	public void init(Graph graph) {
		if (this.graph != null)
			this.graph.removeSink(this);

		this.graph = graph;
		this.graph.addSink(this);

		components = new HashSet<ConnectedComponent>();
		componentsMap = new HashMap<Node, ConnectedComponent>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	@Override
	public void compute() {
		started = true;

		components.clear();
		componentsMap.clear();

		Iterator<? extends Node> nodes = graph.getNodeIterator();

		while (nodes.hasNext()) {
			Node n = nodes.next();

			if (!componentsMap.containsKey(n)) {
				ConnectedComponent cc = new ConnectedComponent();
				computeConnectedComponent(cc, n, null);

				components.add(cc);
			}
		}
	}

	protected void computeConnectedComponent(ConnectedComponent cc, Node from, Edge drop) {
		LinkedList<Node> open = new LinkedList<Node>();

		open.add(from);
		cc.registerNode(from);

		while (!open.isEmpty()) {
			Node n = open.poll();

			Iterator<? extends Edge> edges = n.getEdgeIterator();

			while (edges.hasNext()) {
				Edge e = edges.next();

				if (e != drop && !isCutEdge(e)) {
					Node n2 = e.getOpposite(n);

					if (componentsMap.get(n2) != cc) {
						open.add(n2);
						cc.registerNode(n2);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
	 */
	@Override
	public void terminate() {
		if (graph != null) {
			graph.removeSink(this);

			graph = null;
			started = false;

			components.clear();
			componentsMap.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ConnectedComponent> iterator() {
		return components.iterator();
	}

	public void publish() {
		if (graph == null) {
			return;
		}

		for (Node n : graph) {
			ConnectedComponent cc = componentsMap.get(n);
			assert cc != null;

			n.setAttribute(countAttribute, cc.id);
		}
	}

	/**
	 * Get the connected component that contains the biggest number of nodes.
	 * 
	 * @return the biggest CC.
	 */
	public ConnectedComponent getGiantComponent() {
		checkStarted();

		int maxSize = Integer.MIN_VALUE;
		ConnectedComponent maxCC = null;

		for (ConnectedComponent cc : components) {
			if (cc.size > maxSize) {
				maxCC = cc;
			}
		}

		return maxCC;
	}

	/**
	 * Ask the algorithm for the number of connected components.
	 * 
	 * @return the number of connected components in this graph.
	 */
	public int getConnectedComponentsCount() {
		checkStarted();

		return components.size();
	}

	/**
	 * Ask the algorithm for the number of connected components whose size is
	 * equal to or greater than the specified threshold.
	 * 
	 * @param sizeThreshold
	 *            Minimum size for the connected component to be considered
	 * 
	 * @return the number of connected components, bigger than the given size
	 *         threshold, in this graph.
	 */
	public int getConnectedComponentsCount(int sizeThreshold) {
		return getConnectedComponentsCount(sizeThreshold, 0);
	}

	/**
	 * Ask the algorithm for the number of connected components whose size is
	 * equal to or greater than the specified threshold and lesser than the
	 * specified ceiling.
	 * 
	 * @param sizeThreshold
	 *            Minimum size for the connected component to be considered
	 * @param sizeCeiling
	 *            Maximum size for the connected component to be considered (use
	 *            0 or lower values to ignore the ceiling)
	 * 
	 * @return the number of connected components, bigger than the given size
	 *         threshold, and smaller than the given size ceiling, in this
	 *         graph.
	 */
	public int getConnectedComponentsCount(int sizeThreshold, int sizeCeiling) {
		checkStarted();

		//
		// Simplest case : threshold is lesser than or equal to 1 and
		// no ceiling is specified, we return all the counted components
		//
		if (sizeThreshold <= 1 && sizeCeiling <= 0) {
			return components.size();
		} else {
			int count = 0;

			for (ConnectedComponent cc : components) {
				if (cc.size >= sizeThreshold && (sizeCeiling <= 0 || cc.size < sizeCeiling)) {
					count++;
				}
			}

			return count;
		}
	}

	public ConnectedComponent getConnectedComponentOf(String nodeId) {
		return getConnectedComponentOf(graph.getNode(nodeId));
	}

	public ConnectedComponent getConnectedComponentOf(int nodeIndex) {
		return getConnectedComponentOf(graph.getNode(nodeIndex));
	}

	public ConnectedComponent getConnectedComponentOf(Node n) {
		return componentsMap.get(n);
	}

	/**
	 * Enable (or disable by passing null) an optional attribute that makes
	 * edges that have it invisible (as if the edge did not existed). Be
	 * careful, setting the cut attribute will trigger a new computation of the
	 * algorithm.
	 * 
	 * @param cutAttribute
	 *            The name for the cut attribute or null if the cut attribute
	 *            option must be disabled.
	 */
	public void setCutAttribute(String cutAttribute) {
		this.cutAttribute = cutAttribute;

		if (graph != null) {
			compute();
		}
	}

	/**
	 * Enable (or disable by passing null for countAttribute) an optional
	 * attribute that will be assigned to each node. The value of this attribute
	 * will be an integer different for each computed component.
	 * 
	 * @param countAttribute
	 *            The name of the attribute to put on each node (pass null to
	 *            disable this feature).
	 */
	public void setCountAttribute(String countAttribute) {
		if (this.countAttribute != null && graph != null) {
			for (Node n : graph) {
				n.removeAttribute(countAttribute);
			}
		}

		this.countAttribute = countAttribute;

		publish();
	}

	protected boolean isCutEdge(Edge e) {
		return cutAttribute != null && e.hasAttribute(cutAttribute);
	}

	protected void checkStarted() {
		if (!started && graph != null) {
			compute();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#edgeAdded(java.lang.String, long,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void edgeAdded(String graphId, long timeId, String edgeId, String fromNodeId, String toNodeId,
			boolean directed) {
		if (!started && graph != null) {
			compute();
		} else if (started) {
			Edge edge = graph.getEdge(edgeId);

			if (edge != null) {
				Node n0 = edge.getNode0();
				Node n1 = edge.getNode1();

				ConnectedComponent cc0 = componentsMap.get(n0);
				ConnectedComponent cc1 = componentsMap.get(n1);

				if (cc0 != cc1) {
					computeConnectedComponent(cc0, n1, null);
					assert cc1.size == 0;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#edgeRemoved(java.lang.String,
	 * long, java.lang.String)
	 */
	@Override
	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		if (!started && graph != null) {
			compute();
		}

		if (started) {
			Edge edge = graph.getEdge(edgeId);

			if (edge != null) {
				Node n0 = edge.getNode0();
				Node n1 = edge.getNode1();

				ConnectedComponent cc0 = componentsMap.get(n0);
				ConnectedComponent cc1 = componentsMap.get(n1);

				if (cc0 == cc1) {
					ConnectedComponent ccN = new ConnectedComponent();
					computeConnectedComponent(ccN, n1, edge);

					components.add(ccN);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#nodeAdded(java.lang.String, long,
	 * java.lang.String)
	 */
	@Override
	public void nodeAdded(String graphId, long timeId, String nodeId) {
		if (!started && graph != null) {
			compute();
		} else if (started) {
			Node node = graph.getNode(nodeId);

			if (node != null) {
				ConnectedComponent ccN = new ConnectedComponent();
				computeConnectedComponent(ccN, node, null);

				components.add(ccN);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#nodeRemoved(java.lang.String,
	 * long, java.lang.String)
	 */
	@Override
	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		if (!started && graph != null) {
			compute();
		}

		if (started) {
			Node node = graph.getNode(nodeId);

			if (node != null) {
				ConnectedComponent cc = componentsMap.remove(node);

				if (cc != null) {
					cc.unregisterNode(node);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.stream.SinkAdapter#edgeAttributeAdded(java.lang.String,
	 * long, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void edgeAttributeAdded(String graphId, long timeId, String edgeId, String attribute, Object value) {
		if (cutAttribute != null && attribute.equals(cutAttribute)) {
			if (!started && graph != null)
				compute();

			Edge edge = graph.getEdge(edgeId);

			// The attribute is added. Do as if the edge was removed.

			if (edge != null) {
				Node n0 = edge.getNode0();
				Node n1 = edge.getNode1();

				ConnectedComponent cc0 = componentsMap.get(n0);
				ConnectedComponent cc1 = componentsMap.get(n1);

				if (cc0 == cc1) {
					ConnectedComponent ccN = new ConnectedComponent();
					computeConnectedComponent(ccN, n1, edge);

					components.add(ccN);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.stream.SinkAdapter#edgeAttributeRemoved(java.lang.String,
	 * long, java.lang.String, java.lang.String)
	 */
	@Override
	public void edgeAttributeRemoved(String graphId, long timeId, String edgeId, String attribute) {
		if (cutAttribute != null && attribute.equals(cutAttribute)) {
			if (!started && graph != null)
				compute();

			Edge edge = graph.getEdge(edgeId);

			// The attribute is removed. Do as if the edge was added.

			if (edge != null) {
				Node n0 = edge.getNode0();
				Node n1 = edge.getNode1();

				ConnectedComponent cc0 = componentsMap.get(n0);
				ConnectedComponent cc1 = componentsMap.get(n1);

				if (cc0 != cc1) {
					computeConnectedComponent(cc0, n1, null);
					assert cc1.size == 0;
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#graphCleared(java.lang.String,
	 * long)
	 */
	@Override
	public void graphCleared(String graphId, long timeId) {
		if (started) {
			components.clear();
			componentsMap.clear();
		}
	}

	public class ConnectedComponent implements Iterable<Node> {
		public final int id = currentComponentId++;
		int size;
		Filter<Node> nodeFilter;
		Filter<Edge> edgeFilter;
		Iterable<Edge> eachEdge;

		ConnectedComponent() {
			this.size = 0;

			nodeFilter = new Filter<Node>() {
				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.graphstream.util.Filter#isAvailable(org.graphstream.graph
				 * .Element)
				 */
				@Override
				public boolean isAvailable(Node e) {
					return componentsMap.get(e) == ConnectedComponent.this;
				}
			};

			edgeFilter = new EdgeFilter(nodeFilter);

			eachEdge = new Iterable<Edge>() {
				public Iterator<Edge> iterator() {
					return getEdgeIterator();
				}
			};
		}

		void registerNode(Node n) {
			ConnectedComponent old = componentsMap.put(n, this);

			if (countAttribute != null) {
				n.setAttribute(countAttribute, id);
			}

			if (old != this) {
				size++;

				if (old != null) {
					old.unregisterNode(n);
				}
			}
		}

		void unregisterNode(Node n) {
			size--;

			if (size == 0) {
				components.remove(this);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Iterable#iterator()
		 */
		@Override
		public Iterator<Node> iterator() {
			return new FilteredNodeIterator<Node>(graph, nodeFilter);
		}

		public Iterable<Node> getEachNode() {
			return this;
		}

		public Set<Node> getNodeSet() {
			HashSet<Node> nodes = new HashSet<Node>();

			for (Node n : this) {
				nodes.add(n);
			}

			return nodes;
		}

		public Iterable<Edge> getEachEdge() {
			return eachEdge;
		}

		public Iterator<Edge> getEdgeIterator() {
			return new FilteredEdgeIterator<Edge>(graph, edgeFilter);
		}

		public boolean contains(Node n) {
			return componentsMap.get(n) == this;
		}

		public String toString() {
			return String.format("ConnectedComponent#%d", id);
		}
	}

	private static class EdgeFilter implements Filter<Edge> {
		Filter<Node> f;

		public EdgeFilter(Filter<Node> f) {
			this.f = f;
		}

		public boolean isAvailable(Edge e) {
			return f.isAvailable(e.getNode0()) && f.isAvailable(e.getNode1());
		}
	}
}
