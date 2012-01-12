/*
 * Copyright 2006 - 2012
 *      Stefan Balev       <stefan.balev@graphstream-project.org>
 *      Julien Baudry	<julien.baudry@graphstream-project.org>
 *      Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *      Yoann Pigné	<yoann.pigne@graphstream-project.org>
 *      Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Scale-free graph generator using the preferential attachment rule as defined
 * in the Barabási-Albert model.
 * 
 * <p>
 * This is a very simple graph generator that generates a graph using the
 * preferential attachment rule defined in the Barabási-Albert model: nodes are
 * generated one by one, and each time attached by one or more edges other
 * nodes. The other nodes are chosen using a biased random selection giving more
 * chance to a node if it has a high degree.
 * </p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>
 * The more this generator is iterated, the more nodes are generated. It can
 * therefore generate graphs of any size. One node is generated at each call to
 * {@link #nextEvents()}. At each node added at least one new edge is added. The
 * number of edges added at each step is given by the
 * {@link #getMaxLinksPerStep()}. However by default the generator creates a
 * number of edges per new node chosen randomly between 1 and
 * {@link #getMaxLinksPerStep()}. To have exactly this number of edges at each
 * new node, use {@link #setExactlyMaxLinksPerStep(boolean)}.
 * </p>
 * 
 * <h2>Complexity</h2>
 * 
 * For each new step, the algorithm act in O(n) with n the number of
 * nodes if 1 max edge per new node is created, else the complexity
 * is O(nm) if m max edge per new node is created.
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * Graph graph = new SingleGraph("Barabàsi-Albert");
 * // Between 1 and 3 new links per node added.
 * Generator gen = new BarabasiAlbertGenerator(3);
 * // Generate 100 nodes:
 * gen.addSink(graph); 
 * gen.begin();
 * for(int i=0; i<100; i++) {
 * 		gen.nextEvents();
 * }
 * gen.end();
 * graph.display();
 * </pre>
 * 
 * @reference Albert-László Barabási & Réka Albert
 *            "Emergence of scaling in random networks", Science 286: 509–512.
 *            October 1999. doi:10.1126/science.286.5439.509.
 */
public class BarabasiAlbertGenerator extends BaseGenerator {
	/**
	 * Degree of each node.
	 */
	protected ArrayList<Integer> degrees;

	/**
	 * Maximal degree at time t.
	 */
	protected int degreeMax = 0;

	/**
	 * Number of edges.
	 */
	protected int edgesCount = 0;

	/**
	 * The maximum number of links created when a new node is added.
	 */
	protected int maxLinksPerStep = 1;

	/**
	 * Does the generator generates exactly {@link #maxLinksPerStep}.
	 */
	protected boolean exactlyMaxLinksPerStep = false;

	/**
	 * New generator.
	 */
	public BarabasiAlbertGenerator() {
		this(1, false);
	}

	public BarabasiAlbertGenerator(int maxLinksPerStep) {
		this(maxLinksPerStep, false);
	}

	public BarabasiAlbertGenerator(int maxLinksPerStep,
			boolean exactlyMaxLinksPerStep) {
		this.directed = false;
		this.maxLinksPerStep = maxLinksPerStep;
		this.exactlyMaxLinksPerStep = exactlyMaxLinksPerStep;
	}

	/**
	 * Maximum number of edges created when a new node is added.
	 * 
	 * @return The maximum number of links per step.
	 */
	public int getMaxLinksPerStep() {
		return maxLinksPerStep;
	}

	/**
	 * True if the generator produce exactly {@link #getMaxLinksPerStep()}, else
	 * it produce a random number of links ranging between 1 and
	 * {@link #getMaxLinksPerStep()}.
	 * 
	 * @return Does the generator generates exactly
	 *         {@link #getMaxLinksPerStep()}.
	 */
	public boolean produceExactlyMaxLinkPerStep() {
		return exactlyMaxLinksPerStep;
	}

	/**
	 * Set how many edge (maximum) to create for each new node added.
	 * 
	 * @param max
	 *            The new maximum, it must be strictly greater than zero.
	 */
	public void setMaxLinksPerStep(int max) {
		maxLinksPerStep = max > 0 ? max : 1;
	}

	/**
	 * Set if the generator produce exactly {@link #getMaxLinksPerStep()}
	 * (true), else it produce a random number of links ranging between 1 and
	 * {@link #getMaxLinksPerStep()} (false).
	 * 
	 * @param on
	 *            Does the generator generates exactly
	 *            {@link #getMaxLinksPerStep()}.
	 */
	public void setExactlyMaxLinksPerStep(boolean on) {
		exactlyMaxLinksPerStep = on;
	}

	/**
	 * Start the generator. A single node is added.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		this.degrees = new ArrayList<Integer>();
		this.degreeMax = 0;

		addNode("0");
		degrees.add(0);
	}

	/**
	 * Step of the generator. Add a node and try to connect it with some others.
	 * 
	 * The number of links is randomly chosen between 1 and the maximum number
	 * of links per step specified in {@link #setMaxLinksPerStep(int)}.
	 * 
	 * The complexity of this method is O(n) with n the number of nodes if the
	 * number of edges created per new node is 1, else it is O(nm) with m the
	 * number of edges generated per node.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		// Generate a new node.

		int index = degrees.size();
		String id = Integer.toString(index);
		int n = maxLinksPerStep;

		addNode(id);
		degrees.add(0);

		if (!exactlyMaxLinksPerStep)
			n = random.nextInt(n) + 1;

		n = Math.min(n, degrees.size() - 1);

		// Choose the nodes to attach to.

		LinkedList<Integer> notIn = new LinkedList<Integer>();

		for (int i = 0; i < n; i++) {
			int otherIdx = chooseAnotherNode(index, notIn);
			attachToOtherNode(i, index, id, otherIdx);

			notIn.add(otherIdx);
		}

		// It is always possible to add an element.

		return true;
	}

	/**
	 * Randomly choose a node to attach to, the node is chosen
	 * 
	 * @param index
	 * @return
	 */
	protected int chooseAnotherNode(int index, LinkedList<Integer> notIn) {
		int sumDeg = (edgesCount - degrees.get(index)) * 2;
		double sumProba = 0;
		double rnd = random.nextDouble();
		int otherIdx = -1;

		for (int i = 0; i < notIn.size(); i++)
			sumDeg -= degrees.get(notIn.get(i));

		for (int i = 0; i < index; ++i) {
			double proba = sumDeg == 0 ? 1 : degrees.get(i) / ((double) sumDeg);

			if (notIn.contains(i))
				continue;

			sumProba += proba;

			if (sumProba > rnd) {
				otherIdx = i;
				break;
			}
		}

		return otherIdx;
	}

	protected void attachToOtherNode(int i, int index, String id, int otherIdx) {
		if (otherIdx >= 0) {
			String oid = Integer.toString(otherIdx);
			String eid = id + "_" + oid + "_" + i;

			addEdge(eid, oid, id);
			edgesCount++;
			degrees.set(otherIdx, degrees.get(otherIdx) + 1);
			degrees.set(index, degrees.get(index) + 1);
		} else {
			System.err.printf("PreferentialAttachmentGenerator: *** Aieuu!%n");
		}
	}

	/**
	 * Clean degrees.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	@Override
	public void end() {
		degrees.clear();
		degrees = null;
		degreeMax = 0;
		super.end();
	}
}