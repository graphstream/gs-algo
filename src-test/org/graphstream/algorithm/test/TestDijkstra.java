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
 * @since 2011-11-07
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class TestDijkstra {
	
	public static Graph toyGraph() {
//	        B---9--E
//	       /|      |
//	      / |      |
//	     /  |      |
//	    14  2      6
//	   /    |      |
//	  /     |      |
//	 A---9--C--11--F
//	  \     |     /
//	   \    |    /
//	    7  10   15
//	     \  |  /
//	      \ | /
//	       \|/
//	        D      G
	    Graph g = new SingleGraph("toy");
		g.addNode("A").setAttribute("xy", 0, 1);
		g.addNode("B").setAttribute("xy", 1, 2);
		g.addNode("C").setAttribute("xy", 1, 1);
		g.addNode("D").setAttribute("xy", 1, 0);
		g.addNode("E").setAttribute("xy", 2, 2);
		g.addNode("F").setAttribute("xy", 2, 1);
		g.addNode("G").setAttribute("xy", 2, 0);
		g.addEdge("AB", "A", "B").setAttribute("length", 14);
		g.addEdge("AC", "A", "C").setAttribute("length", 9);
		g.addEdge("AD", "A", "D").setAttribute("length", 7);
		g.addEdge("BC", "B", "C").setAttribute("length", 2);
		g.addEdge("CD", "C", "D").setAttribute("length", 10);
		g.addEdge("BE", "B", "E").setAttribute("length", 9);
		g.addEdge("CF", "C", "F").setAttribute("length", 11);
		g.addEdge("DF", "D", "F").setAttribute("length", 15);
		g.addEdge("EF", "E", "F").setAttribute("length", 6);
		
		g.nodes().forEach(n -> n.setAttribute("label", n.getId()));
		g.edges().forEach(e -> e.setAttribute("label", "" + (int) e.getNumber("length")));
		
		return g;
	}
	
	@Test
	public void toyTest() {
		Graph g = toyGraph();
		
		// Test the weighted case
		Dijkstra d = new Dijkstra(Dijkstra.Element.EDGE, "result", "length");
		d.init(g);
		Node source = g.getNode("A");
		d.setSource(source);
		
		// check the source node
		assertEquals(d.getSource(), source);
		
		d.compute();
		
		// check parent access methods
		assertNull(d.getParent(source));
		assertNull(d.getEdgeFromParent(source));
		assertEquals(source, d.getParent(g.getNode("D")));
		assertEquals(g.getEdge("AD"), d.getEdgeFromParent(g.getNode("D")));
		assertNull(d.getParent(g.getNode("G")));
		assertNull(d.getEdgeFromParent(g.getNode("G")));
		
		
		// check path lengths
		assertEquals(0, d.getPathLength(g.getNode("A")), 0);
		assertEquals(11, d.getPathLength(g.getNode("B")), 0);
		assertEquals(9, d.getPathLength(g.getNode("C")), 0);
		assertEquals(7, d.getPathLength(g.getNode("D")), 0);
		assertEquals(20, d.getPathLength(g.getNode("E")), 0);
		assertEquals(20, d.getPathLength(g.getNode("F")), 0);
		assertEquals(Double.POSITIVE_INFINITY, d.getPathLength(g.getNode("G")), 0);
		
		// check tree length
		assertEquals(38, d.getTreeLength(), 0);
		
		// check nodes on path A->E
		String[] nodesAE = {"E", "B", "C", "A"};
		int i = 0;
		for (Node n : d.getPathNodes(g.getNode("E"))) {
			assertEquals(nodesAE[i], n.getId());
			i++;
		}
		assertEquals(4, i);
		
		// check edges on path A->F
		String[] edgesAF = {"CF", "AC"};
		i = 0;
		for (Edge e : d.getPathEdges(g.getNode("F"))) {
			assertEquals(edgesAF[i], e.getId());
			i++;
		}
		assertEquals(2, i);
		
		// check if path A->E is constructed correctly
		List<Node> ln = d.getPath(g.getNode("E")).getNodePath();
		assertEquals(4, ln.size());
		for (i = 0; i < 4; i++)
			assertEquals(nodesAE[3 - i], ln.get(i).getId());
		
		// There is no path A->G
		assertFalse(d.getPathNodesStream(g.getNode("G")).iterator().hasNext());
		assertFalse(d.getPathEdgesStream(g.getNode("G")).iterator().hasNext());
		
		d.clear();
		assertFalse(source.hasAttribute("result"));
		
		
		// Test unweighted case with nodes
		d = new Dijkstra(Dijkstra.Element.NODE, "result", null);
		d.init(g);
		d.setSource(source);
		d.compute();
		
		// check path lengths
		assertEquals(1, d.getPathLength(g.getNode("A")), 0);
		assertEquals(2, d.getPathLength(g.getNode("B")), 0);
		assertEquals(2, d.getPathLength(g.getNode("C")), 0);
		assertEquals(2, d.getPathLength(g.getNode("D")), 0);
		assertEquals(3, d.getPathLength(g.getNode("E")), 0);
		assertEquals(3, d.getPathLength(g.getNode("F")), 0);
		assertEquals(Double.POSITIVE_INFINITY, d.getPathLength(g.getNode("G")), 0);
		
		// check tree length
		assertEquals(6, d.getTreeLength(), 0);
		
		// check all shortest paths
		// there are two pats A->F
		List<String> lp = new ArrayList<String>();
		for (Path p : d.getAllPaths(g.getNode("F")))
			lp.add(p.toString());
		assertEquals(2, lp.size());
		assertTrue(lp.contains("[A, D, F]"));
		assertTrue(lp.contains("[A, C, F]"));
		
		// now add edge BF and recompute
		g.addEdge("BF", "B", "F");
		d.compute();
		lp.clear();
		for (Path p : d.getAllPaths(g.getNode("F")))
			lp.add(p.toString());
		assertEquals(3, lp.size());
		assertTrue(lp.contains("[A, D, F]"));
		assertTrue(lp.contains("[A, C, F]"));
		assertTrue(lp.contains("[A, B, F]"));
		
		// and don't forget the special case G
		assertFalse(d.getAllPathsStream(g.getNode("G")).iterator().hasNext());
	}
}
