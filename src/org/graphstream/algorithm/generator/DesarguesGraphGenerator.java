/*
 * Copyright 2006 - 2011 
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
package org.graphstream.algorithm.generator;

/**
 * In the mathematical field of graph theory, the Desargues graph is a
 * distance-transitive cubic graph with 20 vertices and 30 edges. It is named
 * after Gérard Desargues, arises from several different combinatorial
 * constructions, has a high level of symmetry, is the only known non-planar
 * cubic partial cube, and has been applied in chemical databases. The name
 * "Desargues graph" has also been used to refer to the complement of the
 * Petersen graph.
 * 
 * Source : <a href="http://en.wikipedia.org/wiki/Desargues_graph">Wikipedia</a>
 */
public class DesarguesGraphGenerator extends BaseGenerator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		int edgeId = 0;

		for (int i = 0; i < 10; i++) {
			String idi = String.format("%02di", i);
			String ido = String.format("%02do", i);

			double xi, yi;
			double xo, yo;

			xi = Math.cos(i * Math.PI / 5.0);
			yi = Math.sin(i * Math.PI / 5.0);
			xo = 2 * Math.cos(i * Math.PI / 5.0);
			yo = 2 * Math.sin(i * Math.PI / 5.0);

			addNode(idi);
			addNode(ido);

			sendNodeAttributeAdded(sourceId, idi, "x", xi);
			sendNodeAttributeAdded(sourceId, idi, "y", yi);

			sendNodeAttributeAdded(sourceId, ido, "x", xo);
			sendNodeAttributeAdded(sourceId, ido, "y", yo);
		}

		for (int i = 0; i < 10; i++) {
			addEdge(String.format("%02d", edgeId++), String.format("%02di", i),
					String.format("%02do", i));

			addEdge(String.format("%02d", edgeId++), String.format("%02do", i),
					String.format("%02do", (i + 1) % 10));

			addEdge(String.format("%02d", edgeId++), String.format("%02di", i),
					String.format("%02di", (i + 3) % 10));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		return false;
	}
}
