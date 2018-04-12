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
 * @since 2011-05-12
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.test;

import javax.swing.JOptionPane;

import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.FullGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.generator.GridGenerator;
import org.graphstream.algorithm.generator.IncompleteGridGenerator;
import org.graphstream.algorithm.generator.RandomEuclideanGenerator;
import org.graphstream.algorithm.generator.RandomGenerator;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.Pipe;
import org.graphstream.ui.view.Viewer;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

@Ignore("need gs-ui package")
public class TestGenerator {
	@Test
	public void testFullGenerator() {
		testGenerator(new FullGenerator(), "FullGenerator", 10);
	}

	@Test
	public void testRandomGenerator() {
		testGenerator(new RandomGenerator(), "RandomGenerator", 100);
	}

	@Test
	public void testRandomEuclideanGenerator() {
		testGenerator(new RandomEuclideanGenerator(),
				"RandomEuclideanGenerator", 100);
	}

	@Test
	public void testGridGenerator() {
		testGenerator(new GridGenerator(), "GridGenerator", 10);
	}

	@Test
	public void testIncompleteGridGenerator() {
		testGenerator(new IncompleteGridGenerator(), "IncompleteGridGenerator",
				10);
	}

	@Test
	public void testPreferentialAttachmentGenerator() {
		testGenerator(new BarabasiAlbertGenerator(),
				"BarabasiAlbertGenerator", 100);
	}

	@Test
	public void testDorogovtsevMendesGenerator() {
		testGenerator(new DorogovtsevMendesGenerator(),
				"DorogovtsevMendesGenerator", 100);
	}

	@Test
	public void testBarabasiAlbertGenerator() {
		testGenerator(new BarabasiAlbertGenerator(3),
				"Barabasi-Albert Generator (3)", 100);
	}

	@Test
	public void testBarabasiAlbertGenerator2() {
		testGenerator(new BarabasiAlbertGenerator(3, true),
				"Barabasi-Albert Generator (3, true)", 100);
	}

	protected void testGenerator(Generator gen, String name, int size) {
		DefaultGraph g = new DefaultGraph("test-" + name);

		int i = size;

		gen.addSink(g);

		if (gen instanceof Pipe)
			g.addAttributeSink((Pipe) gen);

		Viewer gvr = g.display();

		gen.begin();

		while (i-- > 0)
			gen.nextEvents();

		gen.end();

		int r = JOptionPane.showConfirmDialog(null, String.format(
				"%s with %d iterations. Is it correct ?", name, size), name,
				JOptionPane.YES_NO_OPTION);
		
		assertTrue(r == JOptionPane.YES_OPTION);

		gvr.close();
	}
}
