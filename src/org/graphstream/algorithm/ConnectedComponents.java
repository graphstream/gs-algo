/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;
import org.util.set.FixedArrayList;

/**
 * Compute and update the number of connected components of a dynamic graph.
 * 
 * <p>
 * This algorithm computes the connected components of one given graph. The
 * connected components are the set of its connected subgraphs. Two nodes belong
 * to the same connected component when there exists a path (without considering
 * the direction of the edges) between them. Therefore, the algorithm does not
 * consider the direction of the edges. The number of connected components of an
 * undirected graph is equal to the number of connected components of the same
 * directed graph. See <a
 * href="http://en.wikipedia.org/wiki/Connected_component_%28graph_theory%29"
 * >wikipedia</a> for details.
 * </p>
 * 
 * <h2>Dynamics</h2>
 * 
 * <p>
 * This algorithm tries to handle the dynamics of the graph, trying not to
 * recompute all from scratch at each change (kind of re-optimization). In this
 * way, each instance of the algorithm is registered as a graph listener. Each
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
 * <h2>Additional features</h2>
 * 
 * <p>
 * This algorithm allows to consider some edges as non existent by marking them
 * with a specific attribute. Use the {@link #setCutAttribute(String)} to
 * specify the name of the attribute that, if present on an edge (whatever be
 * its value, excepted null, naturally), will make it invisible to the
 * algorithm. This allows to create connected components without really
 * modifying the graph. The name of the attribute is given to the
 * {@link #setCutAttribute(String)} method. The value of the attribute can be
 * chosen arbitrarily, but cannot be null.
 * </p>
 * 
 * <p>
 * You can tag each node with an integer that identifies the component it
 * pertains to using {@link #setCountAttribute(String)}. The argument of this
 * method is an arbitrary name that will be used as attribute on each node of
 * the graph. The value of this attribute will be an integer (counting from
 * zero) that is different for each connected component.
 * </p>
 * 
 * 
 * @author Yoann Pigné
 * @author Antoine Dutot
 * 
 * @since June 26 2007
 * 
 * @complexity For the initial computation, let n be the number of nodes, then
 *             the complexity is 0(n). For the re-optimization steps, let k be
 *             the number of nodes concerned by the changes (k <= n), the
 *             complexity is O(k).
 */
public class ConnectedComponents extends SinkAdapter implements
		DynamicAlgorithm {
	// private static final String CONNECTED_COMPONENT = "connectedComponent";

	/**
	 * Map of connected components.
	 */
	private HashMap<Node, Integer> connectedComponentsMap;

	/**
	 * The Graph the algorithm is working on.
	 */
	protected Graph graph;

	/**
	 * The number of connected components
	 */
	protected int connectedComponents = 0;

	/**
	 * Single IDs to identify the connected components.
	 */
	protected FixedArrayList<String> ids = new FixedArrayList<String>();

	/**
	 * A token to decide whether or not the algorithm is started.
	 */
	protected boolean started = false;

	/**
	 * Optional edge attribute that make it "invisible". The algorithm will find
	 * two connected components if such an edge is the only link between two
	 * node groups.
	 */
	protected String cutAttribute = null;

	/**
	 * Optional attribute to set on each node of a given component. This
	 * attribute will have for value an index different for each component.
	 */
	protected String countAttribute = null;

	public ConnectedComponents() {
		this(null);
	}

	/**
	 * Constructor with the given graph. The computation of the algorithm start
	 * only when the {@link #init(Graph)} method is invoked.
	 * 
	 * @param graph
	 *            The graph who's connected components will be computed.
	 */
	public ConnectedComponents(Graph graph) {
		ids.add(""); // The dummy first identifier (since zero is a special
		// value).

		if (graph != null)
			init(graph);

		connectedComponentsMap = new HashMap<Node, Integer>();
	}

	/**
	 * Computes a list of nodes that belong to the biggest connected component.
	 * 
	 * @return nodes of the biggest CC.
	 */
	public List<Node> getGiantComponent() {
		if (!started) {
			compute();
		}
		ArrayList<Integer> amount = new ArrayList<Integer>(ids.size());
		for (int i = 0; i < ids.size(); i++) {
			amount.add(0);
		}
		for (Node n : graph.getNodeSet()) {
			Integer id = connectedComponentsMap.get(n);
			if (id != null) {
				amount.set(id, amount.get(id) + 1);
			}
		}
		int max = -1;
		int maxIndex = -1;
		for (int i = 0; i < amount.size(); i++) {
			Integer val = amount.get(i);
			if (val != null && max < val) {
				max = amount.get(i);
				maxIndex = i;
			}
		}
		if (maxIndex != -1) {
			ArrayList<Node> giant = new ArrayList<Node>();
			for (Node n : graph.getNodeSet()) {
				if (connectedComponentsMap.get(n) == maxIndex) {
					giant.add(n);
				}
			}
			return giant;
		} else
			return null;
	}

	/**
	 * 
	 * Ask the algorithm for the number of connected components.
	 */
	public int getConnectedComponentsCount() {
		if (!started) {
			compute();
		}

		return connectedComponents;
	}

	/**
	 * Allocate a new identifier for a connected component.
	 * 
	 * @return The new component identifier.
	 */
	protected int addIdentifier() {
		ids.add("");

		return ids.getLastIndex();
	}

	/**
	 * Remove a identifier that is no more used.
	 * 
	 * @param identifier
	 *            The identifier to remove.
	 */
	protected void removeIdentifier(int identifier) {
		/*
		 * // Eventual verification to ensure no used identifier is removed.
		 * 
		 * for( Node node: graph.getNodeSet() ) { if(
		 * connectedComponentsMap.get( node ) == identifier ) System.err.printf(
		 * "     **** ID %d STILL USED BY node %s%n", identifier, node.getId()
		 * ); }
		 */
		ids.remove(identifier);
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

		compute();
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
		removeMarks();
		this.countAttribute = countAttribute;
		remapMarks();
	}

	protected void removeMarks() {
		Iterator<? extends Node> nodes = graph.getNodeIterator();

		while (nodes.hasNext()) {
			Node node = nodes.next();

			if (countAttribute == null)
				node.removeAttribute(countAttribute);
		}
	}

	protected void remapMarks() {

		if (countAttribute != null) {
			Iterator<? extends Node> nodes = graph.getNodeIterator();

			while (nodes.hasNext()) {
				Node v = nodes.next();
				int id = connectedComponentsMap.get(v);

				v.addAttribute(countAttribute, id - 1);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		if (this.graph != null)
			this.graph.removeSink(this);

		this.graph = graph;

		this.graph.addSink(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		connectedComponents = 0;
		started = true;

		ids.clear();
		ids.add(""); // The dummy first identifier (since zero is a special
		// value).

		Iterator<? extends Node> nodes = graph.getNodeIterator();

		while (nodes.hasNext()) {
			connectedComponentsMap.put(nodes.next(), 0);
		}

		nodes = graph.getNodeIterator();

		while (nodes.hasNext()) {
			Node v = nodes.next();

			if (connectedComponentsMap.get(v) == 0) {
				connectedComponents++;

				computeConnectedComponent(v, addIdentifier(), null);
			}
		}

		remapMarks();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
	 */
	public void terminate() {
		if (graph != null) {
			graph.removeSink(this);

			graph = null;
			started = false;

			connectedComponents = 0;
		}
	}

	/**
	 * Goes recursively (depth first) into the connected component and assigns
	 * each node an id.
	 * 
	 * @param v
	 *            The considered node.
	 * @param id
	 *            The id to assign to the given node.
	 * @param exception
	 *            An optional edge that may not be considered (useful when
	 *            receiving a {@link #edgeRemoved(String, long, String)} event.
	 */
	private void computeConnectedComponent(Node v, int id, Edge exception) {
		LinkedList<Node> open = new LinkedList<Node>();

		open.add(v);

		while (!open.isEmpty()) {
			Node n = open.remove();

			connectedComponentsMap.put(n, id);

			markNode(n, id);

			Iterator<? extends Edge> edges = n.getEdgeIterator();

			while (edges.hasNext()) {
				Edge e = edges.next();

				if (e != exception) {
					if ((cutAttribute != null) ? (!e.hasAttribute(cutAttribute))
							: true) {
						Node n2 = e.getOpposite(n);

						if (connectedComponentsMap.get(n2) != id) {
							open.add(n2);
							connectedComponentsMap.put(n2, id);
							markNode(n2, id); /* useless */
						}
						// Also work with (but slower):
						/*
						 * if( connectedComponentsMap.get( n2 ) != id && !
						 * open.contains(n2) ) { open.add( n2 ); }
						 */

					}
				}
			}
		}
	}

	protected void markNode(Node node, int id) {
		if (countAttribute != null) {
			node.addAttribute(countAttribute, id - 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#edgeAdded(java.lang.String, long,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		if (!started && graph != null) {
			compute();
		} else if (started) {
			Edge edge = graph.getEdge(edgeId);

			if (edge != null) {
				if (!(connectedComponentsMap.get(edge.getNode0())
						.equals(connectedComponentsMap.get(edge.getNode1())))) {
					connectedComponents--;

					int id0 = connectedComponentsMap.get(edge.getNode0());
					int id1 = connectedComponentsMap.get(edge.getNode1());

					computeConnectedComponent(edge.getNode1(), id0, edge);
					removeIdentifier(id1);
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
				connectedComponents++;

				int id = addIdentifier();

				connectedComponentsMap.put(node, id);
				markNode(node, id);
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
				int id = addIdentifier();
				int oldId = connectedComponentsMap.get(edge.getNode0());

				computeConnectedComponent(edge.getNode0(), id, edge);

				if (!(connectedComponentsMap.get(edge.getNode0())
						.equals(connectedComponentsMap.get(edge.getNode1())))) {
					connectedComponents++;
				} else {
					removeIdentifier(oldId);
				}
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
				connectedComponents--;
				removeIdentifier(connectedComponentsMap.get(node));
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
		terminate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.stream.SinkAdapter#edgeAttributeAdded(java.lang.String,
	 * long, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void edgeAttributeAdded(String graphId, long timeId, String edgeId,
			String attribute, Object value) {
		if (cutAttribute != null && attribute.equals(cutAttribute)) {
			if (!started && graph != null)
				compute();

			Edge edge = graph.getEdge(edgeId);

			// The attribute is added. Do as if the edge was added.

			int id = addIdentifier();
			int oldId = connectedComponentsMap.get(edge.getNode0());

			computeConnectedComponent(edge.getNode0(), id, edge);

			if (!connectedComponentsMap.get(edge.getNode0()).equals(
					connectedComponentsMap.get(edge.getNode1()))) {
				connectedComponents++;
			} else {
				removeIdentifier(oldId);
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
	public void edgeAttributeRemoved(String graphId, long timeId,
			String edgeId, String attribute) {
		if (cutAttribute != null && attribute.equals(cutAttribute)) {
			if (!started && graph != null)
				compute();

			Edge edge = graph.getEdge(edgeId);

			// The attribute is removed. Do as if the edge was removed.

			if (!(connectedComponentsMap.get(edge.getNode0())
					.equals(connectedComponentsMap.get(edge.getNode1())))) {
				connectedComponents--;

				int id0 = connectedComponentsMap.get(edge.getNode0());
				int id1 = connectedComponentsMap.get(edge.getNode1());

				computeConnectedComponent(edge.getNode1(), id0, edge);
				removeIdentifier(id1);
			}
		}
	}
}