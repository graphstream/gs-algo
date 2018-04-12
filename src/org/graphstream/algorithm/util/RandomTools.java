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
 * @since 2012-04-19
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * This class provides several static methods for generating random numbers and
 * sets
 */
public class RandomTools {

	/**
	 * Returns a pseudorandom number drawn from exponential distribution with
	 * mean 1.
	 * 
	 * Uses the von Neumann's exponential generator.
	 * 
	 * @param rnd
	 *            source of randomness
	 * @return a pseudorandom number drawn from exponential distribution with
	 *         mean 1.
	 * @complexity O(1) average complexity. The expected number of uniformly
	 *             distributed numbers used is e^2 / (e - 1)
	 * 
	 */
	public static double exponential(Random rnd) {
		double y, w, u;
		int z, k;
		z = -1;
		do {
			w = y = rnd.nextDouble();
			k = 1;
			while (true) {
				u = rnd.nextDouble();
				if (u > w)
					break;
				w = u;
				k++;
			}
			z++;
		} while ((k & 1) == 0);
		return z + y;
	}

	/**
	 * Returns a pseudorandom number drawn from binomial distribution B(n, p).
	 * 
	 * Uses a simple waiting time method based on exponential distribution.
	 * 
	 * @param n
	 *            number of tries
	 * @param p
	 *            success probability
	 * @param rnd
	 *            source of randomness
	 * @return a pseudorandom number drawn from binomial distribution
	 * @complexity Average complexity O(np)
	 */
	public static int binomial(int n, double p, Random rnd) {
		double q = -Math.log(1 - p);
		int x = 0;
		double s = 0;
		do {
			s += exponential(rnd) / (n - x);
			x++;
		} while (s <= q);
		return x - 1;
	}

	/**
	 * Generates a pseudorandom subset of size k of the set {0, 1,...,n - 1}.
	 * Each element has the same chance to be chosen.
	 * 
	 * Uses Floyd's method of subset generation with only k iterations. Note
	 * that the quality of this generator is limited by Java's random generator.
	 * Java stores the internal state in 48 bits, so in the best case we can
	 * only generate 2^48 different subsets.
	 * 
	 * @param n
	 *            the size of the initial set
	 * @param k
	 *            the size of the generated set
	 * @param subset
	 *            if not null, this set is cleared and the result is stored
	 *            here. This avoids creations of sets at each call of this
	 *            method
	 * @param rnd
	 *            source of randomness
	 * @return a pseudorandom subset of size k of the set {0, 1,...,n}
	 * @complexity Depends on the set implementation. If add and lookup
	 *             operations take constant time, the complexity is O(k)
	 */
	public static Set<Integer> randomKsubset(int n, int k, Set<Integer> subset,
			Random rnd) {
		if (subset == null)
			subset = new HashSet<Integer>(4 * k / 3 + 1);
		else
			subset.clear();
		for (int i = n - k; i < n; i++) {
			int j = rnd.nextInt(i + 1);
			subset.add(subset.contains(j) ? i : j);
		}
		return subset;
	}

	/**
	 * Generates a pseudorandom subset of the set {0, 1,...,n - 1}. Each element
	 * is chosen with probability p.
	 * 
	 * @param n
	 *            the size of the initial set
	 * @param p
	 *            the probability to choose each element
	 * @param subset
	 *            if not null, this set is cleared and the result is stored
	 *            here. This avoids creations of sets at each call of this
	 *            method
	 * @param rnd
	 *            source of randomness
	 * @return a pseudorandom subset of the set {0, 1,...,n}.
	 * @complexity Depends on the set implementation. If add and lookup
	 *             operations take constant time, the complexity is O(np)
	 */
	public static Set<Integer> randomPsubset(int n, double p,
			Set<Integer> subset, Random rnd) {
		return randomKsubset(n, binomial(n, p, rnd), subset, rnd);
	}
}
