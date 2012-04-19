package org.graphstream.algorithm.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.junit.Test;

public class TestRandomGenerator {
	
	@Test
	public void test() {
		// number of nodes
		int n = 10000;
		
		// average degree
		double k = 49.8765;
		
		Graph g = new AdjacencyListGraph("test");
		Generator gen = new RandomGenerator(k);
		gen.addSink(g);
		
		gen.begin();
		while (g.getNodeCount() < n) {
			boolean next = gen.nextEvents();
			assertTrue(next);
		}
		gen.end();
		gen.removeSink(g);
		
		// check if the average degree is k
		assertEquals(k, Toolkit.averageDegree(g), 0.01 * k);
		
		int[] ddR = Toolkit.degreeDistribution(g);
		
		// generate Erdos-Reni graph directly
		g.clear();
		double p = k / (n - 1);
		Random rnd = new Random();
		for (int i = 0; i < n; i++)
			g.addNode(i + "");
		for (int i = 0; i < n; i++)
			for (int j = i + 1; j < n; j++)
				if (rnd.nextDouble() < p)
					g.addEdge(i + "_" + j, i, j);
		int[] ddE = Toolkit.degreeDistribution(g);
		
		// compare the distributions
		int dMax = Math.max(ddR.length, ddE.length);
		for (int d = 0; d < dMax; d++) {
			int dR = d < ddR.length ? ddR[d] : 0;
			int dE = d < ddE.length ? ddE[d] : 0;
			assertEquals(0, (dR - dE + 0.0) / n, 0.01);
		}
	}

}
