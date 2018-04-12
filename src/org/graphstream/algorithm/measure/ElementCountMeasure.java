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
 */
package org.graphstream.algorithm.measure;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.stream.Sink;
import org.graphstream.stream.SinkAdapter;

public abstract class ElementCountMeasure extends ChartSeries2DMeasure
		implements DynamicAlgorithm {
	/**
	 * Graph being used to compute the measure or null.
	 */
	protected Graph g;
	private Sink trigger;

	protected ElementCountMeasure(String name) {
		super(name);
		trigger = new StepTrigger();
	}

	/**
	 * Get the amount of elements.
	 * 
	 * @return amount of elements
	 */
	public abstract double getElementCount();

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
		addValue(g.getStep(), getElementCount());
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

	/**
	 * Measure the count of nodes in a graph.
	 */
	public static class NodeCountMeasure extends ElementCountMeasure {
		public NodeCountMeasure() {
			super("nodes");
		}
		
		public NodeCountMeasure(String name) {
			super(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.algorithm.measure.ElementCountMeasure#getElementCount
		 * ()
		 */
		public double getElementCount() {
			return g.getNodeCount();
		}
	}

	/**
	 * Measure the count of edges in a graph.
	 */
	public static class EdgeCountMeasure extends ElementCountMeasure {
		public EdgeCountMeasure() {
			super("edges");
		}
		
		public EdgeCountMeasure(String name) {
			super(name);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.graphstream.algorithm.measure.ElementCountMeasure#getElementCount
		 * ()
		 */
		public double getElementCount() {
			return g.getEdgeCount();
		}
	}

	private class StepTrigger extends SinkAdapter {
		public void stepBegins(String sourceId, long timeId, double step) {
			compute();
		}
	}
}
