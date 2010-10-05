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

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Dummy distributed community algorithm. This does nothing but creating a
 * community per node.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public final class DummyCommunityAlgorithm extends
		DecentralizedCommunityAlgorithm {

	public DummyCommunityAlgorithm() {
		super();
	}

	public DummyCommunityAlgorithm(Graph graph) {
		super(graph);
	}

	public DummyCommunityAlgorithm(Graph graph, String marker) {
		super(graph, marker);
	}

	/**
	 * Simply creates a community for this node if none is existing. Doesn't try
	 * to assign the node to any other community.
	 * 
	 * @complexity O(1)
	 */
	@Override
	public void computeNode(Node node) {
		if (!node.hasAttribute(marker))
			super.originateCommunity(node);

		System.out
				.println(node.getId() + "<" + node.getAttribute(marker) + ">");
	}

}
