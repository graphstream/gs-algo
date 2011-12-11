package org.graphstream.algorithm.networksimplex;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.graphstream.graph.Edge;
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
		for (NSNode node : nodes.values())
			node.supply = node.id.equals(sourceId) ? nodes.size() - 1 : -1;
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

	protected class NodeIterator<T extends Node> implements Iterator<T> {
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

		public T next() {
			if (nextNode == root)
				throw new NoSuchElementException();
			T node = graph.getNode(nextNode.id);
			nextNode = nextNode.parent;
			return node;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"This iterator does not support remove");
		}
	}

	protected class EdgeIterator<T extends Edge> implements Iterator<T> {
		protected NSNode nextNode;

		protected EdgeIterator(NSNode target) {
			nextNode = target;
		}

		public boolean hasNext() {
			return nextNode.parent != root;
		}

		@Override
		public T next() {
			if (nextNode.parent == root)
				throw new NoSuchElementException();
			T edge = graph.getEdge(nextNode.arcToParent.getOriginalId());
			nextNode = nextNode.parent;
			return edge;
		}

		@Override
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
	public <T extends Node> Iterator<T> getPathNodesIterator(Node target) {
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
		return new EdgeIterator<T>(nodes.get(target.getId()));
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
