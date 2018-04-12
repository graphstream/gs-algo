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
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import java.util.Arrays;

import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

public class EigenvectorCentrality extends AbstractCentrality {
	public static final String DEFAULT_ATTRIBUTE_KEY = "eigenvector";
	
	public static final int DEFAULT_MAX_ITER = 100;

	protected int maxIter;
	protected String weightAttribute;

	public EigenvectorCentrality() {
		this(DEFAULT_ATTRIBUTE_KEY, NormalizationMode.NONE);
	}
	
	public EigenvectorCentrality(String attribute, NormalizationMode normalize) {
		this(attribute, normalize, DEFAULT_MAX_ITER, "weight");
	}

	public EigenvectorCentrality(String attribute, NormalizationMode normalize,
			int maxIter, String weightAttribute) {
		super(attribute, normalize);
		
		this.maxIter = maxIter;
		this.weightAttribute = weightAttribute;
	}

	@Override
	protected void computeCentrality() {
		int n = graph.getNodeCount();
		double[] x1 = new double[n];
		double[] x2 = new double[n];
		double[] t;
		double f, s;
		int iter = maxIter;
		Node node;
		Edge edge;

		Arrays.fill(x2, 1.0 / n);

		while (iter-- > 0) {
			//
			// Swap x1 and x2
			//
			t = x1;
			x1 = x2;
			x2 = t;

			Arrays.fill(x2, 0);
			s = 0;

			for (int idx = 0; idx < n; idx++) {
				node = graph.getNode(idx);

				for (int i = 0; i < node.getDegree(); i++) {
					edge = node.getEdge(i);
					f = 1;

					if (edge.hasNumber(weightAttribute))
						f = edge.getNumber(weightAttribute);

					x2[idx] += x1[edge.getOpposite(node).getIndex()] * f;
				}

				s += x2[idx] * x2[idx];
			}

			s = s == 0 ? 1.0 : 1.0 / Math.sqrt(s);
			for (int idx = 0; idx < n; idx++)
				x2[idx] *= s;
		}

		data = x2;
	}
	
	@Result
	public String defaultMessage() {
		return "Result stored in \""+this.weightAttribute+"\" attribute";
	}
}
