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
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import java.util.Random;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;

/**
 * Dorogovtsev - Mendes graph generator.
 * 
 * <p>
 * This generator creates graph using the Dorogovtsev - Mendes algorithm. This
 * starts by creating three nodes and tree edges, making a triangle, and then
 * add one node at a time. Each time a node is added, an edge is chosen randomly
 * and the node is connected via two new edges to the two extremities of the
 * chosen edge.
 * </p>
 * 
 * <p>
 * This process generates a power-low degree distribution, as nodes that have
 * more edges have more chances to be selected since their edges are more
 * represented in the edge set.
 * </p>
 * 
 * <p>
 * The Dorogovtsev - Mendes algorithm always produce planar graphs.
 * </p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>
 * The more this generator is iterated, the more nodes are generated. It can
 * therefore generate trees of any size. A each call to {@link #nextEvents()},
 * a new node and two edges are added.
 * </p>
 * 
 * <h2>Complexity</h2>
 * 
 * At each step only one node and two edges are added.
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * Graph graph = new SingleGraph("Dorogovtsev mendes");
 * Generator gen = new DorogovtsevMendesGenerator();
 * gen.addSink(graph);
 * gen.begin();
 * for(int i=0; i<100; i++) {
 * 		gen.nextEvents();
 * }
 * gen.end();
 * graph.display();
 * </pre>
 * 
 * <h2>References</h2>
 * 
 * <p>
 * This kind of graph is described, among others, in the "Evolution of networks"
 * by Dorogovtsev and Mendes.
 * </p>
 * 
 * @reference S. N. Dorogovtsev and J. F. F. Mendes, "Evolution of networks", in
 *            Adv. Phys. 51, 2002, 1079--1187, 
 *			  arXiv:cond-mat/0106144v2
 * 
 * @since 20070117
 */
public class DorogovtsevMendesGenerator extends BaseGenerator {
	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;

	/**
	 * Create a new generator with default random object.
	 */
	public DorogovtsevMendesGenerator() {
		setUseInternalGraph(true);
	}

	/**
	 * New generator with the given random number generator.
	 * 
	 * @param random
	 *            The number generator to use.
	 */
	public DorogovtsevMendesGenerator(Random random) {
		this();

		this.random = random;
	}

	/**
	 * Init the generator. An initial full graph of three nodes is build here.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		this.random = this.random == null ? new Random(
				System.currentTimeMillis()) : this.random;

		addNode("0");
		addNode("1");
		addNode("2");

		addEdge("0-1", "0", "1");
		addEdge("1-2", "1", "2");
		addEdge("2-0", "2", "0");

		nodeNames = 3;
	}

	/**
	 * Step of the DorogovtsevMendes generator. Add a new node <i>n</i>, then an
	 * edge is chosen randomly and its extremities are connected to the new node
	 * <i>n</i>.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		String name = Integer.toString(nodeNames++);
		Edge edge = Toolkit.randomEdge(internalGraph, random);
		String n0 = edge.getNode0().getId();
		String n1 = edge.getNode1().getId();

		addNode(name);

		addEdge(n0 + "-" + name, n0, name);
		addEdge(n1 + "-" + name, n1, name);

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
}