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
 * @since 2011-04-20
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

/**
 * Generate a Petersen graph.
 * 
 * <p>
 * In the mathematical field of graph theory, the Petersen graph is an
 * undirected graph with 10 vertices and 15 edges. It is a small graph that
 * serves as a useful example and counterexample for many problems in graph
 * theory. The Petersen graph is named for Julius Petersen, who in 1898
 * constructed it to be the smallest bridgeless cubic graph with no
 * three-edge-coloring. Although the graph is generally credited to Petersen, it
 * had in fact first appeared 12 years earlier, in a paper by A. B. Kempe
 * (1886). Donald Knuth states that the Petersen graph is "a remarkable
 * configuration that serves as a counterexample to many optimistic predictions
 * about what might be true for graphs in general."
 * </p>
 * 
 * Source : <a href="http://en.wikipedia.org/wiki/Petersen_graph">Wikipedia</a>
 * 
 * @reference Petersen, Julius (1898), "Sur le théorème de Tait",
 *            L'Intermédiaire des Mathématiciens 5: 225–227.
 * 
 */
public class PetersenGraphGenerator extends BaseGenerator {

	private int[][] nodes = { { 0, 2, 4, 1, 3 }, { 7, 6, 5, 9, 8 } };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		makeCycle(0, 4);
		makeCycle(5, 9);

		double a = Math.PI / 2.0;
		double d = 2.0 * Math.PI / 5.0;

		for (int i = 0; i < 5; i++) {
			String id1 = getNodeId(nodes[0][i]);
			String id2 = getNodeId(nodes[1][i]);

			addEdge(getEdgeId(nodes[0][i], nodes[1][i]), id1, id2);

			double x = Math.cos(a);
			double y = Math.sin(a);

			sendNodeAttributeAdded(sourceId, id1, "x", x);
			sendNodeAttributeAdded(sourceId, id1, "y", y);

			sendNodeAttributeAdded(sourceId, id2, "x", 2 * x);
			sendNodeAttributeAdded(sourceId, id2, "y", 2 * y);

			a += d;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		return false;
	}

	protected void makeCycle(int i, int j) {
		for (int k = i; k <= j; k++) {
			addNode(getNodeId(k));

			if (k > i)
				addEdge(getEdgeId(k - 1, k), getNodeId(k - 1), getNodeId(k));
		}

		addEdge(getEdgeId(i, j), getNodeId(i), getNodeId(j));
	}

	protected String getNodeId(int i) {
		return String.format("%02d", i);
	}

	protected String getEdgeId(int i, int j) {
		if (i > j) {
			j += i;
			i = j - i;
			j -= i;
		}

		return String.format("(%s;%s)", getNodeId(i), getNodeId(j));
	}
}
