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
 * @since 2012-09-21
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.stream.Stream;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

/**
 * This interface defines the basic functionalities of a spanning tree algorithm.
 * 
 * <p> It defines methods related to tagging the edges of the spanning tree and for iterating on them.</p>
 */
public interface SpanningTree extends Algorithm {
	/**
	 * Get key attribute which will be used to set if edges are in the spanning
	 * tree, or not.
	 * 
	 * @return flag attribute
	 */
	String getFlagAttribute();
	
	/**
	 * Set the flag attribute.
	 * 
	 * @param flagAttribute
	 *            New attribute used. If {@code null} edges are not tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	void setFlagAttribute(String flagAttribute);
	
	/**
	 * Get value used to set that an edge is in the spanning tree.
	 * 
	 * @return on value
	 */
	Object getFlagOn();
	
	/**
	 * Set value used to set that an edge is in the spanning tree.
	 * 
	 * @param flagOn
	 *            on value. If {@code null} edges in the tree are not tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	void setFlagOn(Object flagOn);
	
	/**
	 * Get value used to set that an edge is not in the spanning tree.
	 * 
	 * @return off value
	 */
	Object getFlagOff();
	
	
	/**
	 * Set value used to set that an edge is not in the spanning tree.
	 * 
	 * @param flagOff
	 *            off value. If {@code null} edges out of the tree are not
	 *            tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	void setFlagOff(Object flagOff);
	
	/**
	 * Removes the tags of all edges. Use this method to save memory if the
	 * spanning tree is used no more.
	 */
	void clear();

	/**
	 * An iterator on the tree edges.
	 * 
	 * @return An iterator on the tree edges
	 */
	Stream<Edge> getTreeEdgesStream();
	
	
	/**
	 * Iterable view of the spanning tree edges. This implementation uses
	 * {@link #getTreeEdgesStream()}.
	 * 
	 * @param <T> elements
	 * @return Iterable view of the tree edges.
	 */
	<T extends Edge> Iterable<T> getTreeEdges();
}
