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
 * @since 2011-05-14
 * 
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.randomWalk;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * A base entity for the {@link RandomWalk} algorithm. 
 */
public abstract class Entity {

	/**
	 * The shared information.
	 */
	protected RandomWalk.Context context;
	
	/**
	 * The current node the entity is on.
	 */
	protected Node current;
	
	/**
	 * Should initialize the entity, starting it on the given node.
	 * @param start The node on which the entity starts.
	 */
	void init(RandomWalk.Context context, Node start) {
		this.current = start;
		this.context = context;
	}
	
	/**
	 * Should move the entity from its current node to another.
	 */
	abstract void step();
	
	/**
	 * The weight of an edge.
	 * @param e The edge.
	 * @return The weight of the edge.
	 */
	protected double weight(Edge e) {
		if (!e.hasAttribute(context.weightAttribute))
			return 1.0;

		return e.getNumber(context.weightAttribute);
	}
}