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
 */
package org.graphstream.algorithm.generator.lcf;

import org.graphstream.algorithm.generator.LCFGenerator;

/**
 * Build a Balaban 10-cage graph.
 * 
 * <dl>
 * <dt>Nodes</dt>
 * <dd>70</dd>
 * <dt>LCF</dt>
 * <dd>[-9, -25, -19, 29, 13, 35, -13, -29, 19, 25, 9, -29, 29, 17, 33, 21,
 * 9,-13, -31, -9, 25, 17, 9, -31, 27, -9, 17, -19, -29, 27, -17, -9, -29, 33,
 * -25,25, -21, 17, -17, 29, 35, -29, 17, -17, 21, -25, 25, -33, 29, 9, 17, -27,
 * 29, 19, -17, 9, -27, 31, -9, -17, -25, 9, 31, 13, -9, -21, -33, -17, -29, 29]
 * </dd>
 * </dl>
 * 
 * @reference A. T. Balaban, A trivalent graph of girth ten, J. Combinatorial
 *            Theory, Set. B, 12:1-5, 1972
 * 
 */
public class Balaban10CageGraphGenerator extends LCFGenerator {
	/**
	 * LCF notation of a Balaban 10-cage graph.
	 */
	public static final LCF BALABAN_10CAGE_GRAPH_LCF = new LCF(1, -9, -25, -19,
			29, 13, 35, -13, -29, 19, 25, 9, -29, 29, 17, 33, 21, 9, -13, -31,
			-9, 25, 17, 9, -31, 27, -9, 17, -19, -29, 27, -17, -9, -29, 33,
			-25, 25, -21, 17, -17, 29, 35, -29, 17, -17, 21, -25, 25, -33, 29,
			9, 17, -27, 29, 19, -17, 9, -27, 31, -9, -17, -25, 9, 31, 13, -9,
			-21, -33, -17, -29, 29);

	public Balaban10CageGraphGenerator() {
		super(BALABAN_10CAGE_GRAPH_LCF, 70, false);
	}
}
