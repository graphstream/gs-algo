/*
 * Copyright 2006 - 2012
 *      Stefan Balev       <stefan.balev@graphstream-project.org>
 *      Julien Baudry	<julien.baudry@graphstream-project.org>
 *      Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *      Yoann Pigné	<yoann.pigne@graphstream-project.org>
 *      Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
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

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Compute a spanning tree using the Kruskal algorithm.
 * 
 * <p>
 * Kruskal's algorithm is a greedy algorithm which allows to find a minimal
 * spanning tree in a weighted connected graph. More informations on <a
 * href="http://en.wikipedia.org/wiki/Kruskal%27s_algorithm">Wikipedia</a>.
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * The following example generates a graph with the Dorogovtsev-Mendes generator
 * and then compute a spanning-tree using the Kruskal algorithm. The generator
 * creates random weights for edges that will be used by the Kruskal algorithm.
 * 
 * If no weight is present, algorithm considers that all weights are set to 1.
 * 
 * When an edge is in the spanning tree, the algorithm will set its "ui.class"
 * attribute to "intree", else the attribute is set to "notintree". According to
 * the css stylesheet that is defined, spanning will be displayed with thick
 * black lines while edges not in the spanning tree will be displayed with thin
 * gray lines.
 * 
 * <pre>
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * 
 * import org.graphstream.algorithm.Kruskal;
 * import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
 * 
 * public class KruskalTest {
 *  
 * 	public static void main(String .. args) {
 * 		DorogovtsevMendesGenerator gen = new DorogovtsevMendesGenerator();
 * 		Graph graph = new DefaultGraph("Kruskal Test");
 * 
 *  	String css = "edge .notintree {size:1px;fill-color:gray;} " +
 *  				 "edge .intree {size:3px;fill-color:black;}";
 *  
 * 		graph.addAttribute("ui.stylesheet", css);
 * 		graph.display();
 * 
 * 		gen.addEdgeAttribute("weight");
 * 		gen.setEdgeAttributesRange(1, 100);
 * 		gen.addSink(graph);
 * 		gen.begin();
 * 		for (int i = 0; i < 100 && gen.nextEvents(); i++)
 * 			;
 * 		gen.end();
 * 
 * 		Kruskal kruskal = new Kruskal("ui.class", "intree", "notintree");
 * 
 * 		kruskal.init(g);
 * 		kruskal.compute();
 *  }
 * }
 * </pre>
 * 
 * @complexity m*(log(m)+3)+n+n<sup>2</sup>, m = |E|, n = |V|
 * @reference Joseph. B. Kruskal: On the Shortest Spanning Subtree of a Graph
 *            and the Traveling Salesman Problem. In: Proceedings of the
 *            American Mathematical Society, Vol 7, No. 1 (Feb, 1956), pp. 48–50
 * @see org.graphstream.algorithm.AbstractSpanningTree
 * 
 */
public class Kruskal extends AbstractSpanningTree {
	/**
	 * Attribute which will be used to compare edges.
	 */
	protected String weightAttribute;

	/**
	 * Attribute used to clusterize the graph.
	 */
	protected String clusterAttribute = "Kruskal.cluster";

	/**
	 * List of edges that will be added to the tree.
	 */
	protected LinkedList<Edge> edgesToTreat;

	/**
	 * Create a new Kruskal's algorithm.
	 */
	public Kruskal() {
		this("weight", "Kruskal.flag");
	}

	/**
	 * Create a new Kruskal's algorithm.
	 * 
	 * @param weightAttribute
	 *            attribute used to compare edges
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 */
	public Kruskal(String weightAttribute, String flagAttribute) {
		this(weightAttribute, flagAttribute, true, false);
	}

	/**
	 * Create a new Kruskal's algorithm.
	 * 
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 * @param flagOn
	 *            value of the <i>flagAttribute</i> if edge is in the spanning
	 *            tree
	 * @param flagOff
	 *            value of the <i>flagAttribute</i> if edge is not in the
	 *            spanning tree
	 */
	public Kruskal(String flagAttribute, Object flagOn, Object flagOff) {
		this("weight", flagAttribute, flagOn, flagOff);
	}

	/**
	 * Create a new Kruskal's algorithm.
	 * 
	 * @param weightAttribute
	 *            attribute used to compare edges
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 * @param flagOn
	 *            value of the <i>flagAttribute</i> if edge is in the spanning
	 *            tree
	 * @param flagOff
	 *            value of the <i>flagAttribute</i> if edge is not in the
	 *            spanning tree
	 */
	public Kruskal(String weightAttribute, String flagAttribute, Object flagOn,
			Object flagOff) {
		super(flagAttribute, flagOn, flagOff);

		this.weightAttribute = weightAttribute;
		this.edgesToTreat = new LinkedList<Edge>();
	}

	/**
	 * Get key attribute used to compare edges.
	 * 
	 * @return weight attribute
	 */
	public String getWeightAttribute() {
		return this.weightAttribute;
	}

	/**
	 * Set the weight attribute.
	 * 
	 * @param newWeightAttribute
	 *            new attribute used
	 */
	public void setWeightAttribute(String newWeightAttribute) {
		this.weightAttribute = newWeightAttribute;
	}

	// Protected Access

	/**
	 * Sort edges using <i>weightAttribute</i> to compare.
	 */
	protected void sortEdgesByWeight() {
		Collections.sort(edgesToTreat, new WeightEdgeComparator());
	}

	/**
	 * Create the <i>edgesToTreat</i> list. Also check if all edges as a
	 * <i>weightAttribute</i> which is an instance of Comparable.
	 * 
	 * @see java.lang.Comparable
	 */
	protected void buildAndCheck() {
		Iterator<? extends Edge> iteE;
		boolean error = false;

		edgesToTreat.clear();

		iteE = this.graph.getEdgeIterator();

		while (iteE.hasNext()) {
			edgesToTreat.addLast(iteE.next());
			if (!edgesToTreat.getLast().hasAttribute(weightAttribute,
					Comparable.class)) {
				error = true;
			}
		}

		if (error) {
			System.err
					.printf("*** error *** Kruskal's algorithm: some weight are not comparable%n");
		}
	}

	/**
	 * Reset cluster and flag attribute values.
	 */
	@Override
	protected void resetFlags() {
		super.resetFlags();

		Iterator<? extends Node> iteN;
		int cluster = 0;

		iteN = this.graph.getNodeIterator();

		while (iteN.hasNext()) {
			iteN.next().setAttribute(clusterAttribute, cluster++);
		}
	}

	/**
	 * Get weight of an edge.
	 * 
	 * @param e
	 *            an edge
	 * @return weight of <i>e</i>
	 */
	protected Double getWeight(Edge e) {
		if (!e.hasNumber(weightAttribute))
			return Double.valueOf(1);

		return e.getNumber(weightAttribute);
	}

	/**
	 * Get cluster of a node.
	 * 
	 * @param n
	 *            a node
	 * @return cluster of <i>n</i>
	 */
	protected int getCluster(Node n) {
		return (Integer) n.getAttribute(clusterAttribute);
	}

	/**
	 * Build the spanning tree.
	 */
	@Override
	protected void makeTree() {
		buildAndCheck();
		sortEdgesByWeight();

		int treeSize = 0, c1, c2;
		Edge e = null;

		while (treeSize < graph.getNodeCount() - 1) {
			if (edgesToTreat.size() == 0) {
				System.err
						.printf("*** warning *** Kruskal's algorithm: error while making tree%n");
				break;
			}

			e = edgesToTreat.poll();
			c1 = getCluster(e.getNode0());
			c2 = getCluster(e.getNode1());

			if (c1 != c2) {
				edgeOn(e);
				treeSize++;
				mergeClusters(e.getNode0(), e.getNode1());
			}
		}
	}

	/**
	 * Merge two clusters.
	 * 
	 * @param n0
	 *            first node
	 * @param n1
	 *            second node
	 */
	protected void mergeClusters(Node n0, Node n1) {
		int c1 = getCluster(n0);
		int c2 = getCluster(n1);

		LinkedList<Node> pool = new LinkedList<Node>();
		Node current = null;
		Iterator<? extends Node> iteN = null;

		pool.add(n1);

		while (pool.size() > 0) {
			current = pool.poll();
			current.setAttribute(clusterAttribute, c1);

			iteN = current.getNeighborNodeIterator();
			while (iteN.hasNext()) {
				current = iteN.next();
				if (getCluster(current) == c2 && !pool.contains(current)) {
					pool.add(current);
				}
			}
		}
	}

	// Stuff needed to work

	/**
	 * A comparator which uses the <i>weightAttribute</i> of its parent's class
	 * to compare edges.
	 */
	private final class WeightEdgeComparator implements Comparator<Edge> {
		/**
		 * Compare two edges.
		 * 
		 * @return an integer less than 0 if e1 less than e2, more than 0 if e1
		 *         more than e2
		 */
		@SuppressWarnings("all")
		public int compare(Edge e1, Edge e2) {
			return getWeight(e1).compareTo(getWeight(e2));
		}

		@Override
		public boolean equals(Object o) {
			return o instanceof WeightEdgeComparator;
		}
	}
}
