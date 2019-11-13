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
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.coloring;

import java.util.LinkedList;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.util.FibonacciHeap;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Welsh Powell static graph coloring algorithm.
 * 
 * <p>
 * This class is intended to implement the Welsh-Powell algorithm for the
 * problem of graph coloring. It provides a greedy algorithm that runs on a
 * static graph.
 * </p>
 * 
 * <p>
 * This is an iterative greedy algorithm:
 * <ul>
 * <li>Step 1: All vertices are sorted according to the decreasing value of
 * their degree in a list V.
 * <li>Step 2: Colors are ordered in a list C.
 * <li>Step 3: The first non colored vertex v in V is colored with the first
 * available color in C. <i>available</i> means a color that was not previously
 * used by the algorithm.
 * <li>Step 4: The remaining part of the ordered list V is traversed and the
 * same color is allocated to every vertex for which no adjacent vertex has the
 * same color.
 * <li>Step 5: Steps 3 and 4 are applied iteratively until all the vertices have
 * been colored.
 * </ul>
 * <p>
 * 
 * <p>
 * Note that the given colors are not real colors. Instead they are positive
 * integers starting 0. So, for instance, if a colored graph's chromatic number
 * is 3, then nodes will be "colored" with one of 0, 1 or 2.
 * </p>
 * 
 * 
 * <p>
 * After computation (using {@link #compute()}, the algorithm result for the
 * computation, the chromatic number, is accessible with the
 * {@link #getChromaticNumber()} method. Colors (of "Integer" type) are stored
 * in the graph as attributes (one for each node). By default the attribute name
 * is "WelshPowell.color", but you can optional choose the attribute name.
 * </p>
 * 
 * 
 * 
 * <h2>Example</h2> import java.io.IOException; import java.io.StringReader;
 * 
 * import org.graphstream.algorithm.coloring.WelshPowell; import
 * org.graphstream.graph.ElementNotFoundException; import
 * org.graphstream.graph.Graph; import org.graphstream.graph.Node; import
 * org.graphstream.graph.implementations.DefaultGraph; import
 * org.graphstream.stream.GraphParseException; import
 * org.graphstream.stream.file.FileSourceDGS;
 * 
 * public class WelshPowellTest { // B-(1)-C // / \ // (1) (10) // / \ // A F //
 * \ / // (1) (1) // \ / // D-(1)-E static String my_graph = "DGS004\n" +
 * "my 0 0\n" + "an A \n" + "an B \n" + "an C \n" + "an D \n" + "an E \n" +
 * "an F \n" + "ae AB A B weight:1 \n" + "ae AD A D weight:1 \n" +
 * "ae BC B C weight:1 \n" + "ae CF C F weight:10 \n" + "ae DE D E weight:1 \n"
 * + "ae EF E F weight:1 \n" ; public static void main(String[] args) throws
 * IOException, ElementNotFoundException, GraphParseException { Graph graph =
 * new DefaultGraph("Welsh Powell Test"); StringReader reader = new
 * StringReader(my_graph);
 * 
 * FileSourceDGS source = new FileSourceDGS(); source.addSink(graph);
 * source.readAll(reader);
 * 
 * WelshPowell wp = new WelshPowell("color"); wp.init(graph); wp.compute();
 * 
 * System.out.println("The chromatic number of this graph is : "+wp.
 * getChromaticNumber()); for(Node n : graph){
 * System.out.println("Node "+n.getId()+ " : color " +n.getAttribute("color"));
 * } } } </pre>
 * 
 * This shall return:
 * 
 * <pre>
 * The chromatic number of this graph is : 3
 * Node D : color 0
 * Node E : color 2 
 * Node F : color 1
 * Node A : color 2
 * Node B : color 1
 * Node C : color 0
 * </pre>
 * 
 * 
 * <h2>Extra Feature</h2>
 * 
 * <p>
 * Consider you what to display the result of they coloring algorithm on a
 * displayed graph, then adding the following code to the previous example may
 * help you:
 * </p>
 * 
 * <pre>
 * Color[] cols = new Color[wp.getChromaticNumber()];
 * for (int i = 0; i &lt; wp.getChromaticNumber(); i++) {
 * 	cols[i] = Color.getHSBColor((float) (Math.random()), 0.8f, 0.9f);
 * }
 * for (Node n : graph) {
 * 	int col = (int) n.getNumber(&quot;color&quot;);
 * 	n.addAttribute(&quot;ui.style&quot;, &quot;fill-color:rgba(&quot; + cols[col].getRed() + &quot;,&quot;
 * 			+ cols[col].getGreen() + &quot;,&quot; + cols[col].getBlue() + &quot;,200);&quot;);
 * }
 * 
 * graph.display();
 * </pre>
 * 
 * 
 * 
 * @complexity This algorithm is known to use at most d(G)+1 colors where d(G)
 *             represents the largest value of the degree in the graph G.
 * 
 * @reference Welsh, D. J. A.; Powell, M. B. (1967),
 *            "An upper bound for the chromatic number of a graph and its application to timetabling problems"
 *            , The Computer Journal 10 (1): 85–86, doi:10.1093/comjnl/10.1.85
 * 
 * @version 0.1 30/08/2007
 * @author Frédéric Guinand
 * @author Antoine Dutot
 * @author Yoann Pigné
 */
public class WelshPowell implements Algorithm {

	/**
	 * Name of the attributes added to the graph.
	 */
	protected String attrName = "WelshPowell.color";

	/**
	 * The graph.
	 */
	protected Graph g;

	/**
	 * The algorithm's result : the chromatic number.
	 */
	protected int chromaticNumber;

	// Constructors

	/**
	 * New Welsh and Powell coloring algorithm.
	 * 
	 * @param attrName
	 *            name of the attribute corresponding to the color allocated by
	 *            this algorithm.
	 */
	public WelshPowell(String attrName) {
		this.attrName = attrName;
	}

	/**
	 * New Welsh and Powell coloring algorithm, using "WelshPowell.color" as the
	 * attribute name.
	 * 
	 */
	public WelshPowell() {
	}

	// Accessors

	/**
	 * Return the last computed result of the algorithm.
	 * 
	 * @return The number of colors.
	 * @see #compute()
	 */
	public int getChromaticNumber() {
		return chromaticNumber;
	}

	// Commands

	/**
	 * Set the name of the attribute to put in the graph if it is modified.
	 * 
	 * @param attrName
	 *            An attribute name.
	 */
	@Parameter
	public void setAttributeName(String attrName) {
		this.attrName = attrName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph g) {
		this.g = g;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		String attributeName = "WelshPowell.color";

		if (attrName != null)
			attributeName = attrName;

		// ------- STEP 1 -----------
		// the algorithm requires the use of a sorted list using
		// degree values for sorting them.

		LinkedList<Node> sortedNodes = new LinkedList<Node>();
		FibonacciHeap<Integer, Node> heap = new FibonacciHeap<Integer, Node>();

		for (int i = 0; i < g.getNodeCount(); i++) {
			Node n = g.getNode(i);
			heap.add(n.getDegree(), n);
		}

		while (!heap.isEmpty())
			sortedNodes.addFirst(heap.extractMin());

		heap = null;

		// ------ STEP 2 --------
		// color initialization

		int nbColors = 0;

		// ------- STEP 3 --------

		while (sortedNodes.size() > 0) {
			Node root = sortedNodes.poll();
			LinkedList<Node> myGroup = new LinkedList<Node>();
			myGroup.add(root);

			root.setAttribute(attributeName, nbColors);

			for (int i = 0; i < sortedNodes.size();) {
				Node p = sortedNodes.get(i);
				boolean conflict = false;

				for (int j = 0; !conflict && j < myGroup.size(); j++)
					conflict = p.getEdgeBetween(myGroup.get(j).getIndex()) != null;

				if (conflict)
					i++;
				else {
					p.setAttribute(attributeName, nbColors);
					myGroup.add(p);
					sortedNodes.remove(i);
				}
			}

			myGroup.clear();
			nbColors++;
		}

		chromaticNumber = nbColors;
	}
	
	@Result
	public String defaultMessage() {
		return "Result stored in \""+this.attrName+"\" attribute";
	}
}