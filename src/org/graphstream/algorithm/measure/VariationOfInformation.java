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
 * @since 2010-10-04
 * 
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

public class VariationOfInformation extends NormalizedMutualInformation {

	public VariationOfInformation(String marker) {
		super(marker);
	}

	public VariationOfInformation(String marker, String referenceMarker) {
		super(marker, referenceMarker);
	}

	@Override
	/**
	 * B.Karrer, E.Levina and M.E.J.Newman, 
	 * RobustnessofCommunity Structure in Networks, 
	 * Physical Review E (Statistical, Nonlinear, and Soft Matter Physics), 
	 * vol. 77, no. 4, 2008.
	 */
	public void compute() {
		// Get the updated confusion matrix
		int[][] N = confusionMatrix();

		// Get the arrays of the rows and columns sums
		int[] N_A = new int[referenceCommunities.size()];
		int[] N_B = new int[communities.size()];
		for (int i = 0; i < N_A.length; i++) {
			int ttl = 0;
			for (int j = 0; j < N_B.length; j++)
				ttl += N[i][j];
			N_A[i] = ttl;
		}
		for (int j = 0; j < N_B.length; j++) {
			int ttl = 0;
			for (int i = 0; i < N_A.length; i++)
				ttl += N[i][j];
			N_A[j] = ttl;
		}

		// Get the total nodes number
		float n = graph.getNodeCount();

		/*
		 * Let's go and compute the NMI
		 */
		float voi = 0;

		for (int i = 0; i < N_A.length; i++)
			for (int j = 0; j < N_B.length; j++)
				voi += N[i][j]
						* (Math.log((float) N[i][j] / (float) N_B[j]) + Math
								.log((float) N[i][j] / (float) N_A[i]));
		M = (-1 / n) * voi;

	}

}
