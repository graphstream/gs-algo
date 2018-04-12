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
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.community;

import java.util.HashMap;
import java.util.Objects;
import java.util.TreeMap;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * This class implements the "Epidemic Community Detection Algorithm" as
 * presented by Raghavan <i>et al</i>. It also serves as base class for all
 * algorithms using the epidemic label propagation paradigm.
 * 
 * @reference U. N. Raghavan, R. Albert, and S. Kumara, “Near Linear Time Al-
 *            gorithm to Detect Community Structures in Large-scale Networks,”
 *            Physical Review E (Statistical, Nonlinear, and Soft Matter
 *            Physics), vol. 76, no. 3, 2007.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class EpidemicCommunityAlgorithm extends DecentralizedCommunityAlgorithm {

	/**
	 * Heard communities and their associated scores
	 */
	protected HashMap<Object, Double> communityScores;

	public EpidemicCommunityAlgorithm() {
		super();
	}

	public EpidemicCommunityAlgorithm(Graph graph) {
		super(graph);
	}

	public EpidemicCommunityAlgorithm(Graph graph, String marker) {
		super(graph, marker);
	}

	/**
	 * Perform computation of one iteration of the algorithm on a given node
	 * using the epidemic label propagation algorithm.
	 * 
	 * @complexity k times the complexity of the communityScores() function,
	 *             where k is the average number of neighboring communities.
	 * @param node node to compute
	 */
	@Override
	public void computeNode(Node node) {
		/*
		 * Compute the community scores for this node
		 */
		communityScores(node);

		/*
		 * Search for the community with the highest score
		 */
		Object maxCommunity = null;
		Double maxScore = Double.NEGATIVE_INFINITY;

		TreeMap<Object, Double> scores = new TreeMap<Object, Double>(
				communityScores);
		for (Object c : scores.keySet()) {
			Double s = communityScores.get(c);

			if ((s > maxScore) || ((Objects.equals(s, maxScore)) && (rng.nextDouble() >= 0.5))) {
				maxCommunity = c;
				maxScore = s;
			}
		}

		/*
		 * Update the node community
		 */
		if (maxCommunity == null)
			originateCommunity(node);
		else {
			node.setAttribute(marker, maxCommunity);
			node.setAttribute(marker + ".score", maxScore);
		}
	}

	/**
	 * Compute the scores for all relevant communities for the selected node
	 * using epidemic label propagation paradigm.
	 * 
	 * @param u
	 *            The node for which the scores computation is performed
	 * @complexity O(DELTA) where DELTA is is the average node degree in the
	 *             network
	 */
	protected void communityScores(Node u) {
		/*
		 * Reset the scores for each communities
		 */
		communityScores = new HashMap<>();

		/*
		 * Iterate over the nodes that this node "hears"
		 */
		u.enteringEdges()
			.filter(e -> e.getOpposite(u).hasAttribute(marker))
			.forEach(e -> {
				/*
				 * Update the count for this community
				 */
				Node v = e.getOpposite(u);
				
				if (communityScores.get(v.getAttribute(marker)) == null)
					communityScores.put(v.getAttribute(marker), 1.0);
				else
					communityScores.put(v.getAttribute(marker),
							communityScores.get(v.getAttribute(marker)) + 1.0);
		});
	}

	@Override
	protected void originateCommunity(Node node) {
		super.originateCommunity(node);
		node.setAttribute(marker + ".score", 0.0);
	}
}
