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
 * @since 2011-12-11
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.networksimplex;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

public class DynamicOneToAllShortestPath extends NetworkSimplex {
	protected String sourceId;

	public DynamicOneToAllShortestPath(String costName) {
		super(null, null, costName);
	}

	public String getSource() {
		return sourceId;
	}

	public void setSource(String sourceId) {
		this.sourceId = sourceId;
		if (nodes != null)
			for (NSNode node : nodes.values())
				changeSupply(node, node.id.equals(sourceId) ? nodes.size() - 1
						: -1);
	}

	@Override
	protected void cloneGraph() {
		super.cloneGraph();
		
		nodes.values().stream().forEach(node -> node.supply = node.id.equals(sourceId) ? nodes.size() - 1 : -1);			
	}
	
	/**
	 * NS is much slower than Dijkstra when starting from a big graph.
	 * The idea is to call Dijkstra and then to construct the initial BFS from it.
	 * This method should be called just after {@link #createInitialBFS()}.
	 */
	protected void bfsFromDijkstra() {
		// first check if we can apply Dijkstra
		if (nodes.get(sourceId) == null)
			return;
		for (NSArc arc : arcs.values())
			if (arc.cost.isNegative())
				return;
		
		// instantiate and compute Dijkstra
		Dijkstra dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, costName);
		dijkstra.init(graph);
		dijkstra.setSource(graph.getNode(sourceId));
		dijkstra.compute();
		
		// init
		Map<NSNode, NSNode> last = new HashMap<NSNode, NSNode>(4 * (nodes.size() + 1) / 3 + 1);
		last.put(root, root);
		root.thread = root;
		
		nodes.values().forEach(node -> {
			last.put(node, node);
			node.artificialArc.status = ArcStatus.NONBASIC_LOWER;
			node.artificialArc.flow = 0;
			node.thread = node;
		});
		
		// restore parent and thread
		
		nodes.values().forEach(node -> {
			Node gNode = graph.getNode(node.id);
			Node gParent = dijkstra.getParent(gNode);
			NSNode parent = gParent == null ? root : nodes.get(gParent.getId());
			node.parent = parent;
			NSArc arc = node.artificialArc;
			if (gParent != null) {
				Edge gEdge = dijkstra.getEdgeFromParent(gNode);
				if (gEdge.getSourceNode() == gParent)
					arc = arcs.get(gEdge.getId());
				else
					arc = arcs.get(PREFIX + "REVERSE_" + gEdge.getId());
			}
			node.arcToParent = arc;
			arc.status = ArcStatus.BASIC;
			nonBasicArcs.remove(arc);
			NSNode nodeLast = last.get(node);
			nodeLast.thread = parent.thread;
			parent.thread = node;
			for (NSNode x = parent; last.get(x) == parent; x = x.parent)
				last.put(x, nodeLast);
		});

		last.clear();
		dijkstra.clear();
		
		// compute depths, potentials, flows and objective value
		for (NSNode node = root.thread; node != root; node = node.thread) {
			node.depth = node.parent.depth + 1;
			node.computePotential();
			for (NSNode x = node; x != root; x = x.parent)
				x.arcToParent.flow++;
		}
		NSArc arc = nodes.get(sourceId).arcToParent;
		arc.flow = nodes.size() - arc.flow;
		
		objectiveValue.set(0);
		for (NSNode node = root.thread; node != root; node = node.thread)
			objectiveValue.plusTimes(node.arcToParent.flow, node.arcToParent.cost);
	}
	
	
	
	@Override
	public void init(Graph graph) {
		// Do not call super.init(graph), make BFS from Dijkstra before start listening
		this.graph = graph;
		cloneGraph();
		createInitialBFS();
		bfsFromDijkstra();
		graph.addSink(this);		
	}

	@Override
	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		NSNode node = new NSNode(graph.getNode(nodeId));
		if (nodeId.equals(this.sourceId)) {
			node.supply = nodes.size();
			addNode(node);
		} else {
			node.supply = -1;
			addNode(node);
			NSNode source = nodes.get(this.sourceId);
			if (source != null)
				changeSupply(source, nodes.size() - 1);
		}
	}

	@Override
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		removeNode(nodes.get(nodeId));
		if (!nodeId.equals(this.sourceId)) {
			NSNode source = nodes.get(this.sourceId);
			if (source != null)
				changeSupply(source, nodes.size() - 1);
		}
	}

	// Iterators

	protected class NodeIterator<T extends Node> implements Iterator<Node> {
		protected NSNode nextNode;

		protected NodeIterator(NSNode target) {
			if (target.id.equals(sourceId) || target.parent != root)
				nextNode = target;
			else
				nextNode = root;
		}

		public boolean hasNext() {
			return nextNode != root;
		}

		public Node next() {
			if (nextNode == root)
				throw new NoSuchElementException();
			Node node = graph.getNode(nextNode.id);
			nextNode = nextNode.parent;
			return node;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"This iterator does not support remove");
		}
	}

	protected class EdgeIterator<T extends Edge> implements Iterator<Edge> {
		protected NSNode nextNode;

		protected EdgeIterator(NSNode target) {
			nextNode = target;
		}

		public boolean hasNext() {
			return nextNode.parent != root;
		}

		public Edge next() {
			if (nextNode.parent == root)
				throw new NoSuchElementException();
			Edge edge = graph.getEdge(nextNode.arcToParent.getOriginalId());
			nextNode = nextNode.parent;
			return edge;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"This iterator does not support remove");
		}
	}

	public long getPathLength(Node node) {
		NSNode nsNode = nodes.get(node.getId());
		if (nsNode.id.equals(sourceId))
			return 0;
		if (nsNode.parent == root)
			return Long.MAX_VALUE;
		return -nsNode.potential.small;
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
	public <T extends Node> Iterator<Node> getPathNodesIterator(Node target) {
		return new NodeIterator<T>(nodes.get(target.getId()));
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
	public <T extends Node> Iterable<Node> getPathNodes(final Node target) {
		return new Iterable<Node>() {
			public Iterator<Node> iterator() {
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
	public <T extends Edge> Iterator<Edge> getPathEdgesIterator(Node target) {
		return new EdgeIterator<Edge>(nodes.get(target.getId()));
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
	public <T extends Edge> Iterable<Edge> getPathEdges(final Node target) {
		return new Iterable<Edge>() {
			public Iterator<Edge> iterator() {
				return getPathEdgesIterator(target);
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
		if (getPathLength(target) == Long.MAX_VALUE)
			return path;
		Stack<Edge> stack = new Stack<Edge>();
		for (Edge e : getPathEdges(target))
			stack.push(e);
		path.setRoot(graph.getNode(sourceId));
		while (!stack.isEmpty())
			path.add(stack.pop());
		return path;
	}
}
