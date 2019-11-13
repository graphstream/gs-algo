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
 * @since 2011-12-11
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.networksimplex.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Random;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.networksimplex.DynamicOneToAllShortestPath;
import org.graphstream.algorithm.test.TestDijkstra;
import org.graphstream.graph.BreadthFirstIterator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class TestDynamicOneToAllShortestPath {

	@Test
	public void toyTest() {
		Graph g = TestDijkstra.toyGraph();

		DynamicOneToAllShortestPath d = new DynamicOneToAllShortestPath(
				"length");
		d.init(g);
		Node source = g.getNode("A");

		d.setSource(source.getId());

		// check the source node
		assertEquals(d.getSource(), source.getId());

		d.compute();

		// check parent access methods
		assertNull(d.getParent(source));
		assertNull(d.getEdgeFromParent(source));
		assertEquals(source, d.getParent(g.getNode("D")));
		assertEquals(g.getEdge("AD"), d.getEdgeFromParent(g.getNode("D")));
		assertNull(d.getParent(g.getNode("G")));
		assertNull(d.getEdgeFromParent(g.getNode("G")));

		// check path lengths
		assertEquals(0, d.getPathLength(g.getNode("A")));
		assertEquals(11, d.getPathLength(g.getNode("B")));
		assertEquals(9, d.getPathLength(g.getNode("C")));
		assertEquals(7, d.getPathLength(g.getNode("D")));
		assertEquals(20, d.getPathLength(g.getNode("E")));
		assertEquals(20, d.getPathLength(g.getNode("F")));
		assertEquals(Long.MAX_VALUE, d.getPathLength(g.getNode("G")));

		// check nodes on path A->E
		String[] nodesAE = { "E", "B", "C", "A" };
		int i = 0;
		for (Node n : d.getPathNodes(g.getNode("E"))) {
			assertEquals(nodesAE[i], n.getId());
			i++;
		}
		assertEquals(4, i);

		// check edges on path A->F
		String[] edgesAF = { "CF", "AC" };
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
	}

	@Test
	public void compareWithBFS() {
		Random rnd = new Random(123456);
		Generator gen = new DorogovtsevMendesGenerator(rnd);
		Graph g = new SingleGraph("test");
		gen.addSink(g);
		gen.begin();
		Node source = g.getNode(0);

		DynamicOneToAllShortestPath d = new DynamicOneToAllShortestPath(null);
		d.setSource(source.getId());
		d.init(g);
		d.compute();

		// Add a node and recompute. Compare with the results obtained by
		// breadth-first search
		for (int i = 3; i <= 100; i++) {
			gen.nextEvents();
			d.compute();
			BreadthFirstIterator bfs = (BreadthFirstIterator) source
					.getBreadthFirstIterator();
			while (bfs.hasNext())
				bfs.next();
			for (Node n : g)
				assertEquals(bfs.getDepthOf(n), d.getPathLength(n));
		}
		
		// Now start removing nodes and check again
		for (int i = 100; i >= 3; i--) {
			g.removeNode(i);
			d.compute();
			BreadthFirstIterator bfs = (BreadthFirstIterator) source
					.getBreadthFirstIterator();
			while (bfs.hasNext())
				bfs.next();
			for (Node n : g)
				assertEquals(bfs.getDepthOf(n), d.getPathLength(n));
		}
	}
	
	@Test
	public void compareWithDijkstra() {
		Random rnd = new Random(123456);
		Generator gen = new DorogovtsevMendesGenerator(rnd);
		Graph g = new SingleGraph("test");
		gen.addSink(g);
		gen.begin();
		Node source = g.getNode(0);
		g.getEdge(0).setAttribute("length", 10 + rnd.nextInt(91));
		g.getEdge(1).setAttribute("length", 10 + rnd.nextInt(91));
		g.getEdge(2).setAttribute("length", 10 + rnd.nextInt(91));

		DynamicOneToAllShortestPath dsp = new DynamicOneToAllShortestPath("length");
		dsp.setSource(source.getId());
		dsp.init(g);
		dsp.compute();
		
		Dijkstra d = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
		d.setSource(source);
		d.init(g);
		d.compute();
		
		// Add node and recompute. Compare the results with Dijkstra
		for (int i = 3; i <= 100; i++) {
			gen.nextEvents();
			g.getEdge(g.getEdgeCount() - 1).setAttribute("length", 10 + rnd.nextInt(91));
			g.getEdge(g.getEdgeCount() - 2).setAttribute("length", 10 + rnd.nextInt(91));
			dsp.compute();
			d.compute();
			for (Node n : g)
				assertEquals(d.getPathLength(n), dsp.getPathLength(n), 0);
		}
		
		// Now change the costs of edge and recompute. Compare with Dijkstra
		for (int i = 0; i < g.getEdgeCount(); i++) {
			g.getEdge(i).setAttribute("length", 10 + rnd.nextInt(91));
			dsp.compute();
			d.compute();
			for (Node n : g)
				assertEquals(d.getPathLength(n), dsp.getPathLength(n), 0);			
		}
		
		// Remove a node and recompute. Compare with Dijkstra
		for (int i = 100; i >= 3; i--) {
			g.removeNode(i);
			dsp.compute();
			d.compute();
			for (Node n : g)
				assertEquals(d.getPathLength(n), dsp.getPathLength(n), 0);						
		}
	}
}
