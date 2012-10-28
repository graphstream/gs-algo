/*
 * Copyright 2006 - 2012
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.algorithm.measure.demo;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.measure.EigenvectorCentrality;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.AdjacencyListGraph;

public class EigenvectorCentralityDemo {
	public static final String STYLE = "node {" + "fill-mode: dyn-plain;"
			+ "fill-color: blue,yellow;" + "size-mode: dyn-size;"
			+ "stroke-color: black;" + "stroke-width: 1px;"
			+ "stroke-mode: plain;" + "}";

	public static void main(String... args) {
		System.setProperty("org.graphstream.ui.renderer",
				"org.graphstream.ui.j2dviewer.J2DGraphRenderer");

		Graph g = new AdjacencyListGraph("g");

		g.addAttribute("ui.quality");
		g.addAttribute("ui.antialias");
		g.addAttribute("ui.stylesheet", STYLE);

		BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
		gen.addSink(g);
		gen.begin();
		for (int i = 0; i < 200; i++)
			gen.nextEvents();
		gen.end();

		EigenvectorCentrality dc = new EigenvectorCentrality("ui.color", true);
		dc.init(g);
		dc.compute();

		for (int i = 0; i < g.getNodeCount(); i++)
			g.getNode(i).addAttribute("ui.size",
					g.getNode(i).getNumber("ui.color") * 25 + 5);

		g.display();
	}

}
