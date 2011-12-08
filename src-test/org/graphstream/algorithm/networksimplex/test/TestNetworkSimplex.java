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
	
	public static void checkReferenceSolution(NetworkSimplex ns) {
		Graph g = ns.getGraph();
		
		assertEquals(NetworkSimplex.SolutionStatus.OPTIMAL, ns.getSolutionStatus());
		assertEquals(47, ns.getObjectiveValue());

		assertEquals(0, ns.getNetworkBalance());
		for (Node n : g)
			assertEquals(0, ns.getNodeBalance(n), 0);

		assertEquals(3, ns.getFlow(g.getEdge("AB")));
		assertEquals(2, ns.getFlow(g.getEdge("AC")));
		assertEquals(5, ns.getFlow(g.getEdge("BC")));
		assertEquals(1, ns.getFlow(g.getEdge("CD")));
		assertEquals(1, ns.getFlow(g.getEdge("CE")));
		assertEquals(5, ns.getFlow(g.getEdge("CF")));
		assertEquals(3, ns.getFlow(g.getEdge("FE")));
		
		assertEquals(NetworkSimplex.ArcStatus.NONBASIC_UPPER, ns.getStatus(g.getEdge("AB")));
		assertEquals(NetworkSimplex.ArcStatus.BASIC, ns.getStatus(g.getEdge("AC")));
		assertEquals(NetworkSimplex.ArcStatus.BASIC, ns.getStatus(g.getEdge("BC")));
		assertEquals(NetworkSimplex.ArcStatus.BASIC, ns.getStatus(g.getEdge("CD")));
		assertEquals(NetworkSimplex.ArcStatus.BASIC, ns.getStatus(g.getEdge("CE")));
		assertEquals(NetworkSimplex.ArcStatus.BASIC, ns.getStatus(g.getEdge("CF")));
		assertEquals(NetworkSimplex.ArcStatus.NONBASIC_UPPER, ns.getStatus(g.getEdge("FE")));
		
		assertNull(ns.getEdgeToParent(g.getNode("A")));
		assertEquals("BC", ns.getEdgeToParent(g.getNode("B")).getId());
		assertEquals("AC", ns.getEdgeToParent(g.getNode("C")).getId());
		assertEquals("CD", ns.getEdgeToParent(g.getNode("D")).getId());
		assertEquals("CE", ns.getEdgeToParent(g.getNode("E")).getId());
		assertEquals("CF", ns.getEdgeToParent(g.getNode("F")).getId());
	}
	
	public static void compareSolutions(NetworkSimplex ns1, NetworkSimplex ns2) {
		Graph g = ns1.getGraph();
		assertEquals(ns1.getNetworkBalance(), ns2.getNetworkBalance());
		assertEquals(ns1.getSolutionStatus(), ns2.getSolutionStatus());
		assertEquals(ns1.getObjectiveValue(), ns2.getObjectiveValue());
		for (Node n : g) {
			assertEquals(ns1.getNodeBalance(n), ns2.getNodeBalance(n));
			assertEquals(ns1.getEdgeToParent(n), ns2.getEdgeToParent(n));
		}
		for (Edge e : g.getEachEdge()) {
			assertEquals(ns1.getFlow(e, true), ns2.getFlow(e, true));
			assertEquals(ns1.getFlow(e, false), ns2.getFlow(e, false));
			assertEquals(ns1.getStatus(e, true), ns2.getStatus(e, true));
			assertEquals(ns1.getStatus(e, false), ns2.getStatus(e, false));
			
		}
	}

	@Test
	public void toyTest() {
		Graph g = toyGraph();
		NetworkSimplex ns1 = new NetworkSimplex("supply", "capacity", "cost");
		ns1.init(g);
		assertEquals(NetworkSimplex.SolutionStatus.UNDEFINED, ns1.getSolutionStatus());
		ns1.compute();
		checkReferenceSolution(ns1);
		
		// now see if we obtain the same solution using other pricing strategy
		NetworkSimplex ns2 = new NetworkSimplex("supply", "capacity", "cost");
		ns2.init(g);
		assertEquals(NetworkSimplex.SolutionStatus.UNDEFINED, ns2.getSolutionStatus());
		ns2.compute();
		compareSolutions(ns1, ns2);
	}
	
	@Test
	public void costChangeTest() {
		Graph g = toyGraph();
		NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		ns.init(g);
		ns.compute();

		// change the cost of FE (NONBASIC_UPPER) and recompute
		// minor pivot should happen
		g.getEdge("FE").addAttribute("cost", 4);
		assertEquals(NetworkSimplex.SolutionStatus.UNDEFINED, ns.getSolutionStatus());
		ns.compute();
		// and see if we obtain the same result computing from scratch
		NetworkSimplex nsCheck = new NetworkSimplex("supply", "capacity", "cost");
		nsCheck.init(g);
		nsCheck.compute();
		compareSolutions(nsCheck, ns);
		// now restore the cost of FE and see if we find the initial solution
		g.getEdge("FE").addAttribute("cost", 1);
		ns.compute();
		checkReferenceSolution(ns);
		
		// now change the cost of AC (BASIC) and recompute
		// AB should enter and AC should leave
		g.getEdge("AC").addAttribute("cost", 2);
		ns.compute();
		// and see if we obtain the same result computing from scratch
		nsCheck = new NetworkSimplex("supply", "capacity", "cost");
		nsCheck.init(g);
		nsCheck.compute();
		compareSolutions(nsCheck, ns);
		//now restore the cost of AC and see if we obtain the initial solution
		g.getEdge("AC").addAttribute("cost", 4);
		ns.compute();
		checkReferenceSolution(ns);
		
		// now change the both arcs together
		g.getEdge("FE").addAttribute("cost", 4);
		g.getEdge("AC").addAttribute("cost", 2);
		ns.compute();
		// and see if we obtain the same result computing from scratch
		nsCheck = new NetworkSimplex("supply", "capacity", "cost");
		nsCheck.init(g);
		nsCheck.compute();
		compareSolutions(nsCheck, ns);	
		// restore the both arcs and see if we obtain the initial solution
		g.getEdge("FE").addAttribute("cost", 1);
		g.getEdge("AC").addAttribute("cost", 4);
		ns.compute();
		checkReferenceSolution(ns);
	}
}
