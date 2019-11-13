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

import java.util.concurrent.atomic.DoubleAccumulator;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.stream.Sink;
import org.graphstream.stream.SinkAdapter;

public class DegreeMeasure extends ChartMinMaxAverageSeriesMeasure implements
		DynamicAlgorithm {
	/**
	 * Graph being used to compute the measure or null.
	 */
	protected Graph g;
	private Sink trigger;

	public DegreeMeasure() {
		super("Degree");
		trigger = new StepTrigger();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
	 */
	public void terminate() {
		g.removeSink(trigger);
		g = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		DoubleAccumulator min, max, avg;
		
		min = new DoubleAccumulator((x,y) -> y, Double.MAX_VALUE);
		max = new DoubleAccumulator((x,y) -> y, Double.MIN_VALUE);
		avg = new DoubleAccumulator((x,y) -> x + y, 0);
		
		g.nodes().forEach(n -> {
			min.accumulate(Math.min(min.get(), n.getDegree()));
			max.accumulate(Math.max(max.get(), n.getDegree()));
			avg.accumulate(n.getDegree());
		});

		int avgFinal = (int) (avg.get() / g.getNodeCount());
		addValue(g.getStep(), min.get(), avgFinal, max.get());
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

	private class StepTrigger extends SinkAdapter {
		public void stepBegins(String sourceId, long timeId, double step) {
			compute();
		}
	}
}
