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
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.algorithm.NotInitializedException;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Graph;

/**
 * Base class for centrality measures. Subclasses should implements a
 * {@link #computeCentrality()} method where centrality values will be stored in
 * {@link #data}.
 */
public abstract class AbstractCentrality implements Algorithm {
	public static enum NormalizationMode {
		NONE, SUM_IS_1, MAX_1_MIN_0
	}

	/**
	 * Attribute name where centrality value will be stored.
	 */
	protected String centralityAttribute;

	/**
	 * Flag indicating if centrality values should be normalized between 0 and
	 * 1.
	 */
	protected NormalizationMode normalize;

	/**
	 * Array containing centrality values computed by centrality algorithms.
	 */
	protected double[] data;

	/**
	 * Graph on which centrality is computed.
	 */
	protected Graph graph;

	/**
	 * Default contructor.
	 * 
	 * @param attribute
	 *            attribute where centrality will be stored
	 * @param normalize
	 *            define the normalization mode
	 */
	protected AbstractCentrality(String attribute, NormalizationMode normalize) {
		this.centralityAttribute = attribute;
		this.normalize = normalize;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		if (graph == null)
			throw new NullPointerException();

		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		if (graph == null)
			throw new NotInitializedException(this);

		int count = graph.getNodeCount();

		if (data == null || data.length != count)
			data = new double[count];

		computeCentrality();
		copyValuesTo(centralityAttribute, normalize);
	}

	/**
	 * Copy values previously computed to a specific attribute. The
	 * {@link #compute()} method needs to have been call before calling this
	 * one.
	 * 
	 * @param attribute
	 *            destination attribute where values of centrality will be
	 *            stored
	 */
	public void copyValuesTo(String attribute) {
		copyValuesTo(attribute, NormalizationMode.NONE);
	}

	/**
	 * Copy values previously computed to a specific attribute. The
	 * {@link #compute()} method needs to have been call before calling this
	 * one.
	 * 
	 * @param attribute
	 *            destination attribute where values of centrality will be
	 *            stored
	 * @param normalize
	 *            defines the way that values have to be normalized
	 */
	public void copyValuesTo(String attribute, NormalizationMode normalize) {
		int count = graph.getNodeCount();

		switch (normalize) {
		case SUM_IS_1:
			double s = 0;

			for (int idx = 0; idx < count; idx++)
				s += data[idx];
			for (int idx = 0; idx < count; idx++)
				graph.getNode(idx).setAttribute(attribute,
						data[idx] / s);

			break;
		case MAX_1_MIN_0:
			double max = data[0];
			double min = max;

			for (int idx = 1; idx < count; idx++) {
				max = max < data[idx] ? data[idx] : max;
				min = min > data[idx] ? data[idx] : min;
			}

			for (int idx = 0; idx < count; idx++) {
				graph.getNode(idx).setAttribute(attribute,
						(data[idx] - min) / (max - min));
			}

			break;
		case NONE:
			for (int idx = 0; idx < count; idx++)
				graph.getNode(idx).setAttribute(attribute, data[idx]);

			break;
		}
	}

	/**
	 * Getter for {@link #centralityAttribute}.
	 * 
	 * @return {@link #centralityAttribute}
	 */
	public String getCentralityAttribute() {
		return centralityAttribute;
	}

	/**
	 * Setter for {@link #centralityAttribute}.
	 * 
	 * @param centralityAttribute
	 *            new value of {@link #centralityAttribute}
	 */
	@Parameter
	public void setCentralityAttribute(String centralityAttribute) {
		this.centralityAttribute = centralityAttribute;
	}

	/**
	 * Getter for {@link #normalize}.
	 * 
	 * @return {@link #normalize}
	 */
	public NormalizationMode getNormalizationMode() {
		return normalize;
	}

	/**
	 * Setter for {@link #normalize}.
	 * 
	 * @param normalize
	 *            new value of {@link #normalize}
	 */
	public void setNormalizationMode(NormalizationMode normalize) {
		this.normalize = normalize;
	}

	/**
	 * Define the computation of centrality values. These values are stored in
	 * {@link #data} using node index.
	 */
	protected abstract void computeCentrality();
	
	@Result
	public String defaultMessage() {
		return "Result stored in \""+centralityAttribute+"\" attribute";
	}
}
