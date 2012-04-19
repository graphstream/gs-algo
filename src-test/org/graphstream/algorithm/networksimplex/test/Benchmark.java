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
		g.addEdge(t.i + "-" + t.j, t.i, t.j).addAttribute("length", randomLength());
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
				g.getEdge(rnd.nextInt(g.getEdgeCount())).changeAttribute("length", randomLength());
			
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
