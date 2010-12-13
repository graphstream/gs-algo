/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm.generator;

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
		enableKeepNodesId();

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

			if (nodes.contains(neigh))
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
		}

		if (x < currentWidth - 1) {
			neigh = getNodeId(x + 1, y);

			if (nodes.contains(neigh))
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
		}

		if (y > 0) {
			neigh = getNodeId(x, y - 1);

			if (nodes.contains(neigh))
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
		}

		if (y < currentHeight - 1) {
			neigh = getNodeId(x, y + 1);

			if (nodes.contains(neigh))
				addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
		}

		if (cross) {
			if (x > 0) {
				if (y > 0) {
					neigh = getNodeId(x - 1, y - 1);

					if (nodes.contains(neigh))
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x - 1, y + 1);

					if (nodes.contains(neigh))
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				}
			}

			if (x < currentWidth - 1) {
				if (y > 0) {
					neigh = getNodeId(x + 1, y - 1);

					if (nodes.contains(neigh))
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x + 1, y + 1);

					if (nodes.contains(neigh))
						addEdge(getEdgeId(nodeId, neigh), nodeId, neigh);
				}
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

			if (nodes.contains(neigh))
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (x < currentWidth - 1) {
			neigh = getNodeId(x + 1, y);

			if (nodes.contains(neigh))
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (y > 0) {
			neigh = getNodeId(x, y - 1);

			if (nodes.contains(neigh))
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (y < currentHeight - 1) {
			neigh = getNodeId(x, y + 1);

			if (nodes.contains(neigh))
				delEdge(getEdgeId(nodeId, neigh));
		}

		if (cross) {
			if (x > 0) {
				if (y > 0) {
					neigh = getNodeId(x - 1, y - 1);

					if (nodes.contains(neigh))
						delEdge(getEdgeId(nodeId, neigh));
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x - 1, y + 1);

					if (nodes.contains(neigh))
						delEdge(getEdgeId(nodeId, neigh));
				}
			}

			if (x < currentWidth - 1) {
				if (y > 0) {
					neigh = getNodeId(x + 1, y - 1);

					if (nodes.contains(neigh))
						delEdge(getEdgeId(nodeId, neigh));
				}

				if (y < currentHeight - 1) {
					neigh = getNodeId(x + 1, y + 1);

					if (nodes.contains(neigh))
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
			addNode(getNodeId(i, currentHeight));
			connectNode(i, currentHeight);
		}

		for (int i = 0; i < currentHeight; i++) {
			addNode(getNodeId(currentWidth, i));
			connectNode(currentWidth, i);
		}

		addNode(getNodeId(currentWidth, currentHeight));
		connectNode(currentWidth, currentHeight);

		currentWidth++;
		currentHeight++;

		for (int k = 0; k < holesPerStep; k++) {
			if (random.nextFloat() < holeProbability) {
				int x1, y1;
				int sizeX, sizeY;

				do {
					x1 = random.nextInt(currentWidth);
					y1 = random.nextInt(currentHeight);
				} while (!nodes.contains(getNodeId(x1, y1)));

				sizeX = random.nextInt(holeMaxSize);
				sizeY = random.nextInt(holeMaxSize - sizeX);

				for (int i = 0; i < sizeX; i++)
					for (int j = 0; j < sizeY; j++) {
						if (nodes.contains(getNodeId(x1 + i, y1 + j))) {
							disconnectNode(x1 + i, y1 + j);
							delNode(getNodeId(x1 + i, y1 + j));
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
