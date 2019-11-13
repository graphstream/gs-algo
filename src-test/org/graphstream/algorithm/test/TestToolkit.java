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
 * @since 2011-07-13
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestToolkit {
	/**
	 * A graph used to test clique methods
	 */
	public static Graph toyCliqueGraph() {
		// B-----E H
		// /|\ /|\
		// / | \ / | \
		// A--+--D--F--G--I
		// \ | /
		// \|/
		// C
		//		    
		// This graph has 6 maximal cliques:
		// [A, B, C, D], [B, D, E], [D, E, F], [E, F, G], [H], [G, I]

		Graph g = new SingleGraph("cliques");
		g.addNode("A").setAttribute("xy", 0, 1);
		g.addNode("B").setAttribute("xy", 1, 2);
		g.addNode("C").setAttribute("xy", 1, 0);
		g.addNode("D").setAttribute("xy", 2, 1);
		g.addNode("E").setAttribute("xy", 3, 2);
		g.addNode("F").setAttribute("xy", 3, 1);
		g.addNode("G").setAttribute("xy", 4, 1);
		g.addNode("H").setAttribute("xy", 5, 2);
		g.addNode("I").setAttribute("xy", 5, 1);

		for (Node n : g)
			n.setAttribute("label", n.getId());

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
	 * {@link Toolkit#getDegeneracy(Graph, List, String)}
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

	@Test
	public void testClusteringCoefficient() {
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		double cc;

		g.addNode("A");
		g.addNode("B");
		g.addNode("C");
		g.addNode("D");

		g.addEdge("AB", "A", "B");
		g.addEdge("AC", "A", "C");
		g.addEdge("AD", "A", "D");

		cc = Toolkit.clusteringCoefficient(g.getNode("A"));
		assertTrue(cc == 0);

		g.addEdge("BC", "B", "C");

		cc = Toolkit.clusteringCoefficient(g.getNode("A"));
		assertTrue(cc == 1.0 / 3.0);

		g.addEdge("BD", "B", "D");
		g.addEdge("CD", "C", "D");

		cc = Toolkit.clusteringCoefficient(g.getNode("A"));
		assertTrue(cc == 1.0);
	}
}
