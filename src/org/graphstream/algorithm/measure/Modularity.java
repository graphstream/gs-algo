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
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm.measure;

import static org.graphstream.algorithm.Toolkit.modularity;
import static org.graphstream.algorithm.Toolkit.modularityMatrix;

/**
 * Computes and updates the modularity of a given graph as it evolves.
 * 
 * @reference M. E. Newman and M. Girvan, “Finding and Evaluating Community
 *            Structure in Networks,” <i>Physical Review E (Statistical,
 *            Nonlinear, and Soft Matter Physics)</i>, vol. 69, no. 2, pp. 026
 *            113+, Feb 2004.
 * 
 * @author Yoann Pigné
 * @author Guillaume-Jean Herbiet
 */
public class Modularity extends CommunityMeasure {

	/**
	 * Possible weighted extension for the modularity computation
	 */
	protected String weightMarker = null;

	/**
	 * New modularity algorithm using the default marker for communities and no
	 * weight on edges.
	 */
	public Modularity() {
		super("community");
	}

	/**
	 * New modularity algorithm with a given marker for communities and no
	 * weight on edges.
	 * 
	 * @param marker
	 *            name of the attribute marking the communities.
	 */
	public Modularity(String marker) {
		super(marker);
	}

	/**
	 * New weighted modularity algorithm with a given marker for communities and
	 * the given weightMarker for edge weights.
	 * 
	 * @param marker
	 *            name of the attribute marking the communities.
	 * @param weightMarker
	 *            name of the attribute marking the weight of edges.
	 */
	public Modularity(String marker, String weightMarker) {
		super(marker);
		this.weightMarker = weightMarker;
	}

	/**
	 * Enables weighted extension of the modularity using the
	 * given weightMarker for edge weights.
	 * 
	 * @param weightMarker
	 *            name of the attribute marking the weight of edges.
	 */
	public void setWeightMarker(String weightMarker) {
		this.weightMarker = weightMarker;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	/**
	 * @complexity O(n+m!+m!k)
	 */
	public void compute() {

		if (graphChanged) {
			float[][] E = modularityMatrix(graph, communities, weightMarker);
			M = modularity(E);
			graphChanged = false;
		}
	}
}