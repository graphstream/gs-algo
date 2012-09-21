package org.graphstream.algorithm.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
			e.addAttribute("weight", weights[i]);
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
		int edgeCount = 0;
		for (Edge e : k.getTreeEdges()) {
			boolean b = e.getAttribute(k.getFlagAttribute()); 
			assertTrue(b);
			edgeCount++;
		}
		assertEquals(expectedCount, edgeCount);
		
		edgeCount = 0;
		double treeWeight = 0;
		for (Edge e : g.getEachEdge()) {
			boolean b = e.getAttribute(k.getFlagAttribute());
			if (b) {
				edgeCount++;
				treeWeight += e.getNumber("weight");
			}
		}
		assertEquals(expectedWeight, treeWeight, 0);
		assertEquals(expectedCount, edgeCount);
	}
}
