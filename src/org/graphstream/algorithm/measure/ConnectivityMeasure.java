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
 * @since 2012-02-10
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.algorithm.flow.EdmondsKarpAlgorithm;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.Sink;
import org.graphstream.stream.SinkAdapter;

/**
 * Get the vertex-connectivity of a graph.
 * 
 * A graph is said to be k-vertex-connected (or k-connected) if the graph
 * remains connected when you delete fewer than k vertices from the graph (from
 * <a
 * href="https://en.wikipedia.org/wiki/K-vertex-connected_graph">Wikipedia</a>).
 * 
 */
public class ConnectivityMeasure {
	/**
	 * Get the vertex-connectivity k of a graph such that there is a k-tuple of
	 * nodes whose removal disconnects the graph.
	 * 
	 * @param g
	 *            the graph
	 * @return vertex connectivity
	 */
	public static int getVertexConnectivity(Graph g) {
		int previous;
		int current = Integer.MIN_VALUE;
		boolean isPreviousConnected;
		boolean isCurrentConnected;

		/*
		 * We start with the max degree.
		 */
		current = (g.nodes()
			.max((x, y) -> Integer.compare(x.getDegree(), y.getDegree())) 
			.get())
			.getDegree();
		
		isCurrentConnected = isKVertexConnected(g, current);

		do {
			isPreviousConnected = isCurrentConnected;
			previous = current;

			if (isPreviousConnected)
				current = previous + 1;
			else
				current = previous - 1;

			isCurrentConnected = isKVertexConnected(g, current);
		} while (!((isPreviousConnected && !isCurrentConnected && previous == current - 1) || (!isPreviousConnected
				&& isCurrentConnected && previous == current + 1)));

		if (!isPreviousConnected)
			return current;

		return previous;
	}

	/**
	 * Get the edge-connectivity k of a graph such that there is a k-tuple of
	 * edges whose removal disconnects the graph. This uses the Ford-Fulkerson
	 * algorithm to compute maximum flows in the graph.
	 * 
	 * A simple algorithm would, for every pair (u,v), determine the maximum
	 * flow from u to v with the capacity of all edges in G set to 1 for both
	 * directions. A graph is k-edge-connected if and only if the maximum flow
	 * from u to v is at least k for any pair (u,v), so k is the least u-v-flow
	 * among all (u,v). Source <a
	 * href="https://en.wikipedia.org/wiki/K-edge-connected_graph"
	 * >Wikipedia</a>.
	 * 
	 * @param g
	 *            the graph
	 * @return edge connectivity
	 */
	public static int getEdgeConnectivity(Graph g) {
		int k = Integer.MAX_VALUE;
		EdmondsKarpAlgorithm flow = new EdmondsKarpAlgorithm();

		if (g.getNodeCount() < 2)
			return 0;

		for (int u = 0; u < g.getNodeCount() - 1; u++) {
			for (int v = u + 1; v < g.getNodeCount(); v++) {
				flow.init(g, g.getNode(u).getId(), g.getNode(v).getId());
				flow.setAllCapacities(1.0);
				flow.compute();

				k = Math.min(k, (int) flow.getMaximumFlow());
			}
		}

		return k;
	}

	/**
	 * Check if a graph is k-vertex-connected, ie. there is no (k-1)-node-tuple
	 * such that the removal of these nodes leads to disconnect the graph.
	 * 
	 * @param g
	 *            the graph
	 * @param k
	 *            connectivity being checked
	 * @return true if g is k-vertex-connected
	 */
	public static boolean isKVertexConnected(Graph g, int k) {
		Node[] tuple = getKDisconnectingNodeTuple(g, k - 1);
		return tuple == null;
	}

	/**
	 * Check if a graph is k-edge-connected, ie. there is no (k-1)-edge-tuple
	 * such that the removal of these edges leads to disconnect the graph.
	 * 
	 * @param g
	 *            the graph
	 * @param k
	 *            connectivity being checked
	 * @return true if g is k-edge-connected
	 */
	public static boolean isKEdgeConnected(Graph g, int k) {
		Edge[] tuple = getKDisconnectingEdgeTuple(g, k - 1);
		return tuple == null;
	}

	/**
	 * Get a k-tuple of nodes whose removal causes the disconnection of the
	 * graph.
	 * 
	 * @param g
	 *            the graph
	 * @param k
	 *            max size of the required tuple
	 * @return a k-tuple of nodes or null if graph is (k+1)-vertex-connected
	 */
	public static Node[] getKDisconnectingNodeTuple(Graph g, int k) {
		LinkedList<Integer> toVisit = new LinkedList<Integer>();
		boolean[] visited = new boolean[g.getNodeCount()];
		HashSet<Integer> removed = new HashSet<Integer>();
		KIndexesArray karray = new KIndexesArray(k, g.getNodeCount());

		if (k >= g.getNodeCount())
			return g.nodes().toArray(Node[]::new);
		
		do {
			toVisit.clear();
			removed.clear();
			Arrays.fill(visited, false);

			for (int j = 0; j < k; j++)
				removed.add(karray.get(j));

			for (int j = 0; toVisit.size() == 0; j++)
				if (!removed.contains(j))
					toVisit.add(j);

			while (toVisit.size() > 0) {
				Node n = g.getNode(toVisit.poll());
				visited[n.getIndex()] = true;
				
				n.neighborNodes().forEach(o -> {
					Integer index = o.getIndex();

					if (!visited[index] && !toVisit.contains(index)
							&& !removed.contains(index))
						toVisit.add(index);
				});
				
			}

			for (int i = 0; i < visited.length; i++)
				if (!visited[i] && !removed.contains(i)) {
					Node[] tuple = new Node[k];

					for (int j = 0; j < k; j++)
						tuple[j] = g.getNode(karray.get(j));

					return tuple;
				}
		} while (karray.next());

		return null;
	}

	/**
	 * Get a k-tuple of edges whose removal causes the disconnection of the
	 * graph.
	 * 
	 * @param g
	 *            the graph
	 * @param k
	 *            max size of the required tuple
	 * @return a k-tuple of edges or null if graph is (k+1)-edge-connected
	 */
	public static Edge[] getKDisconnectingEdgeTuple(Graph g, int k) {
		LinkedList<Integer> toVisit = new LinkedList<Integer>();
		boolean[] visited = new boolean[g.getNodeCount()];
		HashSet<Integer> removed = new HashSet<Integer>();
		KIndexesArray karray = new KIndexesArray(k, g.getNodeCount());

		int minDegree = Integer.MAX_VALUE;
		Node nodeWithMinDegree = null;

		if (k >= g.getEdgeCount())
			return g.edges().toArray(Edge[]::new);
			
		for (int i = 0; i < g.getNodeCount(); i++) {
			Node n = g.getNode(i);

			if (n.getDegree() < minDegree) {
				minDegree = n.getDegree();
				nodeWithMinDegree = n;
			}
		}

		if (k > minDegree) {
			Edge[] tuple = new Edge[minDegree];

			for (int i = 0; i < minDegree; i++)
				tuple[i] = nodeWithMinDegree.getEdge(i);

			return tuple;
		}

		do {
			toVisit.clear();
			removed.clear();
			Arrays.fill(visited, false);

			for (int j = 0; j < k; j++)
				removed.add(karray.get(j));

			toVisit.add(0);

			while (toVisit.size() > 0) {
				Node n = g.getNode(toVisit.poll());
				
				visited[n.getIndex()] = true;
				
				n.edges().forEach(e -> {
					Node o = e.getOpposite(n);
					Integer index = o.getIndex();

					if (!visited[index] && !toVisit.contains(index)
							&& !removed.contains(e.getIndex()))
						toVisit.add(index);
				});
			}

			for (int i = 0; i < visited.length; i++)
				if (!visited[i]) {
					Edge[] tuple = new Edge[k];

					for (int j = 0; j < k; j++)
						tuple[j] = g.getEdge(karray.get(j));

					return tuple;
				}
		} while (karray.next());

		return null;
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

	public static class VertexConnectivityMeasure implements DynamicAlgorithm {
		protected Graph g;
		protected int vertexConnectivity;
		protected Sink trigger;

		public VertexConnectivityMeasure() {
			g = null;
			vertexConnectivity = -1;
			trigger = new StepTrigger(this);
		}

		/**
		 * Get the last vertex-connectivity of the registered graph compute in
		 * the last call of {@link #compute()}.
		 * 
		 * @return vertex connectivity
		 */
		public int getVertexConnectivity() {
			return vertexConnectivity;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.algorithm.Algorithm#compute()
		 */
		public void compute() {
			vertexConnectivity = ConnectivityMeasure.getVertexConnectivity(g);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
		 */
		public void init(Graph graph) {
			g = graph;
			g.addSink(trigger);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
		 */
		public void terminate() {
			g.removeSink(trigger);
		}
	}

	public static class EdgeConnectivityMeasure implements DynamicAlgorithm {
		protected Graph g;
		protected int edgeConnectivity;
		protected Sink trigger;

		public EdgeConnectivityMeasure() {
			g = null;
			edgeConnectivity = -1;
			trigger = new StepTrigger(this);
		}

		/**
		 * Get the last vertex-connectivity of the registered graph compute in
		 * the last call of {@link #compute()}.
		 * 
		 * @return vertex connectivity
		 */
		public int getEdgeConnectivity() {
			return edgeConnectivity;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.algorithm.Algorithm#compute()
		 */
		public void compute() {
			edgeConnectivity = ConnectivityMeasure.getEdgeConnectivity(g);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
		 */
		public void init(Graph graph) {
			g = graph;
			g.addSink(trigger);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
		 */
		public void terminate() {
			g.removeSink(trigger);
		}
	}

	private static class StepTrigger extends SinkAdapter {
		Algorithm algo;

		StepTrigger(Algorithm algo) {
			this.algo = algo;
		}

		public void stepBegins(String sourceId, long timeId, double step) {
			algo.compute();
		}
	}
}
