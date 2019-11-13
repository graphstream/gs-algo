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
 * @since 2009-02-19
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import org.graphstream.stream.Pipe;

/**
 * Random Euclidean graph generator.
 * 
 * <p>
 * This generator creates random graphs of any size. Links of such graphs are
 * created according to a threshold. If the Euclidean distance between two nodes
 * is less than a given threshold, then a link is created between those 2 nodes.
 * <p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>
 * Calling {@link #begin()} put one unique node in the graph, then
 * {@link #nextEvents()} will add a new node each time it is called and connect
 * this node to its neighbors according to the threshold planar Euclidean
 * distance.
 * </p>
 * 
 * <p>
 * This generator has the ability to add randomly chosen numerical values on
 * arbitrary attributes on edges or nodes of the graph, and to randomly choose a
 * direction for edges.
 * </p>
 * 
 * <p>
 * A list of attributes can be given for nodes and edges. In this case each new
 * node or edge added will have this attribute and the value will be a randomly
 * chosen number. The range in which these numbers are chosen can be specified.
 * </p>
 * 
 * <p>
 * By default, edges are not oriented. It is possible to ask orientation, in
 * which case the direction is chosen randomly.
 * </p>
 * 
 * <p>
 * By default, the graph is generated in the plane (2 dimensions) . Cartesian
 * coordinates on nodes will be generated at random. So, each node will
 * automatically be given two attributes: "x" and "y". If a dimension is
 * specified, then |dimension| attributes are generated, and the 2-norm distance
 * (<a href="http://en.wikipedia.org/wiki/Euclidean_distance">Euclidean
 * distance</a>) is considered in that dimension between the nodes.
 * </p>
 * 
 * <p>
 * If the dimension is 2, then attributes "x" and "y" are defined for each node.
 * If dimension is 3, then attributes "x", "y" and "z" are used. For other
 * values of dimension, |dimension| attributes are defined ("xi" with "i" \in
 * |dimension|) .
 * </p>
 * 
 * <h2>Complexity</h2>
 * 
 * For the construction of a n nodes graph, the complexity is about O(n^2).
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * Graph graph = new SingleGraph("random euclidean");
 * Generator gen = new RandomEuclideanGenerator();
 * gen.addSink(graph);
 * gen.begin();
 * for(int i=0; i<1000; i++) {
 * 		gen.nextEvents();
 * }
 * gen.end();
 * graph.display(false);
 * </pre>
 * 
 * @since June 25 2007
 * @complexity For the construction of a n nodes graph, the complexity is about
 *             O(n^2).
 */
public class RandomEuclideanGenerator extends BaseGenerator implements Pipe {
	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;

	/**
	 * The dimension of the space.
	 */
	protected int dimension = 2;

	/**
	 * The threshold that defines whether or not a link is created between to
	 * nodes. Since the coordinate system is defined between 0 and 1, the
	 * threshold has to be set between these two bounds.
	 */
	protected double threshold = 0.1;
	
	/**
	 * New random Euclidean graph generator. By default no attributes are added
	 * to nodes and edges. Dimension of the space is two.
	 */
	public RandomEuclideanGenerator() {
		super();
		initDimension(2);
		setUseInternalGraph(true);
	}

	/**
	 * New random Euclidean graph generator. By default no attributes are added
	 * to nodes and edges. You may also specify a dimension for the space.
	 * 
	 * @param dimension
	 *            The dimension of the space for the graph. By default it is
	 *            two.
	 */
	public RandomEuclideanGenerator(int dimension) {
		super();
		initDimension(dimension);
		setUseInternalGraph(true);
	}

	/**
	 * New random Euclidean graph generator. By default no attributes are added
	 * to nodes and edges. It is possible to make edge randomly directed. You
	 * may also specify a dimension for the space.
	 * 
	 * @param dimension
	 *            The dimension of the space for the graph. By default it is
	 *            two.
	 * @param directed
	 *            If true the edges are directed.
	 * @param randomlyDirectedEdges
	 *            If true edge, are directed and the direction is chosen at
	 *            randomly.
	 */
	public RandomEuclideanGenerator(int dimension, boolean directed,
			boolean randomlyDirectedEdges) {
		super(directed, randomlyDirectedEdges);
		initDimension(dimension);
		setUseInternalGraph(true);
	}

	/**
	 * New random Euclidean graph generator.
	 * 
	 * @param dimension
	 *            The dimension of the space for the graph. By default it is
	 *            two.
	 * @param directed
	 *            If true the edges are directed.
	 * @param randomlyDirectedEdges
	 *            It true, edges are directed and the direction is chosen at
	 *            random.
	 * @param nodeAttribute
	 *            put an attribute by that name on each node with a random
	 *            numeric value.
	 * @param edgeAttribute
	 *            put an attribute by that name on each edge with a random
	 *            numeric value.
	 */
	public RandomEuclideanGenerator(int dimension, boolean directed,
			boolean randomlyDirectedEdges, String nodeAttribute,
			String edgeAttribute) {
		super(directed, randomlyDirectedEdges, nodeAttribute, edgeAttribute);
		initDimension(dimension);
		setUseInternalGraph(true);
	}

	private void initDimension(int dimension) {
		this.dimension = dimension;

		if (dimension > 0) {
			if (dimension == 2) {
				super.addNodeAttribute("x");
				super.addNodeAttribute("y");
			} else if (dimension == 3) {
				super.addNodeAttribute("x");
				super.addNodeAttribute("y");
				super.addNodeAttribute("z");
			} else {
				for (int i = 0; i < dimension; i++)
					super.addNodeAttribute("x" + i);
			}
		} else
			System.err.println("dimension has to be higher that zero");
	}

	/**
	 * Start the generator. A single node is added.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		String id = Integer.toString(nodeNames++);

		addNode(id);
	}

	/**
	 * Step of the generator. Add a new node and connect it with some others.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		String id = Integer.toString(nodeNames++);

		addNode(id);

		internalGraph.nodes()
			.filter(n -> !id.equals(n.getId()) && distance(id, n.getId()) < threshold)
			.forEach(n -> addEdge(id + "-" + n.getId(), id, n.getId()));

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	@Override
	public void end() {
		super.end();
	}

	/**
	 * Distance between two nodes.
	 * 
	 * @param n1
	 *            first node
	 * @param n2
	 *            second node
	 * @return distance between n1 and n2
	 */
	private double distance(String n1, String n2) {
		double d = 0.0;

		if (dimension == 2) {
			double x1 = internalGraph.getNode(n1).getNumber("x");
			double y1 = internalGraph.getNode(n1).getNumber("y");
			double x2 = internalGraph.getNode(n2).getNumber("x");
			double y2 = internalGraph.getNode(n2).getNumber("y");

			d = Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
		} else if (dimension == 3) {
			double x1 = internalGraph.getNode(n1).getNumber("x");
			double y1 = internalGraph.getNode(n1).getNumber("y");
			double x2 = internalGraph.getNode(n2).getNumber("x");
			double y2 = internalGraph.getNode(n2).getNumber("y");
			double z1 = internalGraph.getNode(n1).getNumber("z");
			double z2 = internalGraph.getNode(n2).getNumber("z");

			d = Math.pow(z1 - z2, 2) + Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2);
		} else {
			for (int i = 0; i < dimension; i++) {
				double xi1 = internalGraph.getNode(n1).getNumber("x" + i);
				double xi2 = internalGraph.getNode(n2).getNumber("x" + i);

				d += Math.pow(xi1 - xi2, 2);
			}
		}

		return Math.sqrt(d);
	}

	/**
	 * Set the threshold that defines whether or not a link is created between
	 * to notes. Since the coordinate system is defined between 0 and 1, the
	 * threshold has to be set between these two bounds.
	 * 
	 * @param threshold
	 *            The defined threshold.
	 */
	public void setThreshold(double threshold) {
		if (threshold <= 1f && threshold >= 0f)
			this.threshold = threshold;
	}

	protected void nodeAttributeHandling(String nodeId, String key, Object val) {
		if (key != null && key.matches("x|y|z") && val instanceof Float) {
			int i = ((int) key.charAt(0)) - (int) 'x';

			if (i < dimension)
				internalGraph.getNode(nodeId).setAttribute(key, val);
		}
	}

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		nodeAttributeHandling(nodeId, attribute, value);
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		nodeAttributeHandling(nodeId, attribute, newValue);
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
	}

	public void graphCleared(String sourceId, long timeId) {
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
	}

	public void stepBegins(String sourceId, long timeId, double step) {
	}
}