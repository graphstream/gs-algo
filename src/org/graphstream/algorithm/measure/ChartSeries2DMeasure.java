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

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/**
 * A measure allowing to add 2D entries (x,y).
 */
public class ChartSeries2DMeasure extends ChartSeriesMeasure {
	/**
	 * Data containing x values.
	 */
	protected DescriptiveStatistics xData;
	/**
	 * Data containing y values.
	 */
	protected DescriptiveStatistics yData;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            names of the measure
	 */
	public ChartSeries2DMeasure(String name) {
		super(name);

		this.xData = new DescriptiveStatistics();
		this.yData = new DescriptiveStatistics();
		this.xData.setWindowSize(DEFAULT_WINDOW_SIZE);
		this.yData.setWindowSize(DEFAULT_WINDOW_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.measure.ChartMeasure#setWindowSize(int)
	 */
	public void setWindowSize(int size) {
		super.setWindowSize(size);
		xData.setWindowSize(size);
		yData.setWindowSize(size);
	}

	/**
	 * Add a new point to the series.
	 * 
	 * @param x
	 *            x value of the new point
	 * @param y
	 *            y value osf the new point
	 */
	public void addValue(double x, double y) {
		xData.addValue(x);
		yData.addValue(y);
		series.add(x, y);
	}

	/**
	 * Get the count of points added to this series.
	 * 
	 * @return count of points.s
	 */
	public long getCount() {
		return xData.getN();
	}

	/**
	 * Get the mean of x values.
	 * 
	 * @return x values means
	 */
	public double getXMean() {
		return xData.getMean();
	}

	/**
	 * Get the max of x values.
	 * 
	 * @return x values max
	 */
	public double getXMax() {
		return xData.getMax();
	}

	/**
	 * Get the min of x values.
	 * 
	 * @return x values min
	 */
	public double getXMin() {
		return xData.getMin();
	}

	/**
	 * Get the variance of x values.
	 * 
	 * @return x values variance
	 */
	public double getXVariance() {
		return xData.getVariance();
	}

	/**
	 * Get the mean of y values.
	 * 
	 * @return y values means
	 */
	public double getYMean() {
		return yData.getMean();
	}

	/**
	 * Get the max of y values.
	 * 
	 * @return y values max
	 */
	public double getYMax() {
		return yData.getMax();
	}

	/**
	 * Get the min of y values.
	 * 
	 * @return y values min
	 */
	public double getYMin() {
		return yData.getMin();
	}

	/**
	 * Get the variance of y values.
	 * 
	 * @return y values variance
	 */
	public double getYVariance() {
		return yData.getVariance();
	}

}
