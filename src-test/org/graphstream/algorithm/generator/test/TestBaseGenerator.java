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
 * @since 2018-02-27
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator.test;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.stream.Stream;

public class TestBaseGenerator {
	@Test
	public void testAddNodeAttributeWithFactory() {
		String[] strings = { "this", "is", "a", "test", "!" };
		Graph g = new AdjacencyListGraph("test");
		BaseGenerator gen = new BaseGenerator() {
			@Override
			public void begin() {
			}

			@Override
			public boolean nextEvents() {
				addNode(Integer.toString(random.nextInt()));
				return true;
			}
		};

		gen.addSink(g);
		gen.addNodeAttribute("boolean", Random::nextBoolean);
		gen.addNodeAttribute("long", Random::nextLong);
		gen.addNodeAttribute("string", random -> strings[random.nextInt(strings.length)]);

		for (int i = 0; i < 10000; i++) {
			gen.nextEvents();
		}

		gen.end();

		g.nodes().forEach(node -> {
			Object b = node.getAttribute("boolean");
			Object l = node.getAttribute("long");
			Object s = node.getAttribute("string");

			Assert.assertNotNull(b);
			Assert.assertNotNull(l);
			Assert.assertNotNull(s);

			Assert.assertTrue(b instanceof Boolean);
			Assert.assertTrue(l instanceof Long);
			Assert.assertTrue(s instanceof String);

			Assert.assertTrue(Stream.of(strings).anyMatch(str -> str.equals(s)));
		});
	}

	@Test
	public void testAddNodeAttributeWithRange() {
		final double min = -10, max = 10;
		Graph g = new AdjacencyListGraph("test");
		BaseGenerator gen = new BaseGenerator() {
			@Override
			public void begin() {
			}

			@Override
			public boolean nextEvents() {
				addNode(Integer.toString(random.nextInt()));
				return true;
			}
		};

		gen.addSink(g);
		gen.addNodeAttribute("real", min, max);

		for (int i = 0; i < 10000; i++) {
			gen.nextEvents();
		}

		gen.end();

		g.nodes().map(node -> (Double) node.getAttribute("real")).forEach(value -> {
			Assert.assertNotNull(value);
			Assert.assertTrue(value instanceof Double);

			Assert.assertTrue(value <= max);
			Assert.assertTrue(value >= min);
		});

		Assert.assertTrue(g.nodes().map(node -> (Double) node.getAttribute("real"))
				.anyMatch(value -> value != max && value != min));
	}

	@Test
	public void testAddNodeAttributeDefault() {
		Graph g = new AdjacencyListGraph("test");
		BaseGenerator gen = new BaseGenerator() {
			@Override
			public void begin() {
			}

			@Override
			public boolean nextEvents() {
				addNode(Integer.toString(random.nextInt()));
				return true;
			}
		};

		gen.addSink(g);
		gen.addNodeAttribute("real");

		for (int i = 0; i < 10000; i++) {
			gen.nextEvents();
		}

		gen.end();

		g.nodes().map(node -> (Double) node.getAttribute("real")).forEach(value -> {
			Assert.assertNotNull(value);
			Assert.assertTrue(value instanceof Double);

			Assert.assertTrue(value <= 1);
			Assert.assertTrue(value >= 0);
		});

		Assert.assertTrue(
				g.nodes().map(node -> (Double) node.getAttribute("real")).anyMatch(value -> value != 1 && value != 0));
	}
}
