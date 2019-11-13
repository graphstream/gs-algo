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
 * @since 2017-10-23
 * 
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.graphstream.algorithm.BellmanFord;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Before;
import org.junit.Test;

/**
 * Small test of the BellmanFord algorithm.
 * 
 * @author Hicham Brahimi
 */
public class TestBellmanFord
{
	Graph graph;
	BellmanFord bellman ;
	
	@Before
	public void setUp() {
		
		graph = new SingleGraph("Test BellmanFord", false, true);
		
		Edge e = graph.addEdge( "AB", "B", "A", true);
		e.setAttribute("weight", 5.0);
		e = graph.addEdge( "BC", "B", "C", true);
		e.setAttribute("weight", 1.0);
		e = graph.addEdge( "CA", "C", "A", true);
		e.setAttribute("weight", 2.0);
		e = graph.addEdge( "BF", "F", "B", true);
		e.setAttribute("weight", 1.0);
		e = graph.addEdge( "CE", "E", "C", true);
		e.setAttribute("weight", 8.0);
		e = graph.addEdge( "EF", "E", "F", true);
		e.setAttribute("weight", 1.0);
		
		bellman = new BellmanFord("weight");
	}
	
	@Test
	public void testShortedPath() {		
		bellman.setSource("E");
		bellman.init(graph);
		bellman.compute();

		Node a = graph.getNode("A") ;
		Path p = bellman.getShortestPath(a);
		System.out.println(p);
		
		List<Edge> edges = p.getEdgePath();
		Iterator<? extends Edge> i = edges.iterator();
		
		Edge e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("CA"));
		e = i.next();
		System.out.println(e);
		assertTrue(e != null);
		assertTrue(e.getId().equals("BC"));
		e = i.next();
		System.out.println(e);
		assertTrue(e != null);
		assertTrue(e.getId().equals("BF"));
		e = i.next();
		System.out.println(e);
		assertTrue(e != null);
		assertTrue(e.getId().equals("EF"));
		System.out.println(e);
		assertTrue(!i.hasNext());
		
	}
	
	@Test
	public void testShortedPathValue() {
		bellman.setSource("E");
		bellman.init(graph);
		bellman.compute();

		Node a = graph.getNode("A") ;
		double v = bellman.getShortestPathValue(a);
		
		assertTrue(v == 5);
	}
}
