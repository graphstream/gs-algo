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
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.ui.layout.test;

import java.io.IOException;

//import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
//import org.graphstream.algorithm.generator.Generator;
//import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.graph.ElementNotFoundException;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.GraphParseException;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

public class DemoLayout {
	public static void main(String args[]) throws ElementNotFoundException, IOException, GraphParseException {
		//System.getProperties().put("gs.ui.layout", "org.graphstream.ui.layout.springbox.implementations.LinLog");
		(new DemoLayout()).test();
	}
	
	public void test() throws ElementNotFoundException, IOException, GraphParseException {
		Graph graph = new MultiGraph("test");
		SpriteManager sm = new SpriteManager(graph);
		Sprite C = sm.addSprite("C");
		C.setPosition(0, 0, 0);
		C.setAttribute("ui.label", "(0,0)");
		graph.display();
		graph.setAttribute("ui.stylesheet", styleSheet);
		graph.setAttribute("layout.stabilization-limit", 1);
		graph.setAttribute("layout.quality", 3);
		graph.setAttribute("layout.gravity", 0.01);
//		int steps = 50;
//		Generator gen = new BarabasiAlbertGenerator(2);
//		int steps = 6;
//		Generator gen = new GridGenerator();
		
//		gen.addSink(graph);
//		gen.begin();
//		for(int i=0; i<steps; i++) {
//			gen.nextEvents();
//			sleep(10);
//		}
//		gen.end();
//		graph.read("src-test/org/graphstream/ui/layout/test/data/fourComponents.dgs");
		graph.read("src-test/org/graphstream/ui/layout/test/data/polbooks.gml");
//		graph.read("src-test/org/graphstream/ui/layout/test/data/dolphins.gml");
	}
	
	public static void sleep(long ms) {
		try { Thread.sleep(ms); } catch(Exception e) {}
	}
	
	protected static final String styleSheet = 
			"sprite#C { fill-color: red; text-color: red; }";
}