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
 * @since 2012-02-18
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.flow;

import java.util.LinkedList;
import java.util.stream.Collectors;

import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Node;

/**
 * The Ford-Fulkerson algorithm to compute maximum flow.
 * 
 * @reference Ford, L. R.; Fulkerson, D. R. (1956).
 *            "Maximal flow through a network". Canadian Journal of Mathematics
 *            8: 399–404
 * @complexity O(Ef), where E is the number of edges in the graph and f is the
 *             maximum flow in the graph
 */
public class FordFulkersonAlgorithm extends FlowAlgorithmBase {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		Node source = flowGraph.getNode(sourceId);
		Node sink = flowGraph.getNode(sinkId);

		if (source == null)
			throw new ElementNotFoundException("node \"%s\"", sourceId);

		if (sink == null)
			throw new ElementNotFoundException("node \"%s\"", sinkId);

		checkArrays();
		loadCapacitiesFromAttribute();

		for (int i = 0; i < flowGraph.getEdgeCount(); i++) {
			Edge e = flowGraph.getEdge(i);

			setFlow(e.getNode0(), e.getNode1(), 0);
			setFlow(e.getNode1(), e.getNode0(), 0);
		}

		double minCf;
		LinkedList<Node> path = new LinkedList<Node>();

		while ((minCf = findPath(path, source, sink)) > 0) {
			for (int i = 1; i < path.size(); i++) {
				Node u = path.get(i - 1);
				Node v = path.get(i);

				setFlow(u, v, getFlow(u, v) + minCf);
				setFlow(v, u, getFlow(v, u) - minCf);
			}

			path.clear();
		}

		double flow = 0;

		for (int i = 0; i < source.getDegree(); i++)
			flow += getFlow(source, source.getEdge(i).getOpposite(source));

		maximumFlow = flow;
	}

	protected double findPath(LinkedList<Node> path, Node source, Node target) {
		path.addLast(source);
		
		if (source == target)
			return Double.MAX_VALUE;

		double minCf;

		for (int i = 0; i < source.getDegree(); i++) {
			Edge e = source.getEdge(i);
			Node o = e.getOpposite(source);

			if (getCapacity(source, o) - getFlow(source, o) > 0
					&& !path.contains(o)) {
				if ((minCf = findPath(path, o, target)) > 0)
					return Math.min(minCf,
							getCapacity(source, o) - getFlow(source, o));
			}
		}
		
		path.removeLast();
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
