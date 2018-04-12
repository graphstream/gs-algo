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
package org.graphstream.algorithm.measure.test;

import java.io.IOException;

import org.graphstream.algorithm.measure.SurpriseMeasure;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.junit.Assert;
import org.junit.Test;

public class TestSurpriseMeasure {
	protected static final double DELTA = 0.0001;

	@Test
	public void testBinomialCoefficient() {
		double[][] data = { { 13, 0, 1 }, { 39, 5, 575757 },
				{ 52, 5, 2598960 }, { 13, 1, 13 }, { 39, 4, 82251 },
				{ 13, 2, 78 }, { 39, 3, 9139 } };

		for (int i = 0; i < data.length; i++)
			Assert.assertEquals(
					data[i][2],
					SurpriseMeasure.binomialCoefficient(data[i][0], data[i][1]),
					DELTA);
	}

	@Test
	public void testHypergeometricDistribution() {
		double[][] data = { { 2, 52, 5, 26, 0.32513 },
				{ 0, 52, 5, 13, 0.2215 }, { 1, 52, 5, 13, 0.4114 },
				{ 2, 52, 5, 13, 0.2743 } };

		for (int i = 0; i < data.length; i++)
			Assert.assertEquals(data[i][4], SurpriseMeasure
					.hypergeometricDistribution(data[i][0], data[i][1],
							data[i][2], data[i][3]), DELTA);
	}

	@Test
	public void testCumulativeHypergeometricDistribution() {
		// h(x < 2; 52, 5, 13) = 0.9072
		double[][] data = { { 0, 2, 52, 5, 13, 0.9072 } };

		for (int i = 0; i < data.length; i++)
			Assert.assertEquals(data[i][5], SurpriseMeasure
					.cumulativeHypergeometricDistribution(data[i][0],
							data[i][1], data[i][2], data[i][3], data[i][4]),
					DELTA);
	}

	@Test
	public void check() throws IOException {
		Graph g = new AdjacencyListGraph("g");
		FileSourceDGS in = new FileSourceDGS();

		in.addSink(g);
		in.begin(TestSurpriseMeasure.class
				.getResourceAsStream("data/TestSurpriseMeasure.dgs"));

		in.nextStep();

		SurpriseMeasure surprise = new SurpriseMeasure("meta.index");
		surprise.init(g);
		surprise.compute();

		Assert.assertEquals(g.getNumber("expectedvalue"),
				g.getNumber(SurpriseMeasure.ATTRIBUTE), DELTA);
	}
}
