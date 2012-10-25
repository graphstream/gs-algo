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
package org.graphstream.algorithm.measure;

import org.graphstream.algorithm.APSP;
import org.graphstream.algorithm.APSP.APSPInfo;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;

/**
 * Compute closeness centrality.
 *
 */
public class ClosenessCentrality implements Algorithm {
	protected boolean computeAPSP;

	protected boolean normalize;

	protected String centralityAttribute;

	protected Graph graph;

	private double[] data;

	protected boolean useDangalchevMethod = false;

	protected APSP apsp;

	public ClosenessCentrality(String centralityAttribute, boolean normalize,
			boolean computeAPSP, boolean useDangalchevMethod) {
		this.computeAPSP = computeAPSP;
		this.centralityAttribute = centralityAttribute;
		this.normalize = normalize;
	}

	public void init(Graph graph) {
		this.graph = graph;

		if (computeAPSP) {
			apsp = new APSP();
			apsp.init(graph);
		}
	}

	public void compute() {
		int count = graph.getNodeCount();
		Node node, other;

		if (data == null || data.length != count)
			data = new double[count];

		if (computeAPSP)
			apsp.compute();

		for (int idx = 0; idx < count; idx++) {
			node = graph.getNode(idx);
			data[idx] = 0;

			APSP.APSPInfo info = node.getAttribute(APSPInfo.ATTRIBUTE_NAME);

			if (info == null)
				System.err
						.printf("APSPInfo missing. Did you compute APSP before ?\n");

			for (int idx2 = 0; idx2 < count; idx2++) {
				if (idx != idx2) {
					other = graph.getNode(idx2);
					double d = info.getLengthTo(other.getId());

					if (useDangalchevMethod)
						data[idx] += Math.pow(2, -d);
					else {
						if (d < 0)
							System.err
									.printf("Found a negative length value in centroid algorithm. "
											+ "Is graph connected ?\n");
						else
							data[idx] += d;
					}
				}
			}

			if (!useDangalchevMethod)
				data[idx] = 1 / data[idx];
		}

		if (normalize) {
			double max = data[0];

			for (int idx = 1; idx < count; idx++)
				max = Math.max(max, data[idx]);

			for (int idx = 0; idx < count; idx++)
				data[idx] /= max;
		}

		for (int idx = 0; idx < count; idx++)
			graph.getNode(idx).setAttribute(centralityAttribute, data[idx]);
	}

	public static void main(String... args) {
		Graph g = new AdjacencyListGraph("g");
		g.addAttribute("ui.stylesheet",
				"node {fill-mode: dyn-plain; fill-color: blue,yellow;}");
		BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
		gen.addSink(g);
		gen.begin();
		for (int i = 0; i < 1000; i++)
			gen.nextEvents();
		gen.end();

		ClosenessCentrality cc = new ClosenessCentrality("ui.color", true, true, true);
		cc.init(g);
		cc.compute();
		
		g.display();
	}
}
