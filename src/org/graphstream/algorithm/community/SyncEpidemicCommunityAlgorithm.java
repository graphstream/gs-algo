/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Project copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 *
 * This file is copyright 2010
 *  Guillaume-Jean Herbiet
 */
package org.graphstream.algorithm.community;

import java.util.HashMap;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

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
		for (Edge e : u.getEnteringEdgeSet()) {
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
				int updateStep = ((Integer) v.getAttribute(marker + ".step"))
						.intValue();

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
		}
	}
}
