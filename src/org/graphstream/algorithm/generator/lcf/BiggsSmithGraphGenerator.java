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
 * Build a Biggs-Smith graph.
 * 
 * <dl>
 * <dt>Nodes</dt>
 * <dd>102</dd>
 * <dt>LCF</dt>
 * <dd>[16, 24, -38, 17, 34, 48, -19, 41, -35, 47, -20, 34, -36, 21, 14, 48,
 * -16, -36, -43, 28, -17, 21, 29, -43, 46, -24, 28, -38, -14, -50, -45, 21, 8,
 * 27, -21, 20, -37, 39, -34, -44, -8, 38, -21, 25, 15, -34, 18, -28, -41, 36,
 * 8, -29, -21, -48, -28, -20, -47, 14, -8, -15, -27, 38, 24, -48, -18, 25, 38,
 * 31, -25, 24, -46, -14, 28, 11, 21, 35, -39, 43, 36, -38, 14, 50, 43, 36, -11,
 * -36, -24, 45, 8, 19, -25, 38, 20, -24, -14, -21, -8, 44, -31, -38, -28, 37]</dd>
 * </dl>
 * 
 * @reference On trivalent graphs, NL Biggs, DH Smith - Bulletin of the London
 *            Mathematical Society, 3 (1971) 155-158
 * 
 */
public class BiggsSmithGraphGenerator extends LCFGenerator {
	/**
	 * LCF notation of a Biggs-Smith graph.
	 */
	public static final LCF BIGGS_SMITH_GRAPH_LCF = new LCF(1, 16, 24, -38, 17,
			34, 48, -19, 41, -35, 47, -20, 34, -36, 21, 14, 48, -16, -36, -43,
			28, -17, 21, 29, -43, 46, -24, 28, -38, -14, -50, -45, 21, 8, 27,
			-21, 20, -37, 39, -34, -44, -8, 38, -21, 25, 15, -34, 18, -28, -41,
			36, 8, -29, -21, -48, -28, -20, -47, 14, -8, -15, -27, 38, 24, -48,
			-18, 25, 38, 31, -25, 24, -46, -14, 28, 11, 21, 35, -39, 43, 36,
			-38, 14, 50, 43, 36, -11, -36, -24, 45, 8, 19, -25, 38, 20, -24,
			-14, -21, -8, 44, -31, -38, -28, 37);

	public BiggsSmithGraphGenerator() {
		super(BIGGS_SMITH_GRAPH_LCF, 102, false);
	}
}
