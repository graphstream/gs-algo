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
 * @since 2009-02-19
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import static org.graphstream.algorithm.Toolkit.modularity;
import static org.graphstream.algorithm.Toolkit.modularityMatrix;

import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;

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
	 * Enables weighted extension of the modularity using the given weightMarker
	 * for edge weights.
	 * 
	 * @param weightMarker
	 *            name of the attribute marking the weight of edges.
	 */
	@Parameter
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
	@Override
	public void compute() {
		if (graphChanged) {
			double[][] E = modularityMatrix(graph, communities, weightMarker);
			M = modularity(E);
			graphChanged = false;
		}
	}
	
	@Result
	public String defaultMessage() {
		return communities+"";
	}
}