/*
 * Copyright 2006 - 2015
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
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
package org.graphstream.algorithm;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;

public class Spectrum implements Algorithm {

	public static enum EigenValuesAlgorithm {
		POWER_ITERATION, INVERSE_ITERATION
	}

	protected EigenValuesAlgorithm mode;
	protected Graph graph;
	protected EigenDecomposition decomposition;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		if (graph == null)
			throw new NullPointerException();

		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		if (graph == null)
			throw new NotInitializedException(this);

		int m = graph.getNodeCount();
		RealMatrix a = new Array2DRowRealMatrix(m, m);
		Edge e;

		for (int idx1 = 0; idx1 < m; idx1++)
			for (int idx2 = 0; idx2 < m; idx2++) {
				e = graph.getNode(idx1).getEdgeToward(idx2);
				a.setEntry(idx1, idx2, e != null ? 1 : 0);
			}

		decomposition = new EigenDecomposition(a, 0);
	}

	public int getEigenvaluesCount() {
		double[] values = decomposition.getRealEigenvalues();
		return values == null ? 0 : values.length;
	}

	public double getEigenvalue(int i) {
		return decomposition.getRealEigenvalue(i);
	}

	public double[] getEigenvalues() {
		return decomposition.getRealEigenvalues();
	}
	
	public double[] getEigenvector(int i) {
		return decomposition.getEigenvector(i).toArray();
	}

	public double getLargestEigenvalue() {
		double[] values = decomposition.getRealEigenvalues();
		double max = Double.MIN_VALUE;

		if (values != null)
			for (int i = 0; i < values.length; i++)
				max = Math.max(max, values[i]);

		return max;
	}

	public static void main(String... args) {
		Graph g = new AdjacencyListGraph("g");

		BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
		gen.addSink(g);
		gen.begin();
		for (int i = 0; i < 200; i++)
			gen.nextEvents();
		gen.end();

		Spectrum spectrum = new Spectrum();
		spectrum.init(g);
		spectrum.compute();

	}
}
