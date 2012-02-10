/*
 * Copyright 2006 - 2012 
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.algorithm.measure;

import java.util.Random;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

public class ChartSeriesMeasure extends ChartMeasure {

	protected DescriptiveStatistics data;

	public ChartSeriesMeasure(String name) {
		super(name);
		this.data = new DescriptiveStatistics();
	}

	public void addValue(double v) {
		data.addValue(v);
	}

	public long getCount() {
		return data.getN();
	}

	public double getMean() {
		return data.getMean();
	}

	public double getMax() {
		return data.getMax();
	}

	public double getMin() {
		return data.getMin();
	}

	public double getVariance() {
		return data.getVariance();
	}

	public void plot() throws PlotException {
		PlotParameters params = new PlotParameters();
		params.title = name;
		params.type = PlotType.LINE;

		plot(params, this);
	}

	public static void main(String... args) throws Exception {
		ChartSeriesMeasure m1 = new ChartSeriesMeasure("my first measure");
		ChartSeriesMeasure m2 = new ChartSeriesMeasure("my second measure");
		Random r = new Random();

		for (int i = 0; i < 100; i++) {
			m1.addValue(r.nextDouble() * 10 + 10);
			m2.addValue(r.nextDouble() * 10 + 10);
		}

		PlotParameters params = new PlotParameters();
		params.title = "Hello World";
		plot(params, m1, m2);
	}
}
