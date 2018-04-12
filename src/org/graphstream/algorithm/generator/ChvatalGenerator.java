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
 * @since 2011-04-19
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

/**
 * Generate the Chvatal graph.
 * 
 * <p>
 * In the mathematical field of graph theory, the Chvátal graph is an undirected
 * graph with 12 vertices and 24 edges, discovered by Václav Chvátal (1970). It
 * is triangle-free: its girth (the length of its shortest cycle) is four. It is
 * 4-regular: each vertex has exactly four neighbors. And its chromatic number
 * is 4: it can be colored using four colors, but not using only three. It is,
 * as Chvátal observes, the smallest possible 4-chromatic 4-regular
 * triangle-free graph; the only smaller 4-chromatic triangle-free graph is the
 * Grötzsch graph, which has 11 vertices but is not regular.
 * </p>
 * 
 * Source : <a
 * href="http://en.wikipedia.org/wiki/Chv%C3%A1tal_graph">Wikipedia</a>
 * 
 * @reference Chvátal, V. (1970),
 *            "The smallest triangle-free 4-chromatic 4-regular graph", Journal
 *            of Combinatorial Theory 9 (1): 93–94,
 *            doi:10.1016/S0021-9800(70)80057-6
 */
public class ChvatalGenerator extends BaseGenerator {

	private static final boolean[][] adjacencyMatrix = {
			{ false, true, false, true, true, true, false, false, false, false,
					false, false },
			{ false, false, true, false, false, false, true, true, false,
					false, false, false },
			{ false, false, false, true, false, false, false, false, true,
					true, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, true, true },
			{ false, false, false, false, false, false, false, false, true,
					true, false, true },
			{ false, false, false, false, false, false, true, false, true,
					true, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, true, true },
			{ false, false, false, false, false, false, false, false, true,
					false, true, true },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, true, false },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false },
			{ false, false, false, false, false, false, false, false, false,
					false, false, false }, };

	private static final double[][] coordinates = { { 0, 0 }, { 5, 0 },
			{ 5, 5 }, { 0, 5 }, { 1, 2 }, { 2, 1 }, { 3, 1 }, { 4, 2 },
			{ 4, 3 }, { 3, 4 }, { 2, 4 }, { 1, 3 } };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		for (int i = 0; i < 12; i++) {
			String id = String.format("%02d", i + 1);

			addNode(id);
			sendNodeAttributeAdded(sourceId, id, "x", coordinates[i][0]);
			sendNodeAttributeAdded(sourceId, id, "y", coordinates[i][1]);
		}

		for (int i = 0; i < 12; i++) {
			for (int j = i; j < 12; j++) {
				if (adjacencyMatrix[i][j])
					addEdge(String.format("%02d_%02d", i + 1, j + 1),
							String.format("%02d", i + 1),
							String.format("%02d", j + 1));
			}
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
}
