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

import java.util.LinkedList;
import java.util.StringJoiner;

import org.graphstream.algorithm.util.FibonacciHeap;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

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
 * 	public static void main(String... args) {
 * 		DorogovtsevMendesGenerator gen = new DorogovtsevMendesGenerator();
 * 		Graph graph = new DefaultGraph(&quot;Prim Test&quot;);
 * 
 * 		String css = &quot;edge .notintree {size:1px;fill-color:gray;} &quot;
 * 				+ &quot;edge .intree {size:3px;fill-color:black;}&quot;;
 * 
 * 		graph.addAttribute(&quot;ui.stylesheet&quot;, css);
 * 		graph.display();
 * 
 * 		gen.addEdgeAttribute(&quot;weight&quot;);
 * 		gen.setEdgeAttributesRange(1, 100);
 * 		gen.addSink(graph);
 * 		gen.begin();
 * 		for (int i = 0; i &lt; 100 &amp;&amp; gen.nextEvents(); i++)
 * 			;
 * 		gen.end();
 * 
 * 		Prim prim = new Prim(&quot;ui.class&quot;, &quot;intree&quot;, &quot;notintree&quot;);
 * 
 * 		prim.init(graph);
 * 		prim.compute();
 * 	}
 * }
 * </pre>
 * 
 * @complexity 0(m + n log n), where m is the number of edges and n is the
 *             number of nodes in the graph
 * @reference R. C. Prim: Shortest connection networks and some generalizations.
 *            In: Bell System Technical Journal, 36 (1957), pp. 1389–1401
 * @see org.graphstream.algorithm.AbstractSpanningTree
 * 
 */
public class Prim extends Kruskal {

	/**
	 * Create a new Prim's algorithm. Uses the default weight attribute and
	 * does not tag the edges.
	 */
	public Prim() {
		super();
	}

	/**
	 * Create a new Prim's algorithm. The value of the flag attribute is
	 * {@code true} for the tree edges and false for the non-tree edges.
	 * 
	 * @param weightAttribute
	 *            attribute used to compare edges
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 */
	public Prim(String weightAttribute, String flagAttribute) {
		super(weightAttribute, flagAttribute);
	}

	/**
	 * Create a new Prim's algorithm. Uses the default weight attribute.
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
		super(flagAttribute, flagOn, flagOff);
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
		super(weightAttribute, flagAttribute, flagOn, flagOff);
	}

	@Override
	protected void makeTree() {
		if (treeEdges == null)
			treeEdges = new LinkedList<Edge>();
		else
			treeEdges.clear();

		int n = graph.getNodeCount();
		Data[] data = new Data[n];
		FibonacciHeap<Double, Node> heap = new FibonacciHeap<Double, Node>();
		for (int i = 0; i < n; i++) {
			data[i] = new Data();
			data[i].edgeToTree = null;
			data[i].fn = heap.add(Double.POSITIVE_INFINITY, graph.getNode(i));
		}

		treeWeight = 0;
		while (!heap.isEmpty()) {
			Node u = heap.extractMin();
			Data dataU = data[u.getIndex()];
			data[u.getIndex()] = null;
			if (dataU.edgeToTree != null) {
				treeEdges.add(dataU.edgeToTree);
				edgeOn(dataU.edgeToTree);
				treeWeight += dataU.fn.getKey();
				dataU.edgeToTree = null;
			}
			dataU.fn = null;
			
			u.edges()
				.filter(e -> data[e.getOpposite(u).getIndex()] != null)
				.forEach(e -> {
					Node v = e.getOpposite(u);
					Data dataV = data[v.getIndex()];
					
					double w = getWeight(e);
					if (w < dataV.fn.getKey()) {
						heap.decreaseKey(dataV.fn, w);
						dataV.edgeToTree = e;
					}
				});
		}
	}
	
	@Result
	public String defaultResult() {
		//return getPath(graph.getNode(target));
		
		StringJoiner sj = new StringJoiner(" | ", "====== Prim ====== \n", "");
		getTreeEdgesStream()
			.forEach(n -> {
				sj.add(n.getId());
			});
		
		return sj.toString();
	}
	
	protected static class Data {
		Edge edgeToTree;
		FibonacciHeap<Double, Node>.Node fn;
	}
}
