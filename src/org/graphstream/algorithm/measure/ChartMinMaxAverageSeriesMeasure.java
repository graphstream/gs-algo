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
 * @since 2012-02-15
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * A measure to plot special entries composed of a minimum, average and maximum
 * value.
 */
public class ChartMinMaxAverageSeriesMeasure extends ChartSeries2DMeasure {
	/**
	 * Title of the plot. It is used as title in default plot parameters.
	 */
	protected String title;
	/**
	 * Series modeling min and max.
	 */
	protected XYSeries min, max;
	/**
	 * Flag used to define if min and max are plotted to a different axis than
	 * average. If true, a new axis is created on the right/bottom of the plot.
	 */
	protected boolean separateMinMaxAxis;

	public ChartMinMaxAverageSeriesMeasure(String name) {
		super("avg");

		title = name;

		min = new XYSeries("min");
		max = new XYSeries("max");
		min.setMaximumItemCount(DEFAULT_WINDOW_SIZE);
		max.setMaximumItemCount(DEFAULT_WINDOW_SIZE);

		separateMinMaxAxis = true;
	}

	/**
	 * Flag used to define if min and max are plotted to a different axis than
	 * average.
	 * 
	 * @param on
	 *            true if a new axis should be created on the right/bottom for
	 *            min/max series
	 */
	public void setSeparateMinMaxAxis(boolean on) {
		this.separateMinMaxAxis = on;
	}

	/**
	 * Flag used to define if min and max are plotted to a different axis than
	 * average.
	 * 
	 * @return true if a new axis is created on the right/bottom for min/max
	 *         series
	 */
	public boolean isSeparateMinMaxAxis() {
		return separateMinMaxAxis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.measure.ChartMeasure#setWindowSize(int)
	 */
	public void setWindowSize(int size) {
		super.setWindowSize(size);

		min.setMaximumItemCount(size);
		max.setMaximumItemCount(size);
	}

	/**
	 * Add a new entry to series.
	 * 
	 * @param x value
	 * @param min min value
	 * @param avg average value
	 * @param max max value
	 */
	public void addValue(double x, double min, double avg, double max) {
		addValue(x, avg);

		this.min.add(x, min);
		this.max.add(x, max);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.ChartSeriesMeasure#getChart(org.graphstream
	 * .algorithm.measure.ChartMeasure.PlotParameters)
	 */
	public JFreeChart createChart(PlotParameters params) throws PlotException {
		XYSeriesCollection minMax = new XYSeriesCollection();
		XYSeriesCollection avgCol = new XYSeriesCollection();
		XYPlot plot;
		XYBarRenderer r = new XYBarRenderer();
		r.setBarPainter(new StandardXYBarPainter());
		r.setMargin(0.35);

		minMax.addSeries(min);
		avgCol.addSeries(series);
		minMax.addSeries(max);

		JFreeChart chart = ChartFactory.createXYLineChart(params.title,
				params.xAxisLabel, params.yAxisLabel, avgCol,
				params.orientation, params.showLegend, true, false);

		plot = ((XYPlot) chart.getPlot());
		plot.setDataset(1, minMax);
		plot.setRenderer(1, r);

		if (separateMinMaxAxis) {
			NumberAxis minMaxAxis = new NumberAxis("min/max");

			plot.setRangeAxis(1, minMaxAxis);
			plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
			plot.mapDatasetToRangeAxis(1, 1);
		}

		return chart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.ChartSeriesMeasure#getDefaultPlotParameters
	 * ()
	 */
	public PlotParameters getDefaultPlotParameters() {
		PlotParameters params = new PlotParameters();
		params.title = title;
		params.xAxisLabel = "x";
		params.yAxisLabel = "average";

		return params;
	}
}
