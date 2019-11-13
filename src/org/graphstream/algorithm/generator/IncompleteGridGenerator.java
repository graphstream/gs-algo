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
 */
package org.graphstream.algorithm.generator;

import java.util.HashSet;

/**
 * A grid generator with holes.
 * 
 * @author Guilhelm Savin
 */
public class IncompleteGridGenerator extends BaseGenerator {
	/**
	 * Current width of the grid.
	 */
	protected int currentWidth = 0;

	/**
	 * Current height of the grid.
	 */
	protected int currentHeight = 0;

	/**
	 * Probability of hole creation.
	 */
	protected float holeProbability = 0.5f;

	/**
	 * Max size of holes.
	 */
	protected int holeMaxSize = 5;

	/**
	 * Number of attempt to create a hole by step.
	 */
	protected int holesPerStep = 3;

	/**
	 * Connect nodes diagonally.
	 */
	protected boolean cross = true;

	/**
	 * Id of nodes that were connected to a deleted node. These nodes can not be
	 * deleted. This ensure to produce a connected graph.
	 */
	protected HashSet<String> unbreakable = new HashSet<String>();

	/**
	 * New generator.
	 */
	public IncompleteGridGenerator() {
		this(true, 0.5f, 5, 3);
	}

	/**
	 * New generator.
	 * 
	 * @param cross
	 *            connect nodes diagonally
	 * @param holeProbability
	 *            probability of an hole in the grid
	 * @param holeMaxSize
	 *            max size of holes
	 * @param holesPerStep
	 *            number of attempt to create a hole by step
	 */
	public IncompleteGridGenerator(boolean cross, float holeProbability,
			int holeMaxSize, int holesPerStep) {
		setUseInternalGraph(true);

		this.cross = cross;
		this.holeProbability = holeProbability;
		this.holeMaxSize = holeMaxSize;
		this.holesPerStep = holesPerStep;
	}

	protected String getNodeId(int x, int y) {
		return String.format("%d_%d", x, y);
	}

	protected String getEdgeId(String n1, String n2) {
		if (n1.compareTo(n2) < 0) {
			String tmp = n2;
			n2 = n1;
			n1 = tmp;
		}

		return String.format("%s-%s", n1, n2);
	}

	/**
	 * Connect a node.
	 * 
	 * @param x
	 *            abscissa of the node to disconnect
	 * @param y
	 *            ordina of the node to disconnect
	 */
	protected void connectNode(int x, int y) {
		String nodeId = getNodeId(x, y);
		String neigh;

		if (x > 0) {
			neigh = getNodeId(x - 1, y);

			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}

		if (x < currentWidth - 1) {
			neigh = getNodeId(x + 1, y);

			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}

		if (y > 0) {
			neigh = getNodeId(x, y - 1);

			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}

		if (y < currentHeight - 1) {
			neigh = getNodeId(x, y + 1);

			if (internalGraph.getNode(neigh) != null)
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
			else
				unbreakable.add(nodeId);
		}

		// Cross

		if (x > 0) {
			if (y > 0) {
				neigh = getNodeId(x - 1, y - 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross)
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				} else
					unbreakable.add(nodeId);
			}

			if (y < currentHeight - 1) {
				neigh = getNodeId(x - 1, y + 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross)
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				} else
					unbreakable.add(nodeId);
			}
		}

		if (x < currentWidth - 1) {
			if (y > 0) {
				neigh = getNodeId(x + 1, y - 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross)
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				} else
					unbreakable.add(nodeId);
			}

			if (y < currentHeight - 1) {
				neigh = getNodeId(x + 1, y + 1);

				if (internalGraph.getNode(neigh) != null) {
					if (cross)
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				} else
					unbreakable.add(nodeId);
			}
		}
	}

	/**
	 * Disconnect a node. Used to create holes.
	 * 
	 * @param x
	 *            abscissa of the node to disconnect
	 * @param y
	 *            ordina of the node to disconnect
	 */
	protected void disconnectNode(int x, int y) {
		String nodeId = getNodeId(x, y);
		String neigh;

		if (x > 0) {
			neigh = getNodeId(x - 1, y);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (x < currentWidth - 1) {
			neigh = getNodeId(x + 1, y);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (y > 0) {
			neigh = getNodeId(x, y - 1);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (y < currentHeight - 1) {
			neigh = getNodeId(x, y + 1);

			if (internalGraph.getNode(neigh) != null)
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (cross) {
			if (x > 0) {
				if (y > 0) {
					neigh = getNodeId(x - 1, y - 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x - 1, y + 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}
			}

			if (x < currentWidth - 1) {
				if (y > 0) {
					neigh = getNodeId(x + 1, y - 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x + 1, y + 1);

					if (internalGraph.getNode(neigh) != null)
						delEdge(getEdgeId(nodeId, neigh));
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {

	}

	/**
	 * Grow the graph. If grid dimensions are width x height, then after a call
	 * to this method dimensions will be (width+1)x(height+1). Eventually create
	 * some hopes.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		for (int i = 0; i < currentWidth; i++) {
			addNode(getNodeId(i, currentHeight), i, currentHeight);
			connectNode(i, currentHeight);
		}

		for (int i = 0; i < currentHeight; i++) {
			addNode(getNodeId(currentWidth, i), currentWidth, i);
			connectNode(currentWidth, i);
		}

		addNode(getNodeId(currentWidth, currentHeight), currentWidth, currentHeight);
		connectNode(currentWidth, currentHeight);

		currentWidth++;
		currentHeight++;

		for (int k = 0; k < holesPerStep; k++) {
			if (random.nextFloat() < holeProbability) {
				int x1, y1, t;
				int sizeX, sizeY;

				t = 0;

				do {
					x1 = random.nextInt(currentWidth);
					y1 = random.nextInt(currentHeight);
					t++;
				} while ((internalGraph.getNode(getNodeId(x1, y1)) == null || unbreakable
						.contains(getNodeId(x1, y1)))
						&& t < internalGraph.getNodeCount());

				if (t >= internalGraph.getNodeCount())
					continue;

				sizeX = random.nextInt(holeMaxSize);
				sizeY = random.nextInt(holeMaxSize - sizeX);

				for (int i = 0; i < sizeX; i++)
					for (int j = 0; j < sizeY; j++) {
						String id = getNodeId(x1 + i, y1 + j);
						if (internalGraph.getNode(id) != null
								&& !unbreakable.contains(id)) {
							disconnectNode(x1 + i, y1 + j);
							delNode(getNodeId(x1 + i, y1 + j));

							if (j == 0 && y1 > 0)
								unbreakable.add(getNodeId(x1 + i, y1 - 1));
							if (j == sizeY - 1 && y1 + sizeY < currentHeight)
								unbreakable.add(getNodeId(x1 + i, y1 + sizeY));

							if (i == 0 && x1 > 0)
								unbreakable.add(getNodeId(x1 - 1, y1 + j));
							if (i == sizeX - 1 && x1 + sizeX < currentWidth)
								unbreakable.add(getNodeId(x1 + sizeX, y1 + j));

							if (i == 0 && x1 > 0 && j == 0 && y1 > 0)
								unbreakable.add(getNodeId(x1 - 1, y1 - 1));
							if (i == sizeX - 1 && x1 + sizeX < currentWidth
									&& j == sizeY - 1
									&& y1 + sizeY < currentHeight)
								unbreakable.add(getNodeId(x1 + sizeX, y1
										+ sizeY));
							if (i == 0 && x1 > 0 && j == sizeY - 1
									&& y1 + sizeY < currentHeight)
								unbreakable.add(getNodeId(x1 - 1, y1 + sizeY));
							if (i == sizeX - 1 && x1 + sizeX < currentWidth
									&& j == 0 && y1 > 0)
								unbreakable.add(getNodeId(x1 + sizeX, y1 - 1));
						}
					}
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
}
