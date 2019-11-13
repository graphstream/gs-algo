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
 * @since 2010-12-01
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.HashSet;
import java.util.StringJoiner;
import java.util.concurrent.atomic.DoubleAccumulator;

import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Compute the centroid of a connected graph.
 * 
 * <p>
 * In a graph G, if d(u,v) is the shortest length between two nodes u and v (ie
 * the number of edges of the shortest path) let m(u) be the sum of d(u,v) for
 * all nodes v of G. Centroid of a graph G is a subgraph induced by vertices u
 * with minimum m(u).
 * </p>
 * 
 * <h2>Requirements</h2>
 * 
 * <p>
 * This algorithm needs that APSP algorithm has been computed before its own
 * computation.
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * import java.io.StringReader;
 * import java.io.IOException;
 * 
 * import org.graphstream.algorithm.APSP;
 * import org.graphstream.algorithm.Centroid;
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.Node;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * import org.graphstream.stream.file.FileSourceDGS;
 * 
 * //                     +--- E
 * // A --- B --- C -- D -|--- F
 * //                     +--- G
 * 
 * public class CentroidTest {
 * 	static String my_graph = &quot;DGS004\n&quot; + &quot;my 0 0\n&quot; + &quot;an A \n&quot; + &quot;an B \n&quot;
 * 			+ &quot;an C \n&quot; + &quot;an D \n&quot; + &quot;an E \n&quot; + &quot;an F \n&quot; + &quot;an G \n&quot;
 * 			+ &quot;ae AB A B \n&quot; + &quot;ae BC B C \n&quot; + &quot;ae CD C D \n&quot; + &quot;ae DE D E \n&quot;
 * 			+ &quot;ae DF D F \n&quot; + &quot;ae DG D G \n&quot;;
 * 
 * 	public static void main(String[] args) throws IOException {
 * 		Graph graph = new DefaultGraph(&quot;Centroid Test&quot;);
 * 		StringReader reader = new StringReader(my_graph);
 * 
 * 		FileSourceDGS source = new FileSourceDGS();
 * 		source.addSink(graph);
 * 		source.readAll(reader);
 * 
 * 		APSP apsp = new APSP();
 * 		apsp.init(graph);
 * 		apsp.compute();
 * 
 * 		Centroid centroid = new Centroid();
 * 		centroid.init(graph);
 * 		centroid.compute();
 * 
 * 		for (Node n : graph.getEachNode()) {
 * 			Boolean in = n.getAttribute(&quot;centroid&quot;);
 * 
 * 			System.out.printf(&quot;%s is%s in the centroid.\n&quot;, n.getId(), in ? &quot;&quot;
 * 					: &quot; not&quot;);
 * 		}
 * 
 * 		// Output will be :
 * 		//
 * 		// A is not in the centroid
 * 		// B is not in the centroid
 * 		// C is not in the centroid
 * 		// D is in the centroid
 * 		// E is not in the centroid
 * 		// F is not in the centroid
 * 		// G is not in the centroid
 * 	}
 * }
 * </pre>
 * 
 * @complexity O(n2)
 * @see org.graphstream.algorithm.APSP.APSPInfo
 * @reference F. Harary, Graph Theory. Westview Press, Oct. 1969. [Online].
 *            Available: http://www.amazon.com/exec/obidos/
 *            redirect?tag=citeulike07-20\&path=ASIN/ 0201410338
 */
public class Centroid implements Algorithm {
	/**
	 * The graph on which centroid is computed.
	 */
	protected Graph graph;

	/**
	 * Attribute in which APSPInfo are stored.
	 */
	protected String apspInfoAttribute;

	/**
	 * Attribute to store centroid information.
	 */
	protected String centroidAttribute;

	/**
	 * Value of the attribute if node is in the centroid.
	 */
	protected Object isInCentroid;

	/**
	 * Value of the attribute if node is not in the centroid.
	 */
	protected Object isNotInCentroid;

	/**
	 * Build a new centroid algorithm with default parameters.
	 */
	public Centroid() {
		this("centroid");
	}

	/**
	 * Build a new centroid algorithm, specifying the attribute name of the
	 * computation result
	 * 
	 * @param centroidAttribute
	 *            attribute name of the computation result.
	 */
	public Centroid(String centroidAttribute) {
		this(centroidAttribute, Boolean.TRUE, Boolean.FALSE);
	}

	/**
	 * Build a new centroid as in {@link #Centroid(String)} but specifying
	 * values of centroid membership.
	 * 
	 * @param centroidAttribute
	 *            attribute name of the computation result.
	 * @param isInCentroid
	 *            the value of elements centroid attribute when this element is
	 *            in the centroid.
	 * @param isNotInCentroid
	 *            the value of elements centroid attribute when this element is
	 *            not in the centroid.
	 */
	public Centroid(String centroidAttribute, Object isInCentroid,
			Object isNotInCentroid) {
		this(centroidAttribute, Boolean.TRUE, Boolean.FALSE,
				APSP.APSPInfo.ATTRIBUTE_NAME);
	}

	/**
	 * Build a new centroid algorithm as in
	 * {@link #Centroid(String, Object, Object)} but specifying the name of the
	 * attribute where the APSP informations are stored.
	 * 
	 * @param centroidAttribute
	 *            attribute name of the computation result.
	 * @param isInCentroid
	 *            the value of elements centroid attribute when this element is
	 *            in the centroid.
	 * @param isNotInCentroid
	 *            the value of elements centroid attribute when this element is
	 *            not in the centroid.
	 * @param apspInfoAttribute
	 *            the name of the attribute where the APSP informations are
	 *            stored
	 */
	public Centroid(String centroidAttribute, Object isInCentroid,
			Object isNotInCentroid, String apspInfoAttribute) {
		this.centroidAttribute = centroidAttribute;
		this.isInCentroid = isInCentroid;
		this.isNotInCentroid = isNotInCentroid;
		this.apspInfoAttribute = apspInfoAttribute;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		//float min = Float.MAX_VALUE;
		DoubleAccumulator min = new DoubleAccumulator((x, y) -> y, Double.MAX_VALUE);
		HashSet<Node> centroid = new HashSet<Node>();
		
		graph.nodes().forEach(node -> {
			DoubleAccumulator m = new DoubleAccumulator((x, y) -> x + y, 0);
			APSP.APSPInfo info = (APSPInfo) node.getAttribute(apspInfoAttribute);

			if (info == null)
				System.err
						.printf("APSPInfo missing. Did you compute APSP before ?\n");
			
			graph.nodes().forEach(other -> {
				if (node != other) {
					double d = info.getLengthTo(other.getId());

					if (d < 0)
						System.err
								.printf("Found a negative length value in centroid algorithm. "
										+ "Is graph connected ?\n");
					else
						m.accumulate(d);
				}
			});

			if (m.get() < min.get()) {
				centroid.clear();
				centroid.add(node);
				min.accumulate(m.get());
			} else if (m.get() == min.get()) {
				centroid.add(node);
			}
		});
		
		graph.nodes().forEach(node ->
			node.setAttribute(centroidAttribute, centroid.contains(node) ? isInCentroid : isNotInCentroid));


		centroid.clear();
	}

	/**
	 * Get the APSP info attribute name.
	 * 
	 * @return the name of the attribute where the APSP informations are stored.
	 */
	public String getAPSPInfoAttribute() {
		return apspInfoAttribute;
	}

	/**
	 * Set the APSP info attribute name.
	 * 
	 * @param attribute
	 *            the name of the attribute where the APSP informations are
	 *            stored.
	 */
	@Parameter
	public void setAPSPInfoAttribute(String attribute) {
		apspInfoAttribute = attribute;
	}

	/**
	 * Get the value of the centroid attribute when element is in the centroid.
	 * Default value is Boolean.TRUE.
	 * 
	 * @return the value of elements centroid attribute when this element is in
	 *         the centroid.
	 */
	public Object getIsInCentroidValue() {
		return isInCentroid;
	}

	/**
	 * Set the value of the centroid attribute when element is in the centroid.
	 * On computation, this value is used to set the centroid attribute.
	 * 
	 * @param value
	 *            the value of elements centroid attribute when this element is
	 *            in the centroid.
	 */
	@Parameter
	public void setIsInCentroidValue(Object value) {
		isInCentroid = value;
	}

	/**
	 * Get the value of the centroid attribute when element is not in the
	 * centroid. Default value is Boolean.FALSE.
	 * 
	 * @return the value of elements centroid attribute when this element is not
	 *         in the centroid.
	 */
	public Object getIsNotInCentroidValue() {
		return isNotInCentroid;
	}

	/**
	 * Set the value of the centroid attribute when element is not in the
	 * centroid. On computation, this value is used to set the centroid
	 * attribute.
	 * 
	 * @param value
	 *            the value of elements centroid attribute when this element is
	 *            not in the centroid.
	 */
	@Parameter
	public void setIsNotInCentroidValue(Object value) {
		isNotInCentroid = value;
	}

	/**
	 * Get the name of the attribute where computation result is stored. Value
	 * of this attribute can be {@link #getIsInCentroidValue()} if the element
	 * is in the centroid, {@link #getIsNotInCentroidValue()} else.
	 * 
	 * @return the centroid attribute name.
	 */
	public String getCentroidAttribute() {
		return centroidAttribute;
	}

	/**
	 * Set the name of the attribute where computation result is stored.
	 * 
	 * @param centroidAttribute
	 *            the name of the element attribute where computation result is
	 *            stored.
	 */
	@Parameter
	public void setCentroidAttribute(String centroidAttribute) {
		this.centroidAttribute = centroidAttribute;
	}
	
	@Result
	public String defaultResult() {
		StringJoiner sj = new StringJoiner(" | ", "====== Centroid ====== \n", "");
		graph.nodes()
			.filter(n -> ((Boolean) n.getAttribute(centroidAttribute)))
			.forEach(n -> {
				sj.add(n.getId());
			});
		
		return sj.toString();
	}
}
