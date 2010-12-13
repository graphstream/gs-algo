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
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm;

import java.util.HashSet;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Compute the eccentricity of a connected graph. In a graph G, if d(u,v) is the
 * shortest length between two nodes u and v (ie the number of edges of the
 * shortest path) let e(u) be the d(u,v) such that v is the farthest of u.
 * Eccentricity of a graph G is a subgraph induced by vertices u with minimum
 * e(u). (Centers and Centroids of unicyclic graph, Miroslav Truszczynski, 1985)
 * 
 * This algorithm needs that APSP algorithm has been computed before its own
 * computation.
 * 
 * @complexity O(n2)
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

	public Eccentricity() {
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
		float min = Float.MAX_VALUE;
		HashSet<Node> eccentricity = new HashSet<Node>();

		for (Node node : graph.getEachNode()) {
			float m = Float.MIN_VALUE;
			APSP.APSPInfo info = node.getAttribute(apspInfoAttribute);

			if (info == null)
				System.err
						.printf("APSPInfo missing. Did you compute APSP before ?\n");

			for (Node other : graph.getEachNode()) {
				if (node != other) {
					float d = info.getLengthTo(other.getId());

					if (d < 0)
						System.err
								.printf("Found a negative length value in centroid algorithm. "
										+ "Is graph connected ?\n");
					else if (d > m)
						m = d;
				}
			}

			if (m < min) {
				eccentricity.clear();
				eccentricity.add(node);
				min = m;
			} else if (m == min) {
				eccentricity.add(node);
			}
		}

		for (Node node : graph.getEachNode())
			node.setAttribute(eccentricityAttribute, eccentricity
					.contains(node) ? isInEccentricity : isNotInEccentricity);
	}
}
