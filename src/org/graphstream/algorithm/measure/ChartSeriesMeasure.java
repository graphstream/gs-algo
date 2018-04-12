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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Base for series measure.
 */
public abstract class ChartSeriesMeasure extends ChartMeasure {

	public static final int DEFAULT_WINDOW_SIZE = 100;
	
	protected XYSeries series;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            name of this measure
	 */
	public ChartSeriesMeasure(String name) {
		super(name);
		series = new XYSeries(name);
		series.setMaximumItemCount(DEFAULT_WINDOW_SIZE);
	}

	/**
	 * Create a {@link org.jfree.data.xy.XYSeries} object that can be used to
	 * create plot.
	 * 
	 * @return a XYSeries
	 */
	public XYSeries getXYSeries() {
		return series;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.measure.ChartMeasure#setWindowSize(int)
	 */
	public void setWindowSize(int size) {
		series.setMaximumItemCount(size);
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

		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.ChartMeasure#getChart(org.graphstream
	 * .algorithm.measure.ChartMeasure.PlotParameters)
	 */
	public JFreeChart createChart(PlotParameters params) throws PlotException {
		JFreeChart chart;
		XYSeriesCollection dataset = new XYSeriesCollection();

		dataset.addSeries(getXYSeries());

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
}
