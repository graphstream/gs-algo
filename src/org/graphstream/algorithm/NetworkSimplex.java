package org.graphstream.algorithm;

import java.util.Map;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

/**
 * Network simplex method is an algorithm that solves the minimum cost flow
 * (MCF) problem for an oriented graph.
 * <p>
 * The MCF problem can be stated as follows. Each node has associated number
 * <i>supply</i> representing the available supply of or demand for flow at that
 * node. If <i>supply</i> is positive, the node is a supply node, if
 * <i>supply</i> is negative, the node is a demand node and if <i>supply</i> is
 * zero, the node is a transshipment node. Each arc has associated
 * <i>capacity</i> (possibly infinite) and <i>cost</i> per unit flow. The MCF
 * problem is to send the required flows from supply nodes to demand nodes at
 * minimum cost, respecting the capacities of the arcs. Note that if the sum of
 * <i>supply</i> attributes of all nodes is nonzero, the problem is infeasible.
 * <p>
 * MCF framework can be used to model a broad variety of network problems,
 * including matching, shortest path, transportation, etc. For example, if we
 * want to find the shortest paths from a source to all other nodes in a graph
 * with <i>n</i> nodes, we can set the <i>supply</i> to <i>n</i>-1 for the
 * source and to -1 for all other nodes, set <i>capacity</i> to <i>n</i>-1 and
 * <i>cost</i> to the weight for each arc. The solution of the MCF problem with
 * these particular settings will be minimum cost unit flow from the source to
 * all other nodes passing by the shortest paths.
 * <p>
 * 
 * 
 * @author Stefan Balev
 */
public class NetworkSimplex extends SinkAdapter implements DynamicAlgorithm {

	protected String supplyAttribute;
	protected String capacityAttribute;
	protected String costAttribute;
	protected String prefix;
	
	protected Map<String, NSNode> nodes;
	protected Map<String, NSArc> arcs;

	public void init(Graph graph) {
		// TODO Auto-generated method stub

	}

	public void compute() {
		// TODO Auto-generated method stub

	}

	public void terminate() {
		// TODO Auto-generated method stub

	}

	/**
	 * Internal representation of the graph nodes
	 */
	protected class NSNode {
		/**
		 * Node id
		 */
		String id;

		/**
		 * Node supply (or demand if negative). Problem data retrieved from the
		 * original graph
		 */
		double supply;

		/**
		 * Node potential
		 */
		double potential;

		/**
		 * Parent in the BFS tree
		 */
		NSNode parent;

		/**
		 * The next node of the preorder traversal of the BFS tree
		 */
		NSNode thread;

		/**
		 * The depth of the node in the BFS tree
		 */
		int depth;

		/**
		 * The arc connecting the node to its parent in the BFS tree
		 */
		NSArc arcToParent;

		/**
		 * Creates a copy of a node
		 * 
		 * @param node
		 *            a node of the original graph
		 */
		NSNode(Node node) {
			id = node.getId();
			supply = node.getNumber(supplyAttribute);
			if (Double.isNaN(supply))
				supply = 0;
		}

		NSNode() {
		}

		NSNode previousInThread() {
			NSNode node;
			for (node = parent; node.thread != this; node = node.thread)
				;
			return node;
		}

		NSNode lastSuccessor() {
			NSNode node;
			for (node = this; node.thread.depth > depth; node = node.thread)
				;
			return node;
		}

		void changeParent(NSNode newParent, NSArc newArcToParent) {
			NSNode pred = previousInThread();
			NSNode succ = lastSuccessor();

			pred.thread = succ.thread;
			succ.thread = newParent.thread;
			newParent.thread = this;

			parent = newParent;
			arcToParent = newArcToParent;
			for (NSNode node = this; node != succ.thread; node = node.thread) {
				node.depth = node.parent.depth + 1;
				node.potential = node.parent.potential;
				if (node.arcToParent.source == node)
					node.potential += node.arcToParent.cost;
				else
					node.potential -= node.arcToParent.cost;				
			}
		}

	}

	/**
	 * Internal representation of the graph arcs
	 */
	protected class NSArc {
		/**
		 * Arc id
		 */
		String id;

		/**
		 * Arc capacity. Problem data retrieved from the original graph. Must be
		 * non-negative
		 */
		double capacity;

		/**
		 * Arc cost. Problem data retrieved from the original graph.
		 */
		double cost;

		/**
		 * Source node of the arc
		 */
		NSNode source;

		/**
		 * Target node of the arc
		 */
		NSNode target;

		/**
		 * Flow on the arc in the current BFS
		 */
		double flow;

		/**
		 * Status of the arc in the current BFS
		 */
		ArcStatus status;
		
		NSArc(Edge edge, boolean sameDirection) {
			id = edge.getId();
			if (!sameDirection)
				id = prefix + "REVERSE_" + id;
			capacity = edge.getNumber(capacityAttribute);
			if (Double.isNaN(capacity))
				capacity = Double.POSITIVE_INFINITY;
			cost = edge.getNumber(costAttribute);
			if (Double.isNaN(cost))
				cost = 1;

			String sourceId = edge.getSourceNode().getId();
			String targetId = edge.getTargetNode().getId();
			source = nodes.get(sameDirection ? sourceId : targetId);
			target = nodes.get(sameDirection ? targetId : sourceId);
		}
		
		NSArc() {
		}
		
		double reducedCost() {
			double r = cost - source.potential + target.potential;
			return status == ArcStatus.NONBASIC_UPPER ? -r : r;
		}
		
		double allowedFlowChange(NSNode first) {
			return first == source ? capacity - flow : flow;
		}
		
		void changeFlow(double delta, NSNode first) {
			if (first == source)
				flow += delta;
			else
				flow -= delta;
		}

	}

	/**
	 * Arc status
	 */
	protected enum ArcStatus {
		BASIC, NONBASIC_LOWER, NONBASIC_UPPER
	}
	
	
}
