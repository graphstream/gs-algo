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
 * @since 2013-05-27
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.coloring.test;

import java.io.IOException;
import java.io.StringReader;

import org.graphstream.algorithm.coloring.WelshPowell;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.stream.file.FileSourceDGS;

public class WelshPowellDemo {
	// B-(1)-C
	// / \
	// (1) (10)
	// / \
	// A F
	// \ /
	// (1) (1)
	// \ /
	// D-(1)-E
	static String my_graph = "DGS004\n" + "my 0 0\n" + "an A \n" + "an B \n"
			+ "an C \n" + "an D \n" + "an E \n" + "an F \n"
			+ "ae AB A B weight:1 \n" + "ae AD A D weight:1 \n"
			+ "ae BC B C weight:1 \n" + "ae CF C F weight:10 \n"
			+ "ae DE D E weight:1 \n" + "ae EF E F weight:1 \n";

	public static void main(String[] args) throws IOException,
			ElementNotFoundException, GraphParseException {
		Graph graph = new DefaultGraph("Welsh Powell Test");
		StringReader reader = new StringReader(my_graph);

		FileSourceDGS source = new FileSourceDGS();
		source.addSink(graph);
		source.readAll(reader);

		WelshPowell wp = new WelshPowell("color");
		wp.init(graph);
		wp.compute();

		System.out.println("The chromatic number of this graph is : "
				+ wp.getChromaticNumber());
		for (Node n : graph) {
			System.out.println("Node " + n.getId() + " : color "
					+ n.getAttribute("color"));
		}
	}
}
