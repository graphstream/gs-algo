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
 * A measure to add 1D entries (y). x is auto-incremented by one at each new
 * value.
 */
public class ChartSeries1DMeasure extends ChartSeriesMeasure {
	/**
	 * Data containing values.
	 */
	protected DescriptiveStatistics data;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            name of this measure
	 */
	public ChartSeries1DMeasure(String name) {
		super(name);

		this.data = new DescriptiveStatistics();
		this.data.setWindowSize(DEFAULT_WINDOW_SIZE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.measure.ChartMeasure#setWindowSize(int)
	 */
	public void setWindowSize(int size) {
		super.setWindowSize(size);
		data.setWindowSize(size);
	}

	/**
	 * Add a new value to the series.
	 * 
	 * @param v
	 *            the new value
	 */
	public void addValue(double v) {
		data.addValue(v);
		series.add(data.getN() - 1, v);
	}

	/**
	 * Get the count of values that have been added to this series.
	 * 
	 * @return count of values
	 */
	public long getCount() {
		return data.getN();
	}

	/**
	 * Get the mean of the series.
	 * 
	 * @return mean of the series
	 */
	public double getMean() {
		return data.getMean();
	}

	/**
	 * Get the max of the series.
	 * 
	 * @return max of the series
	 */
	public double getMax() {
		return data.getMax();
	}

	/**
	 * Get the min of the series.
	 * 
	 * @return min of the series
	 */
	public double getMin() {
		return data.getMin();
	}

	/**
	 * Get the variance of the series.
	 * 
	 * @return variance of the series
	 */
	public double getVariance() {
		return data.getVariance();
	}
}
