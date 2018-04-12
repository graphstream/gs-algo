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
 * @since 2012-02-18
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.graphstream.algorithm.measure.ConnectivityMeasure;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.junit.Test;

public class TestConnectivityMeasure {
	Graph g;
	FileSourceDGS dgs;

	public TestConnectivityMeasure() {
		dgs = new FileSourceDGS();
		g = new AdjacencyListGraph("test");

		dgs.addSink(g);
	}

	protected InputStream getGraphStream() throws IOException {
		return getClass().getResourceAsStream(
				"data/TestConnectivityMeasure.dgs");
	}

	protected void begin() throws IOException {
		dgs.begin(getGraphStream());
	}

	protected void end() throws IOException {
		dgs.end();
		g.clear();
	}

	@Test
	public void testVertexConnectivity() throws IOException {
		begin();

		/*
		 * Step 1, graph should be 5-connected.
		 */
		dgs.nextStep();

		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 1));
		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 2));
		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 3));
		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 4));
		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 5));
		assertFalse(ConnectivityMeasure.isKVertexConnected(g, 6));
		assertTrue(ConnectivityMeasure.getVertexConnectivity(g) == 5);

		/*
		 * Step 2, graph should be 1-connected.
		 */
		dgs.nextStep();

		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 1));
		assertFalse(ConnectivityMeasure.isKVertexConnected(g, 2));
		assertTrue(ConnectivityMeasure.getVertexConnectivity(g) == 1);

		/*
		 * Step 3, graph should be 2-connected.
		 */
		dgs.nextStep();

		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 1));
		assertTrue(ConnectivityMeasure.isKVertexConnected(g, 2));
		assertFalse(ConnectivityMeasure.isKVertexConnected(g, 3));
		assertTrue(ConnectivityMeasure.getVertexConnectivity(g) == 2);

		end();
	}

	@Test
	public void testEdgeConnectivity() throws IOException {
		begin();

		/*
		 * Step 1, graph should be 4-edge-connected.
		 */
		dgs.nextStep();

		assertTrue(ConnectivityMeasure.isKEdgeConnected(g, 1));
		assertTrue(ConnectivityMeasure.isKEdgeConnected(g, 2));
		assertTrue(ConnectivityMeasure.isKEdgeConnected(g, 3));
		assertTrue(ConnectivityMeasure.isKEdgeConnected(g, 4));
		assertFalse(ConnectivityMeasure.isKEdgeConnected(g, 5));
		assertTrue(ConnectivityMeasure.getEdgeConnectivity(g) == 4);

		/*
		 * Step 2, graph should be 1-edge-connected.
		 */
		dgs.nextStep();

		assertTrue(ConnectivityMeasure.isKEdgeConnected(g, 1));
		assertFalse(ConnectivityMeasure.isKEdgeConnected(g, 2));
		assertTrue(ConnectivityMeasure.getEdgeConnectivity(g) == 1);

		/*
		 * Step 3, graph should be 2-edge-connected.
		 */
		dgs.nextStep();

		assertTrue(ConnectivityMeasure.isKEdgeConnected(g, 1));
		assertTrue(ConnectivityMeasure.isKEdgeConnected(g, 2));
		assertFalse(ConnectivityMeasure.isKEdgeConnected(g, 3));
		assertTrue(ConnectivityMeasure.getEdgeConnectivity(g) == 2);

		end();
	}
}
