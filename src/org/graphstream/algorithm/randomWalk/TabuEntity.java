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
 * @since 2011-05-14
 * 
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.randomWalk;

import static org.graphstream.algorithm.Toolkit.randomNode;

import java.util.ArrayList;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * A basic entity that chooses edges at random eventually
 * avoiding some edges based on a memory of recently traversed
 * edges.
 */
public class TabuEntity extends Entity {
	/**
	 * The edge memory.
	 */
	protected LinkedList<Node> memory;

	/**
	 * The edges weights of the current node.
	 */
	protected double weights[];

	/**
	 * Start the entity on the given node.
	 * @param start The starting node.
	 */
	@Override
	public void init(RandomWalk.Context context, Node start) {
		super.init(context, start);
		if(memory != null)
			memory.clear();
	}

	@Override
	public void step() {
		tabuStep();
	}

	/**
	 * Move the entity from its current node to another via an edge randomly chosen.
	 * 
	 * <p>
	 * This method makes a list of all leaving edges of the current node. If the
	 * node has no leaving edge, the entity jumps to another randomly chosen node.
	 * Then an edge is chosen at random in the list of leaving edges. The edge is
	 * chosen uniformly if there are no weights on the edges, else, an edge with
	 * an higher weight has more chances to be chosen than an edge with a lower
	 * weight.
	 * </p>
	 * 
	 * <p>
	 * When crossed, if the memory is larger than 0, the edge crossed is remembered
	 * so that the entity will not choose it anew until it crosses as many edges as
	 * the memory size.
	 * </p>
	 */
	protected void tabuStep() {
		int n = current.getOutDegree();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		
		current.leavingEdges()
			.filter(e -> !tabu(e.getOpposite(current)))
			.forEach(e -> edges.add(e));

		n = edges.size();

		if (n == 0) {
			jump();
		} else {
			if (context.weightAttribute != null) {
				if (weights == null || n > weights.length)
					weights = new double[n];

				double sum = 0.0;

				for (int i = 0; i < n; i++) {
					weights[i] = weight(edges.get(i));
					sum += weights[i];
				}

				for (int i = 0; i < n; ++i)
					weights[i] /= sum;

				double r = context.random.nextDouble();
				double s = 0;

				for (int i = 0; i < n; i++) {
					s += weights[i];

					if (r < s) {
						cross(edges.get(i));
						i = n;
					}
				}
			} else {
				cross(edges.get(context.random.nextInt(n)));
			}
		}
	}

	/**
	 * Make the entity jump to a randomly chosen node.
	 */
	protected void jump() {
		current = randomNode(context.graph, context.random);
		context.jumpCount++;
	}

	/**
	 * Cross the given edge, eventually storing it in the memory and
	 * incrementing its count as well as the count of the current node.
	 * @param e The edge.
	 */
	protected void cross(Edge e) {
		current = e.getOpposite(current);
		addPass(e, current);
		addToTabu(current);
	}

	/**
	 * Increment the count of the given node and edge.
	 * @param e The edge.
	 * @param n The node.
	 */
	protected void addPass(Edge e, Node n) {
		e.setAttribute(context.passesAttribute, e.getNumber(context.passesAttribute) + 1);
		n.setAttribute(context.passesAttribute, n.getNumber(context.passesAttribute) + 1);
	}

	/**
	 * Add a node to the tabu list.
	 * @param node The node to avoid.
	 */
	protected void addToTabu(Node node) {
		if (context.entityMemory > 0) {
			memory.addFirst(node);

			if (memory.size() > context.entityMemory)
				memory.removeLast();
		}
	}

	/**
	 * Is the given node tabu ?
	 * @param node The node to test.
	 * @return true if the node is tabu.
	 */
	protected boolean tabu(Node node) {
		if (node.hasAttribute("tabu"))
			return true;

		if (context.entityMemory > 0) {
			if (memory == null)
				memory = new LinkedList<Node>();

			int n = memory.size();

			for (int i = 0; i < n; i++) {
				if (node == memory.get(i))
					return true;
			}
		}

		return false;
	}
}
