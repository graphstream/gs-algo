package org.graphstream.algorithm.test;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.List;

import org.graphstream.algorithm.BellmanFord;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Before;
import org.junit.Test;

/**
 * Small test of the BellmanFord algorithm.
 * 
 * @author Hicham Brahimi
 */
public class TestBellmanFord
{
	Graph graph;
	BellmanFord bellman ;
	
	@Before
	public void setUp() {
		
		graph = new SingleGraph("Test BellmanFord", false, true);
		
		Edge e = graph.addEdge( "AB", "B", "A", true);
		e.setAttribute("weight", 5.0);
		e = graph.addEdge( "BC", "B", "C", true);
		e.setAttribute("weight", 1.0);
		e = graph.addEdge( "CA", "C", "A", true);
		e.setAttribute("weight", 2.0);
		e = graph.addEdge( "BF", "F", "B", true);
		e.setAttribute("weight", 1.0);
		e = graph.addEdge( "CE", "E", "C", true);
		e.setAttribute("weight", 8.0);
		e = graph.addEdge( "EF", "E", "F", true);
		e.setAttribute("weight", 1.0);
		
		bellman = new BellmanFord("weight");
	}
	
	@Test
	public void testShortedPath() {		
		bellman.setSource("E");
		bellman.init(graph);
		bellman.compute();

		Node a = graph.getNode("A") ;
		Path p = bellman.getShortestPath(a);
		System.out.println(p);
		
		List<Edge> edges = p.getEdgePath();
		Iterator<? extends Edge> i = edges.iterator();
		
		Edge e = i.next();
		assertTrue(e != null);
		assertTrue(e.getId().equals("CA"));
		e = i.next();
		System.out.println(e);
		assertTrue(e != null);
		assertTrue(e.getId().equals("BC"));
		e = i.next();
		System.out.println(e);
		assertTrue(e != null);
		assertTrue(e.getId().equals("BF"));
		e = i.next();
		System.out.println(e);
		assertTrue(e != null);
		assertTrue(e.getId().equals("EF"));
		System.out.println(e);
		assertTrue(!i.hasNext());
		
	}
	
	@Test
	public void testShortedPathValue() {
		bellman.setSource("E");
		bellman.init(graph);
		bellman.compute();

		Node a = graph.getNode("A") ;
		double v = bellman.getShortestPathValue(a);
		
		assertTrue(v == 5);
	}
}
