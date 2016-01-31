/*
 * Copyright 2006 - 2016
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.algorithm.test;

import java.io.*;
import java.util.Iterator;

import org.graphstream.algorithm.*;
import org.graphstream.graph.*;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.*;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Test the APSP algorithm.
 */
public class TestAPSP {
	public static void main(String args[]) {
		try {
			TestAPSP t = new TestAPSP();
			t.init(args);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public TestAPSP() {

	}

	public void init(String args[]) throws IOException, GraphParseException {
		String filename = null;

		if (args.length > 0)
			filename = args[0];

		Graph G = new SingleGraph("", false, true);

		if (filename == null)
			buildGraph1(G);
		else
			G.read(filename);

		APSP apsp = new APSP(G, "weight", true);

		apsp.compute();

		Iterator<? extends Node> nodes = G.getNodeIterator();

		while (nodes.hasNext()) {
			Node node = nodes.next();

			printNode(node);
			/*
			 * float Dij =
			 * ((APSP.APSPInfo)node.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME
			 * )).getLengthTo( "D" );
			 * 
			 * if( Dij >= 0 ) node.addAttribute( "label",
			 * node.getId()+" -("+Dij+")-> D" );
			 */

			node.addAttribute("label", node.getId());
		}

		if (G.getNode("A") != null && G.getNode("E") != null) {
			APSP.APSPInfo info = G.getNode("A").getAttribute(
					APSP.APSPInfo.ATTRIBUTE_NAME);
			Path path = info.getShortestPathTo("E");

			System.out.printf("Path A -> E:%n    ");
			for (Node node : path.getNodePath())
				System.err.printf(" -> %s", node.getId());
			System.out.printf("%n");
		}

		G.display();
	}

	@Test
	public void Test1() {
		Graph G = new SingleGraph("Test APSP 1", false, true);

		buildGraph1(G);

		APSP apsp = new APSP(G, "weight", true);

		apsp.compute();

		Node A = G.getNode("A");
		Node B = G.getNode("B");
		Node C = G.getNode("C");
		Node D = G.getNode("D");
		Node E = G.getNode("E");

		APSP.APSPInfo info = A.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
		Path path = info.getShortestPathTo("E");
		Object npath[] = path.getNodePath().toArray();
		Object npath1[] = { A, B, C, D, E };

		assertEquals(5, path.getNodeCount(), 0);
		assertArrayEquals(npath1, npath);

		assertEquals(0.5, info.getLengthTo("B"), 0);
		assertEquals(1.0, info.getLengthTo("C"), 0);
		assertEquals(1.5, info.getLengthTo("D"), 0);
		assertEquals(2.0, info.getLengthTo("E"), 0);

		path = info.getShortestPathTo("C");
		npath = path.getNodePath().toArray();
		Object npath2[] = { A, B, C };

		assertEquals(3, path.getNodeCount(), 0);
		assertArrayEquals(npath2, npath);

		info = E.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
		path = info.getShortestPathTo("C");
		npath = path.getNodePath().toArray();
		Object npath3[] = { E, D, B, C };

		assertEquals(4, path.getNodeCount(), 0);
		assertArrayEquals(npath3, npath);

		assertEquals(2.0, info.getLengthTo("A"), 0);
		assertEquals(1.0, info.getLengthTo("B"), 0);
		assertEquals(1.5, info.getLengthTo("C"), 0);
		assertEquals(0.5, info.getLengthTo("D"), 0);
	}

	@Test
	public void Test2() {
		Graph G = new SingleGraph("Test APSP 2", false, true);

		buildGraph2(G);

		APSP apsp = new APSP(G, "weight", true);

		apsp.compute();

		Node A = G.getNode("A");
		Node B = G.getNode("B");
		Node C = G.getNode("C");
		Node D = G.getNode("D");

		APSP.APSPInfo info = A.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
		Path path = info.getShortestPathTo("C");
		Object npath[] = path.getNodePath().toArray();
		Object npath1[] = { A, D, C };

		assertEquals(3, path.getNodeCount(), 0);
		assertArrayEquals(npath, npath1);

		assertEquals(1.0, info.getLengthTo("B"), 0);
		assertEquals(2.0, info.getLengthTo("C"), 0);
		assertEquals(1.0, info.getLengthTo("D"), 0);

		info = B.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
		path = info.getShortestPathTo("C");
		npath = path.getNodePath().toArray();
		Object npath2[] = { B, A, D, C };

		assertEquals(4, path.getNodeCount(), 0);
		assertArrayEquals(npath, npath2);

		assertEquals(1.0, info.getLengthTo("A"), 0);
		assertEquals(3.0, info.getLengthTo("C"), 0);
		assertEquals(2.0, info.getLengthTo("D"), 0);
	}

	@Test
	@SuppressWarnings("unused")
	public void Test3() {
		Graph G = new SingleGraph("Test APSP 3", false, true);

		buildGraph3(G);

		APSP apsp = new APSP(G, "weight", true);

		apsp.compute();

		Node A = G.getNode("A");
		Node B = G.getNode("B");
		Node C = G.getNode("C");
		Node D = G.getNode("D");
		Node E = G.getNode("E");

		APSP.APSPInfo info = A.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);
		Path path = info.getShortestPathTo("B");
		Object npath[] = path.getNodePath().toArray();
		Object npath1[] = { A, D, B }; // ? Or A, D, E, B ? There are two paths.

		assertEquals(3, path.getNodeCount(), 0);
		assertArrayEquals(npath, npath1);

		assertEquals(5.0, info.getLengthTo("B"), 0);
		assertEquals(3.0, info.getLengthTo("C"), 0);
		assertEquals(1.0, info.getLengthTo("D"), 0);
		assertEquals(2.0, info.getLengthTo("E"), 0);

		path = info.getShortestPathTo("C");
		npath = path.getNodePath().toArray();
		Object npath2[] = { A, C };

		assertEquals(2, path.getNodeCount(), 0);
		assertArrayEquals(npath, npath2);
	}

	protected void buildGraph1(Graph G) {
		//
		// +--0.5-->B<--0.5--+
		// | | |
		// 0.5 0.5 0.5
		// | v |
		// A<--0.5--C--0.5-->D--0.5--E
		//

		Edge AB = G.addEdge("AB", "A", "B", true);
		Edge AC = G.addEdge("AC", "C", "A", true);
		Edge BC = G.addEdge("BC", "B", "C", true);
		Edge BD = G.addEdge("BD", "D", "B", true);
		Edge CD = G.addEdge("CD", "C", "D", true);
		Edge DE = G.addEdge("DE", "D", "E", false);

		AB.addAttribute("weight", 0.5f);
		AB.addAttribute("label", "0.5");
		AC.addAttribute("weight", 0.5f);
		AC.addAttribute("label", "0.5");
		BC.addAttribute("weight", 0.5f);
		BC.addAttribute("label", "0.5");
		BD.addAttribute("weight", 0.5f);
		BD.addAttribute("label", "0.5");
		CD.addAttribute("weight", 0.5f);
		CD.addAttribute("label", "0.5");
		DE.addAttribute("weight", 0.5f);
		DE.addAttribute("label", "0.5");
	}

	protected static void buildGraph2(Graph graph) {
		//
		// Weighted graph (edge BC=10, others=1):
		//
		// B
		// / \10
		// / \
		// A C
		// \ /
		// \ /
		// D

		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");

		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CD", "C", "D");
		graph.addEdge("DA", "D", "A");

		A.addAttribute("xyz", -1, 0);
		A.addAttribute("ui.label", "A");
		B.addAttribute("xyz", 0, 1);
		B.addAttribute("ui.label", "B");
		C.addAttribute("xyz", 1, 0);
		C.addAttribute("ui.label", "C");
		D.addAttribute("xyz", 0, -1);
		D.addAttribute("ui.label", "D");

		graph.getEdge("BC").setAttribute("weight", 10.0);
	}

	@SuppressWarnings("unused")
	protected static void buildGraph3(Graph graph) {
		// B--- A-B = 10, A-C = 3, A-D = 1
		// /|\ \ B-C = 6, B-D = 4, B-E = 3
		// / | \ \ C-D = 2, C-E = 10
		// A--+--\--D D-E = 1
		// \ | __\/| This graph allows mutliple shortest paths between several
		// nodes.
		// \|/ \|
		// C-----E

		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");
		Node E = graph.addNode("E");

		Edge AB = graph.addEdge("AB", "A", "B");
		Edge AC = graph.addEdge("AC", "A", "C");
		Edge AD = graph.addEdge("AD", "A", "D");
		Edge BC = graph.addEdge("BC", "B", "C");
		Edge BD = graph.addEdge("BD", "B", "D");
		Edge CD = graph.addEdge("CD", "C", "D");
		Edge EC = graph.addEdge("EC", "E", "C");
		Edge EB = graph.addEdge("EB", "E", "B");
		Edge ED = graph.addEdge("ED", "E", "D");

		AB.setAttribute("weight", 10);
		AC.setAttribute("weight", 3);
		AD.setAttribute("weight", 1);
		BC.setAttribute("weight", 6);
		BD.setAttribute("weight", 4);
		CD.setAttribute("weight", 2);
		EC.setAttribute("weight", 10);
		EB.setAttribute("weight", 3);
		ED.setAttribute("weight", 1);
	}

	protected void printNode(Node node) {
		APSP.APSPInfo info = (APSP.APSPInfo) node
				.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME);

		if (info == null)
			throw new RuntimeException("Node " + node.getId()
					+ " has no APSP info!!");

		System.out.printf("%s:%n", node.getId());

		for (String other : info.targets.keySet()) {
			double Dij = info.targets.get(other).distance;
			System.out.printf("    -> %s = %4.3f%n", other, Dij);
		}
	}
}