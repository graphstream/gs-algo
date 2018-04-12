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
 */
package org.graphstream.algorithm.measure;

public class DegreeCentrality extends AbstractCentrality {
	public static enum Mode {
		INOUT, IN, OUT
	}

	public static final String DEFAULT_ATTRIBUTE_KEY = "degree";

	/**
	 * Defines which degree should be used. It only get a sense with directed
	 * graph.
	 */
	protected Mode mode;

	public DegreeCentrality() {
		this(DEFAULT_ATTRIBUTE_KEY, NormalizationMode.NONE);
	}

	/**
	 * Constructor allowing configuration of centrality attribute and
	 * normalization flag. Mode will be {@link Mode#INOUT}.
	 * 
	 * @param attribute
	 *            name of the attribute where centrality values will be stored
	 * @param normalize
	 *            defines the normalization mode
	 */
	public DegreeCentrality(String attribute, NormalizationMode normalize) {
		this(attribute, normalize, Mode.INOUT);
	}

	/**
	 * Same as {@link #DegreeCentrality(String, NormalizationMode)} but allows to
	 * configure the mode.
	 * 
	 * @param attribute
	 *            name of the attribute where centrality values will be stored
	 * @param normalize
	 *            defines the normalization mode
	 * @param mode
	 *            set which degree should be used (in degree, out degree or
	 *            both)
	 */
	public DegreeCentrality(String attribute, NormalizationMode normalize,
			Mode mode) {
		super(attribute, normalize);
		this.mode = mode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.measure.AbstractCentrality#computeCentrality()
	 */
	protected void computeCentrality() {
		int count = graph.getNodeCount();

		switch (mode) {
		case INOUT:
			for (int idx = 0; idx < count; idx++)
				data[idx] = graph.getNode(idx).getDegree();
			break;
		case IN:
			for (int idx = 0; idx < count; idx++)
				data[idx] = graph.getNode(idx).getInDegree();
			break;
		case OUT:
			for (int idx = 0; idx < count; idx++)
				data[idx] = graph.getNode(idx).getOutDegree();
			break;
		}
	}

}
