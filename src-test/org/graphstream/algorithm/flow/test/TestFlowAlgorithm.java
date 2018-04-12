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
 * @since 2012-02-10
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.flow.test;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import org.graphstream.algorithm.flow.FlowAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.junit.Before;
import org.junit.Test;

public abstract class TestFlowAlgorithm {
	Graph g;
	FileSourceDGS dgs;

	public abstract InputStream getGraphStream() throws IOException;
	
	public abstract FlowAlgorithm getFlowAlgorithm();
	
	@Before
	public void load() throws IOException {
		dgs = new FileSourceDGS();
		g = new AdjacencyListGraph("flow-test");

		dgs.addSink(g);
		dgs.begin(getGraphStream());
	}

	@Test
	public void testFlowAlgorithm() throws IOException {
		FlowAlgorithm flowAlgo = getFlowAlgorithm();
		double maximumFlow;
		
		flowAlgo.setCapacityAttribute("cap");

		while (dgs.nextStep()) {
			flowAlgo.init(g, "s", "t");
			flowAlgo.compute();

			maximumFlow = flowAlgo.getMaximumFlow();
			assertTrue(maximumFlow == g.getNumber("expected maximum flow"));
		}
	}
}
