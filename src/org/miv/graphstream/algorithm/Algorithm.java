/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.miv.graphstream.algorithm;

import org.miv.graphstream.graph.*;

/**
 * Base for algorithms operating on a graph.
 *
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since 2007
 */
public interface Algorithm
{
	/**
	 * The graph the algorithms operates upon.
	 * @return A graph.
	 */
	Graph getGraph();
	
	/**
	 * Set the graph the algorithm will operate upon.
	 * @param graph The graph to use.
	 */
	void setGraph( Graph graph );
	
	/**
	 * Launch the algorithm on the previously specified graph. Please specify
	 * the complexity of the algorithm in this javadoc. A dedicated tag can be
	 * used "@complexity".
	 * @see #setGraph(Graph)
	 */
	void compute();
}