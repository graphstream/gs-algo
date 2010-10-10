/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm;

import static org.graphstream.algorithm.Parameter.processParameters;

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
 * This length can be the absolute length of the path ( a path with 3 edges has
 * a length of 3), it can also be computed considering other constraints
 * situated on the edges or on the nodes.
 * </p>
 * <p>
 * Note that Dijkstra's algorithm only computes with non-negative values.
 * </p>
 * <p>
 * 
 * @complexity O(n^2 + m) with n the number of nodes and m the number of edges.
 * 
 * @author Antoine Dutot
 * @author Yoann Pigné
 */
public class Dijkstra implements Algorithm {
	/**
	 * Graph being used on computation.
	 */
	@DefineParameter(name = "graph")
	protected Graph graph;

	/**
	 * Source node of last computation.
	 */
	protected Node source;

	/**
	 * Id of the source node which will be used on the next computation.
	 */
	@DefineParameter(name = "sourceNodeId")
	protected String sourceNodeId = null;

	/**
	 * object-level unique string that identifies tags of this instance on a
	 * graph.
	 */
	protected String parentEdgesString;

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
	@DefineParameter(name = "attribute")
	protected String attribute;

	/**
	 * The kind of element observed in the graph.
	 */
	@DefineParameter(name = "element")
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
		this.parentEdgesString = this.toString() + "/ParentEdges";
		this.distances = new Hashtable<Node, Double>();
		this.length = new Hashtable<Node, Double>();

		this.attribute = attribute;
		this.element = element;
		this.sourceNodeId = sourceNodeId;
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
				.getAttribute(parentEdgesString);
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
					.getAttribute(parentEdgesString);
			if (list == null) {
				noPath = true;
			} else {
				Edge parentEdge = list.get(0);

				// --- DEBUG ---
				// if( parentEdge == null )
				// {
				// System.out.println( "parentEdge is null, v=" + v.toString() +
				// " source=" + source.toString() );
				// }

				p.add(v, parentEdge);
				v = parentEdge.getOpposite(v);
			}
		}
		return p;
	}

	/**
	 * Returns the weight of the shortest path tree : The of of the distance of
	 * all the nodes.
	 * 
	 * @return the sum of the distances.
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
					.getAttribute(parentEdgesString);
			if (list != null)
				treeEdges.addAll(list);
		}

		return treeEdges;
	}

	/**
	 * <h2 style="color:#F16454;" >WARNING</h2>
	 * <p style="background-color:#F1C4C4; color:black;">
	 * This method tries to construct ALL the possible paths form the source to
	 * <code>end</code>. This may result in a huge number of paths, you may even
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
	public List<Path> getPathSetShortestPaths(Node end) {
		ArrayList<Path> paths = new ArrayList<Path>();
		pathSetShortestPath_facilitate(end, new Path(), paths);
		return paths;
	}

	@SuppressWarnings("unchecked")
	private void pathSetShortestPath_facilitate(Node current, Path path,
			List<Path> paths) {
		if (current != source) {
			Node next = null;
			ArrayList<? extends Edge> parentEdges = (ArrayList<? extends Edge>) current
					.getAttribute(parentEdgesString);
			while (current != source && parentEdges.size() == 1) {
				Edge e = parentEdges.get(0);
				next = e.getOpposite(current);
				path.add(current, e);
				current = next;
				parentEdges = (ArrayList<? extends Edge>) current
						.getAttribute(parentEdgesString);
			}
			if (current != source) {
				for (Edge e : parentEdges) {
					Path p = path.getACopy();
					p.add(current, e);
					pathSetShortestPath_facilitate(e.getOpposite(current), p,
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
	 *         the source node. Returns null if target is the same � the source
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
	public void init(Parameter... params) {
		try {
			processParameters(this, params);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
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
			v.removeAttribute(parentEdgesString);
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
							val = ((Number) runningEdge.getAttribute(attribute))
									.doubleValue();
						} else {
							val = ((Number) neighborNode
									.getAttribute(attribute)).doubleValue();
						}
					}
					if (val < 0) {
						throw new NumberFormatException(
								"Attribute \""
										+ attribute
										+ "\" has a negative value on element "
										+ (element == Element.edge ? runningEdge
												.toString() : neighborNode
												.toString()));
					}
					dist = (distances.get(runningNode) + val);
					len = (int) (length.get(runningNode) + 1);

					if (priorityList.containsKey(neighborNode)) {
						if (dist <= distances.get(neighborNode)) {
							if (dist == distances.get(neighborNode)) {
								((ArrayList<Edge>) neighborNode
										.getAttribute(parentEdgesString))
										.add(runningEdge);
							} else {
								ArrayList<Edge> parentEdges = new ArrayList<Edge>();
								parentEdges.add(runningEdge);
								neighborNode.addAttribute(parentEdgesString,
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
						neighborNode.addAttribute(parentEdgesString,
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
