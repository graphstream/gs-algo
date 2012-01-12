/*
 * Copyright 2006 - 2012
 *      Stefan Balev       <stefan.balev@graphstream-project.org>
 *      Julien Baudry	<julien.baudry@graphstream-project.org>
 *      Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *      Yoann Pigné	<yoann.pigne@graphstream-project.org>
 *      Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
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
package org.graphstream.algorithm.generator;

import org.graphstream.algorithm.Toolkit;

/**
 * Random graph generator.
 * 
 * <p>
 * Generate a random graph of any size.
 * </p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>
 * This generator creates random graphs of any size. Calling {@link #begin()}
 * put one unique node in the graph, then {@link #nextEvents()} will add a new
 * node each time it is called and connect this node randomly to others.
 * </p>
 * 
 * <p>
 * The generator tries to generate nodes with random connections, with each node
 * having in average a given degree. The law in a Poisson law, however, the way
 * this generator works, adding node after node, perturbs this process. We
 * should first allocate all the needed nodes, then create edges. However, we
 * create nodes at the same rate as edges. The more nodes are added the more the
 * degree distribution curve is shifted toward the right.
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
 * <h2>Complexity</h2>
 * 
 * <p>
 * At each call to {@link #nextEvents()} at max k operations are run with
 * k the average degree.
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * Graph graph = new SingleGraph("Random");
 * Generator gen = new RandomGenerator();
 * gen.addSinkg(graph);
 * gen.begin();
 * for(int i=0; i<100; i++)
 * 	gen.nextEvents();
 * gen.end();
 * graph.display();
 * </pre>
 * 
 * @since 2007
 */
public class RandomGenerator extends BaseGenerator {
	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;

	/**
	 * The average degree of each node.
	 */
	protected int averageDegree = 1;

	/**
	 * New full graph generator with default attributes.
	 */
	public RandomGenerator() {
		this(2);
	}

	/**
	 * New full graph generator. By default no attributes are added to nodes and
	 * edges, and edges are not directed.
	 * 
	 * @param averageDegree
	 *            The average degree of nodes.
	 */
	public RandomGenerator(int averageDegree) {
		super();
		setUseInternalGraph(true);
		this.averageDegree = averageDegree;
	}

	/**
	 * New full graph generator.
	 * 
	 * @param averageDegree
	 *            The average degree of nodes.
	 * @param directed
	 *            Are edges directed?.
	 * @param randomlyDirectedEdges
	 *            randomly direct generated edges.
	 */
	public RandomGenerator(int averageDegree, boolean directed,
			boolean randomlyDirectedEdges) {
		super(directed, randomlyDirectedEdges);
		setUseInternalGraph(true);
		this.averageDegree = averageDegree;
	}

	/**
	 * New random graph generator.
	 * 
	 * @param averageDegree
	 *            The average degree of nodes.
	 * @param directed
	 *            Are edges directed?.
	 * @param randomlyDirectedEdges
	 *            randomly direct generated edges.
	 * @param nodeAttribute
	 *            put an attribute by that name on each node with a random
	 *            numeric value.
	 * @param edgeAttribute
	 *            put an attribute by that name on each edge with a random
	 *            numeric value.
	 */
	public RandomGenerator(int averageDegree, boolean directed,
			boolean randomlyDirectedEdges, String nodeAttribute,
			String edgeAttribute) {
		super(directed, randomlyDirectedEdges, nodeAttribute, edgeAttribute);
		setUseInternalGraph(true);
		this.averageDegree = averageDegree;
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
	 * Step the generator. A new node is added and connected with some others.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		String id = Integer.toString(nodeNames++);

		addNode(id);

		// Choose the degree of the node randomly, centred around the
		// predefined average degree.

		int degree = poisson(averageDegree);

		// For this degree, we choose randomly degree other nodes to be linked
		// to the new node.

		for (int i = 0; i < degree; ++i) {
			String otherId = Toolkit.randomNode(internalGraph, random).getId();

			if (otherId != id) {
				String edgeId = getEdgeId(id, otherId);

				if (internalGraph.getEdge(edgeId) == null)
					addEdge(edgeId, id, otherId);
			}
		}

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

	protected String getEdgeId(String src, String trg) {
		if (src.compareTo(trg) < 0) {
			return String.format("%s_%s", src, trg);
		} else {
			return String.format("%s_%s", trg, src);
		}
	}

	/**
	 * Generate a random integer centered around p.
	 * 
	 * @param p
	 *            The average value of the random number.
	 * @return A random int.
	 */
	protected int poisson(float p) {
		double a = Math.exp(-p);
		int n = 0;
		double u = random.nextFloat();

		while (u > a) {
			u *= random.nextFloat();
			n++;
		}

		return n;
	}
}