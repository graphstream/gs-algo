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
 * @since 2012-04-19
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.networksimplex.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.graphstream.algorithm.Dijkstra;
import org.graphstream.algorithm.networksimplex.DynamicOneToAllShortestPath;
import org.graphstream.algorithm.networksimplex.NetworkSimplex.PricingStrategy;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.junit.Ignore;

@Ignore
class Benchmark {
	private static final int LMIN = 10;
	private static final int LMAX = 100;

	private static final int N = 10000;
	private static final int M = 500000;
	private static final int EMAX = 100;

	private Random rnd;
	private Graph g;
	private Node source;
	
	private Dijkstra dijkstra;
	private DynamicOneToAllShortestPath dspF;
	private DynamicOneToAllShortestPath dspM;
	
	private List<TwoInts> eList;
	private int eIndex;
	
	private int randomLength() {
		return LMIN + rnd.nextInt(LMAX - LMIN + 1);
	}
	
	private void addEdge() {
		TwoInts t = eList.get(eIndex);
		g.addEdge(t.i + "-" + t.j, t.i, t.j).setAttribute("length", randomLength());
		eIndex++;
	}
	
	public Benchmark() {
		rnd = new Random(234567);
		g = new AdjacencyListGraph("test");

		for (int i = 0; i < N; i++)
			g.addNode("" + i);
		source = g.getNode(0);
		
		eList = new ArrayList<TwoInts>(N * (N - 1) / 2);
		for (int i = 0; i < N; i++)
			for (int j = i + i; j < N; j++)
				eList.add(new TwoInts(i, j));
		Collections.shuffle(eList, rnd);
		eIndex = 0;
		
		while (g.getEdgeCount() < M)
			addEdge();
		
		dijkstra = new Dijkstra(Dijkstra.Element.EDGE, null, "length");
		dijkstra.init(g);
		dijkstra.setSource(source);
		dijkstra.compute();
		
		dspF = new DynamicOneToAllShortestPath("length");
		dspF.setPricingStrategy(PricingStrategy.FIRST_NEGATIVE);
		dspF.setSource(source.getId());
		dspF.init(g);
		dspF.compute();
		
		dspM = new DynamicOneToAllShortestPath("length");
		dspM.setPricingStrategy(PricingStrategy.MOST_NEGATIVE);
		dspM.setSource(source.getId());
		dspM.init(g);
		dspM.compute();
	}

	public void addEdgeBenchmark(PrintStream ps) {
		long start, t1, t2, t3;
		ps.println("# Adding i edges\n# i m Tdijkstra Tnsfirst Tnsmost\n");

		for (int i = 0; i <= EMAX; i++) {
			for (int j = 0; j < i; j++) 
				addEdge();
			
			start = System.currentTimeMillis();
			dijkstra.compute();
			t1 = System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			dspF.compute();
			t2 = System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			dspM.compute();
			t3 = System.currentTimeMillis() - start;
			ps.printf("%10d%10d%10d%10d%10d%n", i, g.getEdgeCount(), t1, t2, t3);
		}
	}
	
	public void changeCostBenchmark(PrintStream ps) {
		long start, t1, t2, t3;
		ps.println("# Changing i costs\n# i m Tdijkstra Tnsfirst Tnsmost\n");

		for (int i = 0; i <= EMAX; i++) {
			for (int j = 0; j < i; j++)
				g.getEdge(rnd.nextInt(g.getEdgeCount())).setAttribute("length", randomLength());
			
			start = System.currentTimeMillis();
			dijkstra.compute();
			t1 = System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			dspF.compute();
			t2 = System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			dspM.compute();
			t3 = System.currentTimeMillis() - start;
			ps.printf("%10d%10d%10d%10d%10d%n", i, g.getEdgeCount(), t1, t2, t3);
		}		
	}
	
	public void removeEdgeBenchmark(PrintStream ps) {
		long start, t1, t2, t3;
		ps.println("# Removing i edges\n# i m Tdijkstra Tnsfirst Tnsmost\n");

		for (int i = 0; i <= EMAX; i++) {
			for (int j = 0; j < i; j++)
				g.removeEdge(rnd.nextInt(g.getEdgeCount()));
			
			start = System.currentTimeMillis();
			dijkstra.compute();
			t1 = System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			dspF.compute();
			t2 = System.currentTimeMillis() - start;

			start = System.currentTimeMillis();
			dspM.compute();
			t3 = System.currentTimeMillis() - start;
			ps.printf("%10d%10d%10d%10d%10d%n", i, g.getEdgeCount(), t1, t2, t3);
		}				
	}
	
	public static void main(String[] args) {
		Benchmark b = new Benchmark();
		
		PrintStream ps = null;		
		try {
			ps = new PrintStream(new File("add.dat"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		b.addEdgeBenchmark(ps);
		
		try {
			ps = new PrintStream(new File("change.dat"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		b.changeCostBenchmark(ps);

		try {
			ps = new PrintStream(new File("remove.dat"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		}
		b.removeEdgeBenchmark(ps);
	}
}


class TwoInts {
	int i, j;
	
	TwoInts(int i, int j) {
		this.i = i;
		this.j = j;
	}
}
