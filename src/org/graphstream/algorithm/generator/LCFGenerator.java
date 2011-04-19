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

import java.util.HashSet;

import org.graphstream.algorithm.generator.lcf.Balaban10CageGraphGenerator;
import org.graphstream.algorithm.generator.lcf.BiggsSmithGraphGenerator;
import org.graphstream.algorithm.generator.lcf.NauruGraphGenerator;
import org.graphstream.graph.implementations.DefaultGraph;

/**
 * Build a graph using a lcf notation.
 * 
 * Source : <a href="http://en.wikipedia.org/wiki/LCF_notation">Wikipedia</a>
 *
 */
public class LCFGenerator extends BaseGenerator {

	public static class LCF {
		int repeat;
		int[] steps;

		public LCF(int repeat, int... steps) {
			this.repeat = repeat;
			this.steps = steps;
		}
	}

	private int n;
	private int initialRingSize;
	private HashSet<String> crossed;
	protected LCF lcf;
	protected boolean canBeExtended;

	public LCFGenerator(LCF lcf, int initialRingSize, boolean canBeExtended) {
		this.lcf = lcf;
		this.crossed = new HashSet<String>();
		this.initialRingSize = initialRingSize;
		this.canBeExtended = canBeExtended;
	}

	public void begin() {
		addNode(getNodeId(0));
		addNode(getNodeId(1));
		addNode(getNodeId(2));

		addEdge(getEdgeId(0, 1), getNodeId(0), getNodeId(1));
		addEdge(getEdgeId(1, 2), getNodeId(1), getNodeId(2));
		addEdge(getEdgeId(2, 0), getNodeId(2), getNodeId(0));

		n = 3;

		for (int i = n; i < initialRingSize; i++)
			increaseRing();

		flushCoords();
		makeLCF();
	}

	public boolean nextEvents() {
		if (canBeExtended) {
			increaseRing();
			makeLCF();
			flushCoords();
		}

		return canBeExtended;
	}

	protected void increaseRing() {
		addNode(getNodeId(n));

		delEdge(getEdgeId(n - 1, 0));
		addEdge(getEdgeId(n - 1, n), getNodeId(n - 1), getNodeId(n));
		addEdge(getEdgeId(n, 0), getNodeId(n), getNodeId(0));

		n++;
	}

	protected void makeLCF() {
		int i = 0;
		int r = 0;
		HashSet<String> added = new HashSet<String>();

		while (r < lcf.repeat && i < n) {
			for (int k = 0; k < lcf.steps.length && i < n; k++) {
				int j = (i + lcf.steps[k]) % n;

				while (j < 0)
					j += n;

				String edge = getEdgeId(i, j);

				if (!crossed.contains(edge) && !added.contains(edge))
					addEdge(edge, getNodeId(i), getNodeId(j));

				added.add(edge);
				i++;
			}

			r++;
		}

		for (String edge : crossed) {
			if (!added.contains(edge))
				delEdge(edge);
		}

		crossed.clear();
		crossed = added;
	}

	protected void flushCoords() {
		double d = 2 * Math.PI / n;

		for (int i = 0; i < n; i++) {
			sendNodeAttributeChanged(sourceId, getNodeId(i), "x", null,
					Math.cos(i * d));
			sendNodeAttributeChanged(sourceId, getNodeId(i), "y", null,
					Math.sin(i * d));
		}
	}

	protected String getNodeId(int i) {
		return String.format("%03d", i);
	}

	protected String getEdgeId(int i1, int i2) {
		if (i1 > i2) {
			int t = i1;
			i1 = i2;
			i2 = t;
		}

		return String.format("%03d_%03d", i1, i2);
	}

	public static void main(String... args) throws Exception {
		DefaultGraph g = new DefaultGraph("g");
		LCFGenerator gen = new BiggsSmithGraphGenerator();

		gen.addSink(g);
		g.display(false);
		gen.begin();
	}
}
