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
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.graphstream.algorithm.util.DisjointSets;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

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
 * and then computes a spanning-tree using the Kruskal algorithm. The generator
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
 * @complexity O(m log n) where m is the number of edges and n is the number of
 *             nodes of the graph
 * @reference Joseph. B. Kruskal: On the Shortest Spanning Subtree of a Graph
 *            and the Traveling Salesman Problem. In: Proceedings of the
 *            American Mathematical Society, Vol 7, No. 1 (Feb, 1956), pp. 48–50
 * @see org.graphstream.algorithm.AbstractSpanningTree
 * 
 */
public class Kruskal extends AbstractSpanningTree {
	/**
	 * Default weight attribute
	 */
	public static final String DEFAULT_WEIGHT_ATTRIBUTE = "weight";

	/**
	 * Attribute where the weights of the edges are stored
	 */
	protected String weightAttribute;

	/**
	 * List of the tree edges. Used by the iterator.
	 */
	protected List<Edge> treeEdges;

	/**
	 * The weight of the spanning tree
	 */
	protected double treeWeight;

	/**
	 * Create a new Kruskal's algorithm. Uses the default weight attribute and
	 * does not tag the edges.
	 */
	public Kruskal() {
		this(DEFAULT_WEIGHT_ATTRIBUTE, null);
	}

	/**
	 * Create a new Kruskal's algorithm. The value of the flag attribute is
	 * {@code true} for the tree edges and false for the non-tree edges.
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
	 * Create a new Kruskal's algorithm. Uses the default weight attribute.
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
		this(DEFAULT_WEIGHT_ATTRIBUTE, flagAttribute, flagOn, flagOff);
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
	}

	/**
	 * Get weight attribute used to compare edges.
	 * 
	 * @return weight attribute
	 */
	public String getWeightAttribute() {
		return weightAttribute;
	}

	/**
	 * Set the weight attribute.
	 * 
	 * @param newWeightAttribute
	 *            new attribute used
	 */
	@Parameter
	public void setWeightAttribute(String newWeightAttribute) {
		this.weightAttribute = newWeightAttribute;
	}

	@Override
	protected void makeTree() {
		if (treeEdges == null)
			treeEdges = new LinkedList<Edge>();
		else
			treeEdges.clear();

		List<Edge> sortedEdges = new ArrayList<Edge>(graph.edges().collect(Collectors.toList()));
		Collections.sort(sortedEdges, new EdgeComparator());
		
		DisjointSets<Node> components = new DisjointSets<Node>(
				graph.getNodeCount());
		for (Node node : graph)
			components.add(node);
				
		treeWeight = 0;
		for (Edge edge : sortedEdges)
			if (components.union(edge.getNode0(), edge.getNode1())) {
				treeEdges.add(edge);
				edgeOn(edge);
				treeWeight += getWeight(edge);
				if (treeEdges.size() == graph.getNodeCount() - 1)
					break;
			}
		sortedEdges.clear();
		components.clear();
	}

	@Override
	public Stream<Edge> getTreeEdgesStream() {
		return StreamSupport.stream(
		    	Spliterators.spliteratorUnknownSize(
		    			new TreeIterator(),
		                Spliterator.DISTINCT |
		                Spliterator.IMMUTABLE |
		                Spliterator.NONNULL), false);
	}

	@Override
	public void clear() {
		super.clear();
		treeEdges.clear();
	}

	/**
	 * Returns the total weight of the minimum spanning tree
	 * 
	 * @return The sum of the weights of the edges in the spanning tree
	 */
	public double getTreeWeight() {
		return treeWeight;
	}

	// helpers

	protected double getWeight(Edge e) {
		if (weightAttribute == null)
			return 1.0;
		double w = e.getNumber(weightAttribute);
		if (Double.isNaN(w))
			return 1;
		return w;
	}

	@Result
	public String defaultResult() {
		//return getPath(graph.getNode(target));
		
		StringJoiner sj = new StringJoiner(" | ", "====== Kruskal ====== \n", "");
		getTreeEdgesStream()
			.forEach(n -> {
				sj.add(n.getId());
			});
		
		return sj.toString();
	}
	
	protected class EdgeComparator implements Comparator<Edge> {
		public int compare(Edge arg0, Edge arg1) {
			double w0 = getWeight(arg0);
			double w1 = getWeight(arg1);
			if (w0 < w1)
				return -1;
			if (w0 > w1)
				return 1;
			return 0;
		}
	}

	protected class TreeIterator implements Iterator<Edge> {

		protected Iterator<Edge> it = treeEdges.iterator();

		public boolean hasNext() {
			return it.hasNext();
		}

		public Edge next() {
			return it.next();
		}

		public void remove() {
			throw new UnsupportedOperationException(
					"This iterator does not support remove.");
		}
	}
}
