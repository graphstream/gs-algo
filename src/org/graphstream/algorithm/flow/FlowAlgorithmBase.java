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
 * @since 2012-02-18
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.flow;

import java.util.List;

import org.graphstream.algorithm.util.Parameter;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Base for flow algorithms. Provides features to handle capacities and flows.
 */
public abstract class FlowAlgorithmBase implements FlowAlgorithm {
	/**
	 * Graph used by the algorithm.
	 */
	protected Graph flowGraph;
	/**
	 * Edge count. Size of arrays (capacities and flows) is defined as twice
	 * this value.
	 */
	protected int n;
	/**
	 * Capacities of edges.
	 */
	protected double[] capacities;
	/**
	 * Current flows of edges.
	 */
	protected double[] flows;
	/**
	 * Id of the source.
	 */
	protected String sourceId;
	/**
	 * Id of the sink.
	 */
	protected String sinkId;
	/**
	 * Maximum flow computed by the algorithm.
	 */
	protected double maximumFlow;

	protected String capacityAttribute;

	protected FlowAlgorithmBase() {
		flowGraph = null;
		capacityAttribute = null;
	}

	/**
	 * Check if arrays are large enought to support computation.
	 */
	protected void checkArrays() {
		n = flowGraph.getEdgeCount();

		if (capacities == null || capacities.length < 2 * n) {
			capacities = new double[2 * n];
			flows = new double[2 * n];
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.flow.FlowAlgorithm#getFlowSourceId()
	 */
	public String getFlowSourceId() {
		return sourceId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.flow.FlowAlgorithm#getFlowSinkId()
	 */
	public String getFlowSinkId() {
		return sinkId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		flowGraph = graph;
		checkArrays();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.flow.FlowAlgorithm#init(org.graphstream.graph
	 * .Graph, java.lang.String, java.lang.String)
	 */
	public void init(Graph g, String sourceId, String sinkId) {
		init(g);

		this.sourceId = sourceId;
		this.sinkId = sinkId;
	}
	
	@Parameter(true)
	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}
	
	@Parameter(true)
	public void setSinkId(String sinkId) {
		this.sinkId = sinkId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.flow.FlowAlgorithm#getMaximumFlow()
	 */
	public double getMaximumFlow() {
		return maximumFlow;
	}

	/**
	 * Shortcut to {@link #getFlow(Node, Node)}.
	 * 
	 * @param uIndex
	 *            index of source
	 * @param vIndex
	 *            index of target
	 * @return flow of (u,v)
	 */
	public double getFlow(int uIndex, int vIndex) {
		Node u = flowGraph.getNode(uIndex);
		Node v = flowGraph.getNode(vIndex);

		return getFlow(u, v);
	}

	/**
	 * Shortcut to {@link #getFlow(Node, Node)}.
	 * 
	 * @param uId
	 *            id of source
	 * @param vId
	 *            id of target
	 * @return flow of (u,v)
	 */
	public double getFlow(String uId, String vId) {
		Node u = flowGraph.getNode(uId);
		Node v = flowGraph.getNode(vId);

		return getFlow(u, v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.flow.FlowAlgorithm#getFlow(org.graphstream.
	 * graph.Node, org.graphstream.graph.Node)
	 */
	public double getFlow(Node u, Node v) {
		Edge e = u.getEdgeBetween(v);

		if (e.getSourceNode() == u)
			return flows[e.getIndex()];
		else
			return flows[e.getIndex() + n];
	}

	/**
	 * Shortcut to {@link #setFlow(Node, Node, double)}.
	 * 
	 * @param uIndex
	 *            index of u
	 * @param vIndex
	 *            index of v
	 * @param flow
	 *            new float of (u,v)
	 */
	public void setFlow(int uIndex, int vIndex, double flow) {
		Node u = flowGraph.getNode(uIndex);
		Node v = flowGraph.getNode(vIndex);

		setFlow(u, v, flow);
	}

	/**
	 * Shortcut to {@link #setFlow(Node, Node, double)}.
	 * 
	 * @param uId
	 *            id of u
	 * @param vId
	 *            id of v
	 * @param flow
	 *            new float of (u,v)
	 */
	public void setFlow(String uId, String vId, double flow) {
		Node u = flowGraph.getNode(uId);
		Node v = flowGraph.getNode(vId);
		
		setFlow(u, v, flow);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.flow.FlowAlgorithm#setFlow(org.graphstream.
	 * graph.Node, org.graphstream.graph.Node, double)
	 */
	public void setFlow(Node u, Node v, double flow) {
		Edge e = u.getEdgeBetween(v);

		if (e.getSourceNode() == u)
			flows[e.getIndex()] = flow;
		else
			flows[e.getIndex() + n] = flow;
	}

	/**
	 * Shortcut {@link #getCapacity(Node, Node)}.
	 * 
	 * @param uIndex
	 *            index of u
	 * @param vIndex
	 *            index of v
	 * @return capacity of (u,v).
	 */
	public double getCapacity(int uIndex, int vIndex) {
		Node u = flowGraph.getNode(uIndex);
		Node v = flowGraph.getNode(vIndex);

		return getCapacity(u, v);
	}

	/**
	 * Shortcut {@link #getCapacity(Node, Node)}.
	 * 
	 * @param uId
	 *            id of u
	 * @param vId
	 *            id of v
	 * @return capacity of (u,v).
	 */
	public double getCapacity(String uId, String vId) {
		Node u = flowGraph.getNode(uId);
		Node v = flowGraph.getNode(vId);

		return getCapacity(u, v);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.flow.FlowAlgorithm#getCapacity(org.graphstream
	 * .graph.Node, org.graphstream.graph.Node)
	 */
	public double getCapacity(Node u, Node v) {
		Edge e = u.getEdgeBetween(v);

		if (e == null)
			System.err.printf("no edge between %s and %s\n", u.getId(), v
					.getId());

		if (e.getSourceNode() == u)
			return capacities[e.getIndex()];
		else
			return capacities[e.getIndex() + n];
	}

	/**
	 * Shortcut to {@link #setCapacity(Node, Node, double)}.
	 * 
	 * @param uIndex
	 *            index of u
	 * @param vIndex
	 *            index of v
	 * @param capacity
	 *            new capacity of (u,v)
	 */
	public void setCapacity(int uIndex, int vIndex, double capacity) {
		Node u = flowGraph.getNode(uIndex);
		Node v = flowGraph.getNode(vIndex);

		setCapacity(u, v, capacity);
	}

	/**
	 * Shortcut to {@link #setCapacity(Node, Node, double)}.
	 * 
	 * @param uId
	 *            id of u
	 * @param vId
	 *            id of v
	 * @param capacity
	 *            new capacity of (u,v)
	 */
	public void setCapacity(String uId, String vId, double capacity) {
		Node u = flowGraph.getNode(uId);
		Node v = flowGraph.getNode(vId);

		setCapacity(u, v, capacity);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.flow.FlowAlgorithm#setCapacity(org.graphstream
	 * .graph.Node, org.graphstream.graph.Node, double)
	 */
	public void setCapacity(Node u, Node v, double capacity) {
		Edge e = u.getEdgeBetween(v);

		if (e.getSourceNode() == u)
			capacities[e.getIndex()] = capacity;
		else
			capacities[e.getIndex() + n] = capacity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.flow.FlowAlgorithm#setCapacityAttribute(java
	 * .lang.String)
	 */
	@Parameter
	public void setCapacityAttribute(String attribute) {
		capacityAttribute = attribute;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.flow.FlowAlgorithm#getCapacityAttribute()
	 */
	public String getCapacityAttribute() {
		return capacityAttribute;
	}
	
	@Parameter
	public void setAllCapacities(double value) {
		for (int i = 0; i < 2 * n; i++)
			capacities[i] = value;
	}

	/**
	 * Load capacities from edge attributes. Should be called between
	 * {@link #init(Graph, String, String)} and {@link #compute()}.
	 */
	protected void loadCapacitiesFromAttribute() {
		if (capacityAttribute == null)
			return;

		Edge e;

		for (int i = 0; i < n; i++) {
			capacities[i] = 0.0;
			capacities[i + n] = 0.0;

			e = flowGraph.getEdge(i);

			if (e.hasNumber(capacityAttribute)) {
				capacities[i] = e.getNumber(capacityAttribute);
			} else if (e.hasVector(capacityAttribute)) {
				List<? extends Number> capVect = flowGraph.getEdge(i)
						.getVector(capacityAttribute);

				if (capVect.size() > 0)
					capacities[i] = capVect.get(0).doubleValue();
				if (capVect.size() > 1)
					capacities[i + n] = capVect.get(1).doubleValue();
			} else if (e.hasArray(capacityAttribute)) {
				Object[] capArray = e.getArray(capacityAttribute);

				if (capArray.length > 0)
					capacities[i] = ((Number) capArray[0]).doubleValue();
				if (capArray.length > 1)
					capacities[i + n] = ((Number) capArray[1]).doubleValue();
			} else if (e.hasAttribute(capacityAttribute))
				System.err.printf("unknown capacity type \"%s\"\n", e
						.getAttribute(capacityAttribute).getClass());
		}
	}
}
