/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.algorithm;

import java.util.HashSet;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Compute the centroid of a connected graph. In a graph G, if d(u,v) is the
 * shortest length between two nodes u and v (ie the number of edges of the
 * shortest path) let m(u) be the sum of d(u,v) for all nodes v of G. Centroid
 * of a graph G is a subgraph induced by vertices u with minimum m(u). (Centers
 * and Centroids of unicyclic graph, Miroslav Truszczynski, 1985)
 * 
 * This algorithm needs that APSP algorithm has been computed before its own
 * computation.
 * 
 * @complexity O(n2)
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
	protected String apspInfoAttribute = APSP.APSPInfo.ATTRIBUTE_NAME;
	/**
	 * Attribute to store centroid information.
	 */
	protected String centroidAttribute = "centroid";
	/**
	 * Value of the attribute if node is in the centroid.
	 */
	protected Object isInCentroid = Boolean.TRUE;
	/**
	 * Value of the attribute if node is not in the centroid.
	 */
	protected Object isNotInCentroid = Boolean.FALSE;

	public Centroid() {
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
		HashSet<Node> centroid = new HashSet<Node>();

		for (Node node : graph.getEachNode()) {
			float m = 0;
			APSP.APSPInfo info = node.getAttribute(apspInfoAttribute);

			if (info == null)
				System.err
						.printf("APSPInfo missing. Did you compute APSP before ?\n");

			for (Node other : graph.getEachNode()) {
				if (node != other) {
					double d = info.getLengthTo(other.getId());

					if (d < 0)
						System.err
								.printf("Found a negative length value in centroid algorithm. "
										+ "Is graph connected ?\n");
					else
						m += d;
				}
			}

			if (m < min) {
				centroid.clear();
				centroid.add(node);
				min = m;
			} else if (m == min) {
				centroid.add(node);
			}
		}

		for (Node node : graph.getEachNode())
			node.setAttribute(centroidAttribute,
					centroid.contains(node) ? isInCentroid : isNotInCentroid);

		centroid.clear();
	}
}
