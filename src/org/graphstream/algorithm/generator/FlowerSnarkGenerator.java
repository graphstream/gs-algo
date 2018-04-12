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
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

/**
 * Flower snark generator.
 * 
 * <p>
 * In the mathematical field of graph theory, the flower snarks form an infinite
 * family of snarks introduced by Rufus Isaacs in 1975. As snarks, the flower
 * snarks are a connected, bridgeless cubic graphs with chromatic index equal to
 * 4. The flower snarks are non-planar and non-hamiltonian.
 * </p>
 * 
 * Source : <a href="http://en.wikipedia.org/wiki/Flower_snark">Wikipedia</a>
 * 
 * @reference Isaacs, R.
 *            "Infinite Families of Nontrivial Trivalent Graphs Which Are Not Tait Colorable."
 *            Amer. Math. Monthly 82, 221–239, 1975.
 */
public class FlowerSnarkGenerator extends BaseGenerator {

	private int nextStarNumber = 1;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		addStar();
		addStar();
		addStar();

		addEdge(N.B, 1, N.B, 2);
		addEdge(N.B, 2, N.B, 3);
		addEdge(N.B, 3, N.B, 1);

		addEdge(N.C, 1, N.C, 2);
		addEdge(N.C, 2, N.C, 3);
		addEdge(N.C, 3, N.D, 1);
		addEdge(N.D, 1, N.D, 2);
		addEdge(N.D, 2, N.D, 3);
		addEdge(N.D, 3, N.C, 1);

		flushCoords();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		delEdge(N.B, nextStarNumber - 1, N.B, 1);
		delEdge(N.C, nextStarNumber - 1, N.D, 1);
		delEdge(N.D, nextStarNumber - 1, N.C, 1);

		addStar();

		addEdge(N.B, nextStarNumber - 2, N.B, nextStarNumber - 1);
		addEdge(N.B, nextStarNumber - 1, N.B, 1);
		addEdge(N.C, nextStarNumber - 2, N.C, nextStarNumber - 1);
		addEdge(N.C, nextStarNumber - 1, N.D, 1);
		addEdge(N.D, nextStarNumber - 2, N.D, nextStarNumber - 1);
		addEdge(N.D, nextStarNumber - 1, N.C, 1);

		flushCoords();

		return true;
	}

	private void addStar() {
		int i = nextStarNumber++;

		addNode(N.A, i);
		addNode(N.B, i);
		addNode(N.C, i);
		addNode(N.D, i);

		addEdge(N.A, i, N.B, i);
		addEdge(N.A, i, N.C, i);
		addEdge(N.A, i, N.D, i);
	}

	protected static enum N {
		A, B, C, D
	}

	private void addNode(N n, int i) {
		addNode(getNodeId(n, i));
	}

	protected String getNodeId(N n, int i) {
		return String.format("%s%04d", n, i);
	}

	private void addEdge(N n1, int i1, N n2, int i2) {
		addEdge(getEdgeId(n1, i1, n2, i2), getNodeId(n1, i1), getNodeId(n2, i2));
	}

	private void delEdge(N n1, int i1, N n2, int i2) {
		delEdge(getEdgeId(n1, i1, n2, i2));
	}

	protected String getEdgeId(N n1, int i1, N n2, int i2) {
		return String.format("%s%s", getNodeId(n1, i1), getNodeId(n2, i2));
	}

	protected void flushCoords() {
		double d = 2 * Math.PI / (nextStarNumber - 1);

		for (int i = 1; i < nextStarNumber; i++) {
			sendNodeAttributeChanged(sourceId, getNodeId(N.B, i), "x", null,
					Math.cos((i - 1) * d));
			sendNodeAttributeChanged(sourceId, getNodeId(N.B, i), "y", null,
					Math.sin((i - 1) * d));

			sendNodeAttributeChanged(sourceId, getNodeId(N.A, i), "x", null,
					2 * Math.cos((i - 1) * d));
			sendNodeAttributeChanged(sourceId, getNodeId(N.A, i), "y", null,
					2 * Math.sin((i - 1) * d));

			sendNodeAttributeChanged(sourceId, getNodeId(N.C, i), "x", null,
					3 * Math.cos((i - 1) * d - d / 4.0));
			sendNodeAttributeChanged(sourceId, getNodeId(N.C, i), "y", null,
					3 * Math.sin((i - 1) * d - d / 4.0));

			sendNodeAttributeChanged(sourceId, getNodeId(N.D, i), "x", null,
					3 * Math.cos((i - 1) * d + d / 4.0));
			sendNodeAttributeChanged(sourceId, getNodeId(N.D, i), "y", null,
					3 * Math.sin((i - 1) * d + d / 4.0));
		}
	}
}
