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
package org.graphstream.algorithm.flow;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class EdmondsKarpAlgorithm extends FordFulkersonAlgorithm {
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.flow.FordFulkersonAlgorithm#findPath(java.util
	 * .LinkedList, org.graphstream.graph.Node, org.graphstream.graph.Node)
	 */
	protected double findPath(LinkedList<Node> path, Node source, Node target) {
		LinkedList<Node> Q = new LinkedList<Node>();
		Node u;
		int[] P = new int[source.getGraph().getNodeCount()];
		double[] M = new double[source.getGraph().getNodeCount()];
		double r;

		Arrays.fill(P, -1);
		P[source.getIndex()] = -2;
		M[source.getIndex()] = Double.MAX_VALUE;

		Q.add(source);

		while (Q.size() > 0) {
			u = Q.pop();

			for (int i = 0; i < u.getDegree(); i++) {
				Edge e = u.getEdge(i);
				Node v = e.getOpposite(u);

				r = getCapacity(u, v) - getFlow(u, v);

				if (r > 0 && P[v.getIndex()] == -1) {
					P[v.getIndex()] = u.getIndex();
					M[v.getIndex()] = Math.min(M[u.getIndex()], r);

					if (v != target)
						Q.push(v);
					else {
						u = target;

						do {
							path.addFirst(u);
							u = flowGraph.getNode(P[u.getIndex()]);
						} while (u != source);

						path.addFirst(u);
						return M[target.getIndex()];
					}
				}
			}
		}

		return 0;
	}
	
	@Result
	public String defaultResult() {
		LinkedList<Node> path = flowGraph.nodes().collect(Collectors.toCollection(LinkedList::new)) ;
		
		Node s = flowGraph.getNode(sourceId);
		Node t = flowGraph.getNode(sinkId);
		
		return findPath(path, s, t)+"";
	}

}
