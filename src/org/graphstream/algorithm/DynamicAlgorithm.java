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
package org.graphstream.algorithm;

/**
 * Defines algorithms able to handle dynamics of a graph. This is an extension
 * of the {@link org.graphstream.algorithm.Algorithm} so dynamic algorithms are
 * composed of an initialization step followed by several computation steps. A
 * last step, specific to dynamic algorithms, marks the termination of the
 * algorithm and has to be used to close the computation.
 * 
 * The following example computes the amount of apparitions of each node and the
 * average value of apparitions for nodes :
 * 
 * <pre>
 * public class ApparitionAlgorithm extends SinkAdapter implements
 * 		DynamicAlgorithm {
 * 
 * 	Graph theGraph;
 * 	HashMap&lt;String, Integer&gt; apparitions;
 * 	double avg;
 * 
 * 	public void init(Graph graph) {
 * 		theGraph = graph;
 * 		avg = 0;
 * 		graph.addSink(this);
 * 	}
 * 
 * 	public void compute() {
 * 		avg = 0;
 * 
 * 		for (int a : apparitions.values())
 * 			avg += a;
 * 
 * 		avg /= apparitions.size();
 * 	}
 * 
 * 	public void terminate() {
 * 		graph.removeSink(this);
 * 	}
 * 
 * 	public double getAverageApparitions() {
 * 		return avg;
 * 	}
 * 
 * 	public int getApparitions(String nodeId) {
 * 		return apparitions.get(nodeId);
 * 	}
 * 
 * 	public void nodeAdded(String sourceId, long timeId, String nodeId) {
 * 		int a = 0;
 * 
 * 		if (apparitions.containsKey(nodeId))
 * 			a = apparitions.get(nodeId);
 * 
 * 		apparitions.put(nodeId, a + 1);
 * 	}
 * 
 * 	public void stepBegins(String sourceId, long timeId, double step) {
 * 		compute();
 * 	}
 * }
 * </pre>
 * 
 * Note that in the example, the #terminate() method is used to remove the link
 * between graph and the algorithm. The computation here is done at every step
 * but can be done anywhere else, according to the algorithm requirements.
 */
public interface DynamicAlgorithm extends Algorithm {
	/**
	 * Terminate the dynamic algorithm.
	 * 
	 * @see #init(org.graphstream.graph.Graph)
	 */
	void terminate();
}