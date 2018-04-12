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
 * @since 2012-10-25
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Compute closeness centrality.
 * 
 */
public class ClosenessCentrality extends AbstractCentrality {
	public static final String DEFAULT_ATTRIBUTE_KEY = "closeness";
	
	/**
	 * Flag indicating if APSP should be computed in this algorithm. If false,
	 * user needs to compute APSP himself to provide {@link APSPInfo} object in
	 * nodes attribute {@link APSPInfo#ATTRIBUTE_NAME}.
	 */
	protected boolean computeAPSP;

	/**
	 * Flag indicating if computation should use Dangalchev method rather than
	 * the classical method. This method is more adapted for disconnected graph.
	 */
	protected boolean useDangalchevMethod = false;

	// APSP algorithm if computed in this algorithm.
	private APSP apsp;
	
	/**
	 * Default construtor. Same as calling `ClosenessCentrality("closeness")`.
	 */
	public ClosenessCentrality() {
		this(DEFAULT_ATTRIBUTE_KEY);
	}

	/**
	 * Construtor allowing to configure centrality attribute. Same as calling
	 * `ClosenessCentrality(attribute, false)`.
	 * 
	 * @param attribute
	 *            attribute where centrality will be stored
	 */
	public ClosenessCentrality(String attribute) {
		this(attribute, NormalizationMode.NONE);
	}

	/**
	 * Constructor allowing to configure attribute and normalize flag. Same as
	 * calling `ClosenessCentrality(attribute, normalize, true, false)`.
	 * 
	 * @param attribute
	 *            attribute where centrality will be stored
	 * @param normalize
	 *            defines the normalization mode
	 */
	public ClosenessCentrality(String attribute, NormalizationMode normalize) {
		this(attribute, normalize, true, false);
	}

	/**
	 * Fully configurable construtor.
	 * 
	 * @param centralityAttribute
	 *            attribute where centrality will be stored
	 * @param normalize
	 *            defines the normalization mode
	 * @param computeAPSP
	 *            if true, apsp will be computed in this algorithm
	 * @param useDangalchevMethod
	 *            if true, Dangelchev method will be used in this algorithm
	 */
	public ClosenessCentrality(String centralityAttribute, NormalizationMode normalize,
			boolean computeAPSP, boolean useDangalchevMethod) {
		super(centralityAttribute, normalize);
		this.computeAPSP = computeAPSP;
		this.useDangalchevMethod = useDangalchevMethod;
	}

	@Override
	public void init(Graph graph) {
		super.init(graph);
		
		if (computeAPSP) {
			apsp = new APSP();
			apsp.init(graph);
		}
	}
	
	@Parameter
	public void computeAPSP(boolean compute) {
		if (compute) {
			apsp = new APSP();
			apsp.init(graph);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.AbstractCentrality#computeCentrality()
	 */
	protected void computeCentrality() {
		int count = graph.getNodeCount();
		Node node, other;

		if (computeAPSP)
			apsp.compute();

		for (int idx = 0; idx < count; idx++) {
			node = graph.getNode(idx);
			data[idx] = 0;

			APSP.APSPInfo info = (APSPInfo) node.getAttribute(APSPInfo.ATTRIBUTE_NAME);

			if (info == null)
				System.err
						.printf("APSPInfo missing. Did you compute APSP before ?\n");

			for (int idx2 = 0; idx2 < count; idx2++) {
				if (idx != idx2) {
					other = graph.getNode(idx2);
					double d = info.getLengthTo(other.getId());

					if (useDangalchevMethod)
						data[idx] += Math.pow(2, -d);
					else {
						if (d < 0)
							System.err
									.printf("Found a negative length value in centroid algorithm. "
											+ "Is graph connected ?\n");
						else
							data[idx] += d;
					}
				}
			}

			if (!useDangalchevMethod)
				data[idx] = 1 / data[idx];
		}
	}
}
