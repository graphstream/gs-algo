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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Random;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

import static org.graphstream.algorithm.Toolkit.*;

/**
 * A random walk on a graph.
 * 
 * <p>
 * This algorithm create a given number of entities first associated with random
 * nodes in the graph. Then by turns, each entity choose an edge at random and
 * cross it. This is iterated a given number of turns. Each time an entity
 * crosses an edge, a count is incremented on it. When the number of turns
 * awaited is reached, one can observe the counts on each edge. Edges that are
 * very important in terms of topology should have a more important count than
 * others.
 * </p>
 * 
 * @author Antoine Dutot
 */
public class RandomWalk extends SinkAdapter implements DynamicAlgorithm {
	// Attribute

	/**
	 * The graph.
	 */
	protected Graph graph;

	/**
	 * The set of entities travelling on the graph.
	 */
	protected ArrayList<Entity> entities = new ArrayList<Entity>();

	/**
	 * Random number generator.
	 */
	protected Random random;

	/**
	 * The random seed.
	 */
	protected long randomSeed;

	/**
	 * Initial count of entities.
	 */
	protected int entityCount = 100;

	/**
	 * The node tabu list.
	 */
	protected int entityMemory = 40;

	/**
	 * The name of the attribute used to count the number of pass on an edge.
	 */
	protected String passesAttribute = "passes";

	/**
	 * The name of the attribute on edges that give their respective importance.
	 */
	protected String weightAttribute = null;

	/**
	 * Number of entities that jump at each step.
	 */
	protected int jumpCount = 0, goCount = 0, waitCount = 0;

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
		this.random = new Random(randomSeed);
	}

	/**
	 * The name of the attribute where the number of entities passes are stored
	 * (for edges and nodes).
	 * 
	 * @return A string representing the attribute name for entity passes.
	 */
	public String getPassesAttribute() {
		return passesAttribute;
	}

	/**
	 * Set the entity memory in number of nodes remembered. This memory is used
	 * as a tabu list, that is a set of nodes not to cross.
	 * 
	 * @param size
	 *            The memory size, 0 is a valid size to disable the tabu list.
	 */
	public void setEntityMemory(int size) {
		if (size < 0)
			size = 0;

		entityMemory = size;
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
		return jumpCount;
	}

	public int getWaitCount() {
		return waitCount;
	}

	public int getGoCount() {
		return goCount;
	}

	/**
	 * Ratio of entities that executed a jump instead of traversing an edge at
	 * last step. An entity executes a jump when it is blocked in a dead end
	 * (either a real one, or because of its tabu list).
	 * 
	 * @return The jump ratio (in [0-1]).
	 */
	public float getJumpRatio() {
		return (((float) jumpCount) / ((float) entities.size()));
	}

	/**
	 * The name of the attribute used to fetch edges importance.
	 * 
	 * @param name
	 *            A string giving the weight name.
	 */
	public void setWeightAttribute(String name) {
		weightAttribute = name;
	}

	/**
	 * Set the name of the attribute used to store the number of passes of each
	 * entity on each edge or node.
	 * 
	 * @param name
	 *            A string giving the passes name.
	 */
	public void setPassesAttribute(String name) {
		if (graph != null) {
			for (Edge e : graph.getEachEdge()) {
				e.addAttribute(name, e.getNumber(passesAttribute));
				e.removeAttribute(passesAttribute);
			}

			for (Node n : graph) {
				n.addAttribute(name, n.getNumber(passesAttribute));
				n.removeAttribute(passesAttribute);
			}
		}

		passesAttribute = name;
	}

	/**
	 * Set the number of entities which will be created at the algorithm
	 * initialization.
	 * 
	 * @param entityCount
	 */
	public void setEntityCount(int entityCount) {
		this.entityCount = entityCount;
	}

	/**
	 * Create an entity. Override this method to create different kinds of
	 * entity. The default one is the "TabuEntity".
	 * 
	 * @return The new entity.
	 */
	public Entity createEntity() {
		// return new TabuEntity( graph.algorithm().getRandomNode( random ) );
		return new TabuTimedEntity(randomNode(graph, random));
	}

	/**
	 * Initialize the algorithm for a given graph with a given entity count. The
	 * entities are created at random locations on the graph.
	 * 
	 * @param graph
	 *            The graph to explore.
	 */
	public void init(Graph graph) {
		if (this.graph != null)
			throw new RuntimeException(
					"cannot begin a random walk if the previous one was not finished, use end().");

		this.graph = graph;

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
		jumpCount = 0;
		goCount = 0;
		waitCount = 0;

		for (Entity entity : entities) {
			entity.step();
		}
	}

	/**
	 * End the algorithm by removing any listener on the graph and releasing
	 * memory.
	 */
	public void terminate() {
		entities.clear();
		graph.removeElementSink(this);
		this.graph = null;
	}

	protected void equipGraph() {
		for (Edge e : graph.getEachEdge()) {
			e.addAttribute(passesAttribute, 0.0);
		}

		for (Node n : graph) {
			n.addAttribute(passesAttribute, 0.0);
		}
	}

	/**
	 * Sort all edges by their "passes" attribute and return the array of sorted
	 * edges.
	 * 
	 * @return An array with all edges of the graph sorted by their number of
	 *         entity pass.
	 */
	public ArrayList<Edge> findTheMostUsedEdges() {
		ArrayList<Edge> edges = new ArrayList<Edge>(graph.getEdgeCount());
		Iterator<? extends Edge> i = graph.getEdgeIterator();

		while (i.hasNext()) {
			edges.add(i.next());
		}

		Collections.sort(edges, new Comparator<Edge>() {
			public int compare(Edge e1, Edge e2) {
				int n1 = (int) e1.getNumber(passesAttribute);
				int n2 = (int) e2.getNumber(passesAttribute);

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
		ArrayList<Node> nodes = new ArrayList<Node>(graph.getNodeCount());
		Iterator<? extends Node> i = graph.getNodeIterator();

		while (i.hasNext()) {
			nodes.add(i.next());
		}

		Collections.sort(nodes, new Comparator<Node>() {
			public int compare(Node e1, Node e2) {
				int n1 = (int) e1.getNumber(passesAttribute);
				int n2 = (int) e2.getNumber(passesAttribute);

				return (n1 - n2);
			}
		});

		return nodes;
	}

	// Access

	// Command

	// Nested classes

	protected interface Entity {
		void step();
	}

	protected class TabuEntity implements Entity {
		protected LinkedList<Node> memory;

		protected Node current;

		protected float weights[];

		public TabuEntity(Node start) {
			current = start;
		}

		public void step() {
			tabuStep();
		}

		protected float weight(Edge e) {
			if (!e.hasAttribute(weightAttribute))
				return 1;

			return (float) e.getNumber(weightAttribute);
		}

		protected void tabuStep() {
			int n = current.getOutDegree();

			Iterator<? extends Edge> to;

			to = current.getLeavingEdgeIterator();

			ArrayList<Edge> edges = new ArrayList<Edge>();

			while (to.hasNext()) {
				Edge e = to.next();

				if (!tabu(e.getOpposite(current))) {
					edges.add(e);
				}
			}

			n = edges.size();

			if (n == 0) {
				jump();
			} else {
				if (weightAttribute != null) {
					if (weights == null || n > weights.length)
						weights = new float[n];

					float sum = 0;

					for (int i = 0; i < n; i++) {
						weights[i] = weight(edges.get(i));
						sum += weights[i];
					}

					for (int i = 0; i < n; ++i)
						weights[i] /= sum;

					float r = random.nextFloat();
					float s = 0;

					for (int i = 0; i < n; i++) {
						s += weights[i];

						if (r < s) {
							cross(edges.get(i));
							i = n;
						}
					}
				} else {
					cross(edges.get(random.nextInt(n)));
				}
			}
		}

		protected void jump() {
			current = randomNode(graph, random);
			jumpCount++;
		}

		protected void cross(Edge e) {
			current = e.getOpposite(current);
			addPass(e, current);
			addToTabu(current);
		}

		protected void addPass(Edge e, Node n) {
			e.setAttribute(passesAttribute, e.getNumber(passesAttribute) + 1);
			n.setAttribute(passesAttribute, n.getNumber(passesAttribute) + 1);
		}

		protected void addToTabu(Node node) {
			if (entityMemory > 0) {
				memory.addFirst(node);

				if (memory.size() > entityMemory)
					memory.removeLast();
			}
		}

		protected boolean tabu(Node node) {
			if (node.hasAttribute("tabu"))
				return true;

			if (entityMemory > 0) {
				if (memory == null)
					memory = new LinkedList<Node>();

				int n = memory.size();

				for (int i = 0; i < n; i++) {
					if (node == memory.get(i))
						return true;
				}
			}

			return false;
		}
	}

	public class TabuTimedEntity extends TabuEntity {
		protected static final float SPEED = 1000;

		protected float crossing = 0;

		public TabuTimedEntity(Node start) {
			super(start);
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

	// Graph listener

	@Override
	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		Edge edge = graph.getEdge(edgeId);

		if (edge != null)
			edge.addAttribute(passesAttribute, 0.0);
	}

	@Override
	public void nodeAdded(String graphId, long timeId, String nodeId) {
		Node node = graph.getNode(nodeId);

		node.addAttribute(passesAttribute, 0.0);
	}
}