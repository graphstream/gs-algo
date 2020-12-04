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
 * @since 2012-07-12
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.graphstream.algorithm.IterativeTarjanStronglyConnectedComponents;
import org.graphstream.algorithm.TarjanStronglyConnectedComponents;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Utility Class
 */
class Pair<X, Y> {
	final X x;
	final Y y;

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
}

@RunWith(Parameterized.class)
public class TestTarjan {

	private static Graph getGraphFrom(List<String> l) {
		Graph g = new SingleGraph("test", false, true);
		for (String id : l)
			g.addEdge(id, id.substring(0, 1), id.substring(1, 2), true);
		return g;
	}

	@Parameters(name = "some graph")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { 
			{ getGraphFrom(List.of("AB", "BC", "CD", "DB")), 2, 1 },
			{ getGraphFrom(List.of("AB", "BC", "CD", "DA")), 1, 1 },
			{ getGraphFrom(List.of("AB", "AC", "AD", "BC", "CB", "CD", "DC", "DB", "BD")), 2, 1 },
			{ getGraphFrom(List.of("AB", "AC", "AD", "AE", "BC", "DE")), 5, 0 },
			{ getGraphFrom(List.of("AB", "AC", "AD", "AE", "BC", "DE", "CB", "ED")), 3, 2 } 
		});

	}

	@Parameter(value = 0)
	public Graph g;

	@Parameter(value = 1)
	public long scc0;

	@Parameter(value = 2)
	public long scc1;

	@Test
	public void testIterativeTarjan() {
		Pair<Long, Long> res = scc0and1IterativeTarjan(g);
		Assert.assertEquals(scc0, (long) res.x);
		Assert.assertEquals(scc1, (long) res.y);
	}

	@Test
	public void testTarjan() {
		Pair<Long, Long> res = scc0and1Tarjan(g);
		Assert.assertEquals(scc0, (long) res.x);
		Assert.assertEquals(scc1, (long) res.y);
	}

	Pair<Long, Long> scc0and1IterativeTarjan(Graph g) {

		long tarjanCountSup0 = 0;
		long tarjanCountSup1 = 0;

		IterativeTarjanStronglyConnectedComponents tsccs = new IterativeTarjanStronglyConnectedComponents();
		tsccs.init(g);
		tsccs.setSCCIndexAttribute("tsccs");
		tsccs.compute();
		Map<Integer, Integer> indexCount = new HashMap<>();
		g.nodes().map(n -> n.getAttribute("tsccs")).forEach(index -> {
			Integer count = indexCount.getOrDefault(index, 0);
			indexCount.put((Integer) index, count + 1);
		});
		tarjanCountSup0 = indexCount.size();
		tarjanCountSup1 = indexCount.values().stream().filter(i -> i > 1).count();

		return new Pair<>(tarjanCountSup0, tarjanCountSup1);
	}

	Pair<Long, Long> scc0and1Tarjan(Graph g) {

		long tarjanCountSup0 = 0;
		long tarjanCountSup1 = 0;

		TarjanStronglyConnectedComponents tsccs = new TarjanStronglyConnectedComponents();
		tsccs.init(g);
		tsccs.setSCCIndexAttribute("tsccs");
		tsccs.compute();
		Map<Integer, Integer> indexCount = new HashMap<>();
		g.nodes().map(n -> n.getAttribute("tsccs")).forEach(index -> {
			Integer count = indexCount.getOrDefault(index, 0);
			indexCount.put((Integer) index, count + 1);
		});
		tarjanCountSup0 = indexCount.size();
		tarjanCountSup1 = indexCount.values().stream().filter(i -> i > 1).count();

		return new Pair<>(tarjanCountSup0, tarjanCountSup1);
	}

}
