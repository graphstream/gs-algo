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
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import java.util.LinkedList;

/**
 * Generate a Lobster tree. Lobster are trees where the distance between any
 * node and a root path is less than 2. In this generator, the max distance can
 * be customized.
 */
public class LobsterGenerator extends BaseGenerator {
	/**
	 * Max distance from any node to a node of the root path.
	 */
	protected int maxDistance = 2;
	/**
	 * Max degree of nodes.
	 */
	protected int maxDegree = 10;
	/**
	 * Delete some node in step.
	 */
	protected boolean delete = false;
	/**
	 * Average node count. Used in delete-mode to maintain an average count of
	 * nodes.
	 */
	protected int averageNodeCount = 200;
	/**
	 * Used to generate new node index.
	 */
	protected int currentIndex = 0;
	/**
	 * Node data.
	 */
	protected LinkedList<Data> nodes;

	/**
	 * Main constructor to a Lobster generator.
	 */
	public LobsterGenerator() {
		this(2, -1);
	}

	/**
	 * Constructor allowing to customize maximum distance to the root path and
	 * maximum degree of nodes.
	 * 
	 * @param maxDistance
	 *            max distance to root path
	 * @param maxDegree
	 *            max degree of nodes
	 */
	public LobsterGenerator(int maxDistance, int maxDegree) {
		this.maxDistance = maxDistance;
		this.maxDegree = maxDegree;
		this.nodes = new LinkedList<Data>();
	}

	/**
	 * Constructor allowing to customize maximum distance to the root path.
	 * 
	 * @param maxDistance
	 *            max distance to root path
	 */
	public LobsterGenerator(int maxDistance) {
		this(maxDistance, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		nodes.clear();
		add(new Data(newNodeId(), 0, true));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		Data connectTo = null;

		do {
			connectTo = nodes.get(random.nextInt(nodes.size()));
		} while (connectTo.distance >= maxDistance
				|| (maxDegree > 0 && connectTo.degree() >= maxDegree));

		Data newData = null;

		if (connectTo.path && connectTo.degree() <= 1)
			newData = new Data(newNodeId(), 0, true);
		else
			newData = new Data(newNodeId(), connectTo.distance + 1, false);

		add(newData);
		connect(connectTo, newData);

		if (delete && nodes.size() > 1) {
			double d = Math.min(nodes.size() - averageNodeCount,
					averageNodeCount / 10);

			d /= averageNodeCount / 10.0;

			if (d > 0 && random.nextFloat() < d) {
				Data delete = null;

				do {
					delete = nodes.get(random.nextInt(nodes.size()));
				} while (delete.degree() > 1);

				delNode(delete);
			}
		}

		return true;
	}

	protected void add(Data data) {
		nodes.add(data);
		addNode(data.id);
	}

	protected void connect(Data d1, Data d2) {
		d1.connected.add(d2);
		d2.connected.add(d1);

		addEdge(getEdgeId(d1, d2), d1.id, d2.id);
	}

	protected void delNode(Data d) {
		d.connected.forEach(c -> {
			delEdge(getEdgeId(d, c));
			c.connected.remove(d);
		});

		delNode(d.id);
		nodes.remove(d);
	}

	protected String newNodeId() {
		return String.format("%04d", currentIndex++);
	}

	protected String getEdgeId(Data d1, Data d2) {
		if (d1.hashCode() > d2.hashCode()) {
			Data t = d1;
			d1 = d2;
			d2 = t;
		}

		return String.format("%s--%s", d1.id, d2.id);
	}

	protected static class Data {
		String id;
		int distance;
		boolean path;
		LinkedList<Data> connected;

		Data(String id, int distance, boolean path) {
			this.id = id;
			this.distance = distance;
			this.connected = new LinkedList<Data>();
			this.path = path;
		}

		int degree() {
			return connected.size();
		}
	}
}
