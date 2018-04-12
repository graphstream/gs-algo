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
 * @since 2010-10-04
 * 
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import java.util.HashMap;
import java.util.HashSet;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

import static org.graphstream.algorithm.Toolkit.communities;

/**
 * Computes and updates an absolute measure based on the current community
 * assignment on a given graph as it evolves.
 * 
 * @reference M. E. Newman and M. Girvan, “Finding and Evaluating Community
 *            Structure in Networks,” <i>Physical Review E (Statistical,
 *            Nonlinear, and Soft Matter Physics)</i>, vol. 69, no. 2, pp. 026
 *            113+, Feb 2004.
 * 
 * @author Guillaume-Jean Herbiet
 */
public abstract class CommunityMeasure extends SinkAdapter implements
		DynamicAlgorithm {
	/**
	 * The graph for which the modularity will be computed.
	 */
	protected Graph graph;

	/**
	 * Name of the attribute marking the communities.
	 */
	protected String marker;

	/**
	 * All communities indexed by their marker value.
	 */
	protected HashMap<Object, HashSet<Node>> communities;

	/**
	 * Set to false after {@link #compute()}.
	 */
	protected boolean graphChanged = true;

	/**
	 * Last value computed.
	 */
	protected double M;

	/**
	 * New measure algorithm with a given marker for communities.
	 * 
	 * @param marker
	 *            name of the attribute marking the communities.
	 */
	public CommunityMeasure(String marker) {
		this.marker = marker;
	}

	// /**
	// * New measure algorithm based on the results provided by the specified
	// * algorithm.
	// *
	// * @param algo
	// * Algorithm which results will be used for measurement.
	// */
	// public CommunityMeasure(CommunityAlgorithm algo) {
	// this.marker = algo.getMarker();
	// }

	/**
	 * The last computed measure.
	 * 
	 * @complexity O(1)
	 * @return The last computed measure.
	 */
	public double getLastComputedValue() {
		return M;
	}

	/**
	 * Compute the measure (if the graph changed since the last computation).
	 * 
	 * @complexity Depends on the actual measure
	 * @return The current measure.
	 */
	public double getMeasure() {
		compute();
		return M;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		if (graph != this.graph) {
			if (this.graph != null) {
				this.graph.removeSink(this);
			}

			this.graph = graph;

			if (this.graph != null) {
				this.graph.addSink(this);
				initialize();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public abstract void compute();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
	 */
	public void terminate() {
		// NOP.
	}

	protected void initialize() {
		communities = communities(graph, marker);
	}

	/*
	 * @see org.graphstream.stream.Sink#nodeAdded(java.lang.String, long,
	 * java.lang.String)
	 */
	@Override
	public void nodeAdded(String graphId, long timeId, String nodeId) {
		Node n = graph.getNode(nodeId);
		assignNode(nodeId, n.getAttribute(marker), communities);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Sink#nodeRemoved(java.lang.String, long,
	 * java.lang.String)
	 */
	@Override
	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		Node n = graph.getNode(nodeId);
		unassignNode(nodeId, n.getAttribute(marker), communities);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Sink#edgeAdded(java.lang.String, long,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		graphChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Sink#edgeRemoved(java.lang.String, long,
	 * java.lang.String)
	 */
	@Override
	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		graphChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Sink#graphCleared(java.lang.String, long)
	 */
	@Override
	public void graphCleared(String graphId, long timeId) {
		graphChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Sink#nodeAttributeAdded(java.lang.String,
	 * long, java.lang.String, java.lang.String, java.lang.Object)
	 */
	@Override
	public void nodeAttributeAdded(String graphId, long timeId, String nodeId,
			String attribute, Object value) {
		nodeAttributeChanged(graphId, timeId, nodeId, attribute, null, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.Sink#nodeAttributeChanged(java.lang.String,
	 * long, java.lang.String, java.lang.String, java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public void nodeAttributeChanged(String graphId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		if (attribute.equals(marker) && oldValue != newValue) {
			unassignNode(nodeId, oldValue, communities);
			assignNode(nodeId, newValue, communities);
		}
	}

	/**
	 * Put the node referred by nodeId to the community referred by newValue in
	 * the assignment referred by assignment.
	 * 
	 * @param nodeId referred node id
	 * @param newValue community Key
	 * @param assignment map of communities
	 */
	protected void assignNode(String nodeId, Object newValue,
			HashMap<Object, HashSet<Node>> assignment) {
		// A node added, put it in the communities.
		Node node = graph.getNode(nodeId);
		if (node != null) {
			Object communityKey = newValue;

			if (communityKey == null)
				communityKey = "NULL_COMMUNITY";
			HashSet<Node> community = assignment.get(communityKey);

			if (community == null) {
				community = new HashSet<Node>();
				assignment.put(communityKey, community);
			}
			community.add(node);

			graphChanged = true;
		}
	}

	/**
	 * Remove the node referred by nodeId from the community referred by
	 * oldValue in the assignment referred by assignment.
	 * 
	 * @param nodeId referred node id
	 * @param oldValue community Key
	 * @param assignment map of communities
	 */
	protected void unassignNode(String nodeId, Object oldValue,
			HashMap<Object, HashSet<Node>> assignment) {
		Node node = graph.getNode(nodeId);
		if (node != null) {
			Object communityKey = oldValue;

			if (communityKey == null)
				communityKey = "NULL_COMMUNITY";
			HashSet<Node> community = assignment.get(communityKey);

			assert community != null : "Removing a node that was not placed in any community !!";

			if (community != null) {
				community.remove(node);
				if (community.size() == 0) {
					assignment.remove(communityKey);
				}
			}
			graphChanged = true;
		}
	}

}
