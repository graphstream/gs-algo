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
 * Build a Balaban 11-cage graph.
 * 
 * <dl>
 * <dt>Nodes</dt>
 * <dd>112</dd>
 * <dt>LCF</dt>
 * <dd>[44, 26, -47, -15, 35, -39, 11, -27, 38, -37, 43, 14, 28, 51, -29, -16,
 * 41, -11, -26, 15, 22, -51, -35, 36, 52, -14, -33, -26, -46, 52, 26, 16, 43,
 * 33, -15, 17, -53, 23, -42, -35, -28, 30, -22, 45, -44, 16, -38, -16, 50, -55,
 * 20, 28, -17, -43, 47, 34, -26, -41, 11, -36, -23, -16, 41, 17, -51, 26, -33,
 * 47, 17, -11, -20, -30, 21, 29, 36, -43, -52, 10, 39, -28, -17, -52, 51, 26,
 * 37, -17, 10, -10, -45, -34, 17, -26, 27, -21, 46, 53, -10, 29, -50, 35, 15,
 * -47, -29, -41, 26, 33, 55, -17, 42, -26, -36, 16]</dd>
 * </dl>
 * 
 * @reference A. T. Balaban, Trivalent Graphs of Girth Nine and Eleven and
 *            Relationships Among the Cages, Rev. Roumaine Math., 18, 1033-1043,
 *            1973
 * 
 */
public class Balaban11CageGraphGenerator extends LCFGenerator {
	/**
	 * LCF notation of a Balaban 11-cage graph.
	 */
	public static final LCF BALABAN_11CAGE_GRAPH_LCF = new LCF(1, 44, 26, -47,
			-15, 35, -39, 11, -27, 38, -37, 43, 14, 28, 51, -29, -16, 41, -11,
			-26, 15, 22, -51, -35, 36, 52, -14, -33, -26, -46, 52, 26, 16, 43,
			33, -15, 17, -53, 23, -42, -35, -28, 30, -22, 45, -44, 16, -38,
			-16, 50, -55, 20, 28, -17, -43, 47, 34, -26, -41, 11, -36, -23,
			-16, 41, 17, -51, 26, -33, 47, 17, -11, -20, -30, 21, 29, 36, -43,
			-52, 10, 39, -28, -17, -52, 51, 26, 37, -17, 10, -10, -45, -34, 17,
			-26, 27, -21, 46, 53, -10, 29, -50, 35, 15, -47, -29, -41, 26, 33,
			55, -17, 42, -26, -36, 16);

	public Balaban11CageGraphGenerator() {
		super(BALABAN_11CAGE_GRAPH_LCF, 112, false);
	}
}
