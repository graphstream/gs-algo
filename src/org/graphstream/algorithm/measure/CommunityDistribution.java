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
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import org.apache.commons.math.stat.descriptive.moment.*;

/**
 * Provides some statistical information on the size of current community
 * assignment on the specified graph as it evolves.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class CommunityDistribution extends CommunityMeasure {

	/**
	 * Biggest community currently generated.
	 */
	protected Object biggestCommunity = null;

	/**
	 * Smallest community currently generated.
	 */
	protected Object smallestCommunity = null;

	/**
	 * Average size of the currently generated communities.
	 */
	protected float avgSize = 0;

	/**
	 * Standard deviation of the size of currently generated communities.
	 */
	protected float stdevSize = 0;

	/**
	 * New size distribution measure using the specified marker as attribute
	 * name for the community assignment.
	 * 
	 * @param marker
	 *            Attribute name for the community assignment.
	 */
	public CommunityDistribution(String marker) {
		super(marker);
	}

	/**
	 * Computes and update the statistical information on size distribution.
	 * 
	 * @complexity O(C), where C is the expected number of communities.
	 */
	@Override
	public void compute() {
		if (graphChanged) {
			// Default measure is the number of communities
			M = (float) communities.size();

			// Update the smallest/biggest community
			// and creates the size distribution
			int maxSize = 0;
			int minSize = Integer.MAX_VALUE;

			double[] distribution = new double[(int) M];
			int k = 0;
			Mean mean = new Mean();
			StandardDeviation stdev = new StandardDeviation();

			for (Object c : communities.keySet()) {
				distribution[k++] = (communities.get(c)).size();

				if ((communities.get(c)).size() > maxSize) {
					biggestCommunity = c;
					maxSize = (communities.get(c)).size();
				}
				if ((communities.get(c)).size() < minSize) {
					smallestCommunity = c;
					minSize = (communities.get(c)).size();
				}
			}

			// Compute the statistical moments
			avgSize = (float) mean.evaluate(distribution);
			stdevSize = (float) stdev.evaluate(distribution);

			graphChanged = false;
		}
	}

	/**
	 * Get the number of communities
	 * 
	 * @return an int representing the current number of communities
	 */
	public int number() {
		return (int) M;
	}

	/**
	 * Get the biggest generated community
	 * 
	 * @return the biggest community
	 */
	public Object biggestCommunity() {
		return biggestCommunity;
	}

	/**
	 * Get the smallest generated community
	 * 
	 * @return the smallest community
	 */
	public Object smallestCommunity() {
		return smallestCommunity;
	}

	/**
	 * Get the maximum community size
	 * 
	 * @return an int reflecting the size of the biggest community
	 */
	public int maxCommunitySize() {
		if (communities.get(biggestCommunity) == null)
			return 0;
		else
			return (communities.get(biggestCommunity)).size();
	}

	/**
	 * Get the minimum community size
	 * 
	 * @return an int reflecting the size of the smallest community
	 */
	public int minCommunitySize() {
		if (communities.get(smallestCommunity) == null)
			return 0;
		else
			return (communities.get(smallestCommunity)).size();
	}

	/**
	 * Compute the average community size
	 * 
	 * @return Average community size
	 */
	public float average() {
		return avgSize;
	}

	/**
	 * Compute the standard deviation of the community size
	 * 
	 * @return Standard deviation of the community size
	 */
	public float stdev() {
		return stdevSize;
	}

	/**
	 * Updates the distribution information and returns a string for an easy
	 * display of the results.
	 * 
	 * The string has the following format: [number of communities] [average
	 * size] [stdev size] [min size] ([smallest community]) [max size] ([biggest
	 * community])
	 * 
	 * @return a String containing all computed distribution information.
	 */
	@Override
	public String toString() {
		compute();
		return (int) M + " " + avgSize + " " + stdevSize + " "
				+ minCommunitySize() + " (" + smallestCommunity + ") "
				+ maxCommunitySize() + " (" + biggestCommunity + ")";
	}

}
