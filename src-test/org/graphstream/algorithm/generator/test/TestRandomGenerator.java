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
 * @since 2012-04-20
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.junit.Test;

public class TestRandomGenerator {
	/**
	 * Number of nodes 
	 */
	public static final int N = 10000;
	
	/**
	 * Average degree
	 */
	public static final double K = 49.8765;
	
	@Test
	public void test() {
		Graph g = new AdjacencyListGraph("test");
		Generator gen = new RandomGenerator(K);
		gen.addSink(g);
		
		gen.begin();
		while (g.getNodeCount() < N) {
			boolean next = gen.nextEvents();
			assertTrue(next);
		}
		gen.end();
		gen.removeSink(g);
		
		// check if the average degree is k
		assertEquals(K, Toolkit.averageDegree(g), 0.01 * K);
		
		int[] ddR = Toolkit.degreeDistribution(g);
		
		// generate Erdos-Renyi graph directly
		g.clear();
		double p = K / (N - 1);
		Random rnd = new Random();
		for (int i = 0; i < N; i++)
			g.addNode(i + "");
		for (int i = 0; i < N; i++)
			for (int j = i + 1; j < N; j++)
				if (rnd.nextDouble() < p)
					g.addEdge(i + "_" + j, i, j);
		int[] ddE1 = Toolkit.degreeDistribution(g);
		
		// generate another Erdos-Renyi graph more intelligently
		// this will also test Toolkit.randomNodeSet
		g.clear();
		g.addNode("0");
		for (int i = 1; i < N; i++) {
			List<Node> l = Toolkit.randomNodeSet(g, p);
			Node newNode = g.addNode(i + "");
			for (Node oldNode : l)
				g.addEdge(oldNode.getId() + "_" + newNode.getId(), oldNode, newNode);
		}
		int[] ddE2 = Toolkit.degreeDistribution(g);
		
		// compare the distributions
		compareDD(ddR, ddE1);
		compareDD(ddR, ddE2);
		compareDD(ddE1, ddE2);
		
		// finally, generate a graph without edge removal and check only the average degree
		g.clear();
		gen = new RandomGenerator(K, false);
		gen.addSink(g);
		
		gen.begin();
		while (g.getNodeCount() < N) {
			boolean next = gen.nextEvents();
			assertTrue(next);
		}
		gen.end();
		gen.removeSink(g);

		assertEquals(K, Toolkit.averageDegree(g), 0.02 * K);
	}
	
	public void compareDD(int[] dd1, int[] dd2) {
		int dMax = Math.max(dd1.length, dd2.length);
		for (int d = 0; d < dMax; d++) {
			int d1 = d < dd1.length ? dd1[d] : 0;
			int d2 = d < dd2.length ? dd2[d] : 0;
			assertEquals(0, (d1 - d2 + 0.0) / N, 0.02);
		}
	}

}
