package org.graphstream.algorithm.networksimplex;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

// XXX Work in progress

/**
 * <h3>Minimum cost flow problem</h3>
 * 
 * <p>
 * Network simplex method is an algorithm that solves the minimum cost flow
 * (MCF) problem for an oriented graph.
 * </p>
 * 
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
 * </p>
 * 
 * <p>
 * MCF framework can be used to model a broad variety of network problems,
 * including matching, shortest path, transportation, etc. For example, if we
 * want to find the shortest paths from a source to all other nodes in a graph
 * with <i>n</i> nodes, we can set the <i>supply</i> to <i>n</i>-1 for the
 * source and to -1 for all other nodes, set <i>capacity</i> to <i>n</i>-1 and
 * <i>cost</i> to the weight for each arc. The solution of the MCF problem with
 * these particular settings will be minimum cost unit flow from the source to
 * all other nodes passing by the shortest paths.
 * </p>
 * 
 * <h3>Problem data</h3>
 * 
 * <p>
 * The user of this class must store the problem data as attributes of the nodes
 * and the edges of the graph as described below. The names of these attributes
 * are specified in the constructor
 * {@link #NetworkSimplex(String, String, String)}. For efficiency reasons all
 * the data are supposed to be integer. If some of the attributes are real,
 * their fractional part is ignored. To avoid loss of precision, the user must
 * scale her data properly if they are real.
 * </p>
 * 
 * <p>
 * An attribute called {@code supplyName} is used to store the supply (or demand
 * if negative) of each node. If a node has not an attribute with this name or
 * if the value of this attribute is not numeric, the node supply is considered
 * as zero (transshipment node).
 * </p>
 * 
 * <p>
 * An attribute called {@code capacityName} is used to store the capacity of
 * each edge. If an edge has not an attribute with this name, or if the value of
 * this attribute is negative or not numeric, the edge capacity is considered as
 * infinite.
 * </p>
 * 
 * <p>
 * An attribute called {@code costName} is used to store the cost per unit flow
 * of each edge. If an edge has not an attribute with this name or if the value
 * of this attribute is not numeric, the cost per unit flow of the edge is
 * considered 1.
 * </p>
 * 
 * <p>
 * The flow on a directed edge is always from its source node to its target
 * node. Each undirected edge is considered as a couple of directed edges with
 * the same capacity and cost per unit flow. In other words, there are possibly
 * two independent flows on each undirected edge.
 * </p>
 * 
 * <h3>Solutions</h3>
 * 
 * TODO
 * 
 * <h3>Visualization</h3>
 * 
 * TODO
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
		 * The solution is not (re)optimized. This is the value when the graph
		 * has changed since the last call of {@link NetworkSimplex#compute()}
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
	protected SolutionStatus solutionStatus = SolutionStatus.UNDEFINED;

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

	protected long animationDelay = 0;

	/**
	 * Creates a network simplex instance specifying attribute names to be used.
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
			if (animationDelay > 0)
				arc.setUIClass();
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
			arcs.put(arc.id, arc);

			node.parent = root;
			node.arcToParent = arc;
			node.computePotential();
			previous.thread = node;
			node.depth = 1;
			previous = node;

			totalSupply += node.supply;
			objectiveValue.plusTimes(arc.flow, arc.cost);

			if (animationDelay > 0)
				arc.setUIClass();
		}
		previous.thread = root;
		root.supply = (int) -totalSupply;

		solutionStatus = SolutionStatus.UNDEFINED;
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
	 * Turns upside-down the part of the tree between the entering arc and the
	 * leaving arc.
	 */
	protected void updateBFS() {
		NSNode stopNode = oldSubtreeRoot.parent;

		NSNode currentNode = newSubtreeRoot;
		NSNode oldParent = currentNode.parent;
		NSNode newParent = enteringArc.getOpposite(currentNode);
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
	 * Performs a pivot when the entering arc and the leaving arc are known.
	 * Changes the flows on the cycle and updates the BFS.
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
				// The leaving arc is in the direction of the cycle
				leavingArc.status = ArcStatus.NONBASIC_UPPER;
			else
				leavingArc.status = ArcStatus.NONBASIC_LOWER;
			nonBasicArcs.add(leavingArc);
			updateBFS();
		}
		if (animationDelay > 0) {
			enteringArc.setUIClass();
			leavingArc.setUIClass();
			try {
				Thread.sleep(animationDelay);
			} catch (InterruptedException e) {
			}

		}
	}

	/**
	 * The main simplex method loop. Selects leaving and entering arc. Loops
	 * until there are no more candidates or until absorbing cycle is found.
	 */
	protected void simplex() {
		while (true) {
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

	// access and modification of algorithm parameters

	/**
	 * Returns the name of the attribute used to store the supply of each node.
	 * This name is given as constructor parameter and cannot be modified.
	 * 
	 * @return The name of the supply attribute.
	 */
	public String getSupplyName() {
		return supplyName;
	}

	/**
	 * Returns the name of the attribute used to store the capacity of each
	 * edge. This name is given as constructor parameter and cannot be modified.
	 * 
	 * @return The name of the capacity attribute.
	 */
	public String getCapacityName() {
		return capacityName;
	}

	/**
	 * Returns the name of the attribute used to store the cost per unit flow of
	 * each edge. This name is given as constructor parameter and cannot be
	 * modified.
	 * 
	 * @return The name of the cost attribute.
	 */
	public String getCostName() {
		return costName;
	}

	/**
	 * Returns the currently used pricing strategy.
	 * 
	 * @return The pricing strategy
	 */
	public PricingStrategy getPricingStrategy() {
		return pricingStrategy;
	}

	/**
	 * Sets the pricing strategy
	 * 
	 * @param pricingStrategy
	 *            The new pricing strategy
	 */
	public void setPricingStrategy(PricingStrategy pricingStrategy) {
		this.pricingStrategy = pricingStrategy;
	}

	/**
	 * When the animation delay is positive, the algorithm continuously updates
	 * {@code "ui.class"} and {@code "label"} attributes of the edges and the
	 * nodes of the graph and sleeps at the end of each simplex pivot. This
	 * feature can be useful for visualizing the algorithm execution. The user
	 * must provide a stylesheet defining the classes of the graph elements as
	 * described in {@link #setUIClasses()} This feature is disabled by default.
	 * 
	 * @param millis
	 *            The time in milliseconds to sleep between two simplex pivots.
	 * @see #setUIClasses()
	 */
	public void setAnimationDelay(long millis) {
		animationDelay = millis;
	}

	/**
	 * This method can be used to visualize the current solution.
	 * 
	 * <p>
	 * It sets the attributes {@code "label"} and {@code "ui.class"} of the
	 * nodes and the edges of the graph depending on the current solution. The
	 * labels of the nodes are set to their balance (see
	 * {@link #getNodeBalance(Node)}). The labels of the edges are set to the
	 * flow passing through them. The {@code "ui.class"} attribute of the nodes
	 * is set to one of {@code "supply_balanced"}, {@code "supply_unbalanced"},
	 * {@code "demand_balanced"}, {@code "demand_unbalanced"},
	 * {@code "trans_balanced"} or {@code "trans_unbalanced"} depending on the
	 * node type and the node status. The {@code "ui.class"} attribute of the
	 * edges is set to one of {@code "basic"}, {@code "nonbasic_lower"} or
	 * {@code "nonbasic_upper"} according to their status (see
	 * {@link #getStatus(Edge)}.
	 * </p>
	 * <p>
	 * The user must provide a stylesheet defining the visual appearance for
	 * each of these node and edge classes. Note that if the animation delay is
	 * positive (see {@link #setAnimationDelay(long)}), there is no need to call
	 * this method, because in this case the labels and the UI classes of the
	 * graph elements are set and updated during the algorithm execution.
	 * </p>
	 * <p>
	 * Note that in the case of undirected edges the label and the UI class are
	 * set according to the status of one of the corresponding arcs (not
	 * specified which one).
	 * </p>
	 */
	public void setUIClasses() {
		for (Node node : graph)
			arcs.get(PREFIX + "ARTIFICIAL_" + node.getId()).setUIClass();
		for (Edge edge : graph.getEachEdge()) {
			NSArc arc = arcs.get(edge.getId());
			if (!edge.isDirected() && arc.flow == 0)
				arc = arcs.get(PREFIX + "REVERSE_" + edge.getId());
			arc.setUIClass();
		}
	}

	/**
	 * Returns the graph on which the algorithm is applied. This is the graph
	 * passed in parameter in {@link #init(Graph)}.
	 * 
	 * @return The graph on which the algorithm is applied.
	 */
	public Graph getGraph() {
		return graph;
	}

	// solution access methods

	/**
	 * The MCF problem has solution only if the problem is balanced, i.e. if the
	 * total supply is equal to the total demand. This method returns the
	 * missing supply (if positive) or demand (if negative) in order to make the
	 * problem balanced. If the returned value is zero, the problem is balanced.
	 * 
	 * @return The network balance
	 */
	public int getNetworkBalance() {
		return root.supply;
	}

	/**
	 * Returns the amount of unsatisfied supply (for supply nodes) or demand
	 * (for demand nodes) of a given node. This method always returns a
	 * non-negative value. If the value is zero, the current solution satisfies
	 * the node demand / supply. If the current solution is feasible, the
	 * returned value is always zero.
	 * 
	 * @param node
	 *            A node
	 * @return The balance of the node
	 */
	public int getNodeBalance(Node node) {
		return arcs.get(PREFIX + "ARTIFICIAL_" + node.getId()).flow;
	}

	/**
	 * Returns the status of the current solution.
	 * 
	 * @return The solution status
	 */
	public SolutionStatus getSolutionStatus() {
		return solutionStatus;
	}

	/**
	 * Returns the flow on an edge. If {@code sameDirection} is true, returns
	 * the flow from the source to the target of the edge, otherwise returns the
	 * flow from the target to the source of the edge. Note that for directed
	 * edges the flow can only pass from the source node to the target node. For
	 * undirected edges there may be independent flows in both directions.
	 * 
	 * @param edge
	 *            An edge
	 * @param sameDirection
	 *            If true, returns the flow from the source to the target.
	 * @return The flow on the edge.
	 */
	public int getFlow(Edge edge, boolean sameDirection) {
		if (edge.isDirected())
			return sameDirection ? arcs.get(edge.getId()).flow : 0;
		else
			return arcs.get((sameDirection ? "" : PREFIX + "REVERSE_")
					+ edge.getId()).flow;
	}

	/**
	 * Returns the flow on an edge from its source node to its target node. The
	 * same as {@code getFlow(Edge, true)}.
	 * 
	 * @param edge
	 *            An edge
	 * @return The flow on the edge
	 * @see #getFlow(Edge, boolean)
	 */
	public int getFlow(Edge edge) {
		return getFlow(edge, true);
	}

	/**
	 * Returns the status of an edge (basic, non-basic at zero or non-basic at
	 * upper bound) in the current BFS. Note that undirected edges are
	 * interpreted as two directed arcs. If {@code sameDirection} is true, the
	 * method returns the status of the arc from the source to the target of the
	 * edge, otherwise it returns the status of the arc from the target to the
	 * source. If the edge is directed and {@code sameDirection} is false,
	 * returns {@code null}.
	 * 
	 * @param edge
	 *            An edge
	 * @param sameDirection
	 *            If true, returns the status of the arc from the source to the
	 *            target.
	 * @return The status of the edge
	 */
	public ArcStatus getStatus(Edge edge, boolean sameDirection) {
		if (edge.isDirected())
			return sameDirection ? arcs.get(edge.getId()).status : null;
		else
			return arcs.get((sameDirection ? "" : PREFIX + "REVERSE")
					+ edge.getId()).status;
	}

	/**
	 * Returns the status of an edge in the current BFS. The same as
	 * {@code getStatus(edge, true)}.
	 * 
	 * @param edge
	 *            An edge
	 * @return The status of the edge
	 * @see #getStatus(Edge, boolean)
	 */
	public ArcStatus getStatus(Edge edge) {
		return getStatus(edge, true);
	}

	/**
	 * Returns the edge to the parent of a node in the BFS tree.
	 * 
	 * If the parent of the node is the artificial root, this method returns
	 * {@code null}. When the returned edge is undirected, use
	 * {@link #getStatus(Edge, boolean)} to know which of the both arcs is
	 * basic.
	 * 
	 * @param node
	 *            A node
	 * @return The edge to the parent of the node in the BFS tree
	 */
	public Edge getEdgeToParent(Node node) {
		NSArc arc = nodes.get(node.getId()).arcToParent;
		if (arc.isArtificial())
			return null;
		return graph.getEdge(arc.getOriginalId());
	}

	/**
	 * Returns the total cost of the current network flow
	 * 
	 * @return The objective value of the problem
	 */
	public long getObjectiveValue() {
		return objectiveValue.getSmall();
	}

	// DynamicAlgorithm methods

	public void init(Graph graph) {
		this.graph = graph;
		cloneGraph();
		createInitialBFS();
		graph.addSink(this);
	}

	public void compute() {
		simplex();
	}

	public void terminate() {
		graph.removeSink(this);
		solutionStatus = SolutionStatus.UNDEFINED;
	}

	// Sink methods

	@Override
	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		if (attribute.equals(costName)) {
			double v = objectToDouble(value);
			if (Double.isNaN(v))
				v = 1;

			NSArc arc = arcs.get(edgeId);
			work1.set((int) v);
			changeCost(arc, work1);

			arc = arcs.get(PREFIX + "REVERSE_" + edgeId);
			if (arc != null) {
				work1.set((int) v);
				changeCost(arc, work1);
			}
		}
	}

	@Override
	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		edgeAttributeAdded(sourceId, timeId, edgeId, attribute, newValue);
	}

	@Override
	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		edgeAttributeAdded(sourceId, timeId, edgeId, attribute, null);
	}

	@Override
	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		if (attribute.equals(supplyName)) {
			double v = objectToDouble(value);
			if (Double.isNaN(v))
				v = 0;

			NSNode node = nodes.get(nodeId);
			changeSupply(node, (int) v);
		}
	}

	@Override
	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		nodeAttributeAdded(sourceId, timeId, nodeId, attribute, newValue);
	}

	@Override
	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		nodeAttributeAdded(sourceId, timeId, nodeId, attribute, null);
	}

	// helpers for the sink

	/**
	 * Utility method trying to convert object to double in the same way as
	 * {@link org.graphstream.graph.implementations.AbstractElement#getNumber(String)}
	 * does.
	 * 
	 * @param o
	 *            The object to be converted
	 * @return The numeric value of the object or NaN
	 */
	protected static double objectToDouble(Object o) {
		if (o != null) {
			if (o instanceof Number)
				return ((Number) o).doubleValue();

			if (o instanceof String) {
				try {
					return Double.parseDouble((String) o);
				} catch (NumberFormatException e) {
				}
			}
		}
		return Double.NaN;
	}

	/**
	 * Changes the cost of an arc
	 * 
	 * @param arc
	 *            The arc that changes cost
	 * @param newCost
	 *            The new cost
	 */
	protected void changeCost(NSArc arc, BigMNumber newCost) {
		if (arc.cost.compareTo(newCost) == 0)
			return;
		objectiveValue.plusTimes(-arc.flow, arc.cost);
		arc.cost.set(newCost);
		objectiveValue.plusTimes(arc.flow, arc.cost);

		if (arc.status == ArcStatus.BASIC) {
			NSNode subtreeRoot = arc.source.arcToParent == arc ? arc.source
					: arc.target;
			subtreeRoot.computePotential();
			for (NSNode node = subtreeRoot.thread; node.depth > subtreeRoot.depth; node = node.thread)
				node.computePotential();
		}
		solutionStatus = SolutionStatus.UNDEFINED;
	}

	protected void changeSupply(NSNode node, int newSupply) {
		if (node.supply == newSupply)
			return;
		NSArc artificialArc = arcs.get(PREFIX + "ARTIFICIAL_" + node.id);

		// enter the artificial arc is in the tree if not there
		if (artificialArc.status == ArcStatus.NONBASIC_LOWER) {
			enteringArc = artificialArc;
			selectLeavingArc();
			// if we are in infinite cycle, switch the direction
			if (cycleFlowChange.isInfinite()) {
				artificialArc.switchDirection();
				selectLeavingArc();
			}
			pivot();
		}
		// now the artificial arc is basic and we can change its flow
		objectiveValue.plusTimes(-artificialArc.flow, artificialArc.cost);
		int delta = newSupply - node.supply;
		node.supply = newSupply;
		root.supply -= delta;
		if (node == artificialArc.source) {
			artificialArc.flow += delta;
		} else {
			artificialArc.flow -= delta;
		}
		if (artificialArc.flow < 0)
			artificialArc.switchDirection();

		objectiveValue.plusTimes(artificialArc.flow, artificialArc.cost);
		solutionStatus = SolutionStatus.UNDEFINED;

		if (animationDelay > 0)
			artificialArc.setUIClass();
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
	public static enum ArcStatus {
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

	/**
	 * Internal representation of the graph arcs. Stores the arc ids,
	 * capacities, costs, source and target nodes. Maintains BFS information:
	 * flow and status.
	 */
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
				v = 1;
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
			if (animationDelay > 0)
				setUIClass();
		}

		/**
		 * Returns the node on the other side.
		 * 
		 * @param node
		 *            One of the endpoints of this arc
		 * @return The opposite node
		 */
		NSNode getOpposite(NSNode node) {
			if (node == source)
				return target;
			if (node == target)
				return source;
			return null;
		}

		/**
		 * Checks if this arc is artificial.
		 * 
		 * @return True if the arc is artificial
		 */
		public boolean isArtificial() {
			return source == root || target == root;
		}

		/**
		 * Returns the id of the edge of the original graph corresponding to
		 * this arc.
		 * 
		 * @return The id of the original edge
		 */
		String getOriginalId() {
			if (isArtificial())
				return null;
			if (id.startsWith(PREFIX + "REVERSE_"))
				return id.substring(PREFIX.length() + "REVERSE_".length());
			return id;
		}

		/**
		 * Sets the label and the {@code "ui.class"} attribute of the edge (or
		 * node if the arc is artificial) of the original graph corresponding to
		 * this arc.
		 */
		void setUIClass() {
			if (isArtificial()) {
				NSNode node = getOpposite(root);
				String uiClass = "trans";
				if (node.supply > 0)
					uiClass = "supply";
				else if (node.supply < 0)
					uiClass = "demand";
				uiClass += flow == 0 ? "_balanced" : "_unbalanced";
				Node x = graph.getNode(node.id);
				x.addAttribute("label", flow);
				x.addAttribute("ui.class", uiClass);
			} else {
				String uiClass = "basic";
				if (status == ArcStatus.NONBASIC_LOWER)
					uiClass = "nonbasic_lower";
				else if (status == ArcStatus.NONBASIC_UPPER)
					uiClass = "nonbasic_upper";

				Edge e = graph.getEdge(getOriginalId());
				e.addAttribute("label", flow);
				e.addAttribute("ui.class", uiClass);
			}
		}

		/**
		 * Switches arc direction. Use with caution!
		 * Used only in {@link NetworkSimplex#changeSupply(NSNode, int)}
		 * for artificial basic arcs.
		 */
		void switchDirection() {
			NSNode tmp = source;
			source = target;
			target = tmp;
			flow = -flow;
			
			NSNode subtreeRoot = getOpposite(root);
			subtreeRoot.computePotential();
			for (NSNode node = subtreeRoot.thread; node.depth > subtreeRoot.depth; node = node.thread)
				node.computePotential();
		}
	}

	// test and debug

	/**
	 * Prints a table containing informations about the current basic feasible
	 * solution. Useful for testing and debugging purposes.
	 * 
	 * @param ps
	 *            A stream where the output goes.
	 */
	public void printBFS(PrintStream ps) {
		ps.println("=== Nodes ===");
		ps.printf("%20s%10s%10s%20s%20s%10s%n", "id", "supply", "potential",
				"parent", "thread", "depth");
		ps.printf("%20s%10d%10s%20s%20s%10d%n", root.id, root.supply,
				root.potential, "-", root.thread.id, root.depth);
		for (NSNode node : nodes.values())
			ps.printf("%20s%10d%10s%20s%20s%10d%n", node.id, node.supply,
					node.potential, node.parent.id, node.thread.id, node.depth);
		ps.println();

		ps.println("=== Arcs ===");
		ps.printf("%20s%10s%10s%10s%10s%20s%n", "id", "capacity", "cost",
				"flow", "r. cost", "status");
		for (NSArc a : arcs.values()) {
			a.computeReducedCost(work1);
			ps.printf("%20s%10s%10s%10s%10s%20s%n", a.id,
					a.capacity == INFINITE_CAPACITY ? "Inf" : a.capacity,
					a.cost, a.flow, work1, a.status);
		}

		ps.println();
		ps.printf("=== Objective value %s. Solution status %s ===%n%n",
				objectiveValue, solutionStatus);
	}
}
