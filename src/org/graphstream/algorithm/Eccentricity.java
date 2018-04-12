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
 * Compute the eccentricity of a connected graph.
 * 
 * <p>
 * In a graph G, if d(u,v) is the shortest length between two nodes u and v (ie
 * the number of edges of the shortest path) let e(u) be the d(u,v) such that v
 * is the farthest of u. Eccentricity of a graph G is a subgraph induced by
 * vertices u with minimum e(u).
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
 * import org.graphstream.algorithm.Centroid;
 * import org.graphstream.graph.Graph;
 * import org.graphstream.graph.implementations.DefaultGraph;
 * import org.graphstream.stream.file.FileSourceDGS;
 * 
 * //                     +--- E
 * // A --- B --- C -- D -|--- F
 * //                     +--- G
 * 
 * public class EccentriciyTest {
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
 * 		Eccentricity eccentricity = new Eccentricity();
 * 		eccentricity.init(graph);
 * 		eccentricity.compute();
 * 
 * 		for (Node n : graph.getEachNode()) {
 * 			Boolean in = n.getAttribute(&quot;eccentricity&quot;);
 * 
 * 			System.out.printf(&quot;%s is%s in the eccentricity.\n&quot;, n.getId(), in ? &quot;&quot;
 * 					: &quot; not&quot;);
 * 		}
 * 
 * 		// Output will be :
 * 		//
 * 		// A is not in the eccentricity
 * 		// B is not in the eccentricity
 * 		// C is in the eccentricity
 * 		// D is not in the eccentricity
 * 		// E is not in the eccentricity
 * 		// F is not in the eccentricity
 * 		// G is not in the eccentricity
 * 	}
 * }
 * </pre>
 * 
 * @complexity O(n2)
 * @see org.graphstream.algorithm.APSP.APSPInfo
 * @reference F. Harary, Graph Theory. Westview Press, Oct. 1969. [Online].
 *            Available: http://www.amazon.com/exec/obidos/
 *            redirect?tag=citeulike07-20\&path=ASIN/0201410338
 */
public class Eccentricity implements Algorithm {

	/**
	 * The graph on which centroid is computed.
	 */
	protected Graph graph;
	/**
	 * Attribute in which APSPInfo are stored.
	 */
	protected String apspInfoAttribute = APSP.APSPInfo.ATTRIBUTE_NAME;
	/**
	 * Attribute to store eccentricity information.
	 */
	protected String eccentricityAttribute = "eccentricity";
	/**
	 * Value of the attribute if node is in the eccentricity.
	 */
	protected Object isInEccentricity = Boolean.TRUE;
	/**
	 * Value of the attribute if node is not in the eccentricity.
	 */
	protected Object isNotInEccentricity = Boolean.FALSE;

	/**
	 * Build a new eccentricity algorithm with default parameters.
	 */
	public Eccentricity() {
		this("eccentricity");
	}

	/**
	 * Build a new eccentricity algorithm, specifying the attribute name of the
	 * computation result
	 * 
	 * @param eccentricityAttribute
	 *            attribute name of the computation result.
	 */
	public Eccentricity(String eccentricityAttribute) {
		this(eccentricityAttribute, Boolean.TRUE, Boolean.FALSE);
	}

	/**
	 * Build a new eccentricity as in {@link #Eccentricity(String)} but
	 * specifying values of eccentricity membership.
	 * 
	 * @param eccentricityAttribute
	 *            attribute name of the computation result.
	 * @param isInEccentricity
	 *            the value of elements eccentricity attribute when this element
	 *            is in the eccentricity.
	 * @param isNotInEccentricity
	 *            the value of elements eccentricity attribute when this element
	 *            is not in the eccentricity.
	 */
	public Eccentricity(String eccentricityAttribute, Object isInEccentricity,
			Object isNotInEccentricity) {
		this(eccentricityAttribute, Boolean.TRUE, Boolean.FALSE,
				APSP.APSPInfo.ATTRIBUTE_NAME);
	}

	/**
	 * Build a new eccentricity algorithm as in
	 * {@link #Eccentricity(String, Object, Object)} but specifying the name of
	 * the attribute where the APSP informations are stored.
	 * 
	 * @param eccentricityAttribute
	 *            attribute name of the computation result.
	 * @param isInEccentricity
	 *            the value of elements eccentricity attribute when this element
	 *            is in the eccentricity.
	 * @param isNotInEccentricity
	 *            the value of elements eccentricity attribute when this element
	 *            is not in the eccentricity.
	 * @param apspInfoAttribute
	 *            the name of the attribute where the APSP informations are
	 *            stored
	 */
	public Eccentricity(String eccentricityAttribute, Object isInEccentricity,
			Object isNotInEccentricity, String apspInfoAttribute) {
		this.eccentricityAttribute = eccentricityAttribute;
		this.isInEccentricity = isInEccentricity;
		this.isNotInEccentricity = isNotInEccentricity;
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
		DoubleAccumulator min = new DoubleAccumulator((x, y) -> y, Double.MAX_VALUE);
		HashSet<Node> eccentricity = new HashSet<Node>();
		
		graph.nodes().forEach(node -> {
			DoubleAccumulator m = new DoubleAccumulator((x, y) -> y, Double.MIN_VALUE); 
			APSP.APSPInfo info = (APSPInfo) node.getAttribute(apspInfoAttribute);

			if (info == null)
				System.err
						.printf("APSPInfo missing. Did you compute APSP before ?\n");
			
			graph.nodes().forEach(other -> {
				if (node != other) {
					double d = info.getLengthTo(other.getId());

					if (d < 0)
						System.err
								.printf("Found a negative length value in eccentricity algorithm. "
										+ "Is graph connected ?\n");
					else if (d > m.get())
						m.accumulate(d);
				}
			});

			if (m.get() < min.get()) {
				eccentricity.clear();
				eccentricity.add(node);
				min.accumulate(m.get());
			} else if (m.get() == min.get()) {
				eccentricity.add(node);
			}
		});
		
		graph.nodes().forEach(node -> node.setAttribute(eccentricityAttribute, eccentricity
				.contains(node) ? isInEccentricity : isNotInEccentricity));
		

		eccentricity.clear();
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
	 * Get the value of the eccentricity attribute when element is in the
	 * eccentricity. Default value is Boolean.TRUE.
	 * 
	 * @return the value of elements eccentricity attribute when this element is
	 *         in the eccentricity.
	 */
	public Object getIsInEccentricityValue() {
		return isInEccentricity;
	}

	/**
	 * Set the value of the eccentricity attribute when element is in the
	 * eccentricity. On computation, this value is used to set the eccentricity
	 * attribute.
	 * 
	 * @param value
	 *            the value of elements eccentricity attribute when this element
	 *            is in the eccentricity.
	 */
	@Parameter
	public void setIsInEccentricityValue(Object value) {
		isInEccentricity = value;
	}

	/**
	 * Get the value of the eccentricity attribute when element is not in the
	 * eccentricity. Default value is Boolean.FALSE.
	 * 
	 * @return the value of elements eccentricity attribute when this element is
	 *         not in the eccentricity.
	 */
	public Object getIsNotInEccentricityValue() {
		return isNotInEccentricity;
	}

	/**
	 * Set the value of the eccentricity attribute when element is not in the
	 * eccentricity. On computation, this value is used to set the eccentricity
	 * attribute.
	 * 
	 * @param value
	 *            the value of elements eccentricity attribute when this element
	 *            is not in the eccentricity.
	 */
	@Parameter
	public void setIsNotInEccentricityValue(Object value) {
		isNotInEccentricity = value;
	}

	/**
	 * Get the name of the attribute where computation result is stored. Value
	 * of this attribute can be {@link #getIsInEccentricityValue()} if the
	 * element is in the eccentricity, {@link #getIsNotInEccentricityValue()}
	 * else.
	 * 
	 * @return the eccentricity attribute name.
	 */
	public String getEccentricityAttribute() {
		return eccentricityAttribute;
	}

	/**
	 * Set the name of the attribute where computation result is stored.
	 * 
	 * @param eccentricityAttribute
	 *            the name of the element attribute where computation result is
	 *            stored.
	 */
	@Parameter
	public void setEccentricityAttribute(String eccentricityAttribute) {
		this.eccentricityAttribute = eccentricityAttribute;
	}
	
	@Result
	public String defaultResult() {
		StringJoiner sj = new StringJoiner(" | ", "====== Eccentricity ====== \n", "");
		graph.nodes()
			.filter(n -> ((Boolean) n.getAttribute(eccentricityAttribute)))
			.forEach(n -> {
				sj.add(n.getId());
			});
		
		return sj.toString();
	}
}
