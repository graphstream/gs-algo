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
 * @since 2012-02-10
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
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
	 * @param u source node
	 * @param v target node
	 * @return flow of (u,v)
	 */
	double getFlow(Node u, Node v);

	/**
	 * Set flow of edge (u,v).
	 * 
	 * @param u source node
	 * @param v target node
	 * @param flow
	 *            new flow
	 */
	void setFlow(Node u, Node v, double flow);

	/**
	 * Get capacity of edge (u,v).
	 *
	 * @param u source node
	 * @param v target node
	 * @return capacity of (u,v)s
	 */
	double getCapacity(Node u, Node v);

	/**
	 * Set capacity of (u,v). Capacities should be set between calls to
	 * {@link #init(Graph, String, String)} and {@link #compute()}.
	 *
	 * @param u source node
	 * @param v target node
	 * @param capacity
	 *            new capacity of (u,v)
	 */
	void setCapacity(Node u, Node v, double capacity);

	/**
	 * Set the key of the attribute from which capacities will be loaded.
	 * Attribute values of edge (u,v) should be an array of double where first
	 * element is the value of the capacity of (u,v) and second the capacity of
	 * (v,u). If there is only one value, the value of (v,u) will be zero. If no
	 * value is available, both capacities will be zero.
	 * 
	 * If capacity attribute is null, you have to set capacities before calling
	 * {@link #compute()}.
	 * 
	 * @param attribute attribute name
	 */
	void setCapacityAttribute(String attribute);

	/**
	 * Get the key attribute from which capacities are loaded.
	 * 
	 * @see #setCapacityAttribute(String)
	 * @return key attribute of capacities
	 */
	String getCapacityAttribute();

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
	 * Init the algorithm. This method replaces the {@link #init(Graph)} method
	 * of Algorithm so users just have to call this new method.
	 * 
	 * @param g
	 *            graph that should be used by the algorithm
	 * @param sourceId
	 *            id of the source of the flow
	 * @param sinkId
	 *            id of the sink of the flow
	 */
	void init(Graph g, String sourceId, String sinkId);
}
