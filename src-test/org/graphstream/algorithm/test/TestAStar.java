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
 * @since 2011-05-12
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import java.util.Iterator;
import java.util.List;

import org.graphstream.algorithm.AStar;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.MultiGraph;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Simple test of the A* algorithm.
 * 
 * @author Antoine Dutot
 */
public class TestAStar {
	public static void main(String args[]) {
		TestAStar tas = new TestAStar();
		/*
		 * tas.setUp(); tas.testAStarNoWeights(); tas.setUp();
		 * tas.testAStarWeighted1(); tas.setUp(); tas.testAStarWeighted2();
		 * tas.setUp(); tas.testAStarDistances1(); tas.setUp();
		 * tas.testAStarDistances2();
		 */tas.setUp();
		tas.testAStarMultiGraph();
		tas.graph.display();
	}

	public Graph graph;
	Node A, B, C, D, E, F;
	Edge AB, BC, CD, DE, EF, BF;
	AStar astar;

	@Before
	public void setUp() {
		// Do not forget : called for each test !

		graph = new MultiGraph("g1", false, true);

		AB = graph.addEdge("AB", "A", "B");
		BC = graph.addEdge("BC", "B", "C");
		CD = graph.addEdge("CD", "C", "D");
		DE = graph.addEdge("DE", "D", "E");
		EF = graph.addEdge("EF", "E", "F");
		BF = graph.addEdge("BF", "B", "F");

		A = graph.getNode("A");
		A.setAttribute("label", "A");
		B = graph.getNode("B");
		B.setAttribute("label", "B");
		C = graph.getNode("C");
		C.setAttribute("label", "C");
		D = graph.getNode("D");
		D.setAttribute("label", "D");
		E = graph.getNode("E");
		E.setAttribute("label", "E");
		F = graph.getNode("F");
		F.setAttribute("label", "F");

		// C --- D
		// / |
		// A --- B |
		// \ |
		// F --- E
		//
		// The shortest path between A and F is therefore A -> B -> F.

		astar = new AStar(graph);
		astar.init(graph);
		astar.setCosts(new AStar.DefaultCosts("weight"));

		// With this default costs object, the A* algorithm works like the
		// Dijkstra algorithm.
		// This means that g is evaluated as normal, but h is always 0.
	}

	@Test
	public void testAStarNoWeights() {
		// Try to find a path between A and F, with all edges having a
		// default weight of 1. This is done with the default costs, with
		// h always evaluated as 0.

		astar.compute("A", "F");

		Path path = astar.getShortestPath();

		List<Edge> edges = path.getEdgePath();

		for (Edge edge : edges)
			edge.setAttribute("color", "red");

		Iterator<? extends Edge> i = edges.iterator();

		Edge e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("AB"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("BF"));
		assertTrue(!i.hasNext());
	}

	@Test
	public void testAStarWeighted1() {
		// Try to find a path between A and F, with all edges having a
		// default weight of 1, excepted the BF edge with weight 1.5.
		// This is done with the default costs, with
		// h always evaluated as 0. The fact BF has a larger cost would
		// orient a greedy algorithm toward BC then CD. AStar will peek
		// BC, but not CD.

		AB.setAttribute("weight", 1);
		BC.setAttribute("weight", 1);
		BF.setAttribute("weight", 1.5f); // First orient the algorithm toward
											// BC.
		CD.setAttribute("weight", 1);
		DE.setAttribute("weight", 1);
		EF.setAttribute("weight", 1);

		astar.compute("A", "F");

		Path path = astar.getShortestPath();

		List<Edge> edges = path.getEdgePath();

		for (Edge edge : edges)
			edge.setAttribute("color", "red");

		Iterator<? extends Edge> i = edges.iterator();

		Edge e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("AB"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("BF"));
		assertTrue(!i.hasNext());
	}

	@Test
	public void testAStarWeighted2() {
		// Same test as AStarWeighted1, but the cost of path A-B-C-D-E-F is
		// smaller
		// Than the path A-B-F.
		// Therefore the shortest path is A -> B -> C -> D -> E -> F, sum = 1.4.
		// Whereas path A -> B -> F sum = 2.

		AB.setAttribute("weight", 1.0f);
		BC.setAttribute("weight", 0.1f);
		BF.setAttribute("weight", 1.0f);
		CD.setAttribute("weight", 0.1f);
		DE.setAttribute("weight", 0.1f);
		EF.setAttribute("weight", 0.1f);

		astar.compute("A", "F");

		Path path = astar.getShortestPath();

		List<Edge> edges = path.getEdgePath();

		for (Edge edge : edges)
			edge.setAttribute("color", "red");

		Iterator<? extends Edge> i = edges.iterator();

		Edge e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("AB"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("BC"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("CD"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("DE"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("EF"));
		assertTrue(!i.hasNext());
	}

	@Test
	public void testAStarDistances1() {
		// Now make the test with real Euclidian distance.
		// Each node is assigned a position in a 2D space.
		// The costs object is revised to consider h as the
		// straight line distance (the shortest one). This is
		// and admissible h heuristic.

		A.setAttribute("xy", 0, 0);
		B.setAttribute("xy", 1, 0);
		C.setAttribute("xy", 2, 1);
		F.setAttribute("xy", 2, -1);
		D.setAttribute("xy", 3, 1);
		E.setAttribute("xy", 3, -1);

		// 0 1 2 3
		// 1 C --- D
		// / |
		// 0 A --- B |
		// \ |
		// -1 F --- E

		astar.setCosts(new AStar.DistanceCosts());
		astar.compute("A", "F");

		Path path = astar.getShortestPath();
		List<Edge> edges = path.getEdgePath();

		for (Edge edge : edges)
			edge.setAttribute("color", "red");

		Iterator<? extends Edge> i = edges.iterator();

		Edge e = i.next();
		assertNotNull(e);
		assertTrue(e.getId().equals("AB"));
		e = i.next();
		assertNotNull(e);
		assertTrue(e.getId().equals("BF"));
		assertTrue(!i.hasNext());
	}

	@Test
	public void testAStarDistances2() {
		// The same as AStarDistance1 but with the path A-B-C-D-E-F shorter
		// than the path A-B-G-F with a node G added.

		Node G = graph.addNode("G");

		graph.removeEdge("BF");
		graph.addEdge("BG", "B", "G");
		graph.addEdge("GF", "G", "F");

		A.setAttribute("xy", 0, 0);
		B.setAttribute("xy", 1, 0);
		C.setAttribute("xy", 2, 1);
		D.setAttribute("xy", 3, 1);
		E.setAttribute("xy", 4, 1);
		F.setAttribute("xy", 5, 1);
		G.setAttribute("xy", 6, 0);
		G.setAttribute("label", "G");

		// 0 1 2 3 4 5 6
		// 1 C--D--E--F
		// / \
		// 0 A---B------------G

		astar.setCosts(new AStar.DistanceCosts());
		astar.compute("A", "F");

		Path path = astar.getShortestPath();
		List<Edge> edges = path.getEdgePath();

		for (Edge edge : edges)
			edge.setAttribute("color", "red");

		Iterator<? extends Edge> i = edges.iterator();

		Edge e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("AB"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("BC"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("CD"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("DE"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("EF"));
		assertTrue(!i.hasNext());
	}

	@Test
	public void testAStarMultiGraph() {
		// C-----D
		// / |
		// A-----B--+ |
		// |\ | |
		// | \| |
		// +--F -- E
		//
		// We add two edges between node B and F. There are therefore three
		// edges
		// one with weight 1, one with weight 2 and one with weight 3.
		// To further complicate things, the edge BC value is 0.5. All other
		// edges
		// weight is one.

		Edge BF1 = graph.getEdge("BF");
		Edge BF2 = graph.addEdge("BF2", "B", "F");
		Edge BF3 = graph.addEdge("BF3", "B", "F");

		AB.setAttribute("weight", 1f);
		BF1.setAttribute("weight", 3f);
		BF2.setAttribute("weight", 2f);
		BF3.setAttribute("weight", 1f);
		BC.setAttribute("weight", 0.5f);
		CD.setAttribute("weight", 1f);
		DE.setAttribute("weight", 1f);
		EF.setAttribute("weight", 1f);

		astar.compute("A", "F");

		Path path = astar.getShortestPath();

		List<Edge> edges = path.getEdgePath();

		for (Edge edge : edges)
			edge.setAttribute("color", "red");

		Iterator<? extends Edge> i = edges.iterator();

		Edge e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("AB"));
		e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("BF3"));
		assertTrue(!i.hasNext());
	}
}