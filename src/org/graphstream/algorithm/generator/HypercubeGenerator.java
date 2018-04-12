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
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;


/**
 * Generates a hypercube of dimension equal to the number of calls to {@link #nextEvents()}.
 * 
 * <p>
 * After d calls to {@link #nextEvents()} one obtains a graph with 2^d nodes. Nodes i and j are connected
 * if and only if their binary representations differ by exactly one bit.
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * Graph graph = new SingleGraph("hypercube");
 * Generator gen = new HypercubeGenerator();
 * gen.addSink(graph);
 * gen.begin();
 * for (dim = 1; dim <= 4; dim++)
 *     gen.nextEvents();
 * gen.end();
 * </pre>
 *
 */
public class HypercubeGenerator extends BaseGenerator {
	protected int dim;
	protected int edgeId;

	/**
	 * Creates a hypercube generator
	 */
	public HypercubeGenerator() {
		super();
	}

	/**
	 * Creates a hypercube of dimension 0, that is, a single node
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	@Override
	public void begin() {
		dim = edgeId = 0;
		addNode("0");
	}

	/**
	 * Grows the hypercube dimension by doubling all the existing nodes
	 * and adding the corresponding edges.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	@Override
	public boolean nextEvents() {
		int oldN = 1 << dim++;
		int newN = oldN << 1;
		for (int j = oldN; j < newN; j++) {
			addNode("" + j);
			for (int d = 0; d < dim; d++) {
				int mask = 1 << d;
				if ((j & mask) != 0) {
					int i = j & ~mask;
					addEdge("" + edgeId++, i + "", j + "");
				}

			}
		}
		return true;
	}

//	public static void main(String[] args) {
//		Graph g = new SingleGraph("test");
//		g.display();
//		HypercubeGenerator gen = new HypercubeGenerator();
//		gen.addNodeLabels(true);
//		gen.addSink(g);
//		gen.begin();
//		for (int i = 0; i < 5; i++)
//			gen.nextEvents();
//		gen.end();
//	}
}
