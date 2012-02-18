/*
 * Copyright 2006 - 2012
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
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
package org.graphstream.algorithm.flow;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Defines algorithm used to compute maximum flow.
 */
public interface FlowAlgorithm extends Algorithm {
	/**
	 * Get flow value of edge (u,v).
	 * 
	 * @param u
	 * @param v
	 * @return flow of (u,v)
	 */
	double getFlow(Node u, Node v);

	/**
	 * Set flow of edge (u,v).
	 * 
	 * @param u
	 * @param v
	 * @param flow
	 *            new flow
	 */
	void setFlow(Node u, Node v, double flow);

	/**
	 * Get capacity of edge (u,v).
	 * 
	 * @param u
	 * @param v
	 * @return capacity of (u,v)s
	 */
	double getCapacity(Node u, Node v);

	/**
	 * Set capacity of (u,v).
	 * 
	 * @param u
	 * @param v
	 * @param capacity
	 *            new capacity of (u,v)
	 */
	void setCapacity(Node u, Node v, double capacity);

	/**
	 * Get maximum flow compute by {@link #compute()}.
	 * 
	 * @return maximum flow
	 */
	double getMaximumFlow();

	/**
	 * Get id of the source.
	 * 
	 * @return id of the source
	 */
	String getFlowSourceId();

	/**
	 * Get id of the sink.
	 * 
	 * @return id of the sink
	 */
	String getFlowSinkId();

	/**
	 * Init the algorithm.
	 * 
	 * @param g
	 *            graph that should be used by the algorithm
	 * @param sourceId
	 *            source
	 * @param sinkId
	 *            sink
	 */
	void init(Graph g, String sourceId, String sinkId);
}
