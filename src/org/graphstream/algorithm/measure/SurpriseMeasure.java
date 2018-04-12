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
 * @since 2013-03-12
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import java.util.ArrayList;
import java.util.HashMap;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

/**
 * Surprise measure.
 * 
 * Description from <a
 * href="https://en.wikipedia.org/wiki/Surprise_(networks)">Wikipedia</a> :
 * Surprise (denoted S) is a measure of community structure in complex networks.
 * The name Surprise derives from the fact that its maximization finds the most
 * surprising partition into communities of the network, that is, the most
 * unlikely one. S accurately evaluates, in a global manner, the quality of a
 * partition using a cumulative hypergeometric distribution.
 * 
 * @reference Rodrigo Aldecoa, Ignacio Marin,
 *            "Deciphering Network Community Structure by Surprise", 2011, PLoS
 *            ONE 6(9)
 * 
 */
public class SurpriseMeasure implements Algorithm {

	/**
	 * Default attribute key where the result of the algorithm, a double value,
	 * is stored.
	 */
	public static final String ATTRIBUTE = "measure.surprise";

	//
	// Used to group nodes with no meta index under a fake null index.
	//
	private static final Object NULL = new Object();

	/**
	 * Attribute of nodes containing meta index. Default is "meta.index".
	 */
	protected String communityAttributeKey;

	/**
	 * Attribute that will contain the result.
	 */
	protected String surpriseAttributeKey;

	/**
	 * Graph used in the computation.
	 */
	protected Graph graph;

	/**
	 * Default constructor.
	 */
	public SurpriseMeasure() {
		this("meta.index");
	}

	/**
	 * Constructor allowing to set the node attribute key containing index of
	 * organizations.
	 * 
	 * @param communityAttributeKey
	 *            key attribute of organizations
	 */
	public SurpriseMeasure(String communityAttributeKey) {
		this(communityAttributeKey, ATTRIBUTE);
	}

	/**
	 * Same as {@link #SurpriseMeasure(String)} but allowing to set the graph
	 * attribute that will contain the result of the computation.
	 * 
	 * @param communityAttributeKey community attribute key
	 * @param surpriseAttributeKey surprise attribute key
	 */
	public SurpriseMeasure(String communityAttributeKey,
			String surpriseAttributeKey) {
		this.communityAttributeKey = communityAttributeKey;
		this.surpriseAttributeKey = surpriseAttributeKey;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		HashMap<Object, Integer> communities = new HashMap<Object, Integer>();
		ArrayList<Integer> communitiesCount = new ArrayList<Integer>();

		for (int i = 0; i < graph.getNodeCount(); i++) {
			Object community = graph.getNode(i).getAttribute(
					communityAttributeKey);

			if (community == null)
				community = NULL;

			if (!communities.containsKey(community)) {
				communities.put(community, communities.size());
				communitiesCount.add(0);
			}

			int idx = communities.get(community);
			communitiesCount.set(idx, communitiesCount.get(idx) + 1);
		}

		if (communities.containsKey(NULL))
			System.err.printf("[WARNING] Some nodes do not have community.\n");

		double F = graph.getNodeCount() * (graph.getNodeCount() - 1) / 2;
		double p = 0;
		double M = 0;
		double n = graph.getEdgeCount();
		double W;
		double S = 0;

		for (int i = 0; i < graph.getEdgeCount(); i++) {
			Edge e = graph.getEdge(i);
			Object idx0 = e.getNode0().getAttribute(communityAttributeKey);
			Object idx1 = e.getNode1().getAttribute(communityAttributeKey);

			if (idx0.equals(idx1))
				p++;
		}

		for (int i = 0; i < communitiesCount.size(); i++) {
			int k = communitiesCount.get(i);
			M += k * (k - 1) / 2;
		}

		W = Math.min(M, n);
		S = cumulativeHypergeometricDistribution(p, W, F, n, M);
		S = -Math.log(S);

		graph.setAttribute(surpriseAttributeKey, S);
	}
	
	@Result
	public String defaultResult() {
		return "Surprise = "+getSurprise();
	}
	
	@Parameter
	public void setCommunityAttributeKey(String communityAttributeKey) {
		this.communityAttributeKey = communityAttributeKey;
	}
	
	/**
	 * Get the last computed surprise value contained in the graph.
	 * 
	 * @return surprise value
	 */
	public double getSurprise() {
		if (graph == null)
			throw new NullPointerException(
					"Graph is null. Is this algorithm initialized ?");

		if (!graph.hasNumber(surpriseAttributeKey))
			throw new RuntimeException(
					"No surprise value found. Have you called the compute() method ?");

		return graph.getNumber(surpriseAttributeKey);
	}

	/**
	 * Helper to compute the binomial coefficient.
	 * 
	 * @param n number of elements in set
	 * @param r  number of elements
	 * @return the binomial coefficient
	 */
	public static double binomialCoefficient(double n, double r) {
		if (r > n)
			return 0;

		if (r == 0 || n == r)
			return 1;

		double C = n;
		double t = 1;

		for (int i = 1; i < r; i++) {
			C *= (n - i);
			t *= i + 1;
		}

		return C / t;
	}

	/**
	 * Helper to compute the hypergeometric distribution. See <a href=
	 * "http://stattrek.com/probability-distributions/hypergeometric.aspx">this
	 * page</a> for more information about this function.
	 * 
	 * @param x x
	 * @param N N
	 * @param n n
	 * @param k k
	 * @return hypergeometric Distribution
	 */
	public static double hypergeometricDistribution(double x, double N,
			double n, double k) {
		return binomialCoefficient(k, x) * binomialCoefficient(N - k, n - x)
				/ binomialCoefficient(N, n);
	}

	/**
	 * Helper to compute the cumulative hypergeometric distribution. See <a
	 * href=
	 * "http://stattrek.com/probability-distributions/hypergeometric.aspx">this
	 * page</a> for more information about this function.
	 * 
	 * @param xStart xStart
	 * @param xEnd xEnd
	 * @param N N
	 * @param n n
	 * @param k k
	 * @return Cumulative Hypergeometric Distribution
	 */
	public static double cumulativeHypergeometricDistribution(double xStart,
			double xEnd, double N, double n, double k) {
		double chd = 0;

		for (double x = xStart; x <= xEnd; x += 1)
			chd += hypergeometricDistribution(x, N, n, k);

		return chd;
	}
}
