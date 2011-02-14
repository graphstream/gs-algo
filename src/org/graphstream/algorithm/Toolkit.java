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

import java.util.*;

import org.graphstream.graph.*;

/**
 * Lots of small often used algorithms on graphs.
 * 
 * <p>
 * Use this class with a static import. This should be a trait or a mixin in an
 * advanced language.
 * </p>
 */
public class Toolkit {
	// Access

	/**
	 * Compute the degree distribution of this graph. Each cell of the returned
	 * array contains the number of nodes having degree n where n is the index
	 * of the cell. For example cell 0 counts how many nodes have zero edges,
	 * cell 5 counts how many nodes have five edges. The last index indicates
	 * the maximum degree.
	 * 
	 * @complexity O(n) where n is the number of nodes.
	 */
	public static int[] degreeDistribution(Graph graph) {
		if (graph.getNodeCount() == 0)
			return null;

		int max = 0;
		int[] dd;
		int d;

		for (Node node : graph) {
			d = node.getDegree();

			if (d > max)
				max = d;
		}

		dd = new int[max + 1];

		for (Node node : graph) {
			d = node.getDegree();

			dd[d] += 1;
		}

		return dd;
	}

	/**
	 * Return a list of nodes sorted by degree, the larger first.
	 * 
	 * @return The degree map.
	 * @complexity O(n log(n)) where n is the number of nodes.
	 */
	public static ArrayList<Node> degreeMap(Graph graph) {
		ArrayList<Node> map = new ArrayList<Node>();

		for (Node node : graph)
			map.add(node);

		Collections.sort(map, new Comparator<Node>() {
			public int compare(Node a, Node b) {
				return b.getDegree() - a.getDegree();
			}
		});

		return map;
	}

	/**
	 * Returns the value of the average degree of the graph. A node with a loop
	 * edge has degree two.
	 * 
	 * @return The average degree of the graph.
	 * @complexity O(1).
	 */
	public static float averageDegree(Graph graph) {
		float m = graph.getEdgeCount() * 2;
		float n = graph.getNodeCount();

		if (n > 0)
			return m / n;

		return 0;
	}

	/**
	 * The density is the number of links in the graph divided by the total
	 * number of possible links.
	 * 
	 * @return The density of the graph.
	 * @complexity O(1)
	 */
	public static float density(Graph graph) {
		float m = (float) graph.getEdgeCount();
		float n = (float) graph.getNodeCount();

		if (n > 0)
			return ((2 * m) / (n * (n - 1)));

		return 0;
	}

	/**
	 * Returns the value of the degree average deviation of the graph.
	 * 
	 * @return The degree average deviation.
	 * @complexity O(n) where n is the number of nodes.
	 */
	public static float degreeAverageDeviation(Graph graph) {
		float average = averageDegree(graph);
		float sum = 0;

		for (Node node : graph) {
			float d = node.getDegree() - average;
			sum += d * d;
		}

		return (float) Math.sqrt(sum / (float) graph.getNodeCount());
	}

	/**
	 * Clustering coefficient for each node of the graph.
	 * 
	 * @return An array whose size correspond to the number of nodes, where each
	 *         element is the clustering coefficient of a node.
	 * @complexity at worse O(n d^2) where n is the number of nodes and d the
	 *             average or maximum degree of nodes.
	 */
	public static double[] clusteringCoefficients(Graph graph) {
		int n = graph.getNodeCount();

		if (n > 0) {
			int j = 0;
			double[] coefs = new double[n];

			for (Node node : graph)
				coefs[j++] = clusteringCoefficient(node);

			assert (j == n);

			return coefs;
		}

		return new double[0];
	}

	/**
	 * Average clustering coefficient of the whole graph. Average of each node
	 * individual clustering coefficient.
	 * 
	 * @return The average clustering coefficient.
	 * @complexity at worse O(n d^2) where n is the number of nodes and d the
	 *             average or maximum degree of nodes.
	 */
	public static double averageClusteringCoefficient(Graph graph) {
		int n = graph.getNodeCount();

		if (n > 0) {
			double cc = 0;

			for (Node node : graph)
				cc += clusteringCoefficient(node);

			return cc / n;
		}

		return 0;
	}

	/**
	 * Clustering coefficient for one node of the graph.
	 * 
	 * @param node
	 *            The node to compute the clustering coefficient for.
	 * @return The clustering coefficient for this node.
	 * @complexity O(d^2) where d is the degree of the given node.
	 */
	public static double clusteringCoefficient(Node node) {
		double coef = 0.0;
		int n = node.getDegree();

		if (n > 1) {
			// Collect the neighbour nodes.

			Node[] nodes = new Node[n];
			HashSet<Edge> set = new HashSet<Edge>();
			int i = 0;

			for (Edge edge : node.getEdgeSet())
				nodes[i++] = edge.getOpposite(node);

			// Count the number of edges between these nodes.

			for (i = 0; i < n; ++i) // For all neighbour nodes.
			{
				for (int j = 0; j < n; ++j) // For all other nodes of this
											// clique.
				{
					if (j != i) {
						Edge e = nodes[j].getEdgeToward(nodes[i].getId());

						if (e != null) {
							// if( ! set.contains( e ) )
							set.add(e);
						}
					}
				}
			}

			double ne = set.size();
			double max = (n * (n - 1)) / 2.0;

			coef = ne / max;
		}

		return coef;
	}

	/**
	 * Choose a node at random.
	 * 
	 * @return A node chosen at random, null if the graph is empty.
	 * @complexity at worse O(n) where n is the number of nodes.
	 */
	public static Node randomNode(Graph graph) {
		return randomNode(graph, new Random());
	}

	/**
	 * Choose a node at random.
	 * 
	 * @param random
	 *            The random number generator to use.
	 * @return A node chosen at random, null if the graph is empty.
	 * @complexity at worse O(n) where n is the number of nodes.
	 */
	public static Node randomNode(Graph graph, Random random) {
		int n = graph.getNodeCount();
		int r = random.nextInt(n);
		int i = 0;

		for (Node node : graph) {
			if (r == i)
				return node;
			i++;
		}

		return null;
	}

	/**
	 * Choose an edge at random.
	 * 
	 * @return An edge chosen at random. complexity at worse O(n) where n is the
	 *         number of edges.
	 */
	public static Edge randomEdge(Graph graph) {
		return randomEdge(graph, new Random());
	}

	/**
	 * Choose an edge at random.
	 * 
	 * @param random
	 *            The random number generator to use.
	 * @return An edge chosen at random, null if the graph has no edges.
	 *         complexity at worse O(n) where n is the number of edges.
	 */
	public static Edge randomEdge(Graph graph, Random random) {
		int n = graph.getEdgeCount();
		int r = random.nextInt(n);
		int i = 0;

		for (Edge edge : graph.getEachEdge()) {
			if (r == i)
				return edge;
			i++;
		}

		return null;
	}

	/**
	 * Choose an edge at random from the edges connected to the given node.
	 * 
	 * @return An edge chosen at random, null if the node has no edges.
	 *         complexity at worse O(n) where n is the number of edges.
	 */
	public static Edge randomEdge(Node node) {
		return randomEdge(node, new Random());
	}

	/**
	 * Choose an edge at random from the entering edges connected to the given
	 * node.
	 * 
	 * @return An edge chosen at random, null if the node has no entering edges.
	 *         complexity at worse O(n) where n is the number of edges.
	 */
	public static Edge randomInEdge(Node node) {
		return randomInEdge(node, new Random());
	}

	/**
	 * Choose an edge at random from the leaving edges connected to the given
	 * node.
	 * 
	 * @return An edge chosen at random, null if the node has no leaving edges.
	 *         complexity at worse O(n) where n is the number of edges.
	 */
	public static Edge randomOutEdge(Node node) {
		return randomOutEdge(node, new Random());
	}

	/**
	 * Choose an edge at random from the edges connected to the given node.
	 * 
	 * @param random
	 *            The random number generator to use.
	 * @return An edge chosen at random, null if the node has no edges.
	 *         complexity at worse O(n) where n is the number of edges.
	 */
	public static Edge randomEdge(Node node, Random random) {
		int n = node.getDegree();
		int r = random.nextInt(n);
		int i = 0;

		for (Edge edge : node.getEdgeSet()) {
			if (r == i)
				return edge;
			i++;
		}

		return null;
	}

	/**
	 * Choose an edge at random from the entering edges connected to the given
	 * node.
	 * 
	 * @param random
	 *            The random number generator to use.
	 * @return An edge chosen at random, null if the node has no entering edges.
	 *         complexity at worse O(n) where n is the number of edges.
	 */
	public static Edge randomInEdge(Node node, Random random) {
		int n = node.getInDegree();
		int r = random.nextInt(n);
		int i = 0;

		for (Edge edge : node.getEnteringEdgeSet()) {
			if (r == i)
				return edge;
			i++;
		}

		return null;
	}

	/**
	 * Choose an edge at random from the leaving edges connected to the given
	 * node.
	 * 
	 * @param random
	 *            The random number generator to use.
	 * @return An edge chosen at random, null if the node has no leaving edges.
	 *         complexity at worse O(n) where n is the number of edges.
	 */
	public static Edge randomOutEdge(Node node, Random random) {
		int n = node.getOutDegree();
		int r = random.nextInt(n);
		int i = 0;

		for (Edge edge : node.getLeavingEdgeSet()) {
			if (r == i)
				return edge;
			i += 1;
		}

		return null;
	}

	/**
	 * Return set of nodes grouped by the value of one attribute (the marker).
	 * For example, if the marker is "color" and in the graph there are nodes
	 * whose "color" attribute value is "red" and others with value "blue", this
	 * method will return two sets, one containing all nodes corresponding to
	 * the nodes whose "color" attribute is red, the other with blue nodes. If
	 * some nodes do not have the "color" attribute, a third set is returned.
	 * The returned sets are stored in a hash map whose keys are the values of
	 * the marker attribute (in our example, the keys would be "red" and "blue",
	 * and if there are nodes that do not have the "color" attribute, the third
	 * set will have key "NULL_COMMUNITY").
	 * 
	 * @param marker
	 *            The attribute that allows to group nodes.
	 * @return The communities indexed by the value of the marker.
	 * @complexity O(n) with n the number of nodes.
	 */
	public static HashMap<Object, HashSet<Node>> communities(Graph graph,
			String marker) {
		HashMap<Object, HashSet<Node>> communities = new HashMap<Object, HashSet<Node>>();

		for (Node node : graph) {
			Object communityMarker = node.getAttribute(marker);

			if (communityMarker == null)
				communityMarker = "NULL_COMMUNITY";

			HashSet<Node> community = communities.get(communityMarker);

			if (community == null) {
				community = new HashSet<Node>();
				communities.put(communityMarker, community);
			}

			community.add(node);
		}

		return communities;
	}

	/**
	 * Create the modularity matrix E from the communities. The given
	 * communities are set of nodes forming the communities as produced by the
	 * {@link #communities(Graph,String)} method.
	 * 
	 * @param graph
	 *            Graph to which the computation will be applied
	 * @param communities
	 *            Set of nodes.
	 * @return The E matrix as defined by Newman and Girvan.
	 * @complexity O(m!k) with m the number of communities and k the average
	 *             number of nodes per community.
	 */
	public static float[][] modularityMatrix(Graph graph,
			HashMap<Object, HashSet<Node>> communities) {
		return modularityMatrix(graph, communities, null);
	}

	/**
	 * Create the weighted modularity matrix E from the communities. The given
	 * communities are set of nodes forming the communities as produced by the
	 * {@link #communities(Graph,String)} method.
	 * 
	 * @param graph
	 *            Graph to which the computation will be applied
	 * @param communities
	 *            Set of nodes.
	 * @param weightMarker
	 *            The marker used to store the weight of each edge
	 * @return The E matrix as defined by Newman and Girvan.
	 * @complexity O(m!k) with m the number of communities and k the average
	 *             number of nodes per community.
	 */
	public static float[][] modularityMatrix(Graph graph,
			HashMap<Object, HashSet<Node>> communities, String weightMarker) {

		float edgeCount = 0;
		if (weightMarker == null) {
			edgeCount = graph.getEdgeCount();
		} else {
			for (Edge e : graph.getEdgeSet()) {
				if (e.hasAttribute(weightMarker)) {
					edgeCount += (Double) e.getAttribute(weightMarker);
				}
			}
		}

		int communityCount = communities.size();

		float E[][] = new float[communityCount][];
		Object keys[] = new Object[communityCount];

		int k = 0;

		for (Object key : communities.keySet())
			keys[k++] = key;

		for (int i = 0; i < communityCount; ++i)
			E[i] = new float[communityCount];

		for (int y = 0; y < communityCount; ++y) {
			for (int x = y; x < communityCount; ++x) {
				E[x][y] = modularityCountEdges(communities.get(keys[x]),
						communities.get(keys[y]), weightMarker);
				E[x][y] /= edgeCount;

				if (x != y) {
					E[y][x] = E[x][y] / 2;
					E[x][y] = E[y][x];
				}
			}
		}

		return E;
	}

	/**
	 * Compute the modularity of the graph from the E matrix.
	 * 
	 * @param E
	 *            The E matrix given by {@link #modularityMatrix(Graph,HashMap)}
	 *            .
	 * @return The modularity of the graph.
	 * @complexity O(m!) with m the number of communities.
	 */
	public static float modularity(float[][] E) {
		float sumE = 0, Tr = 0;
		float communityCount = E.length;

		for (int y = 0; y < communityCount; ++y) {
			for (int x = y; x < communityCount; ++x) {
				if (x == y)
					Tr += E[x][y];

				sumE += E[x][y] * E[x][y];
			}
		}

		return (Tr - sumE);
	}

	/**
	 * Computes the modularity as defined by Newman and Girvan in "Finding and
	 * evaluating community structure in networks". This algorithm traverses the
	 * graph to count nodes in communities. For this to work, there must exist
	 * an attribute on each node whose value define the community the node
	 * pertains to (see {@link #communities(Graph,String)}).
	 * 
	 * This method is an utility method that call:
	 * <ul>
	 * <li>{@link #communities(Graph,String)}</li>
	 * <li>{@link #modularityMatrix(Graph,HashMap)}</li>
	 * <li>{@link #modularity(float[][])}</li>
	 * </ul>
	 * in order to produce the modularity value.
	 * 
	 * @param marker
	 *            The community attribute stored on nodes.
	 * @return The graph modularity.
	 * @complexity 0(n + m! + m!k) with n the number of nodes, m the number of
	 *             communities and k the average number of nodes per
	 *             communities.
	 * @see org.graphstream.algorithm.measure.Modularity
	 */
	public static float modularity(Graph graph, String marker) {
		return modularity(modularityMatrix(graph, communities(graph, marker)));
	}

	/**
	 * Computes the weighted modularity. This algorithm traverses the graph to
	 * count nodes in communities. For this to work, there must exist an
	 * attribute on each node whose value define the community the node pertains
	 * to (see {@link #communities(Graph,String)}) and a attribute on each edge
	 * storing their weight (all edges without this attribute will be ignored in
	 * the computation).
	 * 
	 * This method is an utility method that call:
	 * <ul>
	 * <li>{@link #communities(Graph,String)}</li>
	 * <li>{@link #modularityMatrix(Graph,HashMap,String)}</li>
	 * <li>{@link #modularity(float[][])}</li>
	 * </ul>
	 * in order to produce the modularity value.
	 * 
	 * @param marker
	 *            The community attribute stored on nodes.
	 * @param weightMarker
	 *            The marker used to store the weight of each edge.
	 * @return The graph modularity.
	 * @complexity 0(n + m! + m!k) with n the number of nodes, m the number of
	 *             communities and k the average number of nodes per
	 *             communities.
	 * @see org.graphstream.algorithm.measure.Modularity
	 */
	public static float modularity(Graph graph, String marker,
			String weightMarker) {
		return modularity(modularityMatrix(graph, communities(graph, marker),
				weightMarker));
	}

	/**
	 * Count the number of edges between the two communities (works if the two
	 * communities are the same).
	 * 
	 * @param community
	 *            The first community.
	 * @param otherCommunity
	 *            The second community.
	 * @return The number of edges between the two communities.
	 */
	protected static float modularityCountEdges(HashSet<Node> community,
			HashSet<Node> otherCommunity) {
		return modularityCountEdges(community, otherCommunity, null);
	}

	/**
	 * Count the total weight of edges between the two communities (works if the
	 * two communities are the same).
	 * 
	 * @param community
	 *            The first community.
	 * @param otherCommunity
	 *            The second community.
	 * @param weightMarker
	 *            The marker used to store the weight of each edge
	 * @return The number of edges between the two communities.
	 */
	protected static float modularityCountEdges(HashSet<Node> community,
			HashSet<Node> otherCommunity, String weightMarker) {
		HashSet<Edge> marked = new HashSet<Edge>();

		float edgeCount = 0;

		if (community != otherCommunity) {
			// Count edges between the two communities

			for (Node node : community) {
				for (Edge edge : node.getEdgeSet()) {
					if (!marked.contains(edge)) {
						marked.add(edge);

						if ((community.contains(edge.getNode0()) && otherCommunity
								.contains(edge.getNode1()))
								|| (community.contains(edge.getNode1()) && otherCommunity
										.contains(edge.getNode0()))) {
							if (weightMarker == null)
								edgeCount++;
							else if (edge.hasAttribute(weightMarker))
								edgeCount += (Double) edge
										.getAttribute(weightMarker);
						}
					}
				}
			}
		} else {
			// Count inner edges.

			for (Node node : community) {
				for (Edge edge : node.getEdgeSet()) {
					if (!marked.contains(edge)) {
						marked.add(edge);

						if (community.contains(edge.getNode0())
								&& community.contains(edge.getNode1())) {
							if (weightMarker == null)
								edgeCount++;
							else if (edge.hasAttribute(weightMarker))
								edgeCount += (Double) edge
										.getAttribute(weightMarker);
						}
					}
				}
			}
		}

		return edgeCount;
	}

	/**
	 * Retrieve a node position from its attributes ("x", "y", "z", or "xy", or
	 * "xyz").
	 * 
	 * @param id
	 *            The node identifier.
	 * @return A newly allocated array of three floats containing the (x,y,z)
	 *         position of the node, or null if the node is not part of the
	 *         graph.
	 */
	public static float[] nodePosition(Graph graph, String id) {
		Node node = graph.getNode(id);

		if (node != null)
			return nodePosition(node);

		return null;
	}

	/**
	 * Like {@link #nodePosition(Graph,String)} but use an existing node as
	 * argument.
	 * 
	 * @param node
	 *            The node to consider.
	 * @return A newly allocated array of three floats containing the (x,y,z)
	 *         position of the node.
	 */
	public static float[] nodePosition(Node node) {
		float xyz[] = new float[3];

		nodePosition(node, xyz);

		return xyz;
	}

	/**
	 * Like {@link #nodePosition(Graph,String)}, but instead of returning a
	 * newly allocated array, fill up the array given as parameter. This array
	 * must have at least three cells.
	 * 
	 * @param id
	 *            The node identifier.
	 * @param xyz
	 *            An array of at least three cells.
	 * @throws RuntimeException
	 *             If the node with the given identifier does not exist.
	 */
	public static void nodePosition(Graph graph, String id, float xyz[]) {
		Node node = graph.getNode(id);

		if (node != null)
			nodePosition(node, xyz);

		throw new RuntimeException("node '" + id + "' does not exist");
	}

	/**
	 * Like {@link #nodePosition(Graph,String,float[])} but use an existing node
	 * as argument.
	 * 
	 * @param node
	 *            The node to consider.
	 * @param xyz
	 *            An array of at least three cells.
	 */
	public static void nodePosition(Node node, float xyz[]) {
		if (xyz.length < 3)
			return;

		if (node.hasAttribute("xyz") || node.hasAttribute("xy")) {
			Object o = node.getAttribute("xyz");

			if (o == null)
				o = node.getAttribute("xy");

			if (o != null && o instanceof Object[]) {
				Object oo[] = (Object[]) o;

				if (oo.length > 0 && oo[0] instanceof Number) {
					xyz[0] = ((Number) oo[0]).floatValue();

					if (oo.length > 1)
						xyz[1] = ((Number) oo[1]).floatValue();
					if (oo.length > 2)
						xyz[2] = ((Number) oo[2]).floatValue();
				}
			}
		} else if (node.hasAttribute("x")) {
			xyz[0] = (float) node.getNumber("x");

			if (node.hasAttribute("y"))
				xyz[1] = (float) node.getNumber("y");

			if (node.hasAttribute("z"))
				xyz[2] = (float) node.getNumber("z");
		}
	}

	/**
	 * Compute the edge length of the given edge according to its two nodes
	 * positions.
	 * 
	 * @param id
	 *            The identifier of the edge.
	 * @return The edge length or -1 if the nodes of the edge have no positions.
	 * @throws RuntimeException
	 *             If the edge cannot be found.
	 */
	public static float edgeLength(Graph graph, String id) {
		Edge edge = graph.getEdge(id);

		if (edge != null)
			return edgeLength(edge);

		throw new RuntimeException("edge '" + id + "' cannot be found");
	}

	/**
	 * Like {@link #edgeLength(Graph,String)} but use an existing edge as
	 * argument.
	 * 
	 * @param edge
	 * @return The edge length or -1 if the nodes of the edge have no positions.
	 */
	public static float edgeLength(Edge edge) {
		float xyz0[] = nodePosition(edge.getNode0());
		float xyz1[] = nodePosition(edge.getNode1());

		if (xyz0 == null || xyz1 == null)
			return -1;

		xyz0[0] = xyz1[0] - xyz0[0];
		xyz0[1] = xyz1[1] - xyz0[1];
		xyz0[2] = xyz1[2] - xyz0[2];

		return (float) Math.sqrt(xyz0[0] * xyz0[0] + xyz0[1] * xyz0[1]
				+ xyz0[2] * xyz0[2]);
	}
}