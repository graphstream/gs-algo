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
 * @since 2011-05-14
 * 
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.randomWalk;

import static org.graphstream.algorithm.Toolkit.randomNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.StringJoiner;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

/**
 * A random walk on a graph.
 * 
 * <h3>Idea</h3>
 * 
 * <p>
 * This algorithm create a given number of entities first associated with random
 * nodes in the graph. Then by turns, each entity chooses an edge at random and
 * crosses it. This is iterated a given number of turns. Each time an entity
 * crosses an edge, a count is incremented on it and each time it arrives on
 * a node a count is counted on it.
 * </p>
 * 
 * <p>
 * You can override the entity class to provide your own behaviour for entity
 * movement.
 * </p>
 *
 * <h3>Counts on edges and nodes</h3>
 * 
 * <p>
 * If the algorithm was run for an infinite number of turns, each counter would
 * have the same value. However we can choose to stop the algorithm when needed.
 * Furthermore the algorithm can be biased by providing each entity with a
 * memory of the already crossed edges. It can avoid these edges when choosing
 * at random its next edge. 
 * </p>
 * 
 * <p>
 * When an entity has no edge to choose (either because of its memory or because
 * it reached a node that is only reachable via a one directed edge), the entity
 * will jump randomly on another node.
 * </p>
 * 
 * <p>
 * When the number of turns
 * awaited is reached, one can observe the counts on each edge and node. Edges
 * and nodes that are very attractive in terms of topology should have a more
 * important count than others.
 * </p>
 * 
 * <p>
 * This algorithm does not cope well with dynamic graphs. You can however improve
 * this by using evaporation. When evaporation is activated, at each turn, the
 * node and edge counts are multiplied by a number between 0 and 1. Therefore each
 * edge or node count must be constantly updated by entities leading to a value that
 * stabilizes in time.
 * </p>
 * 
 * <h3>The basic tabu entity</h3>
 * 
 * <p>
 * At each step, the default entities move from their current node to another via
 * an edge randomly chosen. This is done in the {@link Entity#step()} method.
 * </p>
 * 
 * <p>
 * This method makes a list of all leaving edges of the current node. If the
 * node has no leaving edge, the entity jumps to another randomly chosen node.
 * Then an edge is chosen at random in the list of leaving edges. The edge is
 * chosen uniformly if there are no weights on the edges, else, an edge with
 * an higher weight has more chances to be chosen than an edge with a lower
 * weight.
 * </p>
 * 
 * <p>
 * When crossed, if the memory is larger than 0, the edge crossed is remembered
 * so that the entity will not choose it anew until it crosses as many edges as
 * the memory size.
 * </p>
 *
 * <h2>Usage</h2>
 * 
 * <p>
 * With the default entities, you can make a node entirely tabu by putting the
 * ``tabu`` attribute on it. No entity will traverse an edge that leads
 * to such a node.
 * </p>
 *
 * <p>
 * You can change the default entity class either by overriding the
 * {@link #createEntity()} method or by changing the entity class name
 * using {@link #setEntityClass(String)}.
 * </p>
 * 
 * <p>
 * If the edges have weights, the entities can use them to favour edges
 * with higher weights when randomly choosing them. By default the
 * weights are searched on edges using the ``weight`` attribute. However
 * you can override this using {@link #setWeightAttribute(String)} method.
 * </p>
 *
 * <p>
 * If you choose to have evaporation on edge counts at each turn, you can
 * set it using {@link #setEvaporation(double)}. The evaporation is a number
 * between 0 and 1. If set to 1 (the default), the counts are not modified,
 * else the counts are multiplied by the evaporation at each turn.
 * </p>
 * 
 * <p>
 * To compute a turn, use the {@link #compute()} method. This will move each
 * entity from one node to another.
 * </p>
 *
 * <p>
 * Once computed each edge and node will have an attribute ``passes`` stored
 * on it containing the number of passage of an entity. You can change the
 * name of this attribute using {@link #setPassesAttribute(String)}. After
 * each computation of a turn, you can obtain the edge and nodes counts using
 * either the passes attribute, or the utility methods {@link #getPasses(Node)}
 * and {@link #getPasses(Edge)}.
 * </p>
 * 
 * <p>
 * You can count only the passes on the nodes or edges using the two methods
 * {@link #computeEdgesPasses(boolean)} and {@link #computeNodePasses(boolean)}.
 * </p>
 * 
 * <p>
 * As some entities may have jumped from their node to another one chosen
 * randomly, you can obtain the number of entities that jumped using
 * {@link #getJumpCount()}. 
 * </p>
 * 
 * <h2>Complexity</h2>
 * 
 * The complexity, at each turn is O(n) with n the number of entities.
 * 
 * <h2>Example</h2>
 * 
 * Here is how to compute a simple pass count for 1000 steps:
 * 
 * <pre>
 * Graph graph = new MultiGraph("random walk");
 * RandomWalk rwalk = new RandomWalk();
 * // Populate the graph.
 * rwalk.setEntityCount(graph.getNodeCount()/2);
 * rwalk.init(graph);
 * for(int i=0; i<1000; i++) {
 * 		rwalk.compute();
 * }
 * rwalk.terminate();
 * for(Edge edge: graph.getEachEdge()) {
 * 		System.out.println("Edge %s counts %f%n", edge.getId(), rwalk.getPasses(edge));
 * }
 * </pre>
 */
public class RandomWalk extends SinkAdapter implements DynamicAlgorithm {
	// Attribute
	
	public class Context {
		/**
		 * The graph.
		 */
		protected Graph graph;
		
		/**
		 * The name of the attribute used to count the number of pass on an edge.
		 */
		protected String passesAttribute = "passes";

		/**
		 * The name of the attribute on edges that give their respective importance.
		 */
		protected String weightAttribute = null;

		/**
		 * Random number generator.
		 */
		protected Random random;

		/**
		 * The node tabu list.
		 */
		protected int entityMemory = 0;

		/**
		 * Number of entities that jump at each step.
		 */
		protected int jumpCount = 0, goCount = 0, waitCount = 0;
		
		public String getPassesAttribute() {
			return passesAttribute;
		}
		
		public String getWeightAttribute() {
			return weightAttribute;
		}
		
		public Random getRandom() {
			return random;
		}
		
		public int getEntityMemory() {
			return entityMemory;
		}
	}

	/**
	 * The informations shared between this class an entities.
	 */
	protected Context context = new Context();

	/**
	 * The entity class to use.
	 */
	protected String entityClass = TabuEntity.class.getName();// "org.graphstream.algorithm.RandomWalk#TabuEntity";
	
	/**
	 * The set of entities travelling on the graph.
	 */
	protected ArrayList<Entity> entities = new ArrayList<Entity>();

	/**
	 * The random seed.
	 */
	protected long randomSeed;

	/**
	 * Initial count of entities.
	 */
	protected int entityCount = 100;

	/**
	 * Allow to reduce the amount counted on each edge at each turn. At each turn
	 * the edges counts are multiplied by the evaporation.
	 */
	protected double evaporation = 1;

	/**
	 * Compute counts on nodes.
	 */
	protected boolean doNodes = true;

	/**
	 * Compute counts on edges.
	 */
	protected boolean doEdges = true;

	// Constructor

	/**
	 * New random walk with a new random seed (based on time), with an entity
	 * memory set to 10 nodes (tabu list), with an attributes to store passes
	 * named "passes" and no weight attribute.
	 */
	public RandomWalk() {
		this(System.currentTimeMillis());
	}

	/**
	 * New random walk with a given random seed, with an entity memory set to 10
	 * nodes (tabu list), with an attributes to store passes named "passes" and
	 * no weight attribute.
	 * 
	 * @param randomSeed
	 *            The random seed.
	 */
	public RandomWalk(long randomSeed) {
		this.randomSeed = randomSeed;
		this.context.random = new Random(randomSeed);
	}

	/**
	 * The name of the attribute where the number of entities passes are stored
	 * (for edges and nodes).
	 * 
	 * @return A string representing the attribute name for entity passes.
	 */
	public String getPassesAttribute() {
		return context.passesAttribute;
	}

	/**
	 * Set the name of the entity class to use. If set to null, the default entity
	 * class will be used (RandomWalk.TabuEntity).
	 * @param name The name of the entity class to use.
	 */
	@Parameter
	public void setEntityClass(String name) {
		if(name == null) {
			entityClass = TabuEntity.class.getName();//"org.graphstream.algorithm.RandomWalk#TabuEntity";
		} else {
			entityClass = name;
		}
	}
	
	/**
	 * Set the entity memory in number of nodes remembered. This memory is used
	 * as a tabu list, that is a set of nodes not to cross.
	 * 
	 * @param size
	 *            The memory size, 0 is a valid size to disable the tabu list.
	 */
	@Parameter
	public void setEntityMemory(int size) {
		if (size < 0)
			size = 0;

		context.entityMemory = size;
	}
	
	/**
	 * Set the evaporation of edge counts. This is a number between 0 and 1. If less
	 * than 1, at each turn, each edge count is multiplied by this factor. The use
	 * of evaporation allows to stabilize the counts.
	 * @param evaporation A number between 0 and 1.
	 */
	@Parameter
	public void setEvaporation(double evaporation) {
		if(evaporation>=0 && evaporation<1) {
			this.evaporation = evaporation;
		}
	}
	
	/**
	 * The evaporation value.
	 * @return The evaporation.
	 */
	public double getEvaporation() {
		return evaporation;
	}

	/**
	 * The random seed used.
	 * 
	 * @return A long integer containing the random seed.
	 */
	public long getRandomSeed() {
		return randomSeed;
	}

	/**
	 * Number of entities.
	 * 
	 * @return The number of entities.
	 */
	public int getEntityCount() {
		return entities.size();
	}

	/**
	 * Number of entities that jumped instead of traversing an edge at last
	 * step. An entity executes a jump when it is blocked in a dead end (either
	 * a real one, or because of its tabu list).
	 * 
	 * @return The jump count.
	 */
	public int getJumpCount() {
		return context.jumpCount;
	}

	public int getWaitCount() {
		return context.waitCount;
	}

	public int getGoCount() {
		return context.goCount;
	}

	/**
	 * Ratio of entities that executed a jump instead of traversing an edge at
	 * last step. An entity executes a jump when it is blocked in a dead end
	 * (either a real one, or because of its tabu list).
	 * 
	 * @return The jump ratio (in [0-1]).
	 */
	public double getJumpRatio() {
		return (((double) context.jumpCount) / ((double) entities.size()));
	}

	/**
	 * The name of the attribute used to fetch edges importance.
	 * 
	 * @param name
	 *            A string giving the weight name.
	 */
	@Parameter
	public void setWeightAttribute(String name) {
		context.weightAttribute = name;
	}

	/**
	 * Set the name of the attribute used to store the number of passes of each
	 * entity on each edge or node.
	 * 
	 * @param name
	 *            A string giving the passes name.
	 */
	@Parameter
	public void setPassesAttribute(String name) {
		if (context.graph != null) {
			
			context.graph.edges().forEach(e -> {
				e.setAttribute(name, e.getNumber(context.passesAttribute));
				e.removeAttribute(context.passesAttribute);
			});
			
			context.graph.forEach(n -> {
				n.setAttribute(name, n.getNumber(context.passesAttribute));
				n.removeAttribute(context.passesAttribute);
			});
		}

		context.passesAttribute = name;
	}
	
	/**
	 * The number of entity passage on the given edge.
	 * @param edge The edge to look at.
	 * @return The number of passes on the edge.
	 */
	public double getPasses(Edge edge) {
		return edge.getNumber(context.passesAttribute);
	}

	/**
	 * The number of entity passage on the given node.
	 * @param node The node to look at.
	 * @return The number of passes on the node.
	 */
	public double getPasses(Node node) {
		return node.getNumber(context.passesAttribute);
	}

	/**
	 * Set the number of entities which will be created at the algorithm
	 * initialization.
	 * 
	 * @param entityCount number of entities
	 */
	@Parameter
	public void setEntityCount(int entityCount) {
		this.entityCount = entityCount;
	}

	/**
	 * Activate or not the counts on edges when entities cross thems.
	 * @param on If true (the default) the edges passes are counted.
	 */
	public void computeEdgesPasses(boolean on) {
		doEdges = on;
	}

	/**
	 * Activate or not the counts on nodes when entities cross thems.
	 * @param on If true (the default) the nodes passes are counted.
	 */
	public void computeNodePasses(boolean on) {
		doNodes = on;
	}
	
	/**
	 * Create an entity. Override this method to create different kinds of
	 * entities or change the entity class name. The default one is the "TabuEntity".
	 * 
	 * @return The new entity.
	 * @see #setEntityClass(String)
	 */
	public Entity createEntity() {
		try {
			Object o = Class.forName(entityClass).newInstance();
			if(o instanceof Entity) {
				Entity e = (Entity) o;
				e.init(context, randomNode(context.graph, context.random));
				
				return e;
			} else {
				System.err.printf("Object %s  pointed at by class name '%s' does not implement Entity.%n", o.getClass().getName(), entityClass);
			}
		} catch (Exception e) {
			System.err.printf("Error: %s%n", e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Initialize the algorithm for a given graph with a given entity count. The
	 * entities are created at random locations on the graph.
	 * 
	 * @param graph
	 *            The graph to explore.
	 */
	public void init(Graph graph) {
		if (context.graph != null)
			throw new RuntimeException(
					"cannot begin a random walk if the previous one was not finished, use end().");

		context.graph = graph;

		entities.clear();

		for (int i = 0; i < entityCount; i++)
			entities.add(createEntity());

		equipGraph();
		graph.addElementSink(this);
	}

	/**
	 * Execute one step of the algorithm. During one step, each entity choose a
	 * next edge to cross, toward a new node. The passes attribute of these edge
	 * and node are updated.
	 */
	public void compute() {
		context.jumpCount = 0;
		context.goCount = 0;
		context.waitCount = 0;

		entities.forEach(entity -> entity.step());

		if(evaporation<1)
			evaporate();
	}
	
	/**
	 * Apply evaporation on each edge.
	 */
	protected void evaporate() {
		context.graph.edges().forEach(edge -> {
			edge.setAttribute(context.passesAttribute, edge.getNumber(context.passesAttribute)*evaporation);
		});
		
		context.graph.nodes().forEach(node -> {
			node.setAttribute(context.passesAttribute, node.getNumber(context.passesAttribute)*evaporation);
		});
	}

	/**
	 * End the algorithm by removing any listener on the graph and releasing
	 * memory.
	 */
	public void terminate() {
		entities.clear();
		context.graph.removeElementSink(this);
		context.graph = null;
	}

	protected void equipGraph() {
		context.graph.edges().forEach(e -> e.setAttribute(context.passesAttribute, 0.0));

		context.graph.nodes().forEach(n -> n.setAttribute(context.passesAttribute, 0.0));
	}

	/**
	 * Sort all edges by their "passes" attribute and return the array of sorted
	 * edges.
	 * 
	 * @return An array with all edges of the graph sorted by their number of
	 *         entity pass.
	 */
	public ArrayList<Edge> findTheMostUsedEdges() {
		ArrayList<Edge> edges = new ArrayList<Edge>(context.graph.getEdgeCount());
		
		context.graph.edges().forEach(e -> edges.add(e));
		
		Collections.sort(edges, new Comparator<Edge>() {
			public int compare(Edge e1, Edge e2) {
				int n1 = (int) e1.getNumber(context.passesAttribute);
				int n2 = (int) e2.getNumber(context.passesAttribute);

				return (n1 - n2);
			}
		});

		return edges;
	}

	/**
	 * Sort all nodes by their "passes" attribute and return the array of sorted
	 * nodes.
	 * 
	 * @return An array with all nodes of the graph sorted by their number of
	 *         entity pass.
	 */
	public ArrayList<Node> findTheMostUsedNodes() {
		ArrayList<Node> nodes = new ArrayList<Node>(context.graph.getNodeCount());
				
		context.graph.nodes().forEach(n -> nodes.add(n));

		Collections.sort(nodes, new Comparator<Node>() {
			public int compare(Node e1, Node e2) {
				int n1 = (int) e1.getNumber(context.passesAttribute);
				int n2 = (int) e2.getNumber(context.passesAttribute);

				return (n1 - n2);
			}
		});

		return nodes;
	}
	// Graph listener

	@Override
	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		Edge edge = context.graph.getEdge(edgeId);

		if (edge != null)
			edge.setAttribute(context.passesAttribute, 0.0);
	}

	@Override
	public void nodeAdded(String graphId, long timeId, String nodeId) {
		Node node = context.graph.getNode(nodeId);

		node.setAttribute(context.passesAttribute, 0.0);
	}
	
	@Result
	public String defaultResult() {
		StringJoiner sj = new StringJoiner(" | ", "====== Random Walk ====== \n", "");
		context.graph.edges().forEach(e -> sj.add("Edge "+e.getId()+" counts "+getPasses(e)));
		return sj.toString();
	}
}


// Nested classes
/*
public class TabuTimedEntity extends TabuEntity {
	protected static final float SPEED = 1000;

	protected double crossing = 0;

	@Override
	public void init(Node start) {
		super.init(start);
	}

	@Override
	public void step() {
		if (crossing > 0) {
			waitCount++;
			crossing -= SPEED;
		} else {
			goCount++;
			tabuStep();
		}
	}

	@Override
	protected void jump() {
		super.jump();
		crossing = 0;
	}

	@Override
	protected void cross(Edge edge) {
		super.cross(edge);

		float speed = 1f;

		if (edge.hasLabel("SPEED_CAT")) {
			String s = (String) edge.getLabel("SPEED_CAT");
			speed = 9 - Float.parseFloat(s);
		}
		if (edge.hasLabel("LANE_CAT")) {
			String s = (String) edge.getLabel("LANE_CAT");
			speed *= Float.parseFloat(s);
		}

		if (speed <= 0)
			speed = 1f;

		crossing = edgeLength(edge) / speed;
	}
}
*/