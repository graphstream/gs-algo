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
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Stream;

import org.graphstream.graph.Graph;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;
import org.graphstream.graph.Structure;
import org.graphstream.stream.SinkAdapter;

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
 * @author Guilhelm Savin
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

	/**
	 * Flag used to tell if the {@link #compute()} method has already been
	 * called.
	 */
	protected boolean started;

	/**
	 * Used to get components index.
	 */
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

		graph.nodes()
			.filter(n -> !componentsMap.containsKey(n))
			.forEach(n -> {
				ConnectedComponent cc = new ConnectedComponent();
				computeConnectedComponent(cc, n, null);

				components.add(cc);
			});
	}

	/**
	 * Compute the connected component containing `from`.
	 * 
	 * We use here the {@link ConnectedComponent#registerNode(Node)} method
	 * which will update the {@link #componentsMap}, the size of the new
	 * connected component, the size of the old connected component of `from`
	 * (if any), and set the node attribute if {@link #countAttribute} is set.
	 * 
	 * @param cc
	 *            The connected component being computed
	 * @param from
	 *            The node we start from
	 * @param drop
	 *            Do not consider this edge is not null. This is mainly used
	 *            when an edge has been deleted but the change is not effective
	 *            in the graph structure.
	 */
	protected void computeConnectedComponent(ConnectedComponent cc, Node from, Edge drop) {
		LinkedList<Node> open = new LinkedList<Node>();

		open.add(from);
		cc.registerNode(from);

		while (!open.isEmpty()) {
			Node n = open.poll();
			
			n.edges()
				.filter(e -> (e != drop && !isCutEdge(e)))
				.forEach(e -> {
					Node n2 = e.getOpposite(n);

					if (componentsMap.get(n2) != cc) {
						open.add(n2);
						cc.registerNode(n2);
					}
				});
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

	/**
	 * Publish the index of the connected component where the node belongs.
	 * 
	 * This is more efficient than using the {@link #setCountAttribute(String)}
	 * method. The latter will update the attribute at each change, implying a
	 * bigger complexity cost, while this method is a one shot call, so you can
	 * use it only when you need to manipulate the connected component indexes.
	 * 
	 * @param nodeAttribute
	 *            id of the attribute where the index of the connected component
	 *            will be stored.
	 */
	public void publish(String nodeAttribute) {
		if (graph == null) {
			return;
		}
		
		graph.nodes().forEach(n -> {
			ConnectedComponent cc = componentsMap.get(n);
			assert cc != null;

			n.setAttribute(countAttribute, cc.id);
		});
	}

	/**
	 * Get the connected component that contains the biggest number of nodes.
	 * 
	 * @return the biggest CC.
	 */
	public ConnectedComponent getGiantComponent() {
		checkStarted();

		ConnectedComponent maxCC = null;
		
		maxCC = components.stream()
				.max((cc1, cc2) -> Integer.compare(cc1.size, cc2.size))
				.get();

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
	
	@Result
    public String defaultResult() {
    	return getConnectedComponentsCount()+" connected component(s) in this graph" ;
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
			
			count = (int) components.stream()
					.filter(cc -> (cc.size >= sizeThreshold && (sizeCeiling <= 0 || cc.size < sizeCeiling)))
					.count() ;
		
			return count;
		}
	}

	/**
	 * Return the connected component where a node belonged. The validity of the
	 * result ends if any new computation is done. So you will have to call this
	 * method again to be sure you are manipulating the good component.
	 * 
	 * @param n
	 *            a node
	 * @return the connected component containing `n`
	 */
	public ConnectedComponent getConnectedComponentOf(Node n) {
		return n == null ? null : componentsMap.get(n);
	}

	/**
	 * Same as {@link #getConnectedComponentOf(Node)} but using the node id.
	 * 
	 * @param nodeId
	 *            a node id
	 * @return the connected component containing the node `nodeId`
	 */
	public ConnectedComponent getConnectedComponentOf(String nodeId) {
		return getConnectedComponentOf(graph.getNode(nodeId));
	}

	/**
	 * Same as {@link #getConnectedComponentOf(Node)} but using the node index.
	 * 
	 * @param nodeIndex
	 *            a node index
	 * @return the connected component containing the node `nodeIndex`
	 */
	public ConnectedComponent getConnectedComponentOf(int nodeIndex) {
		return getConnectedComponentOf(graph.getNode(nodeIndex));
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
	@Parameter
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
	@Parameter
	public void setCountAttribute(String countAttribute) {
		if (this.countAttribute != null && graph != null) {
			graph.nodes().forEach(n -> n.removeAttribute(countAttribute));
		}

		this.countAttribute = countAttribute;

		publish(countAttribute);
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
		checkStarted();

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
		checkStarted();

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

	/**
	 * A representation of a connected component. These objects are used to
	 * store informations about components and to allow to iterate over all
	 * nodes of a same component.
	 * 
	 * You can retrieve these objects using the
	 * {@link ConnectedComponents#getConnectedComponentOf(Node)} methods of the
	 * algorithm.
	 *
	 */
	public class ConnectedComponent implements Structure {
		/**
		 * The unique id of this component.
		 * 
		 * The uniqueness of the id is local to an instance of the
		 * {@link ConnectedComponents} algorithm.
		 */
		public final int id = currentComponentId++;

		int size;

		ConnectedComponent() {
			this.size = 0;

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

		/**
		 * Return an stream over the nodes of this component.
		 * 
		 * @return an stream over the nodes of this component
		 */
		
		public Stream<Node> nodes() {
			return graph.nodes().filter(n -> componentsMap.get(n) == ConnectedComponent.this ) ;
		}

		

		/**
		 * Get a set containing all the nodes of this component.
		 * 
		 * A new set is built for each call to this method, so handle with care.
		 * 
		 * @return a new set of nodes belonging to this component
		 */
		public Set<Node> getNodeSet() {
			HashSet<Node> nodes = new HashSet<Node>();
			
			nodes().forEach(n -> nodes.add(n));
			
			return nodes;
		}

		/**
		 * Return an stream over the edge of this component.
		 * 
		 * An edge is in the component if the two ends of this edges are in the
		 * component and the edge does not have the cut attribute. Note that,
		 * using cut attribute, some edges can be in none of the components.
		 * 
		 * @return an stream over the edges of this component
		 */
		public Stream<Edge> edges() {
			return graph.edges().filter(e -> {
				return (componentsMap.get(e.getNode0()) == ConnectedComponent.this)
						&& (componentsMap.get(e.getNode1()) == ConnectedComponent.this)
						&& !isCutEdge(e) ;
			});
		}

		/**
		 * Test if this component contains a given node.
		 * 
		 * @param n
		 *            a node
		 * @return true if the node is in this component
		 */
		public boolean contains(Node n) {
			return componentsMap.get(n) == this;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return String.format("ConnectedComponent#%d", id);
		}

		@Override
		public int getNodeCount() {
			// TODO Auto-generated method stub
			return (int) nodes().count();
		}

		@Override
		public int getEdgeCount() {
			// TODO Auto-generated method stub
			return (int) edges().count();
		}
	}
}
