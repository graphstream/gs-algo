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
 * @since 2009-02-19
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import org.graphstream.algorithm.Toolkit;

/**
 * This is a graph generator that generates dynamic random graphs.
 * 
 * <u>The principle:</u>
 * 
 * <p>
 * A graph consists in a set of vertices and a set of edges. The dynamic relies
 * on 4 kinds of steps of events: at each step:
 * <ul>
 * <li>a subset of nodes is removed</li>
 * <li>a subset of nodes is added</li>
 * <li>a subset of edges is removed (in the current version, edges that are
 * removed are those that were attached to nodes that disappear)</li>
 * <li>a subset of edges is added</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This generator is characterized by:
 * <ul>
 * <li>The parameters:
 * <ul>
 * <li>number of vertices</li>
 * <li>maximum mean degree</li>
 * </ul>
 * </li>
 * <li>The constraints:
 * <ul>
 * <li>graph nervosity</li>
 * <li>creation links rules</li>
 * </ul>
 * </li>
 * <li>The metrics:
 * <ul>
 * <li>mean number of vertices and edges</li>
 * <li>mean age of vertices and edges</li>
 * <li>mean distribution of degree</li>
 * <li>mean number of connected components</li>
 * <li>...</li>
 * <li>...</li>
 * </ul>
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * How to build such graphs ? There exist at least one mathematical function for
 * doing that f(step) = nbVertices*log(step)/log(step+"Pente") the larger
 * "Pente", the softer the "pente" of the curve. Given f(step), it is possible
 * to compute nbCreations and nbDeletions together with the graph nervosity.
 * f(step) represents the number of vertices that should be present within the
 * graph given the step and the value of the parameter "Pente". However, as our
 * graph is dynamic, some vertices may be deleted while some other vertices may
 * be added to the graph.
 * 
 * Question: could it be possible to build a dynamic graph that reaches a stable
 * state (stabilization of the number of vertices, and stabilization of some
 * other properties), just by adding some constraints/characteristics on each
 * node?
 * 
 * @author Frédéric Guinand
 * @since 20080616
 */
public class RandomFixedDegreeDynamicGraphGenerator extends BaseGenerator {
	/**
	 * Average number of vertices.
	 */
	protected int nbVertices;

	/**
	 * Limit for the mean degree of nodes.
	 */
	protected double meanDegreeLimit;

	/**
	 * Nervousness of the generator. It allows to influence the number of nodes
	 * removed at each step and so the number of new nodes added.
	 */
	protected double nervousness;

	/**
	 * Current step of the generator.
	 */
	protected int step = 1;

	/**
	 * Influence the number of nodes created at each step.
	 */
	protected int deltaStep = 100;

	/**
	 * Used to generate node ids.
	 */
	protected int currentNodeId = 0;

	/**
	 * Create a new RandomFixedDegreeDynamicGraphGenerator generator with
	 * default values for attributes.
	 * 
	 * @see #RandomFixedDegreeDynamicGraphGenerator(int, double, double)
	 */
	public RandomFixedDegreeDynamicGraphGenerator() {
		this(50, 5, 0.1f);
	}

	/**
	 * Create a new RandomFixedDegreeDynamicGraphGenerator generator.
	 * 
	 * @param nbVertices
	 *            The number of vertices.
	 * @param meanDegreeLimit
	 *            The average degree.
	 * @param nervousness
	 *            The nervousness.
	 */
	public RandomFixedDegreeDynamicGraphGenerator(int nbVertices,
			double meanDegreeLimit, double nervousness) {
		setUseInternalGraph(true);

		this.nbVertices = nbVertices;
		this.meanDegreeLimit = meanDegreeLimit;
		this.nervousness = nervousness;
	}

	/**
	 * This method computes the mean degree of the graph.
	 */
	public double meanDegree() {
		return 2.0 * internalGraph.getEdgeCount()
				/ (double) internalGraph.getNodeCount();
	}

	protected String getEdgeId(String src, String trg) {
		if (src.compareTo(trg) < 0)
			return String.format("%s_%s", src, trg);

		return String.format("%s_%s", trg, src);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		step = 0;
	}

	/**
	 * Step of the generator. Some nodes will be removed according to the
	 * nervousness, then new nodes and new edges will be added.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		int nbCreations, nbSuppressions, nbCreationsEdges;
		String dead, source, dest;

		sendStepBegins(sourceId, step);

		nbSuppressions = (int) (random.nextFloat() * (internalGraph
				.getNodeCount() * nervousness));

		for (int r = 1; r <= nbSuppressions; r++) {
			dead = Toolkit.randomNode(internalGraph, random).getId();
			delNode(dead);
		}

		nbCreations = (int) (random.nextFloat() * ((nbVertices - internalGraph
				.getNodeCount()) * Math.log(step) / Math.log(step + deltaStep)));

		for (int c = 1; c <= nbCreations; c++) {
			String nodeId = String.format("%d", currentNodeId++);

			addNode(nodeId);
		}

		double degreMoyen = meanDegree();

		nbCreationsEdges = (int) (random.nextFloat() * (((meanDegreeLimit - degreMoyen) * (internalGraph
				.getNodeCount() / 2)) * Math.log(step) / Math.log(step
				+ deltaStep)));

		if (internalGraph.getNodeCount() > 1) {
			for (int c = 1; c <= nbCreationsEdges; c++) {
				do {
					source = Toolkit.randomNode(internalGraph, random).getId();
					dest = Toolkit.randomNode(internalGraph, random).getId();
				} while (source.equals(dest));

				String idEdge = getEdgeId(source, dest);

				while (internalGraph.getEdge(idEdge) != null || source.equals(dest)) {
					dest = Toolkit.randomNode(internalGraph, random).getId();
					idEdge = getEdgeId(source, dest);
				}

				addEdge(idEdge, source, dest);
			}
		}

		step++;

		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	@Override
	public void end() {
		super.end();
	}
}