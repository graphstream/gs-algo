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
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import org.graphstream.algorithm.PageRank;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class DemoPageRank {
	public static void main(String[] args) throws InterruptedException {
		Graph graph = new SingleGraph("test");
		graph.setAttribute("ui.antialias", true);
		graph.setAttribute("ui.stylesheet",
				"node {fill-color: red; size-mode: dyn-size;} edge {fill-color:grey;}");
		graph.display();

		DorogovtsevMendesGenerator generator = new DorogovtsevMendesGenerator();
		generator.setDirectedEdges(true, true);
		generator.addSink(graph);

		PageRank pageRank = new PageRank();
		pageRank.setVerbose(true);
		pageRank.init(graph);

		generator.begin();
		while (graph.getNodeCount() < 100) {
			generator.nextEvents();
			for (Node node : graph) {
				double rank = pageRank.getRank(node);
				node.setAttribute("ui.size",
						5 + Math.sqrt(graph.getNodeCount() * rank * 20));
				node.setAttribute("ui.label",
						String.format("%.2f%%", rank * 100));
			}
			Thread.sleep(1000);
		}
	}

}
