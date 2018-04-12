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
 * @since 2011-12-06
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.networksimplex.test;

import static org.junit.Assert.*;

import org.graphstream.algorithm.networksimplex.NetworkSimplex;
import org.graphstream.algorithm.networksimplex.NetworkSimplex.PricingStrategy;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class TestNetworkSimplex {
	public static Graph toyGraph() {
		Graph g = new SingleGraph("test");

		g.addNode("A").setAttribute("supply", 5);
		g.addNode("B").setAttribute("supply", 2);
		g.addNode("C").setAttribute("supply", 0);
		g.addNode("D").setAttribute("supply", -1);
		g.addNode("E").setAttribute("supply", -4);
		g.addNode("F").setAttribute("supply", -2);

		Edge e;
		e = g.addEdge("AB", "A", "B", true);
		e.setAttribute("capacity", 3);
		e.setAttribute("cost", 1);
		e = g.addEdge("AC", "A", "C", true);
		e.setAttribute("capacity", 3);
		e.setAttribute("cost", 4);
		e = g.addEdge("BC", "B", "C", true);
		e.setAttribute("capacity", 7);
		e.setAttribute("cost", 2);
		e = g.addEdge("CD", "C", "D", true);
		e.setAttribute("capacity", 1);
		e.setAttribute("cost", 8);
		e = g.addEdge("CE", "C", "E", true);
		e.setAttribute("capacity", 7);
		e.setAttribute("cost", 5);
		e = g.addEdge("CF", "C", "F", true);
		e.setAttribute("capacity", 5);
		e.setAttribute("cost", 2);
		e = g.addEdge("FE", "F", "E", true);
		e.setAttribute("capacity", 3);
		e.setAttribute("cost", 1);

		return g;
	}

	public static void checkReferenceSolution(NetworkSimplex ns) {
		Graph g = ns.getGraph();

		assertEquals(0, ns.getNetworkBalance());

		assertEquals(NetworkSimplex.SolutionStatus.OPTIMAL,
				ns.getSolutionStatus());
		assertEquals(47, ns.getSolutionCost());
		assertEquals(0, ns.getSolutionInfeasibility());

		for (Node n : g)
			assertEquals(0, ns.getInfeasibility(n), 0);

		assertEquals(3, ns.getFlow(g.getEdge("AB")));
		assertEquals(2, ns.getFlow(g.getEdge("AC")));
		assertEquals(5, ns.getFlow(g.getEdge("BC")));
		assertEquals(1, ns.getFlow(g.getEdge("CD")));
		assertEquals(1, ns.getFlow(g.getEdge("CE")));
		assertEquals(5, ns.getFlow(g.getEdge("CF")));
		assertEquals(3, ns.getFlow(g.getEdge("FE")));
	}

	public static void compareSolutions(NetworkSimplex ns1, NetworkSimplex ns2) {
		Graph g = ns1.getGraph();
		assertEquals(ns1.getNetworkBalance(), ns2.getNetworkBalance());
		assertEquals(ns1.getSolutionStatus(), ns2.getSolutionStatus());
		assertEquals(ns1.getSolutionCost(), ns2.getSolutionCost());
		assertEquals(ns1.getSolutionInfeasibility(), ns2.getSolutionInfeasibility());
		
		g.nodes().forEach(n -> assertEquals(ns1.getInfeasibility(n), ns2.getInfeasibility(n)));

		g.edges().forEach(e -> {
			assertEquals(ns1.getFlow(e, true), ns2.getFlow(e, true));
			assertEquals(ns1.getFlow(e, false), ns2.getFlow(e, false));
		});
	}

	public static void compareWithNew(NetworkSimplex ns) {
		NetworkSimplex nsCheck = new NetworkSimplex("supply", "capacity",
				"cost");
		nsCheck.init(ns.getGraph());
		nsCheck.compute();
		compareSolutions(nsCheck, ns);
	}

	@Test
	public void toyTest() {
		Graph g = toyGraph();
		NetworkSimplex ns1 = new NetworkSimplex("supply", "capacity", "cost");
		ns1.init(g);
		assertEquals(NetworkSimplex.SolutionStatus.UNDEFINED,
				ns1.getSolutionStatus());
		ns1.compute();
		checkReferenceSolution(ns1);

		// now see if we obtain the same solution using other pricing strategy
		NetworkSimplex ns2 = new NetworkSimplex("supply", "capacity", "cost");
		ns2.setPricingStrategy(PricingStrategy.FIRST_NEGATIVE);
		ns2.init(g);
		assertEquals(NetworkSimplex.SolutionStatus.UNDEFINED,
				ns2.getSolutionStatus());
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
		g.getEdge("FE").setAttribute("cost", 4);
		assertEquals(NetworkSimplex.SolutionStatus.UNDEFINED,
				ns.getSolutionStatus());
		ns.compute();
		// and see if we obtain the same result computing from scratch
		compareWithNew(ns);
		// now restore the cost of FE and see if we find the initial solution
		g.getEdge("FE").setAttribute("cost", 1);
		ns.compute();
		checkReferenceSolution(ns);

		// now change the cost of AC (BASIC) and recompute
		// AB should enter and AC should leave
		g.getEdge("AC").setAttribute("cost", 2);
		ns.compute();
		// and see if we obtain the same result computing from scratch
		compareWithNew(ns);
		// now restore the cost of AC and see if we obtain the initial solution
		g.getEdge("AC").setAttribute("cost", 4);
		ns.compute();
		checkReferenceSolution(ns);

		// now change the both arcs together
		g.getEdge("FE").setAttribute("cost", 4);
		g.getEdge("AC").setAttribute("cost", 2);
		ns.compute();
		// and see if we obtain the same result computing from scratch
		compareWithNew(ns);
		// restore the both arcs and see if we obtain the initial solution
		g.getEdge("FE").setAttribute("cost", 1);
		g.getEdge("AC").setAttribute("cost", 4);
		ns.compute();
		checkReferenceSolution(ns);
	}

	@Test
	public void supplyChangeTest() {
		Graph g = toyGraph();
		NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		ns.init(g);
		ns.compute();

		// The supply of A is 5
		// change the supply of A (AR is already basic)
		g.getNode("A").setAttribute("supply", 4);
		ns.compute();
		compareWithNew(ns);

		// one more change of the supply of A
		g.getNode("A").setAttribute("supply", 6);
		ns.compute();
		compareWithNew(ns);

		// restore
		g.getNode("A").setAttribute("supply", 5);
		ns.compute();
		checkReferenceSolution(ns);

		// Now play with F (RF is non-basic), supply = -2
		g.getNode("F").setAttribute("supply", -1);
		ns.compute();
		compareWithNew(ns);

		// one more change of the supply of F
		g.getNode("F").setAttribute("supply", -3);
		ns.compute();
		compareWithNew(ns);

		// restore
		g.getNode("F").setAttribute("supply", -2);
		ns.compute();
		checkReferenceSolution(ns);

		// Now check with 2 nodes at the same time
		g.getNode("A").setAttribute("supply", 6);
		g.getNode("E").setAttribute("supply", -5);
		ns.compute();
		compareWithNew(ns);

		// restore
		g.getNode("A").setAttribute("supply", 5);
		g.getNode("E").setAttribute("supply", -4);
		ns.compute();
		checkReferenceSolution(ns);

		// one test with 3 nodes
		g.getNode("B").setAttribute("supply", 1);
		g.getNode("C").setAttribute("supply", -1);
		g.getNode("E").setAttribute("supply", -2);
		ns.compute();
		compareWithNew(ns);
		
		// restore
		g.getNode("B").setAttribute("supply", 2);
		g.getNode("C").setAttribute("supply", 0);
		g.getNode("E").setAttribute("supply", -4);
		ns.compute();
		checkReferenceSolution(ns);
	}

	@Test
	public void capacityChangeTest() {
		Graph g = toyGraph();
		NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		ns.init(g);
		ns.compute();

		// Decrease the capacity of AB (NONBASIC_UPPER)
		g.getEdge("AB").setAttribute("capacity", 2);
		ns.compute();
		compareWithNew(ns);
		// restore
		g.getEdge("AB").setAttribute("capacity", 3);
		ns.compute();
		checkReferenceSolution(ns);
		// now increase it
		g.getEdge("AB").setAttribute("capacity", 4);
		ns.compute();
		compareWithNew(ns);
		// restore it again
		g.getEdge("AB").setAttribute("capacity", 3);
		ns.compute();
		checkReferenceSolution(ns);

		// Decrease the capacity of CF (BASIC)
		g.getEdge("CF").setAttribute("capacity", 4);
		ns.compute();
		compareWithNew(ns);
		// restore
		g.getEdge("CF").setAttribute("capacity", 5);
		ns.compute();
		checkReferenceSolution(ns);

		// close CD. The problem should become infeasible
		g.getEdge("CD").setAttribute("capacity", 0);
		ns.compute();
		assertEquals(NetworkSimplex.SolutionStatus.INFEASIBLE,
				ns.getSolutionStatus());
		compareWithNew(ns);
		// restore
		g.getEdge("CD").setAttribute("capacity", 1);
		ns.compute();
		checkReferenceSolution(ns);

		// now several at the same time
		g.getEdge("AB").setAttribute("capacity", 2);
		g.getEdge("CF").setAttribute("capacity", 4);
		g.getEdge("CD").removeAttribute("capacity");
		ns.compute();
		compareWithNew(ns);
		// restore
		g.getEdge("AB").setAttribute("capacity", 3);
		g.getEdge("CF").setAttribute("capacity", 5);
		ns.compute();
		checkReferenceSolution(ns);
	}

	@Test
	public void edgeAddRemoveTest() {
		Graph g = toyGraph();
		NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		ns.init(g);
		ns.compute();

		Edge bf = g.addEdge("BF", "B", "F");
		bf.setAttribute("cost", 3);
		bf.setAttribute("capacity", 4);
		ns.compute();
		compareWithNew(ns);

		g.removeEdge("CF");
		ns.compute();
		compareWithNew(ns);

		Edge ad = g.addEdge("AD", "A", "D");
		ad.setAttribute("cost", 11);
		ns.compute();
		compareWithNew(ns);

		g.removeEdge(ad);
		ns.compute();
		compareWithNew(ns);

		g.removeEdge("BC");
		ns.compute();
		compareWithNew(ns);
	}

	@Test
	public void nodeAddRemoveTest() {
		Graph g = toyGraph();
		NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		ns.init(g);
		ns.compute();

		g.removeNode("F");
		ns.compute();
		compareWithNew(ns);

		g.addNode("F").setAttribute("supply", -2);
		ns.compute();
		compareWithNew(ns);

		g.addEdge("EF", "E", "F").setAttribute("cost", 6);
		ns.compute();
		compareWithNew(ns);
	}

	@Test
	public void graphClearTest() {
		Graph g = toyGraph();
		NetworkSimplex ns = new NetworkSimplex("supply", "capacity", "cost");
		ns.init(g);
		ns.compute();

		g.clear();
		ns.compute();
		compareWithNew(ns);
	}
}
