/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Project copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 *
 * This file is copyright 2010
 *  Guillaume-Jean Herbiet
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

	@Override
	/**
	 * Computes and update the statistical information on size distribution.
	 * 
	 * @complexity O(C), where C is the expected number of communities.
	 */
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
