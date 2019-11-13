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

import java.util.ArrayList;

/**
 * Scale-free tree generator using the preferential attachment rule as
 * defined in the Barabási-Albert model.
 * 
 * <p>
 * THIS GENERATOR IS DEPRECATED, USE THE {@link BarabasiAlbertGenerator} INSTEAD.
 * </p>
 * 
 * <p>
 * This is a very simple graph generator that generates a tree using the
 * preferential attachment rule defined in the Barabási-Albert model: nodes are
 * generated one by one, and each time attached by an edge to another node that
 * has more chance to chosen if it already has lots of nodes attached to it.
 * </p>
 * 
 * <p>
 * The more this generator is iterated, the more nodes are generated. It can
 * therefore generate trees of any size.
 * </p>
 * 
 * @reference Albert-László Barabási & Réka Albert
 *            "Emergence of scaling in random networks". Science 286: 509–512.
 *            October 1999. doi:10.1126/science.286.5439.509.
 * 
 * @since 20061128
 */
@Deprecated
public class PreferentialAttachmentGenerator extends BaseGenerator {
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
	 * New generator.
	 */
	public PreferentialAttachmentGenerator() {
		directed = false;
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
	 * The complexity of this method is O(n) with n the number of nodes actually
	 * in the graph.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		// Generate a new node.

		int index = degrees.size();
		String id = Integer.toString(index);

		addNode(id);
		degrees.add(0);

		// Compute the attachment probability of each previously added node

		int sumDeg = edgesCount * 2;

		// Choose the node to attach to.

		double sumProba = 0;
		double rnd = random.nextDouble();
		int otherIdx = -1;

		for(int i = 0; i < index; ++i) {
			double proba = sumDeg == 0 ? 1 : degrees.get(i) / ((double) sumDeg);

			sumProba += proba;

			if (sumProba > rnd) {
				otherIdx = i;
				i = index;// Stop the loop.
			}
		}

		// Attach to the other node.

		if (otherIdx >= 0) {
			String oid = Integer.toString(otherIdx);
			String eid = id + "_" + oid;

			addEdge(eid, oid, id);
			edgesCount++;
			degrees.set(otherIdx, degrees.get(otherIdx) + 1);
			degrees.set(index, degrees.get(index) + 1);
		} else {
			System.err.printf("PreferentialAttachmentGenerator: Aieuu!%n");
		}

		// It is always possible to add an element.

		return true;
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