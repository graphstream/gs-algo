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
 * @since 2012-02-21
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import org.graphstream.algorithm.AlgorithmComputationTrigger;
import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.algorithm.AlgorithmComputationTrigger.Mode;
import org.graphstream.graph.Graph;
import org.graphstream.stream.Sink;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeriesCollection;

public class ChartConnectivityMeasure extends ChartMeasure implements
		DynamicAlgorithm {

	public static class ChartVertexConnectivityMeasure extends
			ChartSeries2DMeasure implements DynamicAlgorithm {
		Graph g;
		Sink trigger;

		public ChartVertexConnectivityMeasure() {
			super("vertex-connectivity");
			trigger = new AlgorithmComputationTrigger(Mode.BY_STEP, this);
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
			addValue(g.getStep(), ConnectivityMeasure.getVertexConnectivity(g));
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
	}

	public static class ChartEdgeConnectivityMeasure extends
			ChartSeries2DMeasure implements DynamicAlgorithm {
		Graph g;
		Sink trigger;

		public ChartEdgeConnectivityMeasure() {
			super("edge-connectivity");
			trigger = new AlgorithmComputationTrigger(Mode.BY_STEP, this);
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
			addValue(g.getStep(), ConnectivityMeasure.getEdgeConnectivity(g));
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
	}

	protected ChartVertexConnectivityMeasure vertexConnectivity;
	protected ChartEdgeConnectivityMeasure edgeConnectivity;

	public ChartConnectivityMeasure() {
		this(new ChartVertexConnectivityMeasure(),
				new ChartEdgeConnectivityMeasure());
	}

	public ChartConnectivityMeasure(
			ChartVertexConnectivityMeasure vertexConnectivity,
			ChartEdgeConnectivityMeasure edgeConnectivity) {
		super("connectivity");

		this.vertexConnectivity = vertexConnectivity;
		this.edgeConnectivity = edgeConnectivity;
	}

	public ChartVertexConnectivityMeasure getVertexConnectivityMeasure() {
		return vertexConnectivity;
	}

	public ChartEdgeConnectivityMeasure getEdgeConnectivityMeasure() {
		return edgeConnectivity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
	 */
	public void terminate() {
		vertexConnectivity.terminate();
		edgeConnectivity.terminate();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		vertexConnectivity.init(graph);
		edgeConnectivity.init(graph);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.ChartMeasure#createChart(org.graphstream
	 * .algorithm.measure.ChartMeasure.PlotParameters)
	 */
	public JFreeChart createChart(PlotParameters params) throws PlotException {
		JFreeChart chart;
		XYSeriesCollection dataset = new XYSeriesCollection();

		dataset.addSeries(vertexConnectivity.getXYSeries());
		dataset.addSeries(edgeConnectivity.getXYSeries());

		switch (params.type) {
		case LINE:
			chart = ChartFactory.createXYLineChart(params.title,
					params.xAxisLabel, params.yAxisLabel, dataset,
					params.orientation, params.showLegend, false, false);
			break;
		case BAR:
			chart = ChartFactory.createXYBarChart(params.title,
					params.xAxisLabel, false, params.yAxisLabel, dataset,
					params.orientation, params.showLegend, false, false);
			break;
		case SCATTER:
			chart = ChartFactory.createScatterPlot(params.title,
					params.xAxisLabel, params.yAxisLabel, dataset,
					params.orientation, params.showLegend, false, false);
			break;
		default:
			throw new PlotException("unsupported plot type");
		}

		return chart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.ChartMeasure#plot(org.graphstream.algorithm
	 * .measure.ChartMeasure.PlotParameters)
	 */
	public void plot(PlotParameters params) throws PlotException {
		outputPlot(params, createChart(params));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.ChartMeasure#getDefaultPlotParameters()
	 */
	public PlotParameters getDefaultPlotParameters() {
		PlotParameters params = new PlotParameters();
		params.title = name;
		params.type = PlotType.LINE;
		params.xAxisLabel = "steps";
		params.yAxisLabel = "connectivity";

		return params;
	}
}
