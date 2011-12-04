package org.graphstream.algorithm.networksimplex;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
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

	/**
	 * The algorithm maintains some internal data whose names start with this
	 * prefix. The graph <i>must not</i> have any edges whose IDs start with
	 * this prefix.
	 */
	public static final String PREFIX = "__NS_";

	/**
	 * Used as capacity value for uncapacitated arcs
	 */
	protected static final int INFINITE_CAPACITY = -1;
	
	/**
	 * Pricing strategy used at each iteration of the algorithm. Only two simple
	 * strategies are implemented for the moment, more are to come.
	 */
	public static enum PricingStrategy {
		/**
		 * When using this strategy an iteration is faster, but the number of
		 * iterations is generally bigger
		 */
		FIRST_NEGATIVE,

		/**
		 * When using this strategy an iteration is slower, but the number of
		 * iterations is generally smaller
		 */
		MOST_NEGATIVE
	}
	
	/**
	 * The status of the current solution
	 */
	public static enum SolutionStatus {
		/**
		 * Status before initialization
		 */
		UNDEFINED,

		/**
		 * The current solution is optimal
		 */
		OPTIMAL,

		/**
		 * The problem is infeasible, some of the supply/demand constraints
		 * cannot be satisfied
		 */
		INFEASIBLE,

		/**
		 * The problem is unbounded. This happens when the graph contains an
		 * uncapacitated negative cost cycle
		 */
		UNBOUNDED
	}

	/**
	 * Name of the attribute used to store the supply of each node
	 */
	protected String supplyName;

	/**
	 * Name of the attribute used to store the capacity of each arc
	 */
	protected String capacityName;

	/**
	 * Name of the attribute used to store the cost per unit flow of each arc
	 */
	protected String costName;
	
	/**
	 * Current pricing strategy
	 */
	protected PricingStrategy pricingStrategy = PricingStrategy.MOST_NEGATIVE;

	/**
	 * A reference to the original graph
	 */
	protected Graph graph;

	/**
	 * Stores the nodes
	 */
	protected Map<String, NSNode> nodes;

	/**
	 * Stores the arcs
	 */
	protected Map<String, NSArc> arcs;

	/**
	 * Stores the non basic arcs.
	 */
	protected Set<NSArc> nonBasicArcs;

	/**
	 * Artificial root;
	 */
	protected NSNode root;

	/**
	 * The objective value of the current solution
	 */
	protected BigMNumber objectiveValue;
	
	/**
	 * The status of the current BFS 
	 */
	protected SolutionStatus solutionStatus;

	/**
	 * Entering arc for the next pivot. Set to {@code null} if no candidates.
	 * Set in {@link #selectEnteringArc()}
	 */
	protected NSArc enteringArc;

	/**
	 * The nearest common predecessor of the extremities of {@link #enteringArc}
	 * . Set in {@link #findJoinNode()}
	 */
	protected NSNode join;

	/**
	 * The first node of {@link #enteringArc} when traversing it in the
	 * direction of the cycle. Set in {@link #selectLeavingArc()}.
	 */
	protected NSNode first;

	/**
	 * The second node of {@link #enteringArc} when traversing it in the
	 * direction of the cycle. Set in {@link #selectLeavingArc()}.
	 */
	protected NSNode second;

	/**
	 * The root of the subtree detached from the BFS tree when removing
	 * {@link #leavingArc}. Set in {@link #selectLeavingArc()}.
	 */
	protected NSNode oldSubtreeRoot;

	/**
	 * The node of the detached subtree which must be re-attached to the BFS
	 * tree using {@link #enteringArc}. Set in {@link #selectLeavingArc()}.
	 */
	protected NSNode newSubtreeRoot;

	/**
	 * Maximum allowed flow change on the cycle formed by adding
	 * {@link #enteringArc} to the BFS tree. If infinite, the problem is
	 * unbounded. Set in {@link #selectLeavingArc()}.
	 */
	protected BigMNumber cycleFlowChange;

	/**
	 * Leaving arc for the next pivot. Set in {@link #selectLeavingArc()}.
	 */
	protected NSArc leavingArc;

	/**
	 * Working variable, used to avoid frequent instantiations of local
	 * variables.
	 */
	protected BigMNumber work1;

	/**
	 * Working variable, used to avoid frequent instantiations of local
	 * variables.
	 */
	protected BigMNumber work2;

	/**
	 * Creates a network simplex instance specifying attribute name to be used.
	 * Use {@link #init(Graph)} to assign a graph to this instance.
	 * 
	 * @param supplyName
	 *            Name of the attribute used to store the supply of each node.
	 * @param capaciyName
	 *            Name of the attribute used to store the capacity of each edge.
	 * @param costName
	 *            Name of the attribute used to store the cost of each edge.
	 */
	public NetworkSimplex(String supplyName, String capaciyName, String costName) {
		this.supplyName = supplyName;
		this.capacityName = capaciyName;
		this.costName = costName;
		
		objectiveValue = new BigMNumber();
		cycleFlowChange = new BigMNumber();
		work1 = new BigMNumber();
		work2 = new BigMNumber();
		solutionStatus = SolutionStatus.UNDEFINED;
	}
	
	// simplex initialization

	/**
	 * Creates copies of all graph arcs and edges. Instantiates and fills
	 * {@link #nodes} and {@link #arcs}.
	 */
	protected void cloneGraph() {
		nodes = new HashMap<String, NSNode>(4 * graph.getNodeCount() / 3 + 2);
		for (Node node : graph) {
			NSNode copy = new NSNode(node);
			nodes.put(copy.id, copy);
		}

		int arcCount = graph.getEdgeCount() + graph.getNodeCount();
		for (Edge edge : graph.getEachEdge())
			if (!edge.isDirected())
				arcCount++;
		arcs = new HashMap<String, NSArc>(4 * arcCount / 3 + 1);
		for (Edge edge : graph.getEachEdge()) {
			NSArc copy = new NSArc(edge, true);
			arcs.put(copy.id, copy);
			if (!edge.isDirected()) {
				copy = new NSArc(edge, false);
				arcs.put(copy.id, copy);
			}
		}
	}

	/**
	 * Creates artificial root and arcs and sets up the initial BFS
	 */
	protected void createInitialBFS() {
		nonBasicArcs = new HashSet<NSArc>(4 * arcs.size() / 3 + 1);
		for (NSArc arc : arcs.values()) {
			arc.flow = 0;
			arc.status = ArcStatus.NONBASIC_LOWER;
			nonBasicArcs.add(arc);
		}

		root = new NSNode();
		root.id = PREFIX + "ROOT";
		root.potential.set(0);
		root.parent = null;
		root.depth = 0;

		NSNode previous = root;
		long totalSupply = 0;
		objectiveValue.set(0);
		for (NSNode node : nodes.values()) {
			NSArc arc = new NSArc();
			arc.id = PREFIX + "ARTIFICIAL_" + node.id;
			arc.capacity = INFINITE_CAPACITY;
			arc.cost.set(0, 1);
			arc.status = ArcStatus.BASIC;
			if (node.supply >= 0) {
				arc.source = node;
				arc.target = root;
				arc.flow = node.supply;
			} else {
				arc.source = root;
				arc.target = node;
				arc.flow = -node.supply;
			}
			// XXX Do I need this ?
			arcs.put(arc.id, arc);

			node.parent = root;
			node.arcToParent = arc;
			node.computePotential();
			previous.thread = node;
			node.depth = 1;
			previous = node;

			totalSupply += node.supply;
			objectiveValue.plusTimes(arc.flow, arc.cost);
		}
		previous.thread = root;
		root.supply = (int) -totalSupply;
	}
	
	// Simplex machinery
	
	/**
	 * "First negative" pricing strategy
	 */
	protected void selectEnteringArcFirstNegative() {
		enteringArc = null;
		BigMNumber reducedCost = work1;
		for (NSArc arc : nonBasicArcs) {
			arc.computeReducedCost(reducedCost);
			if (reducedCost.isNegative()) {
				enteringArc = arc;
				return;
			}
		}
	}
	
	/**
	 * "Most negative" pricing strategy
	 */
	protected void selectEnteringArcMostNegative() {
		enteringArc = null;
		BigMNumber reducedCost = work1;
		BigMNumber bestReducedCost = work2;
		bestReducedCost.set(0);
		for (NSArc arc : nonBasicArcs) {
			arc.computeReducedCost(reducedCost);
			if (reducedCost.compareTo(bestReducedCost) < 0) {
				bestReducedCost.set(reducedCost);
				enteringArc = arc;
			}
		}
	}
	
	/**
	 * Selects entering arc among the candidates (non-basic arcs with negative
	 * reduced costs). Puts the selected candidate in {@link #enteringArc}. If
	 * there are no candidates, {@link #enteringArc} is set to {@code null}.
	 */
	protected void selectEnteringArc() {
		switch (pricingStrategy) {
		case FIRST_NEGATIVE:
			selectEnteringArcFirstNegative();
			break;
		case MOST_NEGATIVE:
			selectEnteringArcMostNegative();
			break;
		}
	}
	
	/**
	 * Finds the nearest common predecessor of the two nodes of
	 * {@link #enteringArc}. Puts it in {@link #join}.
	 */
	protected void findJoinNode() {
		NSNode i = enteringArc.source;
		NSNode j = enteringArc.target;
		while (i.depth > j.depth)
			i = i.parent;
		while (j.depth > i.depth)
			j = j.parent;
		while (i != j) {
			i = i.parent;
			j = j.parent;
		}
		join = i;
	}

	/**
	 * Selects the leaving arc, that is the arc belonging to the cycle with
	 * minimum allowed flow change. Maintains strongly feasible basis: if there
	 * are more than one candidates, selects the last visited when traversing
	 * the cycle in its direction starting from {@link join}. Sets up
	 * {@link #first}, {@link second}, {@link #cycleFlowChange},
	 * {@link #oldSubtreeRoot}, {@link #newSubtreeRoot} and {@link #leavingArc}.
	 */
	protected void selectLeavingArc() {
		findJoinNode();
		if (enteringArc.status == ArcStatus.NONBASIC_LOWER) {
			first = enteringArc.source;
			second = enteringArc.target;
		} else {
			first = enteringArc.target;
			second = enteringArc.source;
		}
		
		enteringArc.computeAllowedFlowChange(first, cycleFlowChange);
		leavingArc = enteringArc;

		NSArc arc;
		BigMNumber arcFlowChange = work1;

		for (NSNode node = second; node != join; node = node.parent) {
			arc = node.arcToParent;
			arc.computeAllowedFlowChange(node, arcFlowChange);
			if (arcFlowChange.compareTo(cycleFlowChange) <= 0) {
				cycleFlowChange.set(arcFlowChange);
				oldSubtreeRoot = node;
				newSubtreeRoot = second;
				leavingArc = arc;
			}
		}

		for (NSNode node = first; node != join; node = node.parent) {
			arc = node.arcToParent;
			arc.computeAllowedFlowChange(node.parent, arcFlowChange);
			if (arcFlowChange.compareTo(cycleFlowChange) < 0) {
				cycleFlowChange.set(arcFlowChange);
				oldSubtreeRoot = node;
				newSubtreeRoot = first;
				leavingArc = arc;
			}
		}
	}
	
	/**
	 * Changes the flows on the arcs belonging to the cycle and updates the
	 * objective value.
	 */
	protected void changeFlows() {
		int delta = (int) cycleFlowChange.getSmall();
		if (delta == 0)
			return;

		enteringArc.computeReducedCost(work1);
		objectiveValue.plusTimes(delta, work1);

		enteringArc.changeFlow(delta, first);
		for (NSNode node = second; node != join; node = node.parent)
			node.arcToParent.changeFlow(delta, node);
		for (NSNode node = first; node != join; node = node.parent)
			node.arcToParent.changeFlow(delta, node.parent);
	}
	
	/**
	 * Turns upside-down the part of the tree between the entering arc and the leaving arc.
	 */
	protected void updateBFS() {
		NSNode stopNode = oldSubtreeRoot.parent;

		NSNode currentNode = newSubtreeRoot;
		NSNode oldParent = currentNode.parent;
		NSNode newParent = enteringArc.opposite(currentNode);
		NSArc oldArc = currentNode.arcToParent;
		NSArc newArc = enteringArc;
		while (currentNode != stopNode) {
			currentNode.changeParent(newParent, newArc);
			newParent = currentNode;
			currentNode = oldParent;
			oldParent = currentNode.parent;
			newArc = oldArc;
			oldArc = currentNode.arcToParent;
		}
	}
	
	/**
	 * 
	 */
	protected void pivot() {
		changeFlows();
		if (enteringArc == leavingArc) {
			if (enteringArc.status == ArcStatus.NONBASIC_LOWER)
				enteringArc.status = ArcStatus.NONBASIC_UPPER;
			else
				enteringArc.status = ArcStatus.NONBASIC_LOWER;
		} else {
			enteringArc.status = ArcStatus.BASIC;
			nonBasicArcs.remove(enteringArc);
			if ((newSubtreeRoot == first && oldSubtreeRoot == leavingArc.target)
					|| (newSubtreeRoot == second && oldSubtreeRoot == leavingArc.source))
				leavingArc.status = ArcStatus.NONBASIC_LOWER; // XXX
			else
				leavingArc.status = ArcStatus.NONBASIC_UPPER; // XXX

			nonBasicArcs.add(leavingArc);
			updateBFS();
		}
	}
	
	protected void simplex() {
		while(true) {
			selectEnteringArc();
			if (enteringArc == null) {
				if (objectiveValue.isInfinite())
					solutionStatus = SolutionStatus.INFEASIBLE;
				else
					solutionStatus = SolutionStatus.OPTIMAL;
				return;
			}
			selectLeavingArc();
			if (cycleFlowChange.isInfinite()) {
				solutionStatus = SolutionStatus.UNBOUNDED;
				return;
			}
			pivot();
		}
	}




	// DynamicAlgorithm methods

	public void init(Graph graph) {
		this.graph = graph;
		cloneGraph();
		createInitialBFS();
		simplex();
	}

	public void compute() {
		// TODO Auto-generated method stub

	}

	public void terminate() {
		// TODO Auto-generated method stub

	}

	/**
	 * Internal representation of the graph nodes. Stores node ids, supplies and
	 * potentials. Maintains BFS tree using PTD (parent, thread, depth) data
	 * structure.
	 */
	protected class NSNode {
		/**
		 * Node id. The same as in the original graph. Special id for the
		 * artificial root.
		 */
		String id;

		/**
		 * Node supply (or demand if negative). Problem data retrieved from the
		 * original graph.
		 */
		int supply;

		/**
		 * Node potential
		 */
		BigMNumber potential;

		/**
		 * Parent in the BFS tree
		 */
		NSNode parent;

		/**
		 * The next node in the preorder traversal of the BFS tree
		 */
		NSNode thread;

		/**
		 * The depth in the BFS tree
		 */
		int depth;

		/**
		 * The arc connecting this node to its parent in the BFS tree
		 */
		NSArc arcToParent;

		/**
		 * Creates a copy of a node.
		 * 
		 * @param node
		 *            a node of the original graph
		 */
		NSNode(Node node) {
			id = node.getId();
			double v = node.getNumber(supplyName);
			if (Double.isNaN(v))
				v = 0;
			supply = (int) v;
			potential = new BigMNumber();
		}

		/**
		 * Default constructor.
		 */
		NSNode() {
			potential = new BigMNumber();
		}

		/**
		 * Finds the previous node in the preorder traversal of the BFS tree
		 * 
		 * @return the previous node in the thread
		 */
		NSNode previousInThread() {
			NSNode node;
			for (node = parent; node.thread != this; node = node.thread)
				;
			return node;
		}

		/**
		 * Finds the rightmost node of the subtree of this node when following
		 * the thread.
		 * 
		 * @return The last successor of this node
		 */
		NSNode lastSuccessor() {
			NSNode node;
			for (node = this; node.thread.depth > depth; node = node.thread)
				;
			return node;
		}

		/**
		 * Computes the potential of this node knowing the potential of its
		 * father
		 */
		void computePotential() {
			potential.set(parent.potential);
			if (arcToParent.source == this)
				potential.plus(arcToParent.cost);
			else
				potential.minus(arcToParent.cost);
		}

		/**
		 * Changes the parent of this node. Updates PTD structure and node
		 * potentials.
		 * 
		 * @param newParent
		 *            the new parent
		 * @param newArcToParent
		 *            the arc to the new parent
		 */
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
				node.computePotential();
			}
		}
	}

	/**
	 * Arc status
	 */
	protected static enum ArcStatus {
		/**
		 * Basic arc
		 */
		BASIC,
		/**
		 * Non basic arc with zero flow
		 */
		NONBASIC_LOWER,
		/**
		 * Non basic saturated arc
		 */
		NONBASIC_UPPER;
	}

	protected class NSArc {
		/**
		 * Arc id. The same as in the original graph. Special ids for the
		 * artificial arcs and the arcs doubling undirected edges.
		 */
		String id;

		/**
		 * Arc capacity. Problem data retrieved from the original graph.
		 * Considered infinite if equal to -1.
		 */
		int capacity;

		/**
		 * Arc cost. Problem data retrieved from the original graph. Big M for
		 * the artificial arcs.
		 */
		BigMNumber cost;

		/**
		 * Source node
		 */
		NSNode source;

		/**
		 * Target node
		 */
		NSNode target;

		/**
		 * Flow on this arc in the current BFS
		 */
		int flow;

		/**
		 * Status of this arc in the current BFS
		 */
		ArcStatus status;

		/**
		 * Creates a copy of an edge of the original graph
		 * 
		 * @param edge
		 *            an edge of the original graph
		 * @param sameDirection
		 *            true if the arc must have the same direction as the
		 *            original edge
		 */
		NSArc(Edge edge, boolean sameDirection) {
			if (edge.getId().startsWith(PREFIX))
				throw new IllegalArgumentException(
						"Edge ids must not start with " + PREFIX);
			id = (sameDirection ? "" : PREFIX + "REVERSE_") + edge.getId();

			double v = edge.getNumber(capacityName);
			if (Double.isNaN(v) || v < 0)
				v = INFINITE_CAPACITY;
			capacity = (int) v;

			v = edge.getNumber(costName);
			if (Double.isNaN(v))
				v = 0;
			cost = new BigMNumber((int) v);

			String sourceId = edge.getSourceNode().getId();
			String targetId = edge.getTargetNode().getId();
			source = nodes.get(sameDirection ? sourceId : targetId);
			target = nodes.get(sameDirection ? targetId : sourceId);
		}

		/**
		 * Default constructor.
		 */
		NSArc() {
			cost = new BigMNumber();
		}

		/**
		 * Computes the reduced cost of this arc
		 * 
		 * @param reducedCost
		 *            The result is stored here
		 */
		void computeReducedCost(BigMNumber reducedCost) {
			reducedCost.set(cost);
			reducedCost.minus(source.potential);
			reducedCost.plus(target.potential);
			if (status == ArcStatus.NONBASIC_UPPER)
				reducedCost.minus();
		}

		/**
		 * Computes the maximum allowed flow change ot this arc.
		 * 
		 * @param first
		 *            One of the endpoints of the arc. Determines the direction
		 *            in the cycle.
		 * @param flowChange
		 *            The result is stored here
		 */
		void computeAllowedFlowChange(NSNode first, BigMNumber flowChange) {
			if (first == source) {
				// the arc is in the direction of the cycle
				if (capacity == INFINITE_CAPACITY)
					flowChange.set(0, 1);
				else
					flowChange.set(capacity - flow);
			} else {
				flowChange.set(flow);
			}
		}

		/**
		 * Changes the flow on this arc.
		 * 
		 * @param delta
		 *            Flow change
		 * @param first
		 *            One of the endpoints of the arc. Determines the direction
		 *            of the cycle
		 */
		void changeFlow(int delta, NSNode first) {
			if (first == source)
				flow += delta;
			else
				flow -= delta;
		}
		
		/**
		 * Returns the node on the other side.
		 * @param node One of the endpoints of this arc
		 * @return The opposite node
		 */
		NSNode opposite(NSNode node) {
			if (node == source)
				return target;
			if (node == target)
				return source;
			return null;
		}
	}
	
	// test and debug
	public static void main(String[] args) {
		 Graph g = new SingleGraph("test");
		
		 g.addNode("A").addAttribute("supply", 5);
		 g.addNode("B").addAttribute("supply", 2);
		 g.addNode("C").addAttribute("supply", 0);
		 g.addNode("D").addAttribute("supply", -1);
		 g.addNode("E").addAttribute("supply", -4);
		 g.addNode("F").addAttribute("supply", -2);
		
		 for (Node n : g)
			 n.addAttribute("label", n.getId());
		
		 Edge e;
		 e = g.addEdge("AB", "A", "B", true);
		 e.addAttribute("capacity", 3);
		 e.addAttribute("cost", 1);
		 e = g.addEdge("AC", "A", "C", true);
		 e.addAttribute("capacity", 3);
		 e.addAttribute("cost", 4);
		 e = g.addEdge("BC", "B", "C", true);
		 e.addAttribute("capacity", 7);
		 e.addAttribute("cost", 2);
		 e = g.addEdge("CD", "C", "D", true);
		 e.addAttribute("capacity", 1);
		 e.addAttribute("cost", 8);
		 e = g.addEdge("CE", "C", "E", true);
		 e.addAttribute("capacity", 7);
		 e.addAttribute("cost", 5);
		 e = g.addEdge("CF", "C", "F", true);
		 e.addAttribute("capacity", 5);
		 e.addAttribute("cost", 2);
		 e = g.addEdge("FE", "F", "E", true);
		 e.addAttribute("capacity", 3);
		 e.addAttribute("cost", 1);
		 
		 NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		 ns.init(g);
		 System.out.println(ns.objectiveValue);
	}
}
