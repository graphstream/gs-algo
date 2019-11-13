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
 * @since 2011-05-13
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

/**
 * Banana tree generator. A (n,k)-banana tree is composed of a root node and n
 * k-stars with one leaf of each star connected to the root node.
 * 
 * @reference Chen, W. C.; Lü, H. I.; and Yeh, Y. N.
 *            "Operations of Interlaced Trees and Graceful Trees." Southeast
 *            Asian Bull. Math. 21, 337-348, 1997.
 * 
 */
public class BananaTreeGenerator extends BaseGenerator {

	protected int k;
	protected int currentStarIndex;
	protected int edgeId;
	protected boolean setCoordinates;

	/**
	 * Build a new Banana tree generator with default star size.
	 */
	public BananaTreeGenerator() {
		this(4);
	}

	/**
	 * Build a new Banana tree generator composing of k-stars.
	 * 
	 * @param k
	 *            size of star
	 */
	public BananaTreeGenerator(int k) {
		this.k = k;
		this.setCoordinates = true;
		this.currentStarIndex = 0;
		this.edgeId = 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		addNode("root");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		addNode(getNodeId(currentStarIndex, 0));

		for (int i = 1; i < k; i++) {
			addNode(getNodeId(currentStarIndex, i));
			addEdge(String.format("E%04d", edgeId++),
					getNodeId(currentStarIndex, 0),
					getNodeId(currentStarIndex, i));
		}

		addEdge(String.format("E%04d", edgeId++),
				getNodeId(currentStarIndex, 1), "root");

		currentStarIndex++;

		if (setCoordinates)
			flushCoords();

		return true;
	}

	/**
	 * Format node id.
	 * 
	 * @param star
	 *            index of the star
	 * @param index
	 *            index of the node in the star
	 * @return unique node id
	 */
	protected String getNodeId(int star, int index) {
		return String.format("S%02d_%02d", star, index);
	}

	/**
	 * Set coordinates of nodes.
	 */
	protected void flushCoords() {
		sendNodeAttributeChanged(sourceId, "root", "x", null, 0);
		sendNodeAttributeChanged(sourceId, "root", "y", null, 0);

		double r1 = 8.0;

		for (int i = 0; i < currentStarIndex; i++) {
			double a = i * 2 * Math.PI / currentStarIndex;
			double rx = r1 * Math.cos(a);
			double ry = r1 * Math.sin(a);

			sendNodeAttributeChanged(sourceId, getNodeId(i, 0), "x", null, rx);
			sendNodeAttributeChanged(sourceId, getNodeId(i, 0), "y", null, ry);

			for (int j = 1; j < k; j++) {
				double b = a - (j - 1) * 2 * Math.PI / (k - 1);
				double r2 = 0.8 * r1 * Math.sin(Math.PI / currentStarIndex);

				sendNodeAttributeChanged(sourceId, getNodeId(i, j), "x", null,
						rx - r2 * Math.cos(b));
				sendNodeAttributeChanged(sourceId, getNodeId(i, j), "y", null,
						ry - r2 * Math.sin(b));
			}
		}
	}
}
