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
import java.util.TreeMap;

import org.graphstream.graph.Edge;
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
public class EpidemicCommunityAlgorithm extends DistributedCommunityAlgorithm {

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
	 * @param node
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

			if (s > maxScore || (s == maxScore && rng.nextDouble() >= 0.5)) {
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
	 * @param node
	 *            The node for which the scores computation is performed
	 * @complexity O(DELTA) where DELTA is is the average node degree in the
	 *             network
	 */
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
			if (v.hasAttribute(marker))
				if (communityScores.get(v.getAttribute(marker)) == null)
					communityScores.put(v.getAttribute(marker), 1.0);
				else
					communityScores.put(v.getAttribute(marker),
							communityScores.get(v.getAttribute(marker)) + 1.0);
		}
	}

	@Override
	protected void originateCommunity(Node node) {
		super.originateCommunity(node);
		node.setAttribute(marker + ".score", 0.0);
	}

	@Override
	protected void updateDisplay(Node node) {
		super.updateDisplay(node);

		Double score = (Double) node.getAttribute(marker + ".score");
		String scoreStr = String.format("%.3f", score);
		node.setAttribute("label", node.getAttribute("label") + "(" + scoreStr
				+ ")");
		node.setAttribute("ui.style", node.getAttribute("ui.style") + " size: "
				+ 5 * score + "px;");
	}

}