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
 * Build a Wagner graph.
 * 
 * <dl>
 * <dt>Nodes</dt>
 * <dd>8</dd>
 * <ddt>LCF</dt>
 * <dd>[4]^8</dd>
 * </dl>
 * 
 * @reference Wagner, K. (1937). "Über eine Eigenschaft der ebenen Komplexe".
 *            Mathematische Annalen 114 (1): 570–590. doi:10.1007/BF01594196
 * 
 */
public class WagnerGraphGenerator extends LCFGenerator {
	/**
	 * LCF notation of a Wagner graph.
	 */
	public static final LCF WAGNER_LCF = new LCF(8, 4);

	public WagnerGraphGenerator() {
		super(WAGNER_LCF, 8, false);
	}
}
