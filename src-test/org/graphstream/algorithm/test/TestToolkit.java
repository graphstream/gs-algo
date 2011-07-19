package org.graphstream.algorithm.test;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestToolkit {
	/**
	 * A graph used to test clique methods 
	 */
	public static Graph toyCliqueGraph() {
//		    B-----E     H
//		   /|\   /|\
//		  / | \ / | \
//		 A--+--D--F--G--I
//		  \ | /
//		   \|/
//		    C
//		    
//		 This graph has 6 maximal cliques:
//		 [A, B, C, D], [B, D, E], [D, E, F], [E, F, G], [H], [G, I]

		Graph g = new SingleGraph("cliques");
		g.addNode("A").addAttribute("xy", 0, 1);
		g.addNode("B").addAttribute("xy", 1, 2);
		g.addNode("C").addAttribute("xy", 1, 0);
		g.addNode("D").addAttribute("xy", 2, 1);
		g.addNode("E").addAttribute("xy", 3, 2);
		g.addNode("F").addAttribute("xy", 3, 1);
		g.addNode("G").addAttribute("xy", 4, 1);
		g.addNode("H").addAttribute("xy", 5, 2);
		g.addNode("I").addAttribute("xy", 5, 1);

		for (Node n : g)
			n.addAttribute("label", n.getId());

		g.addEdge("AB", "A", "B");
		g.addEdge("AC", "A", "C");
		g.addEdge("AD", "A", "D");
		g.addEdge("BC", "B", "C");
		g.addEdge("BD", "B", "D");
		g.addEdge("BE", "B", "E");
		g.addEdge("CD", "C", "D");
		g.addEdge("DE", "D", "E");
		g.addEdge("DF", "D", "F");
		g.addEdge("EF", "E", "F");
		g.addEdge("EG", "E", "G");
		g.addEdge("FG", "F", "G");
		g.addEdge("GI", "G", "I");
		return g;
	}

	/**
	 * Unit tests for {@link Toolkit#isClique(java.util.Collection)}, 
	 * {@link Toolkit#isClique(java.util.Collection)},
	 * {@link Toolkit#isMaximalClique(java.util.Collection)},
	 * {@link Toolkit#getMaximalCliqueIterator(Graph)},
	 * {@link Toolkit#getMaximalCliques(Graph)} and
	 * {@link Toolkit#getDegeneracy(Graph, List)}
	 */
	@Test
	public void testCliques() {
		Graph g = toyCliqueGraph();
		
		int d = Toolkit.getDegeneracy(g, null);
		assertEquals(3, d);
		List<Node> ordering = new ArrayList<Node>();
		d = Toolkit.getDegeneracy(g, ordering);
		assertEquals(3, d);
		assertEquals(9, ordering.size());
		assertEquals(g.getNode("H"), ordering.get(8));
		assertEquals(g.getNode("I"), ordering.get(7));
		assertEquals(g.getNode("G"), ordering.get(6));
		assertEquals(g.getNode("F"), ordering.get(5));
		assertEquals(g.getNode("E"), ordering.get(4));
		assertTrue(ordering.contains(g.getNode("A")));
		assertTrue(ordering.contains(g.getNode("B")));
		assertTrue(ordering.contains(g.getNode("C")));
		assertTrue(ordering.contains(g.getNode("D")));
		
		int cliqueCount = 0;
		int totalNodeCount = 0;
		List<Node> maximumClique = new ArrayList<Node>();
		for (List<Node> clique : Toolkit.getMaximalCliques(g)) {
			assertTrue(Toolkit.isClique(clique));
			assertTrue(Toolkit.isMaximalClique(clique, g));
			cliqueCount++;
			totalNodeCount += clique.size();
			if (clique.size() > maximumClique.size())
				maximumClique = clique;
		}
		assertEquals(6, cliqueCount);
		assertEquals(16, totalNodeCount);
		
		assertEquals(4, maximumClique.size());
		assertTrue(maximumClique.contains(g.getNode("A")));
		assertTrue(maximumClique.contains(g.getNode("B")));
		assertTrue(maximumClique.contains(g.getNode("C")));
		assertTrue(maximumClique.contains(g.getNode("D")));		
	}
}
