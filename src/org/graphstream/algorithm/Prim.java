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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;

/**
 * Compute a spanning tree using the Prim algorithm.
 * 
 * <p>
 * Prim's algorithm is an algorithm which allows to find a minimal spanning tree
 * in a weighted connected graph. More informations on <a
 * href="http://en.wikipedia.org/wiki/Prim%27s_algorithm">Wikipedia</a>.
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * The following example generates a graph with the Dorogovtsev-Mendes generator
 * and then compute a spanning-tree using the Prim algorithm. The generator
 * creates random weights for edges that will be used by the Prim algorithm.
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
 * import org.graphstream.algorithm.Prim;
 * import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
 * 
 * public class PrimTest {
 *  
 * 	public static void main(String ... args) {
 * 		DorogovtsevMendesGenerator gen = new DorogovtsevMendesGenerator();
 * 		Graph graph = new DefaultGraph("Prim Test");
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
 * 		Prim prim = new Prim("ui.class", "intree", "notintree");
 * 
 * 		prim.init(graph);
 * 		prim.compute();
 *  }
 * }
 * </pre>
 * 
 * @complexity 0(m+m<sup>2</sup>log(m)), where m = |E|
 * @reference R. C. Prim: Shortest connection networks and some generalizations.
 *            In: Bell System Technical Journal, 36 (1957), pp. 1389–1401
 * @see org.graphstream.algorithm.AbstractSpanningTree
 * 
 */
public class Prim extends AbstractSpanningTree {
	/**
	 * Attribute key which will be used to compare edges.
	 */
	protected String weightAttribute;

	/**
	 * Create a new Prim's algorithm.
	 */
	public Prim() {
		this("weight", "Kruskal.flag");
	}

	/**
	 * Create a new Prim's algorithm.
	 * 
	 * @param weightAttribute
	 *            attribute used to compare edges
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 */
	public Prim(String weightAttribute, String flagAttribute) {
		this(weightAttribute, flagAttribute, true, false);
	}

	/**
	 * Create a new Prim's algorithm.
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
	public Prim(String flagAttribute, Object flagOn, Object flagOff) {
		this("weight", flagAttribute, flagOn, flagOff);
	}

	/**
	 * Create a new Prim's algorithm.
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
	public Prim(String weightAttribute, String flagAttribute, Object flagOn,
			Object flagOff) {
		super(flagAttribute, flagOn, flagOff);

		this.weightAttribute = weightAttribute;
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

	/**
	 * Get weight of an edge.
	 * 
	 * @param e
	 *            an edge
	 * @return weight of <i>n</i>
	 */
	@SuppressWarnings("unchecked")
	protected Comparable getWeight(Edge e) {
		if (!e.hasAttribute(weightAttribute))
			return Double.valueOf(1);

		return (Comparable) e.getAttribute(weightAttribute, Comparable.class);
	}

	/**
	 * Check if all edges have a weight attribute and that this attribute is an
	 * instance of Comparable.
	 * 
	 * @see java.lang.Comparable
	 */
	protected void checkWeights() {
		Iterator<? extends Edge> iteE;
		boolean error = false;

		iteE = this.graph.getEdgeIterator();

		while (iteE.hasNext()) {
			if (!iteE.next().hasAttribute(weightAttribute, Comparable.class)) {
				error = true;
			}
		}

		if (error) {
			System.err
					.printf("*** error *** Prim's algorithm: some edges seem to not have a weigth.");
		}
	}

	/**
	 * Build the tree.
	 */
	@Override
	protected void makeTree() {
		checkWeights();

		Iterator<? extends Edge> iteE;
		Node current;
		Edge e;
		LinkedList<Node> pool = new LinkedList<Node>();
		LinkedList<Edge> epool = new LinkedList<Edge>();
		WeightEdgeComparator cmp = new WeightEdgeComparator();
		boolean resort = false;

		current = this.graph.getNodeIterator().next();

		pool.add(current);
		iteE = current.getLeavingEdgeIterator();
		while (iteE.hasNext()) {
			epool.add(iteE.next());
		}

		Collections.sort(epool, cmp);

		while (pool.size() < graph.getNodeCount()) {

			if (epool.size() == 0) {
				//
				// This case is triggered is there are several connected
				// components in the graph. A node which has not been used
				// is selected to continue the process.
				//
				Iterator<? extends Node> nodes = this.graph.getNodeIterator();
				Node toAdd = null;

				while (toAdd == null && nodes.hasNext()) {
					toAdd = nodes.next();

					if (pool.contains(toAdd.getId()))
						toAdd = null;

					if (toAdd != null && toAdd.getDegree() == 0) {
						pool.add(toAdd);
						toAdd = null;
					}
				}

				if (toAdd != null) {
					pool.add(toAdd);

					iteE = toAdd.getLeavingEdgeIterator();
					while (iteE.hasNext()) {
						epool.add(iteE.next());
					}
				}
			}

			e = epool.poll();

			if (e == null)
				throw new NullPointerException();

			current = null;

			if (!pool.contains(e.getNode0()))
				current = e.getNode0();
			if (!pool.contains(e.getNode1()))
				current = e.getNode1();

			if (current == null)
				continue;

			edgeOn(e);
			pool.add(current);

			for (int i = 0; i < epool.size();) {
				Edge tmp = epool.get(i);
				if (tmp.getNode0().equals(current)
						|| tmp.getNode1().equals(current)) {
					epool.remove(i);
				} else
					i++;
			}

			iteE = current.getLeavingEdgeIterator();
			resort = false;
			while (iteE.hasNext()) {
				e = iteE.next();

				if ((!pool.contains(e.getNode0()) && pool
						.contains(e.getNode1()))
						|| (pool.contains(e.getNode0()) && !pool.contains(e
								.getNode1()))) {
					epool.add(e);
					resort = true;
				}
			}

			if (resort) {
				Collections.sort(epool, cmp);
			}
		}
	}

	// Stuff needed to work

	/**
	 * A comparator which uses the <i>weightAttribute</i> of its parent's class
	 * to compare edges.
	 * 
	 * @author Guilhelm Savin
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
