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
