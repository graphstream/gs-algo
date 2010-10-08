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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Set;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Compute the "betweeness" centrality of each vertex of a given graph.
 * 
 * This algorithm, by default, stores the centrality values for each edge inside
 * the "Cb" attribute. You can change this attribute name at construction time.
 * 
 * This algorithm does not accept multi-graphs (p-graphs with p>1).
 * 
 * By default the algorithm performs on a graph considered as not weighted with
 * complexity O(nm). You can specify that the graph edges contain weights in
 * which case the algorithm complexity is O(nm + n^2 log n). By default the
 * weight attribute name is "weight". You can change this using the dedicated
 * constructor or the {@link #setWeightAttributeName(String)} method.
 * 
 * The result of the computation is stored on each node inside the "Cb"
 * attribute. You can change the name of this attribute using the dedicated
 * constructor or the {@link #setCentralityAttributeName(String)} method.
 * 
 * This is based on the algorithm described in "A Faster Algorithm for
 * Betweenness Centrality", Ulrik Brandes, Journal of Mathematical Sociology,
 * 2001 (available on Citeseer).
 */
public class BetweennessCentrality implements Algorithm {
	// Attribute

	protected static double INFINITY = 1000000.0;

	protected String centralityAttributeName = "Cb";

	protected String predAttributeName = "brandes.P";

	protected String sigmaAttributeName = "brandes.sigma";

	protected String distAttributeName = "brandes.d";

	protected String deltaAttributeName = "brandes.delta";

	protected String weightAttributeName = "weight";

	protected boolean unweighted = true;

	protected Graph graph;

	// Construction

	/**
	 * New centrality algorithm that will perform as if the graph was
	 * unweighted. By default the centrality will be stored in a "Cb" attribute
	 * on each node.
	 */
	public BetweennessCentrality() {
	}

	/**
	 * New centrality algorithm that will perform as if the graph was
	 * unweighted. The centrality for each node will be stored in an attribute
	 * whose name is specified by the <code>centralityAttributeName</code>
	 * argument.
	 * 
	 * @param centralityAttributeName
	 *            The name of the attribute used to store the result of the
	 *            algorithm on each node.
	 */
	public BetweennessCentrality(String centralityAttributeName) {
		this.centralityAttributeName = centralityAttributeName;
		this.unweighted = true;
	}

	/**
	 * New centrality algorithm that will perform on the graph using the edges
	 * weights to compute the shortest paths if <code>weighted</code> is true.
	 * The weights must be stored in an attribute named "weight". If there are
	 * no weights, the edge is considered to have weight one.
	 * 
	 * @param weighted
	 *            If true the graph is considered weighted.
	 */
	public BetweennessCentrality(boolean weighted) {
		this.unweighted = !weighted;
	}

	/**
	 * New centrality algorithm that will perform on a weighted graph, taking
	 * the weight of each edge in the given <code>weightAttributeName</code>.
	 * The result of the algorithm is stored for each node using the given
	 * <code>centralityAttributeName</code>. If an edge has no weight attribute,
	 * it is considered as having a weight of one.
	 * 
	 * @param centralityAttributeName
	 *            Name to use to store the centrality results on each node.
	 * @param weightAttributeName
	 *            Name to use to retrieve the edge weights.
	 */
	public BetweennessCentrality(String centralityAttributeName,
			String weightAttributeName) {
		this.centralityAttributeName = centralityAttributeName;
		this.weightAttributeName = weightAttributeName;
		this.unweighted = false;
	}

	// Access

	/**
	 * Name of the attribute used to retrieve weights on edges.
	 */
	public String getWeightAttributeName() {
		return weightAttributeName;
	}

	/**
	 * Name of the attribute used to store centrality values on nodes.
	 */
	public String getCentralityAttributeName() {
		return centralityAttributeName;
	}

	// Command

	/**
	 * Specify the name of the weight attribute to retrieve edge attributes.
	 * This automatically set the algorithm to perform on the graph as if it was
	 * weighted.
	 */
	public void setWeightAttributeName(String weightAttributeName) {
		unweighted = false;
		this.weightAttributeName = weightAttributeName;
	}

	/**
	 * Consider all the edges to have the weight.
	 */
	public void setUnweighted() {
		unweighted = true;
	}

	/**
	 * Specify the name of the attribute used to store the computed centrality
	 * values for each node.
	 */
	public void setCentralityAttributeName(String centralityAttributeName) {
		this.centralityAttributeName = centralityAttributeName;
	}

	/**
	 * Setup the algorithm to work on the given graph.
	 */
	public void init(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Compute the betweenness centrality on the given graph for each node. The
	 * result is by default stored in the "Cb" attribute on each node.
	 */
	public void compute() {
		if (graph != null) {
			betweennessCentrality(graph);
		}
	}

	/**
	 * Compute the betweenness centrality on the given graph for each node. This
	 * method is equivalent to a call in sequence to the two methods
	 * {@link #init(Graph)} then {@link #compute()}.
	 */
	public void betweennessCentrality(Graph graph) {
		init(graph);
		initAllNodes(graph);

		for (Node s : graph) {
			PriorityQueue<Node> S = null;

			if (unweighted)
				S = simpleExplore(s, graph);
			else
				S = dijkstraExplore(s, graph);

			// The really new things in the Brandes algorithm are here:

			while (!S.isEmpty()) {
				Node w = S.poll();

				for (Node v : predecessorsOf(w)) {
					setDelta(v, delta(v)
							+ ((sigma(v) / sigma(w)) * (1.0 + delta(w))));
					if (w != s) {
						setCentrality(w, centrality(w) + delta(w));
					}
				}
			}
		}
	}

	/**
	 * Compute single-source multiple-targets shortest paths on an unweighted
	 * graph.
	 * 
	 * @param source
	 *            The source node.
	 * @param graph
	 *            The graph.
	 * @return A priority queue of explored nodes with sigma values usable to
	 *         compute the centrality.
	 */
	protected PriorityQueue<Node> simpleExplore(Node source, Graph graph) {
		LinkedList<Node> Q = new LinkedList<Node>();
		PriorityQueue<Node> S = new PriorityQueue<Node>(graph.getNodeCount(),
				new BrandesNodeComparatorLargerFirst());

		setupAllNodes(graph);
		Q.add(source);
		setSigma(source, 1.0);
		setDistance(source, 0.0);

		while (!Q.isEmpty()) {
			Node v = Q.removeFirst();
			S.add(v);

			Iterator<? extends Node> ww = v.getNeighborNodeIterator();

			while (ww.hasNext()) {
				Node w = ww.next();

				if (distance(w) == INFINITY) {
					Q.add(w);
					setDistance(w, distance(v) + 1);
				}

				if (distance(w) == (distance(v) + 1.0)) {
					setSigma(w, sigma(w) + sigma(v));
					addToPredecessorsOf(w, v);
				}
			}
		}

		return S;
	}

	/**
	 * Compute single-source multiple-targets paths on a weighted graph.
	 * 
	 * @param source
	 *            The source node.
	 * @param graph
	 *            The graph.
	 * @return A priority queue of explored nodes with sigma values usable to
	 *         compute the centrality.
	 */
	protected PriorityQueue<Node> dijkstraExplore(Node source, Graph graph) {
		PriorityQueue<Node> S = new PriorityQueue<Node>(graph.getNodeCount(),
				new BrandesNodeComparatorLargerFirst());
		PriorityQueue<Node> Q = new PriorityQueue<Node>(graph.getNodeCount(),
				new BrandesNodeComparatorSmallerFirst());

		setupAllNodes(graph);

		setDistance(source, 0.0);
		setSigma(source, 1.0);

		Q.add(source);

		while (!Q.isEmpty()) {
			Node u = Q.poll();

			if (distance(u) < 0.0) { // XXX Can happen ??? XXX
				Q.clear();
				throw new RuntimeException("negative distance ??");
			} else {
				S.add(u);

				Iterator<? extends Node> k = u.getNeighborNodeIterator();

				while (k.hasNext()) {
					Node v = k.next();
					double alt = distance(u) + weight(u, v);

					if (alt < distance(v)) {
						if (distance(v) == INFINITY) {
							setDistance(v, alt);
							Q.add(v);
							setSigma(v, sigma(v) + sigma(u)); // sigma(v)==0
																// always ?? XXX
						} else {
							setDistance(v, alt);
							setSigma(v, sigma(u));
						}
						replacePredecessorsOf(v, u);
					} else if (alt == distance(v)) {
						setSigma(v, sigma(v) + sigma(u));
						addToPredecessorsOf(v, u);
					}
				}
			}
		}

		return S;
	}

	protected double sigma(Node node) {
		return node.getNumber(sigmaAttributeName);
	}

	protected double distance(Node node) {
		return node.getNumber(distAttributeName);
	}

	protected double delta(Node node) {
		return node.getNumber(deltaAttributeName);
	}

	public double centrality(Node node) {
		return node.getNumber(centralityAttributeName);
	}

	@SuppressWarnings("all")
	protected Set<Node> predecessorsOf(Node node) {
		return (HashSet<Node>) node.getAttribute(predAttributeName);
	}

	protected void setSigma(Node node, double sigma) {
		node.setAttribute(sigmaAttributeName, sigma);
	}

	protected void setDistance(Node node, double distance) {
		node.setAttribute(distAttributeName, distance);
	}

	protected void setDelta(Node node, double delta) {
		node.setAttribute(deltaAttributeName, delta);
	}

	public void setCentrality(Node node, double centrality) {
		node.setAttribute(centralityAttributeName, centrality);
	}

	public void setWeight(Node from, Node to, double weight) {
		from.getEdgeToward(to.getId())
				.setAttribute(weightAttributeName, weight);
	}

	public double weight(Node from, Node to) {
		Edge edge = from.getEdgeToward(to.getId());

		if (edge != null) {
			if (edge.hasAttribute(weightAttributeName))
				return edge.getNumber(weightAttributeName);
			else
				return 1.0;
		} else {
			return 0.0;
		}
	}

	protected void replacePredecessorsOf(Node node, Node predecessor) {
		HashSet<Node> set = new HashSet<Node>();

		set.add(predecessor);
		node.setAttribute(predAttributeName, set);
	}

	@SuppressWarnings("all")
	protected void addToPredecessorsOf(Node node, Node predecessor) {
		HashSet<Node> preds = (HashSet<Node>) node
				.getAttribute(predAttributeName);

		preds.add(predecessor);
	}

	protected void initAllNodes(Graph graph) {
		for (Node node : graph) {
			node.addAttribute(centralityAttributeName, 0);
		}
	}

	protected void setupAllNodes(Graph graph) {
		for (Node node : graph) {
			node.addAttribute(predAttributeName, new HashSet<Node>());
			node.addAttribute(sigmaAttributeName, 0.0);
			node.addAttribute(distAttributeName, INFINITY);
			node.addAttribute(deltaAttributeName, 0.0);
		}
	}

	protected class BrandesNodeComparatorLargerFirst implements
			Comparator<Node> {
		public int compare(Node x, Node y) {
			// return (int) ( (distance(y)*1000.0) - (distance(x)*1000.0) );
			double yy = distance(y);
			double xx = distance(x);

			if (xx > yy)
				return -1;
			else if (xx < yy)
				return 1;
			else
				return 0;
		}
	}

	protected class BrandesNodeComparatorSmallerFirst implements
			Comparator<Node> {
		public int compare(Node x, Node y) {
			// return (int) ( (distance(x)*1000.0) - (distance(y)*1000.0) );
			double yy = distance(y);
			double xx = distance(x);

			if (xx > yy)
				return 1;
			else if (xx < yy)
				return -1;
			else
				return 0;
		}
	}
}