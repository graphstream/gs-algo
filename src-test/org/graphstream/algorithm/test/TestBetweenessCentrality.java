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
 * @since 2011-05-12
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.graphstream.algorithm.BetweennessCentrality;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Test;

public class TestBetweenessCentrality {

	/**
	 * Quick manual test of the centrality algorithm.
	 */
	public static void main(String args[]) {
		Graph graph = new SingleGraph("Betweeness Centrality");

		BetweennessCentrality bcb = new BetweennessCentrality();

		buildGraph2(graph, bcb);

		bcb.setWeightAttributeName("weight");
//		for(Edge edge:graph.getEachEdge()) {edge.setAttribute("weight",1.0);}
		bcb.betweennessCentrality(graph);

		
		System.out.printf("Cb(%s) = %f%n", graph.getNode("A").getId(), bcb.centrality(graph.getNode("A")));
		System.out.printf("Cb(%s) = %f%n", graph.getNode("B").getId(), bcb.centrality(graph.getNode("B")));
		System.out.printf("Cb(%s) = %f%n", graph.getNode("C").getId(), bcb.centrality(graph.getNode("C")));
		System.out.printf("Cb(%s) = %f%n", graph.getNode("D").getId(), bcb.centrality(graph.getNode("D")));
//		System.out.printf("Cb(%s) = %f%n", graph.getNode("E").getId(), bcb.centrality(graph.getNode("E")));
	}

	@Test
	public void test1() {
		Graph graph = new SingleGraph("Betweeness Centrality Test 1");
		BetweennessCentrality bcb = new BetweennessCentrality(/* unweighted */);
		buildGraph1(graph, bcb);
		bcb.init(graph);
		bcb.compute();
		assertEquals(1.0, (Double) graph.getNode("A").getAttribute("Cb"), 0.0);
		assertEquals(1.0, (Double) graph.getNode("B").getAttribute("Cb"), 0.0);
		assertEquals(3.0, (Double) graph.getNode("C").getAttribute("Cb"), 0.0);
		assertEquals(3.0, (Double) graph.getNode("D").getAttribute("Cb"), 0.0);
		assertEquals(1.0, (Double) graph.getNode("E").getAttribute("Cb"), 0.0);
		assertEquals(3.0, (Double) graph.getNode("F").getAttribute("Cb"), 0.0);
	}

	@Test
	public void test1b() {
		Graph graph = new SingleGraph("Betweeness Centrality Test 1 (b)");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph1b(graph, bcb);
		bcb.setWeightAttributeName("weight");
		bcb.init(graph);
		bcb.compute();
		assertEquals(1.0, (Double) graph.getNode("A").getAttribute("Cb"), 0.0);
		assertEquals(1.0, (Double) graph.getNode("B").getAttribute("Cb"), 0.0);
		assertEquals(3.0, (Double) graph.getNode("C").getAttribute("Cb"), 0.0);
		assertEquals(3.0, (Double) graph.getNode("D").getAttribute("Cb"), 0.0);
		assertEquals(1.0, (Double) graph.getNode("E").getAttribute("Cb"), 0.0);
		assertEquals(3.0, (Double) graph.getNode("F").getAttribute("Cb"), 0.0);
	}

	@Test
	public void test2() {
		Graph graph = new SingleGraph("Betweeness Centrality Test 2");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph2(graph, bcb);
		bcb.setWeightAttributeName("weight");
		bcb.init(graph);
		bcb.compute();
		assertEquals(4.0, (Double) graph.getNode("A").getAttribute("Cb"), 0.0);
		assertEquals(0.0, (Double) graph.getNode("B").getAttribute("Cb"), 0.0);
		assertEquals(0.0, (Double) graph.getNode("C").getAttribute("Cb"), 0.0);
		assertEquals(4.0, (Double) graph.getNode("D").getAttribute("Cb"), 0.0);
	}

	protected void testIfWeightedAndUnweightedAreEqual(Graph graph1, Graph graph2, BetweennessCentrality bcb) {
		graph1.edges().forEach(edge -> edge.setAttribute("weight", 1));
		graph2.edges().forEach(edge -> edge.setAttribute("weight", 1));
		bcb.setUnweighted();
		bcb.init(graph1);
		bcb.compute();
		bcb.setWeightAttributeName("weight");
		bcb.init(graph2);
		bcb.compute();

		for(Node node: graph1) {
			assertEquals((Double)node.getAttribute("Cb"), (Double)graph2.getNode(node.getId()).getAttribute("Cb"),0);
		}
	}

	@Test
	public void test2b() {
		Graph graph1 = new SingleGraph("Betweenness Centrality Test 2 (b) 1");
		Graph graph2 = new SingleGraph("Betweenness Centrality Test 2 (b) 2");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph2(graph1, bcb);
		buildGraph2(graph2, bcb);
		testIfWeightedAndUnweightedAreEqual(graph1, graph2, bcb);
	}
	
	@Test
	public void test3() {
		Graph graph = new SingleGraph("Betweeness Centrality Test 3");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph3(graph, bcb);
		bcb.setWeightAttributeName("weight");
		bcb.init(graph);
		bcb.compute();
		assertEquals(6.0, (Double) graph.getNode("A").getAttribute("Cb"), 0.0);
		assertEquals(0.0, (Double) graph.getNode("B").getAttribute("Cb"), 0.0);
		assertEquals(6.0, (Double) graph.getNode("C").getAttribute("Cb"), 0.0);
		assertEquals(8.0, (Double) graph.getNode("D").getAttribute("Cb"), 0.0);
		assertEquals(0.0, (Double) graph.getNode("E").getAttribute("Cb"), 0.0);
	}

	@Test
	public void test3b() {
		Graph graph1 = new SingleGraph("Betweenness Centrality Test 3 (b) 1");
		Graph graph2 = new SingleGraph("Betweenness Centrality Test 3 (b) 2");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph3(graph1, bcb);
		buildGraph3(graph2, bcb);
		testIfWeightedAndUnweightedAreEqual(graph1, graph2, bcb);
	}

	@Test
	public void test4() {
		Graph graph = new SingleGraph("Betweeness Centrality Test 4");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph4(graph, bcb);
		bcb.setWeightAttributeName("weight");
		bcb.init(graph);
		bcb.compute();
		assertEquals(0.0, (Double) graph.getNode("0").getAttribute("Cb"), 0.0);
		assertEquals(4.0, (Double) graph.getNode("1").getAttribute("Cb"), 0.0);
		assertEquals(3.0, (Double) graph.getNode("2").getAttribute("Cb"), 0.0);
		assertEquals(1.0, (Double) graph.getNode("3").getAttribute("Cb"), 0.0);
		assertEquals(1.0, (Double) graph.getNode("4").getAttribute("Cb"), 0.0);
	}
	
	@Test
	public void test4b() {
		Graph graph1 = new SingleGraph("Betweenness Centrality Test 4 (b) 1");
		Graph graph2 = new SingleGraph("Betweenness Centrality Test 4 (b) 2");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph4(graph1, bcb);
		buildGraph4(graph2, bcb);
		testIfWeightedAndUnweightedAreEqual(graph1, graph2, bcb);
	}
	
	@Test
	public void test5() {
		Graph graph = new SingleGraph("Betweeness Centrality Test 5");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph5(graph, bcb);
		bcb.setWeightAttributeName("weight");
		bcb.init(graph);
		bcb.compute();
		assertEquals(0.0,    (Double) graph.getNode("A").getAttribute("Cb"), 0.0);
		assertEquals(0.0,    (Double) graph.getNode("B").getAttribute("Cb"), 0.0);
		assertEquals(0.0,    (Double) graph.getNode("C").getAttribute("Cb"), 0.0);
		assertEquals(8.3333, (Double) graph.getNode("D").getAttribute("Cb"), 0.01);
		assertEquals(2.6666, (Double) graph.getNode("E").getAttribute("Cb"), 0.01);
	}

	@Test
	public void test5b() {
		Graph graph1 = new SingleGraph("Betweenness Centrality Test 5 (b) 1");
		Graph graph2 = new SingleGraph("Betweenness Centrality Test 5 (b) 2");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph5(graph1, bcb);
		buildGraph5(graph2, bcb);
		testIfWeightedAndUnweightedAreEqual(graph1, graph2, bcb );
	}
	
	@Test
	public void test6() {
		Graph graph = new SingleGraph("Betweeness Centrality Test 6");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph6(graph, bcb);
		bcb.setWeightAttributeName("weight");
		bcb.init(graph);
		bcb.compute();
		assertEquals(4, (Double) graph.getNode("A").getAttribute("Cb"), 0);
		assertEquals(2, (Double) graph.getNode("B").getAttribute("Cb"), 0);
		assertEquals(0, (Double) graph.getNode("C").getAttribute("Cb"), 0);
		assertEquals(2, (Double) graph.getNode("D").getAttribute("Cb"), 0);
		assertEquals(4, (Double) graph.getNode("E").getAttribute("Cb"), 0);
		assertEquals(8, (Double) graph.getEdge("AB").getAttribute("Cb"), 0);
		assertEquals(8, (Double) graph.getEdge("AE").getAttribute("Cb"), 0);
		assertEquals(0, (Double) graph.getEdge("BE").getAttribute("Cb"), 0);
		assertEquals(4, (Double) graph.getEdge("BC").getAttribute("Cb"), 0);
		assertEquals(4, (Double) graph.getEdge("CD").getAttribute("Cb"), 0);
		assertEquals(8, (Double) graph.getEdge("ED").getAttribute("Cb"), 0);
	}	

	@Test
	public void test6b() {
		Graph graph1 = new SingleGraph("Betweenness Centrality Test 6 (b) 1");
		Graph graph2 = new SingleGraph("Betweenness Centrality Test 6 (b) 2");
		BetweennessCentrality bcb = new BetweennessCentrality();
		buildGraph6(graph1, bcb);
		buildGraph6(graph2, bcb);
		testIfWeightedAndUnweightedAreEqual(graph1, graph2, bcb );
	}

	protected static void buildGraph1(Graph graph, BetweennessCentrality bcb) {
		//
		// Unweighted graph:
		//
		//     F---E     Cb(A) = 1
		//    /|    \    Cb(B) = 1
		//   / |     \   Cb(C) = 3
		//  /  |      \  Cb(D) = 3
		// A---C-------D Cb(E) = 1
		//  \  |     _/  Cb(F) = 3
		//   \ |  __/
		//    \|_/
		//     B

		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");
		Node E = graph.addNode("E");
		Node F = graph.addNode("F");

		graph.addEdge("AB", "A", "B");
		graph.addEdge("AC", "A", "C");
		graph.addEdge("AF", "A", "F");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("FC", "F", "C");
		graph.addEdge("CD", "C", "D");
		graph.addEdge("FE", "F", "E");
		graph.addEdge("ED", "E", "D");
		graph.addEdge("BD", "B", "D");

		A.setAttribute("xyz", -1, 0);
		A.setAttribute("ui.label", "A");
		B.setAttribute("xyz", 0, -1);
		B.setAttribute("ui.label", "B");
		C.setAttribute("xyz", 0, 0);
		C.setAttribute("ui.label", "C");
		D.setAttribute("xyz", 2, 0);
		D.setAttribute("ui.label", "D");
		E.setAttribute("xyz", 1, .7);
		E.setAttribute("ui.label", "E");
		F.setAttribute("xyz", 0, 1);
		F.setAttribute("ui.label", "F");
	}

	protected static void buildGraph1b(Graph graph, BetweennessCentrality bcb) {
		//
		// Weighted graph with all edges at weight 1, should give teh same
		// result as test1.
		//
		//     F---E     Cb(A) = 1
		//    /|    \    Cb(B) = 1
		//   / |     \   Cb(C) = 3
		//  /  |      \  Cb(D) = 3
		// A---C-------D Cb(E) = 1
		//  \  |     _/  Cb(F) = 3
		//   \ |  __/
		//    \|_/
		//     B

		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");
		Node E = graph.addNode("E");
		Node F = graph.addNode("F");

		graph.addEdge("AB", "A", "B");
		graph.addEdge("AC", "A", "C");
		graph.addEdge("AF", "A", "F");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("FC", "F", "C");
		graph.addEdge("CD", "C", "D");
		graph.addEdge("FE", "F", "E");
		graph.addEdge("ED", "E", "D");
		graph.addEdge("BD", "B", "D");

		bcb.setWeight(A, B, 1.0);
		bcb.setWeight(A, C, 1.0);
		bcb.setWeight(A, F, 1.0);
		bcb.setWeight(B, C, 1.0);
		bcb.setWeight(F, C, 1.0);
		bcb.setWeight(C, D, 1.0);
		bcb.setWeight(F, E, 1.0);
		bcb.setWeight(E, D, 1.0);
		bcb.setWeight(B, D, 1.0);
		
		A.setAttribute("xyz", -1, 0);
		A.setAttribute("ui.label", "A");
		B.setAttribute("xyz", 0, -1);
		B.setAttribute("ui.label", "B");
		C.setAttribute("xyz", 0, 0);
		C.setAttribute("ui.label", "C");
		D.setAttribute("xyz", 2, 0);
		D.setAttribute("ui.label", "D");
		E.setAttribute("xyz", 1, .7);
		E.setAttribute("ui.label", "E");
		F.setAttribute("xyz", 0, 1);
		F.setAttribute("ui.label", "F");
	}

	protected static void buildGraph2(Graph graph, BetweennessCentrality bcb) {
		//
		// Weighted graph (edge BC=10, others=1):
		//
		//    B     Cb(A) = 4
		//   / \10  Cb(B) = 0
		//  /   \   Cb(C) = 0
		// A     C  Cb(D) = 4
		//  \   /
		//   \ /
		//    D

		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");

		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CD", "C", "D");
		graph.addEdge("DA", "D", "A");

		A.setAttribute("xyz", -1, 0);
		A.setAttribute("ui.label", "A");
		B.setAttribute("xyz", 0, 1);
		B.setAttribute("ui.label", "B");
		C.setAttribute("xyz", 1, 0);
		C.setAttribute("ui.label", "C");
		D.setAttribute("xyz", 0, -1);
		D.setAttribute("ui.label", "D");

		bcb.setWeight(B, C, 10f);
	}

	protected static void buildGraph3(Graph graph, BetweennessCentrality bcb) {
		//
		// Weighted graph (edge BC=10, others=1):
		//
		//    B     Cb(A) = 6   AB=1,  AE=10, AD=1
		//   /|\    Cb(B) = 0   BC=10, BE=10
		//  / | \   Cb(C) = 6   CD=1,  CE=1
		// A--E--C  Cb(D) = 8   DE=10
		//  \ | /   Cb(E) = 0
		//   \|/
		//    D
		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");
		Node E = graph.addNode("E");

		graph.addEdge("AB", "A", "B");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("CD", "C", "D");
		graph.addEdge("DA", "D", "A");

		graph.addEdge("AE", "A", "E");
		graph.addEdge("BE", "B", "E");
		graph.addEdge("CE", "C", "E");
		graph.addEdge("DE", "D", "E");

		A.setAttribute("xyz", -1, 0);
		A.setAttribute("ui.label", "A");
		B.setAttribute("xyz", 0, 1);
		B.setAttribute("ui.label", "B");
		C.setAttribute("xyz", 1, 0);
		C.setAttribute("ui.label", "C");
		D.setAttribute("xyz", 0, -1);
		D.setAttribute("ui.label", "D");
		E.setAttribute("xyz", 0, 0);
		E.setAttribute("ui.label", "E");

		bcb.setWeight(B, C, 10f);
		bcb.setWeight(A, E, 10f);
		bcb.setWeight(E, D, 10f);
		bcb.setWeight(B, E, 10f);
	}
    
    protected static void buildGraph4(Graph graph, BetweennessCentrality bcb) {
    	//    1    Cb(0) = 0    0-1 = 1, 0-3 = 1
    	//   /|\   Cb(1) = 4    1-2 = 0.5, 1-3 = 1
    	//  / | \  Cb(2) = 3    2-4 = 0.5
    	// 0  |  2 Cb(3) = 1    3-4 = 1
    	//  \ |  | Cb(4) = 1
    	//   \|  | 
    	//    3--4
    	
    	Node N0= graph.addNode("0");
    	Node N1= graph.addNode("1");
    	Node N2= graph.addNode("2");
    	Node N3= graph.addNode("3");
    	Node N4= graph.addNode("4");

    	graph.addEdge("0_1","0","1");
    	graph.addEdge("0_3","0","3");
    	graph.addEdge("1_3","1","3");
    	graph.addEdge("1_2","1","2");
    	graph.addEdge("3_4","3","4");
    	graph.addEdge("4_2","4","2");
    	
    	bcb.setWeight(N0,N1,1);
    	bcb.setWeight(N0,N3,1);
    	bcb.setWeight(N1,N3,1);
    	bcb.setWeight(N1,N2,0.5);
    	bcb.setWeight(N3,N4,1);
    	bcb.setWeight(N4,N2,0.5);
    //	try { graph.write( "test.gml" ); } catch(Exception e){}
   }

	protected static void buildGraph5(Graph graph, BetweennessCentrality bcb) {
		//    B---     A-B = 10, A-C = 3, A-D = 1
		//   /|\  \    B-C = 6,  B-D = 4, B-E = 3
		//  / | \  \   C-D = 2,  C-E = 10
		// A--+--\--D  D-E = 1
		//  \ | __\/|  This graph allows mutliple shortest paths between several nodes. 
		//   \|/   \|  Cb(A) = Cb(B) = Cb(C) = 0
		//    C-----E  Cb(D) = 8.3333, Cb(E) = 2.6666
		
		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");
		Node E = graph.addNode("E");

		graph.addEdge("AB", "A", "B");
		graph.addEdge("AC", "A", "C");
		graph.addEdge("AD", "A", "D");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("BD", "B", "D");
		graph.addEdge("CD", "C", "D");
		graph.addEdge("EC", "E", "C");
		graph.addEdge("EB", "E", "B");
		graph.addEdge("ED", "E", "D");

		bcb.setWeight(A, B, 10);
		bcb.setWeight(A, C, 3);
		bcb.setWeight(A, D, 1);
		bcb.setWeight(B, C, 6);
		bcb.setWeight(B, D, 4);
		bcb.setWeight(C, D, 2);
		bcb.setWeight(E, C, 10);
		bcb.setWeight(E, B, 3);
		bcb.setWeight(E, D, 1);
	}
	
	protected static void buildGraph6(Graph graph, BetweennessCentrality bcb) {

		//    E----D  AB=1, BC=5, CD=3, DE=2, BE=6, EA=4  
		//   /|    |  Cb(A)=4 (NetworkX finds 3.5, by hand I find 4).
		//  / |    |  Cb(B)=2
		// A  |    |  Cb(C)=0
		//  \ |    |  Cb(D)=2
		//   \|    |  Cb(E)=4 (NetworkX finds 3.5, by hand I find 4).
		//    B----C
		
		Node A = graph.addNode("A");
		Node B = graph.addNode("B");
		Node E = graph.addNode("E");
		Node C = graph.addNode("C");
		Node D = graph.addNode("D");

		graph.addEdge("AB", "A", "B");
		graph.addEdge("BE", "B", "E");
		graph.addEdge("BC", "B", "C");
		graph.addEdge("ED", "E", "D");
		graph.addEdge("CD", "C", "D");
		graph.addEdge("AE", "A", "E");
		
		bcb.setWeight(A, B, 1);
		bcb.setWeight(B, E, 6);
		bcb.setWeight(B, C, 5);
		bcb.setWeight(E, D, 2);
		bcb.setWeight(C, D, 3);
		bcb.setWeight(A, E, 4);
	}

	protected static String mkString(Collection<Node> set) {
		int n = set.size();
		StringBuffer buf = new StringBuffer();

		for (Node node : set) {
			buf.append(node.getId());
			if (n > 1)
				buf.append(", ");
			n--;
		}

		return buf.toString();
	}

	protected static String styleSheet = "graph {" + "	padding: 60px;" + "}"
			+ "node {" + "	text-color: black;"
			+ "	text-background-mode: plain;"
			+ "	text-background-color: white;" + "}";
}