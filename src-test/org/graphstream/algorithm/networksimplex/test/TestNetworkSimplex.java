package org.graphstream.algorithm.networksimplex.test;

import static org.junit.Assert.*;

import org.graphstream.algorithm.networksimplex.NetworkSimplex;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class TestNetworkSimplex {
	public static Graph toyGraph() {
		Graph g = new SingleGraph("test");

		g.addNode("A").addAttribute("supply", 5);
		g.addNode("B").addAttribute("supply", 2);
		g.addNode("C").addAttribute("supply", 0);
		g.addNode("D").addAttribute("supply", -1);
		g.addNode("E").addAttribute("supply", -4);
		g.addNode("F").addAttribute("supply", -2);

		Edge e;
		e = g.addEdge("AB", "A", "B", true);
		e.addAttribute("capacity", 3);
		e.addAttribute("cost", 1);
		e = g.addEdge("AC", "A", "C", true);
		e.addAttribute("capacity", 3);
		e.addAttribute("cost", 4);
		e = g.addEdge("BC", "B", "C", true);
		e.addAttribute("capacity", 7);
		e.addAttribute("cost", 2);
		e = g.addEdge("CD", "C", "D", true);
		e.addAttribute("capacity", 1);
		e.addAttribute("cost", 8);
		e = g.addEdge("CE", "C", "E", true);
		e.addAttribute("capacity", 7);
		e.addAttribute("cost", 5);
		e = g.addEdge("CF", "C", "F", true);
		e.addAttribute("capacity", 5);
		e.addAttribute("cost", 2);
		e = g.addEdge("FE", "F", "E", true);
		e.addAttribute("capacity", 3);
		e.addAttribute("cost", 1);

		return g;
	}

	@Test
	public void staticTest() {
		Graph g = toyGraph();
		NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		ns.init(g);

		assertEquals(ns.getSolutionStatus(),
				NetworkSimplex.SolutionStatus.OPTIMAL);
		assertEquals(ns.getObjectiveValue(), 47);

		assertEquals(ns.getNetworkBalance(), 0);
		for (Node n : g)
			assertEquals(ns.getNodeBalance(n), 0);

		assertEquals(ns.getFlow(g.getEdge("AB")), 3);
		assertEquals(ns.getFlow(g.getEdge("AC")), 2);
		assertEquals(ns.getFlow(g.getEdge("BC")), 5);
		assertEquals(ns.getFlow(g.getEdge("CD")), 1);
		assertEquals(ns.getFlow(g.getEdge("CE")), 1);
		assertEquals(ns.getFlow(g.getEdge("CF")), 5);
		assertEquals(ns.getFlow(g.getEdge("FE")), 3);
		
		assertEquals(ns.getStatus(g.getEdge("AB")), NetworkSimplex.ArcStatus.NONBASIC_UPPER);
		assertEquals(ns.getStatus(g.getEdge("AC")), NetworkSimplex.ArcStatus.BASIC);
		assertEquals(ns.getStatus(g.getEdge("BC")), NetworkSimplex.ArcStatus.BASIC);
		assertEquals(ns.getStatus(g.getEdge("CD")), NetworkSimplex.ArcStatus.BASIC);
		assertEquals(ns.getStatus(g.getEdge("CE")), NetworkSimplex.ArcStatus.BASIC);
		assertEquals(ns.getStatus(g.getEdge("CF")), NetworkSimplex.ArcStatus.BASIC);
		assertEquals(ns.getStatus(g.getEdge("FE")), NetworkSimplex.ArcStatus.NONBASIC_UPPER);
		
		assertNull(ns.getEdgeToParent(g.getNode("A")));
		assertEquals(ns.getEdgeToParent(g.getNode("B")).getId(), "BC");
		assertEquals(ns.getEdgeToParent(g.getNode("C")).getId(), "AC");
		assertEquals(ns.getEdgeToParent(g.getNode("D")).getId(), "CD");
		assertEquals(ns.getEdgeToParent(g.getNode("E")).getId(), "CE");
		assertEquals(ns.getEdgeToParent(g.getNode("F")).getId(), "CF");
	}
}
