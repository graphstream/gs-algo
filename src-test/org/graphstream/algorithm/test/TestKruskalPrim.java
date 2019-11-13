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
 * @since 2012-07-25
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.atomic.DoubleAccumulator;

import org.graphstream.algorithm.Kruskal;
import org.graphstream.algorithm.Prim;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class TestKruskalPrim {
//       B-----8-----C-----7-----D
//      /|          / \          |\
//     / |         /   \         | \
//    4  |        2     \        |  9
//   /   |       /       \       |   \
//  /    |      /         \      |    \
// A    11     I           4    14     E
//  \    |    / \           \    |    /
//   \   |   /   \           \   |   /
//    8  |  7     6           \  |  10
//     \ | /       \           \ | /
//      \|/         \           \|/
//       H-----1-----G-----2-----F
	
	public static Graph toyGraph() {
		String[] eIds = {"AB", "AH", "BH", "BC", "HI", "HG", "IC", "IG", "CD", "CF", "GF", "DF", "DE", "FE"};
		int[] weights = {4, 8, 11, 8, 7, 1, 2, 6, 7, 4, 2, 14, 9, 10};
		Graph g = new SingleGraph("test", false, true);
		for (int i = 0; i < eIds.length; i++) {
			String eId = eIds[i];
			Edge e = g.addEdge(eId, eId.substring(0, 1), eId.substring(1, 2));
			e.setAttribute("weight", weights[i]);
		}
		return g;
	}
	
	@Test
	public void toyTest() {
		Graph g = toyGraph();
		Kruskal k = new Kruskal("weight", "kruskal");
		k.init(g);
		k.compute();
		helper(k, g, 37.0, 8);
		
		Prim p = new Prim("weight", "prim");
		p.init(g);
		p.compute();
		helper(p, g, 37.0, 8);
		
		// remove the lightest edge
		g.removeEdge("HG");
		k.compute();
		helper(k, g, 43.0, 8);
		p.compute();
		helper(p, g, 43.0, 8);
		
		// now cut the graph in 2 CC
		g.removeEdge("BC");
		g.removeEdge("HI");
		k.compute();
		helper(k, g, 36.0, 7);
		p.compute();
		helper(p, g, 36.0, 7);
	}
	
	public void helper(Kruskal k, Graph g, double expectedWeight, int expectedCount) {
		assertEquals(expectedWeight, k.getTreeWeight(), 0);
		DoubleAccumulator edgeCount = new DoubleAccumulator((x,y) -> x + y, 0) ;
	
		k.getTreeEdges().forEach(e -> {
			Boolean b = (Boolean) e.getAttribute(k.getFlagAttribute()); 
			assertTrue(b);
			edgeCount.accumulate(1);
		});
		assertEquals(expectedCount, (int)edgeCount.get());
		
		DoubleAccumulator edgeCount2 = new DoubleAccumulator((x,y) -> x + y, 0) ;
		DoubleAccumulator treeWeight = new DoubleAccumulator((x,y) -> x + y, 0) ;
		
		g.edges()
			.filter(e -> (Boolean) e.getAttribute(k.getFlagAttribute()))
			.forEach(e -> {
				edgeCount2.accumulate(1);
				treeWeight.accumulate(e.getNumber("weight"));
			});

		assertEquals(expectedWeight, treeWeight.get(), 0);
		assertEquals(expectedCount, (int)edgeCount2.get());
	}
}
