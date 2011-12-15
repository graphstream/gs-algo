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
			BreadthFirstIterator<Node> bfs = (BreadthFirstIterator<Node>) source
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
			BreadthFirstIterator<Node> bfs = (BreadthFirstIterator<Node>) source
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
		g.getEdge(0).addAttribute("length", 10 + rnd.nextInt(91));
		g.getEdge(1).addAttribute("length", 10 + rnd.nextInt(91));
		g.getEdge(2).addAttribute("length", 10 + rnd.nextInt(91));

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
			g.getEdge(g.getEdgeCount() - 1).addAttribute("length", 10 + rnd.nextInt(91));
			g.getEdge(g.getEdgeCount() - 2).addAttribute("length", 10 + rnd.nextInt(91));
			dsp.compute();
			d.compute();
			for (Node n : g)
				assertEquals(d.getPathLength(n), dsp.getPathLength(n), 0);
		}
		
		// Now change the costs of edge and recompute. Compare with Dijkstra
		for (int i = 0; i < g.getEdgeCount(); i++) {
			g.getEdge(i).addAttribute("length", 10 + rnd.nextInt(91));
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
