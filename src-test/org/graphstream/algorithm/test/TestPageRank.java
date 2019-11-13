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
 * @since 2012-07-12
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import org.graphstream.algorithm.PageRank;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestPageRank {
	// Example from the Wikipedia's article
	public static Graph toyGraph() {
		Graph g = new SingleGraph("test", false, true);
		String[] edgeIds = {"BC", "CB", "DA", "DB", "ED", "EB", "EF", "FB", "FE", "GB", "GE", "HB", "HE", "IB", "IE", "JE", "KE"};
		for (String id : edgeIds)
			g.addEdge(id, id.substring(0, 1), id.substring(1,2), true);
		return g;
	}
	
	@Test
	public void testRank() {
		Graph g = toyGraph();
		PageRank pr = new PageRank();
		pr.init(g);
		pr.compute();
		
		assertEquals(3.3, 100 * pr.getRank(g.getNode("A")), 1.0e-1);
		assertEquals(38.4, 100 * pr.getRank(g.getNode("B")), 1.0e-1);
		assertEquals(34.3, 100 * pr.getRank(g.getNode("C")), 1.0e-1);
		assertEquals(3.9, 100 * pr.getRank(g.getNode("D")), 1.0e-1);
		assertEquals(8.1, 100 * pr.getRank(g.getNode("E")), 1.0e-1);
		assertEquals(3.9, 100 * pr.getRank(g.getNode("F")), 1.0e-1);
		assertEquals(1.6, 100 * pr.getRank(g.getNode("G")), 1.0e-1);
		assertEquals(1.6, 100 * pr.getRank(g.getNode("H")), 1.0e-1);
		assertEquals(1.6, 100 * pr.getRank(g.getNode("I")), 1.0e-1);
		assertEquals(1.6, 100 * pr.getRank(g.getNode("J")), 1.0e-1);
		assertEquals(1.6, 100 * pr.getRank(g.getNode("K")), 1.0e-1);

	}
}
