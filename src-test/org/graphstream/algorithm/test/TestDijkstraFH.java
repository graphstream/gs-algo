package org.graphstream.algorithm.test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.DijkstraFH;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class TestDijkstraFH {
	
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
		g.addNode("A").addAttribute("xy", 0, 1);
		g.addNode("B").addAttribute("xy", 1, 2);
		g.addNode("C").addAttribute("xy", 1, 1);
		g.addNode("D").addAttribute("xy", 1, 0);
		g.addNode("E").addAttribute("xy", 2, 2);
		g.addNode("F").addAttribute("xy", 2, 1);
		g.addNode("G").addAttribute("xy", 2, 0);
		g.addEdge("AB", "A", "B").addAttribute("length", 14);
		g.addEdge("AC", "A", "C").addAttribute("length", 9);
		g.addEdge("AD", "A", "D").addAttribute("length", 7);
		g.addEdge("BC", "B", "C").addAttribute("length", 2);
		g.addEdge("CD", "C", "D").addAttribute("length", 10);
		g.addEdge("BE", "B", "E").addAttribute("length", 9);
		g.addEdge("CF", "C", "F").addAttribute("length", 11);
		g.addEdge("DF", "D", "F").addAttribute("length", 15);
		g.addEdge("EF", "E", "F").addAttribute("length", 6);
		for (Node n : g)
			n.addAttribute("label", n.getId());
		for (Edge e : g.getEachEdge())
			e.addAttribute("label", "" + (int) e.getNumber("length"));
		return g;
	}
	
	@Test
	public void toyTest() {
		Graph g = toyGraph();
		
		// Test the weighted case
		DijkstraFH d = new DijkstraFH(DijkstraFH.Element.EDGE, "result", "length");
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
		assertFalse(d.getPathNodesIterator(g.getNode("G")).hasNext());
		assertFalse(d.getPathEdgesIterator(g.getNode("G")).hasNext());
		
		d.clear();
		assertFalse(source.hasAttribute("result"));
		
		
		// Test unweighted case with nodes
		d = new DijkstraFH(DijkstraFH.Element.NODE, "result");
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
		assertFalse(d.getAllPathsIterator(g.getNode("G")).hasNext());
	}
	
	
	public static Graph randomGraph(int nodes, int degree) {
		Graph g = new SingleGraph("random");
		Generator generator = new RandomGenerator(degree, false, false, null, "length");
		generator.addSink(g);
		generator.begin();
		for (int i = 1; i < nodes; i++)
			generator.nextEvents();
		generator.end();
		return g;
	}
	
	public static void compareDijkstras(Graph g) {
		final double EPS = 1.0e-8;
		
		System.gc();
		long start = System.currentTimeMillis();
		Dijkstra d1 = new Dijkstra(Dijkstra.Element.edge, "length", "0");
		d1.init(g);
		d1.compute();
		long end = System.currentTimeMillis();
		double t1 = (end - start) / 1000.0;
		
		System.gc();
		start = System.currentTimeMillis();
		DijkstraFH d2 = new DijkstraFH(DijkstraFH.Element.EDGE, "result", "length");
		d2.init(g);
		d2.setSource(g.getNode("0"));
		d2.compute();
		end = System.currentTimeMillis();
		double t2 = (end - start) / 1000.0;
		
		// check if both algorithms produce the same result
		for (Node n : g)
			assertEquals(d1.getShortestPathValue(n), d2.getPathLength(n), EPS);
		
		// print running times in gnuplotable form
		System.out.printf("%15d%15d%15.4f%15.4f%n", g.getNodeCount(), g.getEdgeCount(), t1, t2);
	}
	
	@Test
	public void randomTest() {
		for (int n = 100; n <= 1000; n += 100) {
			Graph g = randomGraph(n, n / 10);
			compareDijkstras(g);
		}
	}
}
