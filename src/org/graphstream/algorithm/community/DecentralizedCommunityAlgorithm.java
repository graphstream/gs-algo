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
 * @since 2010-10-01
 * 
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.community;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.stream.Collectors;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Element;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.Sink;

/**
 * Base class for all distributed community detection algorithm. They all
 * implement the DynamicAlgorithm and Sink interfaces.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public abstract class DecentralizedCommunityAlgorithm implements
		DynamicAlgorithm, Sink {
	/**
	 * The graph to apply the algorithm.
	 */
	protected Graph graph;

	/**
	 * Name of the attribute marking the communities. Default is "community".
	 * This is prefixed by the algorithm class and memory location to make this
	 * unique for each instance of the algorithm.
	 */
	protected String marker;
	protected String nonUniqueMarker;

	/**
	 * Set to false after {@link #compute()}, unless static mode is set.
	 */
	protected boolean graphChanged = true;

	/**
	 * Force algorithm to perform even if the graph is static.
	 */
	protected boolean staticMode = false;

	/**
	 * Random number generator used to shuffle the nodes. Shall be used by all
	 * inherited algorithms for random number generation
	 */
	protected Random rng;

	/**
	 * Create a new distributed community detection algorithm, without attaching
	 * it to a graph
	 */
	public DecentralizedCommunityAlgorithm() {
	}

	/**
	 * Create a new distributed community detection algorithm, attached to the
	 * specified graph
	 * 
	 * @param graph
	 *            The graph on which the community assignment will be performed
	 */
	public DecentralizedCommunityAlgorithm(Graph graph) {
		this();
		init(graph);
	}

	/**
	 * Create a new distributed community detection algorithm, attached to the
	 * specified graph, and using the specified marker to store the community
	 * attribute
	 * 
	 * @param graph
	 *            The graph on which the community assignment will be performed
	 * @param marker
	 *            Marker string used to store the current community of a node
	 */
	public DecentralizedCommunityAlgorithm(Graph graph, String marker) {
		this();
		setMarker(marker);
		init(graph);
	}

	/**
	 * Initialize the distributed community detection algorithm, attaching it to
	 * the specified graph, and using the specified marker to store the
	 * community attribute
	 * 
	 * @param graph the graph
	 * @param marker marker string to store the community attribute
	 */
	public void init(Graph graph, String marker) {
		setMarker(marker);
		init(graph);
	}

	/**
	 * Initialize the distributed community detection algorithm, attaching it to
	 * the specified graph, and using the default marker to store the community
	 * attribute.
	 * 
	 * By default an uncontrolled random number generator will be used. For sake
	 * of reproducibility, use the {@link #setRandom(Random)} function to use a
	 * controlled random number generator with this algorithm.
	 * 
	 * @param graph the graph
	 */
//	@Override
	public void init(Graph graph) {
		/*
		 * Set the marker to a default value unless set when instantiating the
		 * class
		 */
		if (this.marker == null)
			setMarker(null);
		this.graph = graph;

		/*
		 * Initiate an uncontrolled random network generator
		 */
		if (this.rng == null)
			rng = new Random();
	}

//	@Override
	public void terminate() {
	}

	/**
	 * Enable the static mode. In this mode, algorithm will perform even if the
	 * graph hasn't changed (useful for static graphs).
	 */
	public void staticMode() {
		staticMode = true;
	}

	/**
	 * Set the marker used to store the community assignment to the specified
	 * value. The given value will be prefixed by
	 * [AlgorithmClass].[InstanceNumber] to make this unique for each instance
	 * of the algorithm.
	 * 
	 * @param marker marker string to store the community attribute
	 */
	@Parameter
	public void setMarker(String marker) {
		if (marker == null) {
			this.nonUniqueMarker = "community";
		} else {
			this.nonUniqueMarker = marker;
		}
		this.marker = this.toString() + "." + nonUniqueMarker;
	}

	/**
	 * Get the marker used to store the community assignment
	 * 
	 * @return the complete (i.e. prefixed) marker
	 */
	public String getMarker() {
		return this.marker;
	}

	/**
	 * Set the random number generator for this algorithm. For sake of
	 * reproducibility, the given random number generator shall be initiated
	 * with a controlled seed.
	 * 
	 * @param rng
	 *            an initialized java.util.Random object.
	 */
	public void setRandom(Random rng) {
		this.rng = rng;
	}

	/**
	 * Get the random number generator currently used for this algorithm.
	 * 
	 * @return the current random number generator.
	 */
	public Random getRandom() {
		return this.rng;
	}

	/**
	 * Compute an iteration of the algorithm for all the nodes of the network.
	 * 
	 * @complexity N times the complexity of the computeNode() function, where N
	 *             is the number of nodes in the network.
	 */
//	@Override
	public void compute() {
		/*
		 * This simply calls the computeNode method for all nodes in the graph.
		 * Nodes are processed in a random order. Computation only occurs if the
		 * graph has changed since last call
		 */
		if (graphChanged) {
			ArrayList<Node> nodeSet = graph.nodes().collect(Collectors.toCollection(ArrayList::new));// new ArrayList<Node>(graph.getNodeSet());
			Collections.shuffle(nodeSet, rng);
			
			nodeSet.forEach(node -> {
				computeNode(node);
				updateDisplayClass(node);
			});
			graphChanged = staticMode;
		}
	}

	/**
	 * Perform computation of one iteration of the algorithm on a given node.
	 * 
	 * @param node node to compute
	 */
	public abstract void computeNode(Node node);

	/**
	 * Generate a new original community and attribute it to a node
	 * 
	 * @param node
	 *            The node that will originate the new community
	 */
	protected void originateCommunity(Node node) {
		node.setAttribute(marker, new Community());
	}

	/**
	 * Update the display class of the node based on its current community.
	 * 
	 * The class name is [marker]_[id] where "marker" is the attribute name used
	 * to store the current community, and [id] the id of this community.
	 * 
	 * @param node node to compute
	 */
	protected void updateDisplayClass(Node node) {
		node.setAttribute(
				"ui.class",
				nonUniqueMarker + "_"
						+ ((Community) node.getAttribute(marker)).getId());
	}

	public void attributeChanged(Element element, String attribute,
			Object oldValue, Object newValue) {
	}

	public void nodeAdded(String graphId, long timeId, String nodeId) {
		graphChanged = true;
	}

	public void nodeRemoved(String graphId, long timeId, String nodeId) {
		graphChanged = true;
	}

	public void edgeAdded(String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		graphChanged = true;
	}

	public void edgeRemoved(String graphId, long timeId, String edgeId) {
		graphChanged = true;
	}

	public void graphCleared(String graphId, long timeId) {
		graphChanged = true;
	}

	public void stepBegins(String graphId, long timeId, double time) {
	}

	public void graphAttributeAdded(String graphId, long timeId,
			String attribute, Object value) {
	}

	public void graphAttributeChanged(String graphId, long timeId,
			String attribute, Object oldValue, Object newValue) {
	}

	public void graphAttributeRemoved(String graphId, long timeId,
			String attribute) {
	}

	public void nodeAttributeAdded(String graphId, long timeId, String nodeId,
			String attribute, Object value) {
		nodeAttributeChanged(graphId, timeId, nodeId, attribute, null, value);
	}

	public void nodeAttributeChanged(String graphId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
	}

	public void nodeAttributeRemoved(String graphId, long timeId,
			String nodeId, String attribute) {
	}

	public void edgeAttributeAdded(String graphId, long timeId, String edgeId,
			String attribute, Object value) {
	}

	public void edgeAttributeChanged(String graphId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
	}

	public void edgeAttributeRemoved(String graphId, long timeId,
			String edgeId, String attribute) {
	}
	
	@Result
	public String defaultMessage() {
		return "Result stored in \"ui.class\" attribute";
	}
}
