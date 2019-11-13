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
 * @author beltex <cse93251@cse.yorku.ca>
 */
package org.graphstream.algorithm.measure;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

/**
 * This is the base for high level measures. These measures allow to compute
 * statistical values and plotting.
 */
public abstract class ChartMeasure {
	/**
	 * Type of plot.s
	 */
	public static enum PlotType {
		/**
		 * Points connected with lines.
		 */
		LINE,
		/**
		 * 
		 */
		BAR,
		/**
		 * 
		 */
		PIE,
		/**
		 * Cloud of points.
		 */
		SCATTER
	}

	/**
	 * Defines the support used for rendering : on screen or in a file.
	 */
	public static enum PlotOutputType {
		SCREEN, PNG, JPEG
	}

	/**
	 * Exception that can be raised when trying to plot measures.
	 */
	public static class PlotException extends Exception {
		private static final long serialVersionUID = -1158885472939044996L;

		public PlotException(String message) {
			super(message);
		}

		public PlotException(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Parameters defining how to plot measures.
	 */
	public static class PlotParameters {
		/**
		 * Type of plot. This type is linked to the type of measure : LINE needs
		 * series measures for example.
		 */
		public PlotType type;
		/**
		 * Defines if plotting is rendered on the screen or saved on a file.
		 */
		public PlotOutputType outputType;
		/**
		 * If rendering is saved on a file, this defines the path of this file.
		 */
		public String path;
		/**
		 * Title of the plot.
		 */
		public String title;
		/**
		 * Label of the x axis.
		 */
		public String xAxisLabel;
		/**
		 * Label of the y axis.
		 */
		public String yAxisLabel;
		/**
		 * Orientation of the plot.
		 */
		public PlotOrientation orientation;
		/**
		 * True if name of measures should be displayed on the plot.
		 */
		public boolean showLegend;
		/**
		 * Dimensions of the plot.
		 */
		public int width, height;

		public PlotParameters() {
			type = PlotType.LINE;
			outputType = PlotOutputType.SCREEN;
			path = null;
			title = "plot";
			xAxisLabel = "x-axis";
			yAxisLabel = "y-axis";
			orientation = PlotOrientation.VERTICAL;
			showLegend = true;
			width = 600;
			height = 300;
		}
	}

	/**
	 * Name of this measure. This name is used when plotting the measure.
	 */
	protected String name;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            name of the new measure
	 */
	protected ChartMeasure(String name) {
		this.name = name;
	}

	/**
	 * Create a new plot with default plot parameters.
	 * 
	 * @see #getDefaultPlotParameters()
	 * @throws PlotException problem during plot
	 */
	public void plot() throws PlotException {
		plot(getDefaultPlotParameters());
	}

	/**
	 * Create a default set of parameters to plot this measure.
	 * 
	 * @return a default PlotParameters adapted to this measure.
	 */
	public abstract PlotParameters getDefaultPlotParameters();

	/**
	 * Plot this measure using a set of parameters.
	 * 
	 * @param params
	 *            parameters that should be used to plot the measure
	 * @throws PlotException
	 * 			error during plot
	 */
	public abstract void plot(PlotParameters params) throws PlotException;

	/**
	 * Create a new chart of this measure according to a set of parameters.
	 * 
	 * @param params
	 *            the set of parameters used to create the chart
	 * @return a new chart
	 * @throws PlotException
	 * 			error during plot
	 */
	public abstract JFreeChart createChart(PlotParameters params)
			throws PlotException;

	/**
	 * Utility function to call
	 * {@link #outputPlot(PlotParameters, JFreeChart...)} with
	 * {@link org.graphstream.algorithm.measure.ChartMeasure} objects.
	 * 
	 * @see #outputPlot(PlotParameters, JFreeChart...)
	 * @param params
	 *            set of parameters used to output the plot
	 * @param measures
	 *            measures to plot
	 * @throws PlotException
	 * 			error during plot
	 */
	public static void outputPlot(PlotParameters params,
			ChartMeasure... measures) throws PlotException {
		if (measures == null || measures.length == 0)
			throw new PlotException("no measure");

		JFreeChart[] charts = new JFreeChart[measures.length];

		for (int i = 0; i < measures.length; i++)
			charts[i] = measures[i].createChart(params);

		outputPlot(params, charts);
	}

	/**
	 * Output some charts according to a set of parameters. Actually, only one
	 * chart is supported. According to {@link PlotParameters#outputType}, plot
	 * is displayed on screen or saved in a file.
	 * 
	 * @param params
	 *            parameters used to plot
	 * @param charts
	 *            charts to output
	 * @throws PlotException
	 * 	 		error during plot
	 */
	public static void outputPlot(PlotParameters params, JFreeChart... charts)
			throws PlotException {
		if (charts == null || charts.length == 0)
			throw new PlotException("no chart");

		if (charts.length > 1)
			throw new PlotException("multiple charts not yet supported");

		JFreeChart chart = charts[0];

		switch (params.outputType) {
		case SCREEN:
			ChartPanel panel = new ChartPanel(chart, params.width,
					params.height, params.width, params.height,
					params.width + 50, params.height + 50, true, true, true,
					true, true, true);

			JFrame frame = new JFrame(params.title);
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setLocationRelativeTo(null);
			frame.add(panel);
			frame.pack();
			frame.setVisible(true);

			break;
		case JPEG:
			try {
				ChartUtilities.saveChartAsJPEG(new File(params.path), chart,
						params.width, params.height);
			} catch (IOException e) {
				throw new PlotException(e);
			}

			break;
		case PNG:
			try {
				ChartUtilities.saveChartAsPNG(new File(params.path), chart,
						params.width, params.height);
			} catch (IOException e) {
				throw new PlotException(e);
			}

			break;
		}

	}
}
