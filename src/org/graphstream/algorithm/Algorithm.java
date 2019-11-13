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
 */
package org.graphstream.algorithm;

import org.graphstream.graph.Graph;

/**
 * Algorithms are used to compute properties on graphs or graph elements. These
 * properties could be a measure, color, spanning tree, etc... Algorithms are
 * divided into two steps :
 * 
 * <ol>
 * <li>initialization, that initialize or reset the algorithm ;</li>
 * <li>computation, that computes a property or updates a previous result.</li>
 * </ol>
 * <p>
 * Algorithm interface aims to define algorithms that do no handle dynamics of
 * the graph, whereas algorithms implementing the
 * {@link org.graphstream.algorithm.DynamicAlgorithm} interface (an extended
 * version of Algorithm) are able to handle this dynamics.
 * </p>
 * <p>
 * This following is an example of an algorithm that computes max, min and
 * average degrees of a graph:
 * </p>
 * <pre>
 * public class DegreesAlgorithm implements Algorithm {
 * 		Graph theGraph;
 * 		int minDegree, maxDegree, avgDegree;
 * 
 * 		public void init(Graph graph) {
 * 			theGraph = graph; 
 * 		}
 * 
 * 		public void compute() {
 * 			avgDegree = 0;
 * 			minDegree = Integer.MAX_VALUE;
 * 			maxDegree = 0;
 * 
 * 			for(Node n : theGraph.getEachNode() ) {
 * 				int deg = n.getDegree();
 * 
 * 				minDegree = Math.min(minDegree, d);
 * 				maxDegree = Math.max(maxDegree, d);
 * 				avgDegree += d;
 * 			}
 * 
 * 			avgDegree /= theGraph.getNodeCount();
 * 		}
 * 
 * 		public int getMaxDegree() {
 * 			return maxDegree;
 * 		}
 * 
 * 		public int getMinDegree() {
 * 			return minDegree;
 * 		}
 * 
 * 		public int getAverageDegree() {
 * 			return avgDegree;
 * 		}
 * }
 * </pre>
 * <p>
 * Complexity of algorithms can be specify in the documentation with the help of
 * the "@complexity" tag.
 * </p>
 */
public interface Algorithm {
	/**
	 * Initialization of the algorithm. This method has to be called before the
	 * {@link #compute()} method to initialize or reset the algorithm according
	 * to the new given graph.
	 * 
	 * @param graph
	 *            The graph this algorithm is using.
	 */
	void init(Graph graph);

	/**
	 * Run the algorithm. The {@link #init(Graph)} method has to be called
	 * before computing.
	 * 
	 * @see #init(Graph)
	 */
	void compute();
}