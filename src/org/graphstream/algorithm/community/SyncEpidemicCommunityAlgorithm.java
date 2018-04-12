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
 * @since 2010-10-01
 * 
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.community;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.HashMap;

/**
 * This class implements the synchronous version of the
 * "Epidemic Community Detection Algorithm" as presented by Raghavan <i>et
 * al</i>.
 * 
 * @reference U. N. Raghavan, R. Albert, and S. Kumara, “Near Linear Time Al-
 *            gorithm to Detect Community Structures in Large-scale Networks,”
 *            Physical Review E (Statistical, Nonlinear, and Soft Matter
 *            Physics), vol. 76, no. 3, 2007.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class SyncEpidemicCommunityAlgorithm extends EpidemicCommunityAlgorithm {

	/**
	 * Identify the current iteration of this algorithm to ensure synchronous
	 * behavior.
	 */
	protected int iteration = 0;

	public SyncEpidemicCommunityAlgorithm() {
		super();
	}

	public SyncEpidemicCommunityAlgorithm(Graph graph) {
		super(graph);
	}

	public SyncEpidemicCommunityAlgorithm(Graph graph, String marker) {
		super(graph, marker);
	}

	@Override
	public void terminate() {
		iteration = 0;
	}

	@Override
	public void compute() {
		super.compute();
		iteration++;
	}

	@Override
	public void computeNode(Node node) {

		/*
		 * Save the node community to previous
		 */
		if (node.hasAttribute(marker))
			node.setAttribute(marker + ".previous", node.getAttribute(marker));

		/*
		 * Perform same assignment as in asynchronous mode difference is in the
		 * redefinition of the communityScores() method
		 */
		super.computeNode(node);

		/*
		 * Save the iteration at which the node was last updated
		 */
		node.setAttribute(marker + ".step", iteration);
	}

	@Override
	protected void communityScores(Node u) {
		/*
		 * Reset the scores for each communities
		 */
		communityScores = new HashMap<Object, Double>();

		/*
		 * Iterate over the nodes that this node "hears"
		 */
		u.enteringEdges().forEach(e -> {
			Node v = e.getOpposite(u);

			/*
			 * Update the count for this community
			 */
			if (v.hasAttribute(marker + ".step")) {

				/*
				 * Set the marker based on the neighbor node current update
				 * status
				 */
				String syncMarker = marker;
				int updateStep = (Integer) v.getAttribute(marker + ".step");

				/*
				 * The neighbor node has been updated for this step use the
				 * previous marker
				 */
				if (updateStep == iteration) {
					syncMarker += ".previous";
				}

				/*
				 * Update the community score based on the selected marker
				 */
				if (v.hasAttribute(syncMarker))
					if (communityScores.get(v.getAttribute(syncMarker)) == null)
						communityScores.put(v.getAttribute(syncMarker), 1.0);
					else
						communityScores
								.put(v.getAttribute(syncMarker),
										communityScores.get(v
												.getAttribute(syncMarker)) + 1.0);
			}
		});
	}
}
