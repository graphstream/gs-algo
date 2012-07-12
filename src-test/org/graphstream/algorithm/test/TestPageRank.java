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
