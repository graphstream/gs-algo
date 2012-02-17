/*
 * Copyright 2006 - 2012
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.algorithm.measure;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class VertexConnectivityMeasure {
	public static boolean isKConnected(Graph g, int k) {
		LinkedList<Integer> toVisit = new LinkedList<Integer>();
		HashSet<Integer> visited = new HashSet<Integer>();
		HashSet<Integer> removed = new HashSet<Integer>();
		KIndexesArray karray = new KIndexesArray(k, g.getNodeCount());

		if (k >= g.getNodeCount())
			return false;

		do {
			toVisit.clear();
			visited.clear();
			removed.clear();

			for (int j = 0; j < k; j++)
				removed.add(karray.get(j));

			for (int j = 0; toVisit.size() == 0; j++)
				if (!removed.contains(j))
					toVisit.add(j);

			while (toVisit.size() > 0) {
				Node n = g.getNode(toVisit.poll());
				Iterator<Node> it = n.getNeighborNodeIterator();
				Integer index;

				visited.add(n.getIndex());

				while (it.hasNext()) {
					Node o = it.next();
					index = o.getIndex();

					if (!visited.contains(index) && !toVisit.contains(index)
							&& !removed.contains(index))
						toVisit.add(index);
				}
			}

			if (visited.size() != g.getNodeCount())
				return false;
		} while (karray.next());

		return true;
	}

	private static class KIndexesArray {
		final int[] data;
		final int k, n;

		public KIndexesArray(int k, int n) {
			this.k = k;
			this.n = n;

			this.data = new int[k];

			for (int i = 0; i < k; i++)
				this.data[i] = i;
		}

		public boolean next() {
			int i = k - 1;

			while (i >= 0 && data[i] >= n - (k - 1 - i))
				i--;

			if (i >= 0) {
				data[i]++;

				for (int j = i + 1; j < k; j++)
					data[j] = data[j - 1] + 1;

				return true;
			}

			return false;
		}

		public int get(int i) {
			return data[i];
		}
	}
}
