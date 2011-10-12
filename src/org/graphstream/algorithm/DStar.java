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
package org.graphstream.algorithm;

import java.util.Iterator;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class DStar implements DynamicAlgorithm {

	protected static enum Tag {
		NEW, OPEN, CLOSED, LOWER, RAISE
	}

	protected String edgeWeightAttribute;
	protected State g;
	protected LinkedList<State> openList;

	public void terminate() {
		// TODO Auto-generated method stub

	}

	public void compute() {
		// TODO Auto-generated method stub

	}

	public void init(Graph graph) {
		// TODO Auto-generated method stub

	}

	protected State minState() {
		// TODO
		return null;
	}

	protected double getKMin() {
		// TODO
		return 0;
	}

	protected double processState() {
		State x;
		double kOld;

		// L1
		x = minState();

		// L2
		if (x == null)
			return -1;

		// L3
		kOld = getKMin();
		// L4
		delete(x);

		// L6 - L11
		for (State y : x) {
			if (y.t == Tag.CLOSED && y.h <= kOld && x.h > y.h + c(y, x)) {
				x.b = y;
				x.h = y.h + c(y, x);
			}
		}

		// L13 - L57
		for (State y : x) {
			if (y.t == Tag.NEW) {
				y.b = x;
				y.h = x.h + c(x, y);
				y.p = y.h;

				insert(y);
			} else {
				if (y.b == x && y.h != x.h + c(x, y)) {
					// L24 - L 27
					if (y.t == Tag.OPEN) {
						if (y.h < y.p)
							y.p = y.h;
						y.h = x.h + c(x, y);
					}
					// L28 - L31
					else {
						y.h = x.h + c(x, y);
						y.p = y.h;
					}
					insert(y);
				}
				// L34 -
				else {
					if (y.b != x && y.h > x.h + c(x, y)) {
						// L37 - L42
						if (x.p >= x.h) {
							y.b = x;
							y.h = x.h + c(x, y);

							if (y.t == Tag.CLOSED)
								y.p = y.h;

							insert(y);
						}
						// L43 - 46
						else {
							x.p = x.h;
							insert(x);
						}
					}
					// L47 - 54
					else {
						if (y.b != x && x.h > y.h + c(y, x)
								&& y.t == Tag.CLOSED && y.h > kOld) {
							y.p = y.h;
							insert(y);
						}
					}
				}
			}
		}

		return getKMin();
	}

	protected double modifyCost(State x, State y, double cval) {

		if (x.t == Tag.CLOSED) {
			x.p = x.h;
			insert(x);
		}
		
		return getKMin();
	}

	public State getState(Node n) {
		State s = n.getAttribute("d*.state");

		if (s == null) {
			s = new State(n);
			n.addAttribute("d*.state", s);
		}

		return s;
	}

	protected double c(State x, State y) {
		// TODO
		return 0;
	}

	protected double k(State x) {
		return Math.min(x.h, x.p);
	}

	protected void insert(State x) {
		openList.add(x);
	}

	protected void delete(State x) {
		openList.remove(x);
	}

	protected class State implements Iterable<State> {
		/**
		 * Associated node
		 */
		Node node;

		/**
		 * Associated tag
		 */
		Tag t;

		/**
		 * Backpointer
		 */
		State b;

		/**
		 * Previous cost
		 */
		double p;

		double h;

		public State(Node node) {
			this.node = node;
			t = Tag.NEW;
			b = null;
			p = 0;
			h = 0;
		}

		public Iterator<State> iterator() {
			return new NeighborStateIterator(this);
		}
	}

	protected class NeighborStateIterator implements Iterator<State> {

		Node source;
		Iterator<Edge> it;

		public NeighborStateIterator(State x) {
			source = x.node;
			it = source.iterator();
		}

		public boolean hasNext() {
			return it.hasNext();
		}

		public State next() {
			return getState(it.next().getOpposite(source));
		}

		public void remove() {
		}
	}
}
