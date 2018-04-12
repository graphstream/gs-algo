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

import org.graphstream.graph.Node;

/**
 * Computes and updated the current Normalized Mutual Information (NMI) measure
 * between a dynamically-performed community assignment on a graph as it evolves
 * and a fixed assignment, known as reference.
 * 
 * @reference L. Danon, A. Diaz-Guilera, J. Duch, and A. Arenas, “Comparing
 *            community structure identification,” <i>Journal of Statistical
 *            Mechanics: Theory and Experiment</i>, vol. 2005, no. 09, pp.
 *            P09008+, September 2005.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class NormalizedMutualInformation extends CommunityRelativeMeasure {

	/**
	 * New NMI measure, using the given marker for the dynamically performed
	 * assignment, and the default marker for the reference assignment.
	 * 
	 * @param marker
	 *            name of the attribute marking the computed communities.
	 */
	public NormalizedMutualInformation(String marker) {
		super(marker);
	}

	/**
	 * New NMI measure, using the given marker for the dynamically performed
	 * assignment, and the given refrenceMarker for the reference assignment.
	 * 
	 * @param marker
	 *            name of the attribute marking the computed communities.
	 * @param referenceMarker
	 *            name of the attribute marking the reference communities.
	 */
	public NormalizedMutualInformation(String marker, String referenceMarker) {
		super(marker, referenceMarker);
	}

	/**
	 * Compute the new NMI measure value.
	 * 
	 * @complexity O(2*C^2 + 6*C), where C is the expected number of communities
	 *             in the graph.
	 */
	@Override
	public void compute() {
		if (graphChanged) {

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
				N_B[j] = ttl;
			}

			// Get the total nodes number
			float n = graph.getNodeCount();

			/*
			 * Let's go and compute the NMI
			 */

			// First the numerator
			float num = 0;
			for (int i = 0; i < N_A.length; i++) {
				for (int j = 0; j < N_B.length; j++) {
					if (N[i][j] > 0) {
						num += -2.0 * N[i][j]
								* Math.log((N[i][j] * n) / (N_A[i] * N_B[j]));
					}
				}
			}

			// Then the denominator
			float denom = 0;
			for (int i = 0; i < N_A.length; i++)
				denom += N_A[i] * Math.log(N_A[i] / n);
			for (int j = 0; j < N_B.length; j++)
				denom += N_B[j] * Math.log(N_B[j] / n);

			// Update the metric value
			M = num / denom;

			// Valid unless the graph changes again
			graphChanged = false;
		}
	}

	/**
	 * Computes the confusion matrix between reference and current community
	 * assignment, i.e. the matrix N where each element N[i][j] is the number of
	 * nodes in reference community i, also in current community j.
	 * 
	 * @complexity O(C^2 + 2C), where C is the expected number of communities in
	 *             the graph.
	 * 
	 * @return the confusion matrix N of all N[i][j]
	 */
	protected int[][] confusionMatrix() {
		// Size of the confusion matrix
		int c_A = referenceCommunities.size();
		int c_B = communities.size();

		// Confusion matrix itself
		int[][] N = new int[c_A][];

		// Relation between confusion matrix indices and communities
		Object keys_A[] = new Object[c_A];
		Object keys_B[] = new Object[c_B];

		int k_A = 0;
		for (Object key : referenceCommunities.keySet())
			keys_A[k_A++] = key;

		int k_B = 0;
		for (Object key : communities.keySet())
			keys_B[k_B++] = key;

		// Initialize each row and fill each element of the confusion matrix
		for (int i = 0; i < c_A; ++i) {
			N[i] = new int[c_B];

			for (int j = 0; j < c_B; j++) {
				// Number of common nodes between communities indexed by i and j
				int commonNodes = 0;

				// Increase the number of common nodes for each node
				// of the found community indexed by j
				// also appearing in the real community indexed by i
				for (Node n : communities.get(keys_B[j])) {
					if (referenceCommunities.get(keys_A[i]).contains(n))
						commonNodes++;
				}

				// Sets the confusion matrix element
				N[i][j] = commonNodes;
			}
		}
		return N;
	}
}
