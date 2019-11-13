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
 * @since 2011-12-16
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure.test;

import static org.junit.Assert.assertTrue;

import org.graphstream.algorithm.measure.MaxSimultaneousEdgeCount;
import org.graphstream.algorithm.measure.MaxSimultaneousNodeCount;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.junit.Test;

public class TestSimultaneousElementCount {

	@Test
	public void testSimultaneousNodeCount() {
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		MaxSimultaneousNodeCount count = new MaxSimultaneousNodeCount();
		int id = 0;
		
		g.addSink(count);
		
		for (int i = 0; i < 100; i++)
			g.addNode(String.format("n%03x", id++));
		
		System.out.printf("max node : %d\n", count.getMaxSimultaneousNodeCount());
		
		assertTrue(count.getMaxSimultaneousNodeCount() == 100);
		
		for (int i = 0; i < 20; i++)
			g.removeNode(String.format("n%03x", i));
		
		assertTrue(count.getMaxSimultaneousNodeCount() == 100);
		
		for (int i = 0; i < 40; i++)
			g.addNode(String.format("n%03x", id++));
		
		assertTrue(count.getMaxSimultaneousNodeCount() == 120);
	}
	
	@Test
	public void testSimultaneousEdgeCount() {
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		MaxSimultaneousEdgeCount count = new MaxSimultaneousEdgeCount();
		int id = 0;

		g.addSink(count);
		
		for (int i = 0; i < 100; i++) {
			String id1, id2;
			
			id1 = String.format("a%03x", id);
			id2 = String.format("b%03x", id);
			
			g.addNode(id1);
			g.addNode(id2);
			g.addEdge(String.format("e%03x", id++), id1, id2); 
		}
		
		assertTrue(count.getMaxSimultaneousEdgeCount() == 100);
		
		for (int i = 0; i < 20; i++)
			g.removeEdge(String.format("e%03x", i));
		
		assertTrue(count.getMaxSimultaneousEdgeCount() == 100);
		
		for (int i = 0; i < 40; i++) {
			String id1, id2;
			
			id1 = String.format("a%03x", id);
			id2 = String.format("b%03x", id);
			
			g.addNode(id1);
			g.addNode(id2);
			g.addEdge(String.format("e%03x", id++), id1, id2);
		}

		assertTrue(count.getMaxSimultaneousEdgeCount() == 120);
	}
}
