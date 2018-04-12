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
 * @since 2012-11-08
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

public class LifeGenerator extends BaseGenerator {

	private static final int[] NEIGHT = { -1, -1, 0, -1, 1, -1, -1, 0, 1, 0,
			-1, 1, 0, 1, 1, 1 };
	private static final int[] LINK_WITH = { -1, -1, 0, -1, 1, -1, -1, 0 };

	int width, height;
	boolean[] cells;
	boolean[] swap;
	boolean tore;
	boolean pushCoords;
	double step;

	public LifeGenerator(int width, int height, boolean[] data) {
		this.width = width;
		this.height = height;
		this.cells = Arrays.copyOf(data, data.length);
		this.swap = new boolean[width * height];
	}

	public LifeGenerator(String path) throws IOException {
		File in = new File(path);
		BufferedImage data = ImageIO.read(in);

		loadData(data);

		pushCoords = true;
		tore = true;
	}
	
	public LifeGenerator(InputStream in) throws IOException {
		BufferedImage data = ImageIO.read(in);

		loadData(data);

		pushCoords = true;
		tore = true;
	}

	public LifeGenerator(BufferedImage cellsData) {
		loadData(cellsData);

		pushCoords = true;
		tore = true;
	}

	protected void loadData(BufferedImage data) {
		int rgb;

		width = data.getWidth();
		height = data.getHeight();
		cells = new boolean[width * height];
		swap = new boolean[width * height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				rgb = data.getRGB(x, y);
				cells[x * height + y] = ((rgb & (0xFF << 16)) != 0x00000000);
			}
		}
	}

	protected void computeNextState() {
		int nx, ny, c, idx;
		boolean alive;

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				c = 0;
				idx = x * height + y;
				alive = cells[idx];

				for (int i = 0; i < NEIGHT.length; i += 2) {
					if (!tore
							&& (x + NEIGHT[i] < 0 || x + NEIGHT[i] >= width
									|| y + NEIGHT[i + 1] < 0 || y
									+ NEIGHT[i + 1] >= height))
						continue;

					nx = (x + NEIGHT[i] + width) % width;
					ny = (y + NEIGHT[i + 1] + height) % height;

					if (cells[nx * height + ny])
						c++;
				}

				swap[idx] = false;

				if (!alive && c == 3)
					swap[idx] = true;
				else if (alive && c < 2)
					swap[idx] = false;
				else if (alive && c < 4)
					swap[idx] = true;
				else if (alive && c > 3)
					swap[idx] = false;
			}
	}

	protected void addNode(int x, int y) {
		String id = nodeId(x, y);
		addNode(id);

		if (pushCoords)
			sendNodeAttributeAdded(sourceId, id, "xyz", new float[] { x, y, 0 });
	}

	protected void delNode(int x, int y) {
		delNode(nodeId(x, y));
	}

	protected void addEdge(int x1, int y1, int x2, int y2) {
		addEdge(edgeId(x1, y1, x2, y2), nodeId(x1, y1), nodeId(x2, y2));
	}

	protected void delEdge(int x1, int y1, int x2, int y2) {
		delEdge(edgeId(x1, y1, x2, y2));
	}

	protected String nodeId(int x, int y) {
		return String.format("%d_%d", x, y);
	}

	protected String edgeId(int x1, int y1, int x2, int y2) {
		return String.format("%d_%d__%d_%d", x1, y1, x2, y2);
	}

	public void begin() {
		step = 0;

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				if (cells[x * height + y])
					addNode(x, y);
			}

		boolean alive, nalive;
		int nx, ny;

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				alive = cells[x * height + y];

				for (int i = 0; i < LINK_WITH.length; i += 2) {

					if (!tore
							&& (x + LINK_WITH[i] < 0
									|| x + LINK_WITH[i] >= width
									|| y + LINK_WITH[i + 1] < 0 || y
									+ LINK_WITH[i + 1] >= height))
						continue;

					nx = (x + LINK_WITH[i] + width) % width;
					ny = (y + LINK_WITH[i + 1] + height) % height;

					nalive = cells[nx * height + ny];

					if (alive && nalive)
						addEdge(x, y, nx, ny);
				}
			}
	}

	public boolean nextEvents() {
		int idx;

		computeNextState();
		sendStepBegins(sourceId, step++);

		boolean alive, alived, nalive, nalived;
		int nx, ny;

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				idx = x * height + y;

				//
				// Node added
				//
				if (!cells[idx] && swap[idx])
					addNode(x, y);
			}

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				alive = swap[x * height + y];
				alived = cells[x * height + y];

				for (int i = 0; i < LINK_WITH.length; i += 2) {

					if (!tore
							&& (x + LINK_WITH[i] < 0
									|| x + LINK_WITH[i] >= width
									|| y + LINK_WITH[i + 1] < 0 || y
									+ LINK_WITH[i + 1] >= height))
						continue;

					nx = (x + LINK_WITH[i] + width) % width;
					ny = (y + LINK_WITH[i + 1] + height) % height;

					nalive = swap[nx * height + ny];
					nalived = cells[nx * height + ny];

					//
					// Edge removed
					//
					if (alived && nalived
							&& ((alive && !nalive) || (!alive && nalive)))
						delEdge(x, y, nx, ny);
					//
					// Edge added
					//
					else if (((!alived && nalived) || (alived && !nalived) || (!alived && !nalived))
							&& alive && nalive)
						addEdge(x, y, nx, ny);
				}
			}

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				idx = x * height + y;

				//
				// Node removed
				//
				if (cells[idx] && !swap[idx])
					delNode(x, y);
			}

		//
		// Swap
		//
		boolean[] tmp = cells;
		cells = swap;
		swap = tmp;

		return true;
	}

	public void setTore(boolean on) {
		tore = on;
	}

	public boolean isTore() {
		return tore;
	}

	public void setPushCoords(boolean on) {
		pushCoords = on;
	}

	public boolean isCoordsPushed() {
		return pushCoords;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
}
