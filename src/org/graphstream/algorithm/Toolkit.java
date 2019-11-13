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
 * @since 2009-02-19
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.atomic.DoubleAccumulator;

import org.graphstream.algorithm.util.RandomTools;
import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.GraphReplay;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;

/**
 * Lots of small often used algorithms on graphs.
 * 
 * <p>
 * This class contains a lot of very small algorithms that could be often useful
 * with a graph. Most methods take a graph as first argument.
 * </p>
 * 
 * <h2>Usage</h2>
 * 
 * <h3>Degrees</h3>
 * 
 * <p>
 * The {@link #degreeDistribution(Graph)} method allows to obtain an array where
 * each cell index represents the degree, and the value of the cell the number
 * of nodes having this degree. Its complexity is O(n) with n the number of
 * nodes.
 * </p>
 * 
 * <p>
 * The {@link #degreeMap(Graph)} returns an array of nodes sorted by degree in
 * descending order. The complexity is O(n log(n)) with n the number of nodes.
 * </p>
 * 
 * <p>
 * The {@link #averageDegree(Graph)} returns the average degree. The complexity
 * is O(1).
 * </p>
 * 
 * <p>
 * The {@link #degreeAverageDeviation(Graph)} returns the deviation of the
 * average degree. The complexity is O(n) with n the number of nodes.
 * </p>
 * * <h3>Density</h3>
 * 
 * <p>
 * The {@link #density(Graph)} method returns the number of links in the graph
 * divided by the total number of possible links. The complexity is O(1).
 * </p>
 * 
 * <h3>Diameter</h3>
 * 
 * <p>
 * The {@link #diameter(Graph)} method computes the diameter of the graph. The
 * diameter of the graph is the largest of all the shortest paths from any node
 * to any other node.
 * </p>
 * 
 * <p>
 * The returned diameter is not an integer since some graphs have non-integer
 * weights on edges.
 * </p>
 * 
 * <p>
 * The {@link #diameter(Graph, String, boolean)} method does the same thing, but
 * considers that the graph is weighted if the second argument is non-null. The
 * second argument is the weight attribute name. The third argument indicates if
 * the graph must be considered as directed or not.
 * </p>
 * 
 * <p>
 * Note that this operation can be quite costly, the algorithm used depends on
 * the fact the graph is weighted or not. If unweighted, the algorithm is in
 * O(n*(n+m)). If weighted the algorithm is the Floyd-Warshall algorithm whose
 * complexity is at worst of O(n^3).
 * </p>
 * 
 * <h3>Clustering coefficient</h3>
 * 
 * <p>
 * The {@link #clusteringCoefficient(Node)} method return the clustering
 * coefficient for the given node. The complexity if O(d^2) where d is the
 * degree of the node.
 * </p>
 * 
 * <p>
 * The {@link #clusteringCoefficients(Graph)} method return the clustering
 * coefficient of each node of the graph as an array.
 * </p>
 * 
 * <p>
 * The {@link #averageClusteringCoefficient(Graph)} method return the average
 * clustering coefficient for the graph.
 * </p>
 * 
 * <h3>Random nodes and edges</h3>
 * 
 * <p>
 * The {@link #randomNode(Graph)} returns a node chosen at random in the graph.
 * You can alternatively pass a ``Random`` instance as parameter with
 * {@link #randomNode(Graph, Random)}. The complexity depends on the kind of
 * graph.
 * </p>
 * 
 * <p>
 * The {@link #randomEdge(Graph)} returns an edge chosen at random in the graph.
 * You can alternatively pass a ``Random`` instance as parameter with
 * {@link #randomEdge(Graph, Random)}. The {@link #randomEdge(Node)} returns an
 * edge chosen at random within the edge set of the given node. You can also use
 * {@link #randomEdge(Node, Random)}. To chose a random edge of a node inside
 * the entering or leaving edge sets only, you can use
 * {@link #randomInEdge(Node)} or {@link #randomInEdge(Node, Random)}, or
 * {@link #randomOutEdge(Node)} or finally {@link #randomOutEdge(Node, Random)}.
 * </p>
 * 
 * <h3>Nodes position</h3>
 * 
 * <p>
 * Extracting nodes position from attributes can be tricky due to the face the
 * positions can be stored either as separate ``x``, ``y`` and ``z`` attributes
 * or inside ``xy`` or ``xyz`` attributes.
 * </p>
 * 
 * <p>
 * To simplify things you can use {@link #nodePosition(Node)} which returns an
 * array of three doubles, containing the position of the node. You can also use
 * {@link #nodePosition(Graph, String)} with a graph and a node identifier.
 * </p>
 * 
 * <p>
 * If you already have an array of doubles with at least three cells you can
 * also use {@link #nodePosition(Node, double[])} that will store the position
 * in the passed array. You can as well use
 * {@link #nodePosition(Graph, String, double[])}.
 * </p>
 * 
 * <p>
 * All these methods can also handle the ``org.graphstream.ui.geom.Point3``
 * class instead of arrays of doubles. Methods that use such an array as
 * argument are the same. Methods that return a ``Point3`` instead of an array
 * are {@link #nodePointPosition(Graph, String)} and
 * {@link #nodePointPosition(Node)}.
 * </p>
 * 
 * <h3>Cliques</h3>
 * 
 * <p>
 * A clique <i>C</i> is a subset of the node set of a graph, such that there
 * exists an edge between each pair of nodes in <i>C</i>. In other words, the
 * subgraph induced by <i>C</i> is complete. A maximal clique is a clique that
 * cannot be extended by adding more nodes, that is, there is no node outside
 * the clique connected to all the clique nodes.
 * </p>
 * 
 * <p>
 * This class provides several methods for dealing with cliques. Use
 * {@link #isClique(Collection)} or {@link #isMaximalClique(Collection, Graph)}
 * to check if a set of nodes is a clique or a maximal clique.
 * </p>
 * 
 * <p>
 * The methods {@link #getMaximalCliqueIterator(Graph)} and
 * {@link #getMaximalCliques(Graph)} enumerate all the maximal cliques in a
 * graph. Iterating on all the maximal cliques of a graph can take much time,
 * because their number can grow exponentially with the size of the graph. For
 * example, the following naive method to find the maximum clique (that is, the
 * largest possible clique) in a graph, is practical only for small and sparse
 * graphs.
 * </p>
 * 
 * <pre>
 * List&lt;Node&gt; maximumClique = new ArrayList&lt;Node&gt;();
 * for (List&lt;Node&gt; clique : Toolkit.getMaximalCliques(g))
 * 	if (clique.size() &gt; maximumClique.size())
 * 		maximumClique = clique;
 * </pre>
 * 
 * <h2>Example</h2>
 * 
 * <p>
 * You can use this class with a static import for example:
 * </p>
 * 
 * <pre>
 * import static org.graphstream.algorithm.Toolkit.*;
 * </pre>
 */
public class Toolkit extends
		org.graphstream.ui.graphicGraph.GraphPosLengthUtils {
	// Access

	/**
	 * Compute the weighted degree of a given node. For each entering
	 * and leaving edge the value contained by the 'weightAttribute' is
	 * considered. If the edge does not have such an attribute, the value
	 * one is used instead, resolving to a normal degree. Loop edges are counted
	 * twice. The 'weightAttribute' must contain a number or the default value
	 * is used.
	 * @param node The node to consider.
	 * @param weightAttribute The name of the attribute to look for weights on edges, it must be a number.
	 * @return The weighted degree. 
	 */
	public static double weightedDegree(Node node, String weightAttribute) {
		return weightedDegree(node, weightAttribute, 1);
	}

	/**
	 * Compute the weighted degree of a given node. For each entering
	 * and leaving edge the value contained by the 'weightAttribute' is
	 * considered. If the edge does not have such an attribute, the value
	 * `defaultWeightValue` is used instead. Loop edges are counted twice.
	 * The 'weightAttribute' must contain a number or the default value
	 * is used.
	 * @param node The node to consider.
	 * @param weightAttribute The name of the attribute to look for weights on edges, it must be a number.
	 * @param defaultWeightValue The default weight value to use if edges do not have the 'weightAttribute'.
	 * @return The weighted degree. 
	 */
	public static double weightedDegree(Node node, String weightAttribute, double defaultWeightValue) {
		DoubleAccumulator wdegree = new DoubleAccumulator((x, y) -> x + y, 0);
		
		node.edges().forEach(edge -> {
			if(edge.hasNumber(weightAttribute)) {
				if(edge.getSourceNode() == edge.getTargetNode()) 
				     wdegree.accumulate(edge.getNumber(weightAttribute) * 2);
				else wdegree.accumulate(edge.getNumber(weightAttribute));
			} else {
				if(edge.getSourceNode() == edge.getTargetNode())
				     wdegree.accumulate(defaultWeightValue * 2);
				else wdegree.accumulate(defaultWeightValue);
			}
		});
		
		return wdegree.get();
	}

	/**
	 * Compute the weighted entering degree of a given node. For each
	 * entering edge the value contained by the 'weightAttribute' is
	 * considered. If the edge does not have such an attribute, the value
	 * one is used instead, resolving to a normal degree. Loop edges are counted once
	 * if directed, but twice if undirected. The 'weightAttribute' must
	 * contain a number or the default value is used.
	 * @param node The node to consider.
	 * @param weightAttribute The name of the attribute to look on edges, it must be a number.
	 * @return The entering weighted degree.
	 */
	public static double enteringWeightedDegree(Node node, String weightAttribute) {
		return enteringWeightedDegree(node, weightAttribute, 1);
	}

	/**
	 * Compute the weighted entering degree of a given node. For each
	 * entering edge the value contained by the 'weightAttribute' is
	 * considered. If the edge does not have such an attribute, the value
	 * 'defaultWeightValue' is used instead. Loop edges are counted once
	 * if directed, but twice if undirected. The 'weightAttribute' must
	 * contain a number or the default value is used.
	 * @param node The node to consider.
	 * @param weightAttribute The name of the attribute to look on edges, it must be a number.
	 * @param defaultWeightValue The default weight value to use if edges do not have the 'weightAttribute'.
	 * @return The entering weighted degree.
	 */
	public static double enteringWeightedDegree(Node node, String weightAttribute, double defaultWeightValue) {
		DoubleAccumulator wdegree = new DoubleAccumulator((x, y) -> x + y, 0);
		
		node.enteringEdges().forEach(edge -> {
			if(edge.hasNumber(weightAttribute)) {
				wdegree.accumulate(edge.getNumber(weightAttribute));
			} else {
				wdegree.accumulate(defaultWeightValue);
			}
		});
		
		return wdegree.get();
	}

	/**
	 * Compute the weighted leaving degree of a given node. For each
	 * leaving edge the value contained by the 'weightAttribute' is
	 * considered. If the edge does not have such an attribute, the value
	 * one is used instead, resolving to a normal degree. Loop edges are counted once
	 * if directed, but twice if undirected. The 'weightAttribute' must
	 * contain a number or the default value is used.
	 * @param node The node to consider.
	 * @param weightAttribute The name of the attribute to look on edges, it must be a number.
	 * @param defaultWeightValue The default weight value to use if edges do not have the 'weightAttribute'.
	 * @return The leaving weighted degree.
	 */
	public static double leavingWeightedDegree(Node node, String weightAttribute) {
		return leavingWeightedDegree(node, weightAttribute, 1);
	}

	/**
	 * Compute the weighted leaving degree of a given node. For each
	 * leaving edge the value contained by the 'weightAttribute' is
	 * considered. If the edge does not have such an attribute, the value
	 * 'defaultWeightValue' is used instead. Loop edges are counted once
	 * if directed, but twice if undirected. The 'weightAttribute' must
	 * contain a number or the default value is used.
	 * @param node The node to consider.
	 * @param weightAttribute The name of the attribute to look on edges, it must be a number.
	 * @param defaultWeightValue The default weight value to use if edges do not have the 'weightAttribute'.
	 * @return The leaving weighted degree.
	 */
	public static double leavingWeightedDegree(Node node, String weightAttribute, double defaultWeightValue) {
		DoubleAccumulator wdegree = new DoubleAccumulator((x, y) -> x + y, 0);
		
		node.leavingEdges().forEach(edge -> {
			if(edge.hasNumber(weightAttribute)) {
				wdegree.accumulate(edge.getNumber(weightAttribute));
			} else {
				wdegree.accumulate(defaultWeightValue);
			}
		});
		
		return wdegree.get();
	}
	
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
		
		max = graph.nodes()
				.map(n -> n.getDegree())
				.max((n1, n2) -> Integer.compare(n1, n2))
				.get();


		dd = new int[max + 1];
		
		graph.nodes().forEach(node -> {
			int d = node.getDegree();

			dd[d] += 1;
		});
		

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

		graph.nodes().forEach(node -> {
			map.add(node);
		});

		Collections.sort(map, new Comparator<Node>() {
			public int compare(Node a, Node b) {
				return b.getDegree() - a.getDegree();
			}
		});

		return map;
	}

	/**
	 * Return a list of nodes sorted by their weighted degree, the larger first.
	 *
	 * @param graph The graph to consider.
	 * @param weightAttribute The name of the attribute to look for weights on edges, it must be a number, or the default value is used.
	 * @param defaultWeightValue The value to use if the weight attribute is not found on edges.
	 * @return The degree map.
	 * @complexity O(n log(n)) where n is the number of nodes.
	 * @see #weightedDegree(Node, String, double)
	 */
	public static ArrayList<Node> weightedDegreeMap(Graph graph, String weightAttribute, double defaultWeightValue) {
		ArrayList<Node> map = new ArrayList<Node>();

		graph.nodes().forEach(node -> {
			map.add(node);
		});

		Collections.sort(map, new WeightComparator(weightAttribute, defaultWeightValue));

		return map;
	}

	/**
	 * Return a list of nodes sorted by their weighted degree, the larger first.
	 * 
	 * @param graph The graph to consider.
	 * @param weightAttribute The name of the attribute to look for weights on edges, it must be a number, or the default value of one is used.
	 * @return The degree map.
	 * @complexity O(n log(n)) where n is the number of nodes.
	 * @see #weightedDegree(Node, String, double)
	 */
	public static ArrayList<Node> weightedDegreeMap(Graph graph, String weightAttribute) {
		return weightedDegreeMap(graph, weightAttribute, 1);
	}
	
	/**
	 * Compare nodes by their weighted degree.
	 */
	private static class WeightComparator implements Comparator<Node> {
		private String weightAttribute = "weight";
		private double defaultWeightValue = 1;
		public WeightComparator(String watt, double dwv) {
			this.weightAttribute = watt;
			this.defaultWeightValue = dwv;
		}
		public int compare(Node a, Node b) {
			double bw = weightedDegree(b, weightAttribute, defaultWeightValue);
			double ba = weightedDegree(a, weightAttribute, defaultWeightValue);
			if(bw < ba) return -1;
			else if(bw > ba) return 1;
			else return 0;
		}
	}

	/**
	 * Returns the value of the average degree of the graph. A node with a loop
	 * edge has degree two.
	 * 
	 * @return The average degree of the graph.
	 * @complexity O(1).
	 */
	public static double averageDegree(Graph graph) {
		float m = graph.getEdgeCount() * 2;
		float n = graph.getNodeCount();

		if (n > 0)
			return m / n;

		return 0;
	}

	/**
	 * Returns the value of the degree average deviation of the graph.
	 * 
	 * @return The degree average deviation.
	 * @complexity O(n) where n is the number of nodes.
	 */
	public static double degreeAverageDeviation(Graph graph) {
		double average = averageDegree(graph);
		DoubleAccumulator sum = new DoubleAccumulator((x, y) -> x + y, 0);

		graph.nodes().forEach(node -> {
			double d = node.getDegree() - average;
			sum.accumulate(d * d);
		});

		return Math.sqrt(sum.get() / graph.getNodeCount());
	}

	/**
	 * The density is the number of links in the graph divided by the total
	 * number of possible links.
	 * 
	 * @return The density of the graph.
	 * @complexity O(1)
	 */
	public static double density(Graph graph) {
		float m = (float) graph.getEdgeCount();
		float n = (float) graph.getNodeCount();

		if (n > 0)
			return ((2 * m) / (n * (n - 1)));

		return 0;
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
			DoubleAccumulator cc = new DoubleAccumulator((x, y) -> x + y, 0);

			graph.nodes().forEach(node -> cc.accumulate(clusteringCoefficient(node)));
			
			return cc.get() / n;
		}

		return 0;
	}

	/**
	 * Clustering coefficient for one node of the graph. For a node i with
	 * degree k, if Ni is the neighborhood of i (a set of nodes), clustering
	 * coefficient of i is defined as the count of edge e_uv with u,v in Ni
	 * divided by the maximum possible count, ie. k * (k-1) / 2.
	 * 
	 * This method only works with undirected graphs.
	 * 
	 * @param node
	 *            The node to compute the clustering coefficient for.
	 * @return The clustering coefficient for this node.
	 * @complexity O(d^2) where d is the degree of the given node.
	 * @reference D. J. Watts and Steven Strogatz (June 1998).
	 *            "Collective dynamics of 'small-world' networks" . Nature 393
	 *            (6684): 440–442
	 */
	public static double clusteringCoefficient(Node node) {
		double coef = 0.0;
		int n = node.getDegree();

		if (n > 1) {
			Node[] nodes = new Node[n];

			//
			// Collect the neighbor nodes.
			//
			for (int i = 0; i < n; i++)
				nodes[i] = node.getEdge(i).getOpposite(node);

			//
			// Check all edge possibilities
			//
			for (int i = 0; i < n; ++i)
				for (int j = 0; j < n; ++j)
					if (j != i) {
						Edge e = nodes[j].getEdgeToward(nodes[i].getId());

						if (e != null && e.getSourceNode() == nodes[j])
							coef++;
					}

			coef /= (n * (n - 1)) / 2.0;
		}

		return coef;
	}

	/**
	 * Choose a node at random.
	 * 
	 * @return A node chosen at random, null if the graph is empty.
	 * @complexity O(1).
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
	 * @complexity O(1).
	 */
	public static Node randomNode(Graph graph, Random random) {
		int n = graph.getNodeCount();

		if (n > 0) {
			return graph.getNode(random.nextInt(n));
			// int r = random.nextInt(n);
			// int i = 0;
			//
			// for (Node node : graph) {
			// if (r == i)
			// return node;
			// i++;
			// }
		}

		return null;
	}

	/**
	 * Choose an edge at random.
	 * 
	 * @return An edge chosen at random.
	 * @complexity O(1).
	 */
	public static Edge randomEdge(Graph graph) {
		return randomEdge(graph, new Random());
	}

	/**
	 * Choose an edge at random.
	 * 
	 * @param random
	 *            The random number generator to use.
	 * @return O(1).
	 */
	public static Edge randomEdge(Graph graph, Random random) {
		int n = graph.getEdgeCount();

		if (n > 0) {
			return graph.getEdge(random.nextInt(n));
			// int r = random.nextInt(n);
			// int i = 0;
			//
			// for (Edge edge : graph.getEachEdge()) {
			// if (r == i)
			// return edge;
			// i++;
			// }
		}

		return null;
	}

	/**
	 * Choose an edge at random from the edges connected to the given node.
	 * 
	 * @return O(1).
	 */
	public static Edge randomEdge(Node node) {
		return randomEdge(node, new Random());
	}

	/**
	 * Choose an edge at random from the entering edges connected to the given
	 * node.
	 * 
	 * @return O(1).
	 */
	public static Edge randomInEdge(Node node) {
		return randomInEdge(node, new Random());
	}

	/**
	 * Choose an edge at random from the leaving edges connected to the given
	 * node.
	 * 
	 * @return An edge chosen at random, null if the node has no leaving edges.
	 * @complexity O(1).
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
	 * @complexity O(1).
	 */
	public static Edge randomEdge(Node node, Random random) {
		int n = node.getDegree();

		if (n > 0) {
			return node.getEdge(random.nextInt(n));
			// int r = random.nextInt(n);
			// int i = 0;
			//
			// for (Edge edge : node.getEdgeSet()) {
			// if (r == i)
			// return edge;
			// i++;
			// }
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
	 * @complexity O(1).
	 */
	public static Edge randomInEdge(Node node, Random random) {
		int n = node.getInDegree();

		if (n > 0) {
			return node.getEnteringEdge(random.nextInt(n));
			// int r = random.nextInt(n);
			// int i = 0;
			//
			// for (Edge edge : node.getEnteringEdgeSet()) {
			// if (r == i)
			// return edge;
			// i++;
			// }
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
	 * @complexity O(1).
	 */
	public static Edge randomOutEdge(Node node, Random random) {
		int n = node.getOutDegree();

		if (n > 0) {
			return node.getLeavingEdge(random.nextInt(n));
			// int r = random.nextInt(n);
			// int i = 0;
			//
			// for (Edge edge : node.getLeavingEdgeSet()) {
			// if (r == i)
			// return edge;
			// i += 1;
			// }
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

		graph.nodes().forEach(node -> {
			Object communityMarker = node.getAttribute(marker);

			if (communityMarker == null)
				communityMarker = "NULL_COMMUNITY";

			HashSet<Node> community = communities.get(communityMarker);

			if (community == null) {
				community = new HashSet<Node>();
				communities.put(communityMarker, community);
			}

			community.add(node);
		});

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
	public static double[][] modularityMatrix(Graph graph,
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
	public static double[][] modularityMatrix(Graph graph,
		HashMap<Object, HashSet<Node>> communities, String weightMarker) {

		DoubleAccumulator edgeCount = new DoubleAccumulator((x, y) -> x + y, 0);
		
		if (weightMarker == null) {
			edgeCount.accumulate(graph.getEdgeCount());
		} else {
			graph.edges()
				.filter(e -> e.hasAttribute(weightMarker))
				.forEach(e -> edgeCount.accumulate((Double) e.getAttribute(weightMarker)));
		}

		int communityCount = communities.size();

		double E[][] = new double[communityCount][];
		Object keys[] = new Object[communityCount];

		int k = 0;

		for (Object key : communities.keySet())
			keys[k++] = key;

		for (int i = 0; i < communityCount; ++i)
			E[i] = new double[communityCount];

		for (int y = 0; y < communityCount; ++y) {
			for (int x = y; x < communityCount; ++x) {
				E[x][y] = modularityCountEdges(communities.get(keys[x]),
						communities.get(keys[y]), weightMarker);
				E[x][y] /= edgeCount.get();

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
	public static double modularity(double[][] E) {
		double sumE = 0, Tr = 0;
		double communityCount = E.length;

//		for (int y = 0; y < communityCount; ++y) {
//			for (int x = y; x < communityCount; ++x) {
//				if (x == y)
//					Tr += E[x][y];
//				
//				sumE += E[x][y] * E[x][y];
//			}
//		}
		
		for (int i = 0; i < communityCount; i++) {
			Tr += E[i][i];
			double a = 0;
			for (int j = 0; j < communityCount; j++)
				a += E[i][j];
			sumE += a * a;
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
	 * <li>{@link #modularity(double[][])}</li>
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
	public static double modularity(Graph graph, String marker) {
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
	 * <li>{@link #modularity(double[][])}</li>
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
	public static double modularity(Graph graph, String marker,
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
	protected static double modularityCountEdges(HashSet<Node> community,
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
	protected static double modularityCountEdges(HashSet<Node> community,
			HashSet<Node> otherCommunity, String weightMarker) {
		HashSet<Edge> marked = new HashSet<Edge>();

		DoubleAccumulator edgeCount = new DoubleAccumulator((x, y) -> x + y, 0);

		if (community != otherCommunity) {
			// Count edges between the two communities
			
			community.stream().forEach(node -> {
				node.edges()
					.filter(edge -> !marked.contains(edge))
					.forEach(edge -> {
						marked.add(edge);

						if ((community.contains(edge.getNode0()) && otherCommunity.contains(edge.getNode1()))
								|| (community.contains(edge.getNode1()) && otherCommunity.contains(edge.getNode0()))) {
							if (weightMarker == null)
								edgeCount.accumulate(1);
							else if (edge.hasAttribute(weightMarker))
								edgeCount.accumulate((Double) edge.getAttribute(weightMarker));
						}
					});
			});
			
		} else {
			// Count inner edges.
			community.stream().forEach(node -> {
				node.edges()
					.filter(edge -> !marked.contains(edge))
					.forEach(edge -> {
						marked.add(edge);

						if (community.contains(edge.getNode0())	&& community.contains(edge.getNode1())) {
							if (weightMarker == null)
								edgeCount.accumulate(1);
							else if (edge.hasAttribute(weightMarker))
								edgeCount.accumulate((Double) edge.getAttribute(weightMarker));
						}
					});
			});
		}

		return edgeCount.get();
	}

	/**
	 * Compute the diameter of the graph.
	 * 
	 * <p>
	 * The diameter of the graph is the largest of all the shortest paths from
	 * any node to any other node. The graph is considered non weighted.
	 * </p>
	 * 
	 * <p>
	 * Note that this operation can be quite costly, O(n*(n+m)).
	 * </p>
	 * 
	 * <p>
	 * The returned diameter is not an integer since some graphs have
	 * non-integer weights on edges. Although this version of the diameter
	 * algorithm will return an integer.
	 * </p>
	 * 
	 * @param graph
	 *            The graph to use.
	 * @return The diameter.
	 */
	public static double diameter(Graph graph) {
		return diameter(graph, null, false);
	}

	/**
	 * Compute the diameter of the graph.
	 * 
	 * <p>
	 * The diameter of the graph is the largest of all the shortest paths from
	 * any node to any other node.
	 * </p>
	 * 
	 * <p>
	 * Note that this operation can be quite costly. Two algorithms are used
	 * here. If the graph is not weighted (the weightAttributeName parameter is
	 * null), the algorithm use breath first search from all the nodes to find
	 * the max depth (or eccentricity) of each node. The diameter is then the
	 * maximum of these maximum depths. The complexity of this algorithm is
	 * O(n*(n+m)), with n the number of nodes and m the number of edges.
	 * </p>
	 * 
	 * <p>
	 * If the graph is weighted, the algorithm used to compute all shortest
	 * paths is the Floyd-Warshall algorithm whose complexity is at worst of
	 * O(n^3).
	 * </p>
	 * 
	 * <p>
	 * The returned diameter is not an integer since weighted graphs have
	 * non-integer weights on edges.
	 * </p>
	 * 
	 * @param graph
	 *            The graph to use.
	 * @param weightAttributeName
	 *            The name used to store weights on the edges (must be a
	 *            Number).
	 * @param directed
	 *            Does The edge direction should be considered ?.
	 * @return The diameter.
	 */
	public static double diameter(Graph graph, String weightAttributeName,
			boolean directed) {
		DoubleAccumulator diameter = new DoubleAccumulator((x, y) -> y, Double.MIN_VALUE);

		if (weightAttributeName == null) {
			
			graph.nodes().forEach(node -> {
				int d = unweightedEccentricity(node, directed);
				if (d > diameter.get())
					diameter.accumulate(d);
			});
		} 
		else {
			APSP apsp = new APSP(graph, weightAttributeName, directed);

			apsp.compute();
			
			graph.nodes().forEach(node -> {
				APSP.APSPInfo info = (APSP.APSPInfo) node
						.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
				
				info.targets.values().stream().forEach(path -> {
					if (path.distance > diameter.get())
						diameter.accumulate(path.distance);
				});
			});
		}

		return diameter.get();
	}

	/**
	 * Eccentricity of a node not considering edge weights.
	 * 
	 * <p>
	 * The eccentricity is the largest shortest path between the given node and
	 * any other. It is here computed on number of edges crossed, not
	 * considering the eventual weights of edges.
	 * </p>
	 * 
	 * <p>
	 * This is computed using a breath first search and looking at the maximum
	 * depth of the search.
	 * </p>
	 * 
	 * @param node
	 *            The node for which the eccentricity is to be computed.
	 * @param directed
	 *            If true, the computation will respect edges direction, if any.
	 * 
	 * @complexity O(n+m) with n the number of nodes and m the number of edges.
	 * 
	 * @return The eccentricity.
	 */
	public static int unweightedEccentricity(Node node, boolean directed) {
		BreadthFirstIterator k = new BreadthFirstIterator(node,
				directed);
		while (k.hasNext()) {
			k.next();
		}
		return k.getDepthMax();
	}

	/**
	 * Checks if a set of nodes is a clique.
	 * 
	 * @param nodes
	 *            a set of nodes
	 * @return {@code true} if {@code nodes} form a clique
	 * @complexity O(<i>k</i>), where <i>k</i> is the size of {@code nodes}
	 */
	public static boolean isClique(Collection<? extends Node> nodes) {
		if (nodes.isEmpty())
			return false;
		for (Node x : nodes)
			for (Node y : nodes)
				if (x != y && x.getEdgeBetween(y.getId()) == null)
					return false;
		return true;
	}

	/**
	 * Checks if a set of nodes is a maximal clique.
	 * 
	 * @param nodes
	 *            a set of nodes
	 * @return {@code true} if {@nodes} form a maximal clique
	 * @complexity O(<i>kn</i>), where <i>n</i> is the number of nodes in the
	 *             graph and <i>k</i> is the size of {@code nodes}
	 */
	public static boolean isMaximalClique(Collection<? extends Node> nodes,
			Graph graph) {
		if (!isClique(nodes))
			return false;
		for (Node x : graph) {
			String xId = x.getId();
			boolean isXConnectedToAll = true;
			for (Node y : nodes)
				if (y == x || y.getEdgeBetween(xId) == null) {
					isXConnectedToAll = false;
					break;
				}
			if (isXConnectedToAll)
				return false;
		}
		return true;
	}

	/**
	 * This iterator traverses all the maximal cliques in a graph. Each call to
	 * {@link java.util.Iterator#next()} returns a maximal clique in the form of
	 * list of nodes. This iterator does not support remove.
	 * 
	 * @param graph
	 *            a graph, must not have loop edges
	 * @return an iterator on the maximal cliques of {@code graph}
	 * @throws IllegalArgumentException
	 *             if {@code graph} has loop edges
	 * @complexity This iterator implements the Bron–Kerbosch algorithm. There
	 *             is no guarantee that each call to
	 *             {@link java.util.Iterator#next()} will run in polynomial
	 *             time. However, iterating over <em>all</em> the maximal
	 *             cliques is efficient in worst case sense. The whole iteration
	 *             takes O(3<sup><i>n</i>/3</sup>) time in the worst case and it
	 *             is known that a <i>n</i>-node graph has at most
	 *             3<sup><i>n</i>/3</sup> maximal cliques.
	 */
	public static <T extends Node> Iterator<List<T>> getMaximalCliqueIterator(Graph graph) {
		graph.edges()
			.filter(e -> e.isLoop())
			.forEach(e -> illegalArgumentException());
				
		return new BronKerboschIterator<T>(graph);
	}
	
	public static void illegalArgumentException() {
		throw new IllegalArgumentException("The graph must not have loop edges");
	}
	
	/**
	 * An iterable view of the set of all the maximal cliques in a graph. Uses
	 * {@link #getMaximalCliqueIterator(Graph)}.
	 * 
	 * @param graph
	 *            a graph
	 * @return An iterable view of the maximal cliques in {@code graph}.
	 */
	public static <T extends Node> Iterable<List<T>> getMaximalCliques(
			final Graph graph) {
		return new Iterable<List<T>>() {
			public Iterator<List<T>> iterator() {
				return getMaximalCliqueIterator(graph);
			}

		};
	}

	protected static class StackElement<T extends Node> {
		protected List<T> candidates;
		protected int candidateIndex;
		protected List<T> excluded;
		protected String pivotId;

		protected boolean moreCandidates() {
			return candidateIndex < candidates.size();
		}

		protected T currentCandidate() {
			return candidates.get(candidateIndex);
		}

		protected int nonNeighborCandidateCount(Node node) {
			int count = 0;
			String nodeId = node.getId();
			for (T c : candidates)
				if (c.getEdgeBetween(nodeId) == null)
					count++;
			return count;
		}

		protected void setPivot() {
			pivotId = null;
			int minCount = candidates.size() + 1;
			for (T x : candidates) {
				int count = nonNeighborCandidateCount(x);
				if (count < minCount) {
					minCount = count;
					pivotId = x.getId();
				}
			}
			for (T x : excluded) {
				int count = nonNeighborCandidateCount(x);
				if (count < minCount) {
					minCount = count;
					pivotId = x.getId();
				}
			}
		}

		protected boolean skipCurrentCandidate() {
			return currentCandidate().getEdgeBetween(pivotId) != null;
		}

		protected void forwardIndex() {
			while (moreCandidates() && skipCurrentCandidate())
				candidateIndex++;
		}

		protected StackElement<T> nextElement() {
			StackElement<T> next = new StackElement<T>();
			String currentId = currentCandidate().getId();

			next.candidates = new ArrayList<T>();
			for (T x : candidates)
				if (x.getEdgeBetween(currentId) != null)
					next.candidates.add(x);

			next.excluded = new ArrayList<T>();
			for (T x : excluded)
				if (x.getEdgeBetween(currentId) != null)
					next.excluded.add(x);

			next.setPivot();
			next.candidateIndex = 0;
			next.forwardIndex();
			return next;
		}

		protected void forward() {
			excluded.add(candidates.remove(candidateIndex));
			forwardIndex();
		}
	}

	protected static class BronKerboschIterator<T extends Node> implements
			Iterator<List<T>> {

		protected Stack<StackElement<T>> stack;
		protected Stack<T> clique;

		protected void constructNextClique() {
			// backtrack
			while (!clique.isEmpty() && !stack.peek().moreCandidates()) {
				stack.pop();
				clique.pop();
			}
			// forward
			StackElement<T> currentElement = stack.peek();
			while (currentElement.moreCandidates()) {
				clique.push(currentElement.currentCandidate());
				stack.push(currentElement.nextElement());
				currentElement.forward();
				currentElement = stack.peek();
			}
		}

		protected void constructNextMaximalClique() {
			do {
				constructNextClique();
			} while (!clique.isEmpty() && !stack.peek().excluded.isEmpty());
		}

		protected BronKerboschIterator(Graph graph) {
			clique = new Stack<T>();
			stack = new Stack<StackElement<T>>();
			StackElement<T> initial = new StackElement<T>();

			// initial.candidates = new ArrayList<T>(graph.<T> getNodeSet());
			// More efficient initial order
			initial.candidates = new ArrayList<T>(graph.getNodeCount());
			getDegeneracy(graph, initial.candidates);

			initial.excluded = new ArrayList<T>();
			initial.setPivot();
			initial.candidateIndex = 0;
			initial.forwardIndex();
			stack.push(initial);
			constructNextMaximalClique();
		}

		public boolean hasNext() {
			return !clique.isEmpty();
		}

		public List<T> next() {
			if (clique.isEmpty())
				throw new NoSuchElementException();
			List<T> result = new ArrayList<T>(clique);
			constructNextMaximalClique();
			return result;
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"This iterator does not support remove");
		}
	}

	/**
	 * <p>
	 * This method computes the gedeneracy and the degeneracy ordering of a
	 * graph.
	 * </p>
	 * 
	 * <p>
	 * The degeneracy of a graph is the smallest number <i>d</i> such that every
	 * subgraph has a node with degree <i>d</i> or less. The degeneracy is a
	 * measure of sparseness of graphs. A degeneracy ordering is an ordering of
	 * the nodes such that each node has at most <i>d</i> neighbors following it
	 * in the ordering. The degeneracy ordering is used, among others, in greedy
	 * coloring algorithms.
	 * </p>
	 * 
	 * 
	 * @param graph
	 *            a graph
	 * @param ordering
	 *            a list of nodes. If not {@code null}, this list is first
	 *            cleared and then filled with the nodes of the graph in
	 *            degeneracy order.
	 * @return the degeneracy of {@code graph}
	 * @complexity O(<i>m</i>) where <i>m</i> is the number of edges in the
	 *             graph
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Node> int getDegeneracy(Graph graph,
			List<T> ordering) {
		int n = graph.getNodeCount();
		if (ordering != null)
			ordering.clear();
		int maxDeg = 0;
		for (Node x : graph)
			if (x.getDegree() > maxDeg)
				maxDeg = x.getDegree();
		List<DegenEntry> heads = new ArrayList<DegenEntry>(maxDeg + 1);
		for (int d = 0; d <= maxDeg; d++)
			heads.add(null);

		Map<Node, DegenEntry> map = new HashMap<Node, DegenEntry>(
				4 * (n + 2) / 3);
		for (Node x : graph) {
			DegenEntry entry = new DegenEntry();
			entry.node = x;
			entry.deg = x.getDegree();
			entry.addToList(heads);
			map.put(x, entry);
		}

		int degeneracy = 0;
		for (int j = 0; j < n; j++) {
			int i;
			DegenEntry entry;
			for (i = 0; (entry = heads.get(i)) == null; i++)
				;
			if (i > degeneracy)
				degeneracy = i;
			entry.removeFromList(heads);
			entry.deg = -1;
			if (ordering != null)
				ordering.add((T) entry.node);
			
			entry.node.neighborNodes()
				.filter(x -> map.get(x).deg != -1 )
				.forEach(x -> {
				DegenEntry entryX = map.get(x);
				

				entryX.removeFromList(heads);
				entryX.deg--;
				entryX.addToList(heads);
			});
		}
		if (ordering != null)
			Collections.reverse(ordering);
		return degeneracy;
	}

	protected static class DegenEntry {
		Node node;
		int deg;
		DegenEntry prev, next;

		protected void addToList(List<DegenEntry> heads) {
			DegenEntry oldHead = heads.get(deg);
			heads.set(deg, this);
			prev = null;
			next = oldHead;
			if (oldHead != null)
				oldHead.prev = this;
		}

		protected void removeFromList(List<DegenEntry> heads) {
			if (prev == null)
				heads.set(deg, next);
			else
				prev.next = next;
			if (next != null)
				next.prev = prev;
		}
	}

	/**
	 * Fills an array with the adjacency matrix of a graph.
	 * 
	 * The adjacency matrix of a graph is a <i>n</i> times <i>n</i> matrix
	 * {@code a}, where <i>n</i> is the number of nodes of the graph. The
	 * element {@code a[i][j]} of this matrix is equal to the number of edges
	 * from the node {@code graph.getNode(i)} to the node
	 * {@code graph.getNode(j)}. An undirected edge between i-th and j-th node
	 * is counted twice: in {@code a[i][j]} and in {@code a[j][i]}.
	 * 
	 * @param graph
	 *            A graph.
	 * @param matrix
	 *            The array where the adjacency matrix is stored. Must be of
	 *            size at least <i>n</i> times <i>n</i>
	 * @throws IndexOutOfBoundsException
	 *             if the size of the matrix is insufficient.
	 * @see Toolkit#getAdjacencyMatrix(Graph)
	 * @complexity <i>O(n<sup>2</sup>)</i>, where <i>n</i> is the number of
	 *             nodes.
	 */
	public static void fillAdjacencyMatrix(Graph graph, int[][] matrix) {
		for (int i = 0; i < matrix.length; i++)
			Arrays.fill(matrix[i], 0);
		
		graph.edges().forEach(e -> {
			int i = e.getSourceNode().getIndex();
			int j = e.getTargetNode().getIndex();
			matrix[i][j]++;
			if (!e.isDirected())
				matrix[j][i]++;
		});
	}

	/**
	 * Returns the adjacency matrix of a graph.
	 * 
	 * The adjacency matrix of a graph is a <i>n</i> times <i>n</i> matrix
	 * {@code a}, where <i>n</i> is the number of nodes of the graph. The
	 * element {@code a[i][j]} of this matrix is equal to the number of edges
	 * from the node {@code graph.getNode(i)} to the node
	 * {@code graph.getNode(j)}. An undirected edge between i-th and j-th node
	 * is counted twice: in {@code a[i][j]} and in {@code a[j][i]}.
	 * 
	 * @param graph
	 *            A graph
	 * @return The adjacency matrix of the graph.
	 * @see Toolkit#fillAdjacencyMatrix(Graph, int[][])
	 * @complexity <i>O(n<sup>2</sup>)</i>, where <i>n</i> is the number of
	 *             nodes.
	 */
	public static int[][] getAdjacencyMatrix(Graph graph) {
		int n = graph.getNodeCount();
		int[][] matrix = new int[n][n];
		fillAdjacencyMatrix(graph, matrix);
		return matrix;
	}

	/**
	 * Fills an array with the incidence matrix of a graph.
	 * 
	 * The incidence matrix of a graph is a <i>n</i> times <i>m</i> matrix
	 * {@code a}, where <i>n</i> is the number of nodes and <i>m</i> is the
	 * number of edges of the graph. The coefficients {@code a[i][j]} of this
	 * matrix have the following values:
	 * <ul>
	 * <li>-1 if {@code graph.getEdge(j)} is directed and
	 * {@code graph.getNode(i)} is its source.</li>
	 * <li>1 if {@code graph.getEdge(j)} is undirected and
	 * {@code graph.getNode(i)} is its source.</li>
	 * <li>1 if {@code graph.getNode(i)} is the target of
	 * {@code graph.getEdge(j)}.</li>
	 * <li>0 otherwise.
	 * </ul>
	 * In the special case when the j-th edge is a loop connecting the i-th node
	 * to itself, the coefficient {@code a[i][j]} is 0 if the loop is directed
	 * and 2 if the loop is undirected. All the other coefficients in the j-th
	 * column are 0.
	 * 
	 * @param graph
	 *            A graph
	 * @param matrix
	 *            The array where the incidence matrix is stored. Must be at
	 *            least of size <i>n</i> times <i>m</i>
	 * @throws IndexOutOfBoundsException
	 *             if the size of the matrix is insufficient
	 * @see #getIncidenceMatrix(Graph)
	 * @complexity <i>O(mn)</i>, where <i>n</i> is the number of nodes and
	 *             <i>m</i> is the number of edges.
	 */
	public static void fillIncidenceMatrix(Graph graph, byte[][] matrix) {
		for (int i = 0; i < matrix.length; i++)
			Arrays.fill(matrix[i], (byte) 0);
		
		graph.edges().forEach(e -> {
			int j = e.getIndex();
			matrix[e.getSourceNode().getIndex()][j] += e.isDirected() ? -1 : 1;
			matrix[e.getTargetNode().getIndex()][j] += 1;
		});
	}

	/**
	 * Returns the incidence matrix of a graph.
	 * 
	 * The incidence matrix of a graph is a <i>n</i> times <i>m</i> matrix
	 * {@code a}, where <i>n</i> is the number of nodes and <i>m</i> is the
	 * number of edges of the graph. The coefficients {@code a[i][j]} of this
	 * matrix have the following values:
	 * <ul>
	 * <li>-1 if {@code graph.getEdge(j)} is directed and
	 * {@code graph.getNode(i)} is its source.</li>
	 * <li>1 if {@code graph.getEdge(j)} is undirected and
	 * {@code graph.getNode(i)} is its source.</li>
	 * <li>1 if {@code graph.getNode(i)} is the target of
	 * {@code graph.getEdge(j)}.</li>
	 * <li>0 otherwise.</li>
	 * </ul>
	 * In the special case when the j-th edge is a loop connecting the i-th node
	 * to itself, the coefficient {@code a[i][j]} is 0 if the loop is directed
	 * and 2 if the loop is undirected. All the other coefficients in the j-th
	 * column are 0.
	 * 
	 * @param graph
	 *            A graph
	 * @return The incidence matrix of the graph.
	 * @see #fillIncidenceMatrix(Graph, byte[][])
	 * @complexity <i>O(mn)</i>, where <i>n</i> is the number of nodes and
	 *             <i>m</i> is the number of edges.
	 */
	public static byte[][] getIncidenceMatrix(Graph graph) {
		byte[][] matrix = new byte[graph.getNodeCount()][graph.getEdgeCount()];
		fillIncidenceMatrix(graph, matrix);
		return matrix;
	}

	/**
	 * Compute coordinates of nodes using a layout algorithm.
	 * 
	 * @param g
	 *            the graph
	 * @param layout
	 *            layout algorithm to use for computing coordinates
	 * @param stab
	 *            stabilization limit
	 */
	public static void computeLayout(Graph g, Layout layout, double stab) {
		GraphReplay r = new GraphReplay(g.getId());

		stab = Math.min(stab, 1);

		layout.addAttributeSink(g);
		r.addSink(layout);
		r.replay(g);
		r.removeSink(layout);

		layout.shake();
		layout.compute();

		do
			layout.compute();
		while (layout.getStabilization() < stab);
		
		layout.removeAttributeSink(g);
	}

	/**
	 * Compute coordinates of nodes using default layout algorithm (SpringBox).
	 * 
	 * @param g
	 *            the graph
	 * @param stab
	 *            stabilization limit
	 */
	public static void computeLayout(Graph g, double stab) {
		computeLayout(g, new SpringBox(), stab);
	}

	/**
	 * Compute coordinates of nodes using default layout algorithm and default
	 * stabilization limit.
	 * 
	 * @param g
	 *            the graph
	 */
	public static void computeLayout(Graph g) {
		computeLayout(g, new SpringBox(), 0.99);
	}

	/**
	 * Returns a random subset of nodes of fixed size. Each node has the same
	 * chance to be chosen.
	 * 
	 * @param graph
	 *            A graph.
	 * @param k
	 *            The size of the subset.
	 * @return A random subset of nodes of size <code>k</code>.
	 * @throws IllegalArgumentException
	 *             If <code>k</code> is negative or greater than the number of
	 *             nodes.
	 * @complexity O(<code>k</code>)
	 */
	public static List<Node> randomNodeSet(Graph graph, int k) {
		return randomNodeSet(graph, k, new Random());
	}

	/**
	 * Returns a random subset of nodes of fixed size. Each node has the same
	 * chance to be chosen.
	 * 
	 * @param graph
	 *            A graph.
	 * @param k
	 *            The size of the subset.
	 * @param random
	 *            A source of randomness.
	 * @return A random subset of nodes of size <code>k</code>.
	 * @throws IllegalArgumentException
	 *             If <code>k</code> is negative or greater than the number of
	 *             nodes.
	 * @complexity O(<code>k</code>)
	 */
	public static <T extends Node> List<Node> randomNodeSet(Graph graph, int k,
			Random random) {
		if (k < 0 || k > graph.getNodeCount())
			throw new IllegalArgumentException("k must be between 0 and "
					+ graph.getNodeCount());
		Set<Integer> subset = RandomTools.randomKsubset(graph.getNodeCount(),
				k, null, random);
		List<Node> result = new ArrayList<Node>(subset.size());
		for (int i : subset)
			result.add(graph.getNode(i));
		return result;
	}

	/**
	 * Returns a random subset of nodes. Each node is chosen with given
	 * probability.
	 * 
	 * @param graph
	 *            A graph.
	 * @param p
	 *            The probability to choose each node.
	 * @return A random subset of nodes.
	 * @throws IllegalArgumentException
	 *             If <code>p</code> is negative or greater than one.
	 * @complexity In average O(<code>n * p<code>), where <code>n</code> is the
	 *             number of nodes.
	 */
	public static <T extends Node> List<Node> randomNodeSet(Graph graph, double p) {
		return randomNodeSet(graph, p, new Random());
	}

	/**
	 * Returns a random subset of nodes. Each node is chosen with given
	 * probability.
	 * 
	 * @param graph
	 *            A graph.
	 * @param p
	 *            The probability to choose each node.
	 * @param random
	 *            A source of randomness.
	 * @return A random subset of nodes.
	 * @throws IllegalArgumentException
	 *             If <code>p</code> is negative or greater than one.
	 * @complexity In average O(<code>n * p<code>), where <code>n</code> is the
	 *             number of nodes.
	 */
	public static <T extends Node> List<Node> randomNodeSet(Graph graph, double p,
			Random random) {
		if (p < 0 || p > 1)
			throw new IllegalArgumentException("p must be between 0 and 1");
		Set<Integer> subset = RandomTools.randomPsubset(graph.getNodeCount(),
				p, null, random);
		List<Node> result = new ArrayList<Node>(subset.size());
		for (int i : subset)
			result.add(graph.getNode(i));
		return result;
	}

	/**
	 * Returns a random subset of edges of fixed size. Each edge has the same
	 * chance to be chosen.
	 * 
	 * @param graph
	 *            A graph.
	 * @param k
	 *            The size of the subset.
	 * @return A random subset of edges of size <code>k</code>.
	 * @throws IllegalArgumentException
	 *             If <code>k</code> is negative or greater than the number of
	 *             edges.
	 * @complexity O(<code>k</code>)
	 */
	public static List<Edge> randomEdgeSet(Graph graph, int k) {
		return randomEdgeSet(graph, k, new Random());
	}

	/**
	 * Returns a random subset of edges of fixed size. Each edge has the same
	 * chance to be chosen.
	 * 
	 * @param graph
	 *            A graph.
	 * @param k
	 *            The size of the subset.
	 * @param random
	 *            A source of randomness.
	 * @return A random subset of edges of size <code>k</code>.
	 * @throws IllegalArgumentException
	 *             If <code>k</code> is negative or greater than the number of
	 *             edges.
	 * @complexity O(<code>k</code>)
	 */
	public static List<Edge> randomEdgeSet(Graph graph, int k,
			Random random) {
		if (k < 0 || k > graph.getEdgeCount())
			throw new IllegalArgumentException("k must be between 0 and "
					+ graph.getEdgeCount());
		Set<Integer> subset = RandomTools.randomKsubset(graph.getEdgeCount(),
				k, null, random);
		List<Edge> result = new ArrayList<Edge>(subset.size());
		for (int i : subset)
			result.add(graph.getEdge(i));
		return result;
	}

	/**
	 * Returns a random subset of edges. Each edge is chosen with given
	 * probability.
	 * 
	 * @param graph
	 *            A graph.
	 * @param p
	 *            The probability to choose each edge.
	 * @return A random subset of edges.
	 * @throws IllegalArgumentException
	 *             If <code>p</code> is negative or greater than one.
	 * @complexity In average O(<code>m * p<code>), where <code>m</code> is the
	 *             number of edges.
	 */
	public static List<Edge> randomEdgeSet(Graph graph, double p) {
		return randomEdgeSet(graph, p, new Random());
	}

	/**
	 * Returns a random subset of edges. Each edge is chosen with given
	 * probability.
	 * 
	 * @param graph
	 *            A graph.
	 * @param p
	 *            The probability to choose each edge.
	 * @param random
	 *            A source of randomness.
	 * @return A random subset of edges.
	 * @throws IllegalArgumentException
	 *             If <code>p</code> is negative or greater than one.
	 * @complexity In average O(<code>m * p<code>), where <code>m</code> is the
	 *             number of edges.
	 */
	public static List<Edge> randomEdgeSet(Graph graph, double p,
			Random random) {
		if (p < 0 || p > 1)
			throw new IllegalArgumentException("p must be between 0 and 1");
		Set<Integer> subset = RandomTools.randomPsubset(graph.getEdgeCount(),
				p, null, random);
		List<Edge> result = new ArrayList<Edge>(subset.size());
		for (int i : subset)
			result.add(graph.getEdge(i));
		return result;
	}
	
	/**
	 * Determines if a graph is (weakly) connected.
	 * 
	 * @param graph A graph.
	 * @return {@code true} if the graph is connected.
	 * @complexity O({@code m + n}) where {@code m} is the number of edges and {@code n} is the number of nodes.
	 */
	public static boolean isConnected(Graph graph) {
		if (graph.getNodeCount() == 0)
			return true;
		Iterator<Node> it = graph.getNode(0).getBreadthFirstIterator(false);
		int visited = 0;
		while (it.hasNext()) {
			it.next();
			visited++;
		}
		return visited == graph.getNodeCount();
	}
}
