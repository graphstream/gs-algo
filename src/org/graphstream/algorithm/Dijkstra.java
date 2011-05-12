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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

/**
 * <p>
 * Dijkstra's algorithm is a greedy algorithm that solves the single-source
 * shortest path problem for a directed graph with non negative edge weights (<a
 * href="http://en.wikipedia.org/wiki/Dijkstra%27s_algorithm">Wikipedia</a>).
 * </p>
 * <p>
 * This length can be the absolute length of the path (a path with 3 edges has a
 * length of 3), it can also be computed considering other constraints situated
 * on the edges or on the nodes.
 * </p>
 * <p>
 * Note that Dijkstra's algorithm only computes with non-negative values.
 * </p>
 * 
 * <h2>Usage</h2>
 * <p>
 * The classical usage of this class takes place in 4 steps.
 * </p>
 * <ol>
 * <li>Definition of a Dijkstra instance with parameters needed for the
 * initialization.</li>
 * <li>Initialization of the algorithm with a graph through the
 * {@link #init(Graph)} method from the
 * {@link org.graphstream.algorithm.Algorithm} interface.</li>
 * <li>Computation of the shortest path tree with the {@link #compute()} method
 * from the {@link org.graphstream.algorithm.Algorithm} interface</li>
 * <li>Retrieving of shortest paths for given destinations with the
 * {@link #getShortestPath(Node)} method for instance.</li>
 * </ol>
 * <p>
 * The creation of the Dijkstra instance is done with the
 * {@link #Dijkstra(Element, String, String)} constructor by giving 3
 * parameters:
 * </p>
 * <ul>
 * <li>First, the type of element selected for the computing of shortest paths
 * (node or edge from {@link org.graphstream.algorithm.Dijkstra.Element}).</li>
 * <li>Second, the key string of the attribute used for the weight computation.</li>
 * <li>The third parameter is the id of the source node the shortest tree will
 * be constructed for.</li>
 * </ul>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * import java.io.ByteArrayInputStream;
 * import java.io.IOException;
 * 
 * import org.graphstream.algorithm.Dijkstra;
 * import org.graphstream.algorithm.Dijkstra.Element;
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * import org.graphstream.stream.file.FileSourceDGS;
 * 
 * //     B-(1)-C
 * //    /       \
 * //  (1)       (10)
 * //  /           \
 * // A             F
 * //  \           /
 * //  (1)       (1)
 * //    \       /
 * //     D-(1)-E
 * 
 * public class DijkstraTest {
 * 	static String my_graph = "DGS004\n" 
 * 			+ "my 0 0\n" 
 * 			+ "an A \n" 
 * 			+ "an B \n"
 * 			+ "an C \n" 
 * 			+ "an D \n" 
 * 			+ "an E \n" 
 * 			+ "an F \n"
 * 			+ "ae AB A B weight:1 \n" 
 * 			+ "ae AD A D weight:1 \n"
 * 			+ "ae BC B C weight:1 \n" 
 * 			+ "ae CF C F weight:10 \n"
 * 			+ "ae DE D E weight:1 \n" 
 * 			+ "ae EF E F weight:1 \n";
 * 
 * 	public static void main(String[] args) throws IOException {
 * 		Graph graph = new DefaultGraph("Dijkstra Test");
 * 		ByteArrayInputStream bs = new ByteArrayInputStream(my_graph.getBytes());
 * 
 * 		FileSourceDGS source = new FileSourceDGS();
 * 		source.addSink(graph);
 * 		source.readAll(bs);
 * 
 * 		Dijkstra dijkstra = new Dijkstra(Element.edge, "weight", "A");
 * 		dijkstra.init(graph);
 * 		dijkstra.compute();
 * 
 * 		System.out.println(dijkstra.getShortestPath(graph.getNode("F")));
 * 	}
 * }
 * </pre>
 * 
 * 
 * <h2>Some Features</h2>
 * 
 * 
 * <h3>Shortest Path Value</h3>
 * 
 * 
 * If you only need to know the value of a shortest path and are not interested
 * in the path itself, then the {@link #getShortestPathValue(Node)} method with its
 * given target element is for you.
 * 
 * <h3>ShortestPath Length</h3>
 * 
 * 
 * If you are interested in the length of a path in terms of elements nodes or
 * edges rather than in the path itself or its value, then you can use
 * {@link #getShortestPathLength(Node)}.
 * 
 * <h3>Static Access</h3>
 * 
 * 
 * The {@link #getShortestPath(String, Node, Node)} is a static method to get
 * shortest paths from a graph already computed with Dijkstra.
 * 
 * This means the given nodes (source and target) should belong to a graph that
 * contains attributes with the given "identifier" as a key.
 * 
 * It allows to get rid of Dijkstra instances once computed. Since all the
 * useful information to retrieve a shortest path is stored in the graph, a
 * Dijkstra instance is not necessary. You will use this method if you are
 * computing a lot of instances of Dijkstra in a raw and want to lower the
 * memory consumption.
 * 
 * 
 * The 3 following parameters are necessary to this method:
 * 
 * <ul>
 * <li>"identifier": a unique string used to identify attributes that represent
 * the solution of a Dijkstra computation (a shortest tree rooted in the source
 * node/edge).</li>
 * <li> "source": the source node for what a Dijkstra object as already
 * been initialized.</li>
 * <li> ``target``: the Target node to be sought in the graph
 * according the given identifier.</li>
 * </ul>
 * 
 * 
 * <h3>All Shortest Paths</h3>
 * 
 * 
 * It is possible that multiple path in a graph are the shortest ones.
 * Especially if the graph is a grid and the weight is unitary. The
 * {@link #getAllShortestPaths(Node)} tries to construct all the possible shortest
 * paths linking the computed source to the given destination.
 * 
 * 
 * <h3>All Edges involved</h3>
 * 
 * 
 * The {@link #getEdgeSetShortestPaths(Node)} methods returns the set of edges that
 * are involved in the shortest paths. If Several paths are the shortest paths
 * and some edges belong to several of these paths, then those edge will appear
 * only once. In other words, the return List is a set according to the
 * mathematical definition of a set (no repetition, no order).
 * 
 * 
 * 
 * @complexity O(n log(n) + m) with n the number of nodes and m the number of
 *             edges.
 * 
 * @reference E. W. Dijkstra. A note on two problems in connexion with graphs.
 *            Numerische Mathematik, 1:269–271, 1959.
 * 
 * @author Antoine Dutot
 * @author Yoann Pigné
 */
public class Dijkstra implements Algorithm {

	/**
	 * Graph being used on computation.
	 */
	protected Graph graph;

	/**
	 * Source node of last computation.
	 */
	protected Node source;

	/**
	 * Id of the source node which will be used on the next computation.
	 */
	protected String sourceNodeId = null;

	/**
	 * object-level unique string that identifies tags of this instance on a
	 * graph.
	 */
	protected String identifier;

	/**
	 * Distances depending on the observed attribute.
	 */
	protected Hashtable<Node, Double> distances;

	/**
	 * Lengths in number of links.
	 */
	protected Hashtable<Node, Double> length;

	/**
	 * The attribute considered for the distance computation.
	 */
	protected String attribute;

	/**
	 * The kind of element observed in the graph.
	 */
	protected Element element;

	/**
	 * Same as {@link #Dijkstra(Element, String, String)} but source node id
	 * will be set to null.
	 * 
	 * @param element
	 *            The kind of element observed in the graph.
	 * @param attribute
	 *            The attribute considered for the distance computation.
	 */
	public Dijkstra(Element element, String attribute) {
		this(element, attribute, null);
	}

	/**
	 * Computes the Dijkstra's algorithm on the given graph starting from the
	 * given source node, considering the given attribute locates on the given
	 * kind of elements (nodes or edges).
	 * 
	 * @param element
	 *            The kind of element observed in the graph.
	 * @param attribute
	 *            The attribute considered for the distance computation.
	 * @param sourceNodeId
	 *            Id of the root node of the shortest path tree.
	 */
	public Dijkstra(Element element, String attribute, String sourceNodeId) {
		this.identifier = this.toString() + "/identifier";
		this.distances = new Hashtable<Node, Double>();
		this.length = new Hashtable<Node, Double>();

		this.attribute = attribute;
		this.element = element;
		this.sourceNodeId = sourceNodeId;
	}

	/**
	 * The Identifier is a string which is used to mark the graph with the
	 * computed Dijkstra's shortest tree.
	 * 
	 * This string is the key of the attribute that the algorithm sets in the
	 * elements of the graph. The main advantage of this identifier is that once
	 * Dijkstra is computed on a graph for one given source, it is then only
	 * necessary to browse the graph according to this identifier; the Dijkstra
	 * instance is no longer necessary since all the useful information is
	 * already in the graph.
	 * 
	 * <b>Warning:</b> If multiple instances of Dijkstra are run on the same
	 * graph (with different sources or different wait values) identifiers have
	 * to be unique! By default the identified is set to be unique.
	 * 
	 * @see #getShortestPath(String,Node,Node)
	 * 
	 * @return the unique identifier of this instance of Dijkstra.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Set the unique identifier for this instance of Dijkstra.
	 * 
	 * @see #getIdentifier()
	 * @see #getShortestPath(String,Node,Node)
	 * 
	 * @param identifier
	 *            the unique identifier to set
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	/**
	 * Set the id of the source node which will be used on the computation.
	 * 
	 * @param sourceNodeId
	 *            id of the source node
	 */
	public void setSource(String sourceNodeId) {
		this.sourceNodeId = sourceNodeId;
	}

	@SuppressWarnings("unchecked")
	private void facilitate_getShortestPaths(List<Edge> g, Node v) {
		if (v == source) {
			return;
		}
		ArrayList<Edge> list = (ArrayList<Edge>) v
				.getAttribute(identifier);
		if (list == null) {
			// System.err.println( "The list of parent Edges  is null, v=" +
			// v.toString() + " source=" + source.toString() );
		} else {
			for (Edge l : list) {
				if (!g.contains(l)) {
					g.add(l);
					facilitate_getShortestPaths(g, l.getOpposite(v));
				}
			}
		}
	}

	/**
	 * Returns the shortest path between the source node and one given target
	 * one. If multiple shortest paths exist, a of them is returned at random.
	 * 
	 * @param target
	 *            the target of the shortest path starting at the source node
	 *            given in the constructor.
	 * @return A {@link org.graphstream.graph.Path} object that constrains the
	 *         list of nodes and edges that constitute it.
	 */
	@SuppressWarnings("unchecked")
	public Path getShortestPath(Node target) {
		Path p = new Path();
		if (target == source) {
			return p;
		}
		boolean noPath = false;
		Node v = target;
		while (v != source && !noPath) {
			ArrayList<? extends Edge> list = (ArrayList<? extends Edge>) v
					.getAttribute(identifier);
			if (list == null) {
				noPath = true;
			} else {
				Edge parentEdge = list.get(0);
				p.add(v, parentEdge);
				v = parentEdge.getOpposite(v);
			}
		}
		return p;
	}

	/**
	 * A Static method to get shortest paths from a graph already computed with
	 * Dijkstra.
	 * 
	 * This means that given nodes (source and target) should belong to a graph
	 * that contains attributes with the given <code>identifier</code> as a key.
	 * 
	 * <h2>What is this method useful for?</h2>
	 * 
	 * It allows to get rid of the Dijkstra instances once computed. Since all
	 * the useful information to retrieve a shortest path is stored in the
	 * graph, a Dijkstra instance is not necessary. You will use this method if
	 * you are computing a lot of instances of Dijkstra in a raw and want to
	 * lower the memory consumption.
	 * 
	 * 
	 * @see #getIdentifier()
	 * 
	 * @param identifier
	 *            The unique string used to identify attributes that represent
	 *            the solution of a Dijkstra computation (a shortest tree rooted
	 *            in the source node).
	 * @param source
	 *            The source node for what a Dijkstra object as already been
	 *            initialized.
	 * @param target
	 *            The Target node to be sought in the graph according the given
	 *            identifier.
	 * @return a shortest path linking the source to the target. The returned
	 *         path can be empty, if the no path can link the source and the
	 *         target.
	 */
	@SuppressWarnings("unchecked")
	public static Path getShortestPath(String identifier, Node source,
			Node target) {
		Path p = new Path();
		if (target == source) {
			return p;
		}
		boolean noPath = false;
		Node v = target;
		while (v != source && !noPath) {
			ArrayList<? extends Edge> list = (ArrayList<? extends Edge>) v
					.getAttribute(identifier);
			if (list == null) {
				noPath = true;
			} else {
				Edge parentEdge = list.get(0);
				p.add(v, parentEdge);
				v = parentEdge.getOpposite(v);
			}
		}
		return p;
	}

	/**
	 * Returns the weight of the shortest path tree : The sum of the weight of
	 * all the elements (nodes of edges, depending on the initialization).
	 * 
	 * @return the sum of the weights of the all shortest path tree.
	 */
	public double treeWeight() {
		double weight = 0;
		for (Double d : distances.values()) {
			weight += d.doubleValue();
		}
		return weight;
	}

	/**
	 * Return the set of edges involved in the shortest path tree.
	 * 
	 * @return The set of edges.
	 */
	@SuppressWarnings("unchecked")
	public List<Edge> treeEdges() {
		ArrayList<Edge> treeEdges = new ArrayList<Edge>();
		Iterator<Node> it = distances.keySet().iterator();
		while (it.hasNext()) {
			Node n = it.next();
			ArrayList<Edge> list = (ArrayList<Edge>) n
					.getAttribute(identifier);
			if (list != null)
				treeEdges.addAll(list);
		}

		return treeEdges;
	}

	/**
	 * This method tries to construct <b>ALL</b> the possible paths form the source to
	 * <code>end</code>.
	 * 
	 * <h2 style="color:#F16454;" >WARNING</h2>
	 * <p style="background-color:#F1C4C4; color:black;">
	 *  This may result in a huge number of paths, you may even
	 * crash the VM because of memory consumption.
	 * </p>
	 * 
	 * @param end
	 *            The destination to which shortest paths are computed.
	 * @return a list of shortest paths given with
	 *         {@link org.graphstream.graph.Path} objects. The List is empty if
	 *         <code>end</code> is not in the same connected component as the
	 *         source node.
	 */
	public List<Path> getAllShortestPaths(Node end) {
		ArrayList<Path> paths = new ArrayList<Path>();
		allShortestPath_facilitate(end, new Path(), paths);
		return paths;
	}

	@SuppressWarnings("unchecked")
	private void allShortestPath_facilitate(Node current, Path path,
			List<Path> paths) {
		if (current != source) {
			Node next = null;
			ArrayList<? extends Edge> parentEdges = (ArrayList<? extends Edge>) current
					.getAttribute(identifier);
			while (current != source && parentEdges.size() == 1) {
				Edge e = parentEdges.get(0);
				next = e.getOpposite(current);
				path.add(current, e);
				current = next;
				parentEdges = (ArrayList<? extends Edge>) current
						.getAttribute(identifier);
			}
			if (current != source) {
				for (Edge e : parentEdges) {
					Path p = path.getACopy();
					p.add(current, e);
					allShortestPath_facilitate(e.getOpposite(current), p,
							paths);

				}
			}
		}
		if (current == source) {
			paths.add(path);
		}
	}

	/**
	 * 
	 * Synonym to {@link #getEdgeSetShortestPaths(Node)}.
	 * 
	 * @deprecated
	 * @see #getEdgeSetShortestPaths(Node)
	 * @param target
	 *            The target node for the shortest path.
	 * @return A list of edges.
	 */
	@Deprecated
	public List<Edge> getShortestPaths(Node target) {
		return getEdgeSetShortestPaths(target);
	}

	/**
	 * Returns a set of edges that compose the shortest path. If more than one
	 * path is the shortest one, the edges are included in the returned set of
	 * edges.
	 * 
	 * @param target
	 *            The endpoint of the path to compute from the source node given
	 *            in the constructor.
	 * @return The set of edges that belong the the solution. Returns an empty
	 *         list if the target node is not in the same connected component as
	 *         the source node. Returns null if target is the same as the source
	 *         node.
	 */
	public List<Edge> getEdgeSetShortestPaths(Node target) {
		if (target == source) {
			// System.out.println( "end=source !!!" );
			return null;
		}
		List<Edge> g = new ArrayList<Edge>();
		Node v = target;
		facilitate_getShortestPaths(g, v);
		return g;
	}

	/**
	 * Returns the value of the shortest path between the source node and the
	 * given target according to the attribute specified in the constructor. If
	 * <code>target</code> is not in the same connected component as the source
	 * node, then the method returns <code>Double.POSITIVE_INFINITY</code>.
	 * 
	 * @param target
	 *            The endpoint of the path to compute from the source node given
	 *            in the constructor.
	 * @return A numerical value that represent the distance of the shortest
	 *         path.
	 */
	public double getShortestPathValue(Node target) {
		Double d = distances.get(target);
		if (d != null)
			return d;
		return Double.POSITIVE_INFINITY;
	}

	/**
	 * Returns the number of edges in the shortest path from the source to the
	 * given target.If <code>target</code> is not in the same connected
	 * component as the source node, then the method returns
	 * <code>Double.POSITIVE_INFINITY</code>.
	 * 
	 * @param target
	 *            The node to compute the shortest path to.
	 * @return the number of edges in the shortest path.
	 */
	public double getShortestPathLength(Node target) {
		if (length.get(target) == null)
			return Double.POSITIVE_INFINITY;
		return length.get(target);
	}

	/**
	 * This enumeration help identifying the kind of element to be used to
	 * compute the shortest path.
	 */
	public static enum Element {
		edge, node
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Computes the Dijkstra's algorithm on the given graph starting from the
	 * given source node, considering the given attribute locates on the given
	 * kind of elements (nodes or edges).
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	@SuppressWarnings("unchecked")
	public void compute() {
		distances.clear();
		length.clear();

		source = graph.getNode(sourceNodeId);

		ArrayList<Node> computed = new ArrayList<Node>();

		double dist;
		double len;

		Node runningNode;
		Node neighborNode;

		PriorityList<Node> priorityList = new PriorityList<Node>();
		priorityList.insertion(source, 0.0);

		distances.put(source, 0.0);
		length.put(source, 0.0);

		// initialization

		for (Node v : graph) {
			v.removeAttribute(identifier);
		}

		while (!priorityList.isEmpty()) {
			runningNode = priorityList.lire(0);

			for (Edge runningEdge : runningNode.getLeavingEdgeSet()) {
				neighborNode = runningEdge.getOpposite(runningNode);

				if (!computed.contains(neighborNode)) {
					double val = 0;
					if (attribute == null) {
						val = 1.0;
					} else {
						if (element == Element.edge) {
							if (runningEdge.hasAttribute(attribute)) {
								val = ((Number) runningEdge
										.getAttribute(attribute)).doubleValue();
							} else {
								val = 1.0;
							}
						} else {
							if (neighborNode.hasAttribute(attribute)) {
								val = ((Number) neighborNode
										.getAttribute(attribute)).doubleValue();
							} else {
								val = 1.0;
							}
						}
					}
					if (val < 0) {
						throw new NumberFormatException("Attribute \""
								+ attribute
								+ "\" has a negative value on element "
								+ (element == Element.edge ? runningEdge
										.toString() : neighborNode.toString()));
					}
					dist = (distances.get(runningNode) + val);
					len = (int) (length.get(runningNode) + 1);

					if (priorityList.containsKey(neighborNode)) {
						if (dist <= distances.get(neighborNode)) {
							if (dist == distances.get(neighborNode)) {
								((ArrayList<Edge>) neighborNode
										.getAttribute(identifier))
										.add(runningEdge);
							} else {
								ArrayList<Edge> parentEdges = new ArrayList<Edge>();
								parentEdges.add(runningEdge);
								neighborNode.addAttribute(identifier,
										parentEdges);

								distances.put(neighborNode, dist);
								// neighborNode.addAttribute( "label",
								// neighborNode.getId() + " - " + dist );
								length.put(neighborNode, len);

								priorityList.suppression(neighborNode);
								priorityList.insertion(neighborNode, dist);
							}
						}
					} else {
						priorityList.insertion(neighborNode, dist);
						distances.put(neighborNode, dist);
						length.put(neighborNode, len);
						ArrayList<Edge> parentEdges = new ArrayList<Edge>();
						parentEdges.add(runningEdge);
						neighborNode.addAttribute(identifier,
								parentEdges);

					}

				}
			}
			priorityList.suppression(runningNode);
			computed.add(runningNode);
		}
	}

}

class PriorityList<E> {

	ArrayList<E> objets;

	ArrayList<Double> priorites;

	int taille;

	public PriorityList() {
		objets = new ArrayList<E>();
		priorites = new ArrayList<Double>();
		taille = 0;
	}

	public boolean containsKey(E objet) {
		boolean contient = false;
		if (objets.contains(objet)) {
			contient = true;
		}
		return contient;
	}

	public void insertion(E element, double prio) {
		boolean trouve = false;
		int max = priorites.size();
		int i = 0;
		while ((!trouve) && (i < max)) {
			if (priorites.get(i) > prio) {
				trouve = true;
			} else {
				i++;
			}
		}
		if (i == max) {
			objets.add(element);
			priorites.add(prio);
		} else {
			objets.add(i, element);

			// MODIF ICI !
			// priorites.add(prio);
			priorites.add(i, prio);
		}
	}

	public boolean isEmpty() {
		boolean vide = false;
		if (objets.size() == 0) {
			vide = true;
		}
		return vide;
	}

	public E lire(int position) {
		return objets.get(position);
	}

	public int size() {
		return objets.size();
	}

	public void suppression(E element) {
		int position = objets.lastIndexOf(element);
		objets.remove(position);
		priorites.remove(position);
	}

	@Override
	public String toString() {
		String laliste = new String(" -------- Liste --------- \n");
		for (int i = 0; i < objets.size(); i++) {
			laliste = laliste + objets.get(i).toString() + ":::"
					+ priorites.get(i).toString() + "\n";
		}
		return laliste;
	}
}
