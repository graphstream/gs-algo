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
 * This class implements an improved community detection algorithm based on the
 * epidemic label propagation paradigm the was presented by Leung <i>et al</i>.
 * 
 * @reference I. X. Y. Leung, P. Hui, P. Lio`, and J. Crowcroft, “Towards Real-
 *            Time Community Detection in Large Networks,” Physical Review E
 *            (Statistical, Nonlinear, and Soft Matter Physics), vol. 79, no. 6,
 *            pp. 066 107+, 2009.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class Leung extends EpidemicCommunityAlgorithm {

	/**
	 * Name of the marker that is used to store weight of links on the graph
	 * that this algorithm is applied to.
	 */
	protected String weightMarker = "weight";

	/**
	 * Comparable node characteristic preference exponent
	 */
	protected double m = 0.1;

	/**
	 * Hop attenuation factor
	 */
	protected double delta = 0.05;

	public Leung() {
		super();
	}

	public Leung(Graph graph) {
		super(graph);
	}

	public Leung(Graph graph, String marker) {
		super(graph, marker);
	}

	/**
	 * Create a new Leung algorithm instance, attached to the specified graph,
	 * using the specified marker to store the community attribute, and the
	 * specified weightMarker to retrieve the weight attribute of graph edges.
	 * 
	 * @param graph
	 *            graph to which the algorithm will be applied
	 * @param marker
	 *            community attribute marker
	 * @param weightMarker
	 *            edge weight marker
	 */
	public Leung(Graph graph, String marker, String weightMarker) {
		super(graph, marker);
		this.weightMarker = weightMarker;
	}

	/**
	 * Create a new Leung algorithm instance, attached to the specified graph,
	 * using the default markers for the node community and edge weight
	 * attributes. Sets the preference exponent and hop attenuation factor to
	 * the given values.
	 * 
	 * @param graph
	 *            graph to which the algorithm will be applied
	 * @param m
	 *            comparable function preference exponent value
	 * @param delta
	 *            hop attenuation factor value
	 */
	public Leung(Graph graph, double m, double delta) {
		super(graph);
		setParameters(m, delta);
	}

	/**
	 * Create a new Leung algorithm instance, attached to the specified graph,
	 * using the specified marker to store the community attribute, and the
	 * default marker to retrieve the weight attribute of graph edges. Sets the
	 * preference exponent and hop attenuation factor to the given values.
	 * 
	 * @param graph
	 *            graph to which the algorithm will be applied
	 * @param marker
	 *            community attribute marker
	 * @param m
	 *            comparable function preference exponent value
	 * @param delta
	 *            hop attenuation factor value
	 */
	public Leung(Graph graph, String marker, double m, double delta) {
		super(graph, marker);
		setParameters(m, delta);
	}

	/**
	 * Create a new Leung algorithm instance, attached to the specified graph,
	 * using the specified marker to store the community attribute, and the
	 * specified weightMarker to retrieve the weight attribute of graph edges.
	 * Sets the preference exponent and hop attenuation factor to the given
	 * values.
	 * 
	 * @param graph
	 *            graph to which the algorithm will be applied
	 * @param marker
	 *            community attribute marker
	 * @param weightMarker
	 *            edge weight marker
	 * @param m
	 *            comparable function preference exponent value
	 * @param delta
	 *            hop attenuation factor value
	 */
	public Leung(Graph graph, String marker, String weightMarker, double m,
			double delta) {
		super(graph, marker);
		this.weightMarker = weightMarker;
		setParameters(m, delta);
	}

	/**
	 * Sets the preference exponent and hop attenuation factor to the given
	 * values.
	 * 
	 * @param m
	 *            comparable function preference exponent value
	 * @param delta
	 *            hop attenuation factor value
	 */
	public void setParameters(double m, double delta) {
		this.m = m;
		this.delta = delta;
	}

	@Override
	public void computeNode(Node node) {
		/*
		 * Recall and update the node current community and previous score
		 */
		Object previousCommunity = node.getAttribute(marker);
		Double previousScore = (Double) node.getAttribute(marker + ".score");
		super.computeNode(node);

		/*
		 * Update the node label score
		 */

		// Handle first iteration
		if (previousCommunity == null) {
			previousCommunity = node.getAttribute(marker);
			previousScore = (Double) node.getAttribute(marker + ".score");
		}

		/*
		 * The node is the originator of the community and hasn't changed
		 * community at this iteration (or we are at the first simulation step):
		 * keep the maximum label score
		 */
		if ((node.getAttribute(marker).equals(previousCommunity))
				&& (previousScore.equals(1.0)))
			node.setAttribute(marker + ".score", 1.0);

		/*
		 * Otherwise search for the highest score amongst neighbors and reduce
		 * it by decreasing factor
		 */
		else {
			Double maxLabelScore = Double.NEGATIVE_INFINITY;
			for (Edge e : node.getEnteringEdgeSet()) {
				Node v = e.getOpposite(node);
				if (v.hasAttribute(marker)
						&& v.getAttribute(marker).equals(
								node.getAttribute(marker))) {
					if ((Double) v.getAttribute(marker + ".score") > maxLabelScore)
						maxLabelScore = (Double) v.getAttribute(marker
								+ ".score");
				}
			}
			node.setAttribute(marker + ".score", maxLabelScore - delta);
		}
	}

	/**
	 * Compute the scores for all relevant communities for the selected node
	 * using Leung algorithm.
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
			if (v.hasAttribute(marker)) {

				// Compute the neighbor node current score
				Double score = (Double) v.getAttribute(marker + ".score")
						* Math.pow(v.getInDegree(), m);

				/*
				 * The rest of the formula depends on the weighted status of the
				 * network
				 */
				Double weight;
				if (e.hasAttribute(weightMarker))
					if (e.isDirected()) {
						Edge e2 = v.getEdgeToward(u.getId());
						if (e2 != null && e2.hasAttribute(weightMarker))
							weight = (Double) e.getAttribute(weightMarker)
									+ (Double) e2.getAttribute(weightMarker);
						else
							weight = (Double) e.getAttribute(weightMarker);
					} else
						weight = (Double) e.getAttribute(weightMarker);
				else
					weight = 1.0;

				// Update the score of the according community
				if (communityScores.get(v.getAttribute(marker)) == null)
					communityScores.put(v.getAttribute(marker), score * weight);
				else
					communityScores.put(v.getAttribute(marker),
							communityScores.get(v.getAttribute(marker))
									+ (score * weight));
			}
		}
	}

	@Override
	protected void originateCommunity(Node node) {
		super.originateCommunity(node);

		// Correct the original community score for the Leung algorithm
		node.setAttribute(marker + ".score", 1.0);
	}
}
