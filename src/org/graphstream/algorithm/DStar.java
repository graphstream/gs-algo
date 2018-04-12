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
 * @since 2011-10-12
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.StringJoiner;

import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.algorithm.generator.Generator;
import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.Sink;

/**
 * An implementation of the D* algorithm.
 * 
 * @author Guilhelm Savin
 * 
 * @reference Stentz, Anthony (1994),
 *            "Optimal and Efficient Path Planning for Partially-Known Environments"
 *            , Proceedings of the International Conference on Robotics and
 *            Automation: 3310–3317
 */
public class DStar implements DynamicAlgorithm, Sink {

	protected static enum Tag {
		NEW, OPEN, CLOSED, LOWER, RAISE
	}

	public static final String STATE_ATTRIBUTE = "d*.state";
	public static final String COST_ATTRIBUTE = "d*.cost";

	protected String edgeWeightAttribute;
	protected double defaultEdgeWeight;
	protected State g, position;
	protected LinkedList<State> openList;
	protected Graph env;

	protected final Comparator<State> stateComparator = new Comparator<State>() {
		public int compare(State o1, State o2) {
			return (int) Math.signum(k(o1) - k(o2));
		}
	};

	public DStar() {
		edgeWeightAttribute = "weight";
		defaultEdgeWeight = 1;
		g = null;
		env = null;
		openList = new LinkedList<State>();
	}

	public void terminate() {
		env.removeSink(this);
	}

	public void compute() {
		while (processState() >= 0 && position.t != Tag.CLOSED)
			;
	}

	public void init(Graph graph) {
		openList.clear();
		env = graph;
		env.addSink(this);
	}

	public void init(Node source, Node target, Graph graph) {
		init(graph);
		g = getState(target);
		g.h = 0;
		insert(g);

		position = getState(source);
	}
	
	@Parameter(true)
	public void setTarget(String target) {
		g = getState(env.getNode(target));
		g.h = 0;
		insert(g);
	}

	@Parameter(true)
	public void setSource(String source) {
		position = getState(env.getNode(source));
	}
	
	protected State minState() {
		Collections.sort(openList, stateComparator);
		return openList.getFirst();
	}

	protected double getKMin() {
		double kmin = Double.MAX_VALUE;

		for (State x : openList)
			kmin = Math.min(kmin, k(x));

		return kmin;
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

				assert !Double.isNaN(x.h);
			}
		}

		// L13 - L57
		for (State y : x) {
			if (y.t == Tag.NEW) {
				y.b = x;
				y.h = x.h + c(x, y);
				y.p = y.h;

				assert !Double.isNaN(y.h);
				insert(y);
			} else {
				if (y.b == x && y.h != x.h + c(x, y)) {
					// L24 - L 27
					if (y.t == Tag.OPEN) {
						if (y.h < y.p)
							y.p = y.h;
						y.h = x.h + c(x, y);

						assert !Double.isNaN(y.h);
					}
					// L28 - L31
					else {
						y.h = x.h + c(x, y);
						y.p = y.h;

						assert !Double.isNaN(y.h);
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

							assert !Double.isNaN(y.h);
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

							assert !Double.isNaN(y.h);
							insert(y);
						}
					}
				}
			}
		}

		return getKMin();
	}

	protected void modifyCost(String edgeId, double cval) {
		Edge e = env.getEdge(edgeId);

		if (e.isDirected())
			modifyCost(getState(e.getSourceNode()),
					getState(e.getTargetNode()), cval);
		else {
			modifyCost(getState(e.getNode0()), getState(e.getNode1()), cval);
			modifyCost(getState(e.getNode1()), getState(e.getNode0()), cval);
		}
	}

	protected void modifyCost(State x, State y, double cval) {
		Edge e = x.node.getEdgeBetween(y.node);

		if (e != null)
			e.setAttribute(COST_ATTRIBUTE, cval);

		if (x.b == y)
			x.b = null;
		
		if (x.t == Tag.CLOSED) {
			x.p = x.h;
			insert(x);
		}
	}

	public State getState(Node n) {
		State s = (State) n.getAttribute(STATE_ATTRIBUTE);

		if (s == null) {
			s = new State(n);
			n.setAttribute(STATE_ATTRIBUTE, s);
		}

		return s;
	}

	protected double c(State x, State y) {
		Edge e = x.node.getEdgeBetween(y.node);

		if (e != null) {
			if (e.hasNumber(COST_ATTRIBUTE))
				return e.getNumber(COST_ATTRIBUTE);
			else
				return defaultEdgeWeight;
		}

		return Double.NaN;
	}

	protected double k(State x) {
		if (x.t != Tag.OPEN)
			return Double.NaN;

		return Math.min(x.h, x.p);
	}

	protected void insert(State x) {
		openList.add(x);
		x.t = Tag.OPEN;
		x.p = x.h;
	}

	protected void delete(State x) {
		openList.remove(x);
		x.t = Tag.CLOSED;
	}

	protected boolean isMonotonic(State xn, int n) {
		State xi1 = xn;
		State xi = xi1.b;

		for (int i = n; i > 0; i--) {
			if (!((xi.t == Tag.CLOSED && xi.h < xi1.h) || (xi.t == Tag.OPEN && xi.p < xi1.h)))
				return false;

			xi1 = xi;
			xi = xi1.b;
		}

		return true;
	}

	public void markPath(String attribute, Object on, Object off) {
		env.nodes().forEach(n -> n.setAttribute(attribute, off));
		env.edges().forEach(e -> e.setAttribute(attribute, off));

		State s = position;

		do {
			s.node.setAttribute(attribute, on);
			s.node.getEdgeBetween(s.b.node).setAttribute(attribute, on);
			s = s.b;
		} while (s != g);

		g.node.setAttribute(attribute, on);
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

		/**
		 * 
		 */
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

	public void edgeAttributeAdded(String sourceId, long timeId, String edgeId,
			String attribute, Object value) {
		if (attribute.equals(edgeWeightAttribute) && value != null
				&& value instanceof Number)
			modifyCost(edgeId, ((Number) value).doubleValue());
	}

	public void edgeAttributeChanged(String sourceId, long timeId,
			String edgeId, String attribute, Object oldValue, Object newValue) {
		if (attribute.equals(edgeWeightAttribute) && newValue != null
				&& newValue instanceof Number)
			modifyCost(edgeId, ((Number) newValue).doubleValue());
	}

	public void edgeAttributeRemoved(String sourceId, long timeId,
			String edgeId, String attribute) {
		// Nothing to do
	}

	public void graphAttributeAdded(String sourceId, long timeId,
			String attribute, Object value) {
		// Nothing to do
	}

	public void graphAttributeChanged(String sourceId, long timeId,
			String attribute, Object oldValue, Object newValue) {
		// Nothing to do
	}

	public void graphAttributeRemoved(String sourceId, long timeId,
			String attribute) {
		// Nothing to do
	}

	public void nodeAttributeAdded(String sourceId, long timeId, String nodeId,
			String attribute, Object value) {
		// Nothing to do
	}

	public void nodeAttributeChanged(String sourceId, long timeId,
			String nodeId, String attribute, Object oldValue, Object newValue) {
		// Nothing to do
	}

	public void nodeAttributeRemoved(String sourceId, long timeId,
			String nodeId, String attribute) {
		// Nothing to do
	}

	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed) {
		modifyCost(edgeId, defaultEdgeWeight);
	}

	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		modifyCost(edgeId, Double.NaN);
	}

	public void graphCleared(String sourceId, long timeId) {
		//
		// WTF ? You want a shortest path but you clear the graph.
		//
		throw new RuntimeException();
	}

	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		// Nothing to do
	}

	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		Node n = env.getNode(nodeId);
		State s = getState(n);

		if (s == g || s == position) {
			//
			// WTF ? How does the robot go to g if you remove it ??
			//
			throw new RuntimeException();
		}

		Iterator<State> it = new NeighborStateIterator(s);

		while (it.hasNext()) {
			State o = it.next();

			modifyCost(s, o, Double.NaN);
			modifyCost(o, s, Double.NaN);
		}
	}

	public void stepBegins(String sourceId, long timeId, double step) {
		// Nothing to do
	}

	public static void main(String... args) {
		Generator gen = new DorogovtsevMendesGenerator();
		AdjacencyListGraph g = new AdjacencyListGraph("g");
		DStar dstar = new DStar();
		Random r = new Random();
		boolean alive = true;

		g
				.setAttribute(
						"ui.stylesheet",
						"node.on { fill-color: red; } node.off { fill-color: black; } edge.on { fill-color: red; } edge.off { fill-color: black; }");

		gen.addSink(g);
		g.display(true);

		gen.begin();
		for (int i = 0; i < 150; i++)
			gen.nextEvents();

		dstar.init(Toolkit.randomNode(g), Toolkit.randomNode(g), g);

		do {
			dstar.compute();
			dstar.markPath("ui.class", "on", "off");

			try {
				Thread.sleep(2000);
			} catch (Exception e) {
			}

			State s = dstar.position;

			while (s.b != dstar.g && s.b != null && r.nextBoolean())
				s = s.b;

			if (r.nextBoolean() && s != dstar.position && s.b != dstar.g) {
				g.removeNode(s.node);
			} else {
				g.removeEdge(s.node.getEdgeBetween(s.b.node));
			}
			
			gen.nextEvents();
		} while (alive);

		gen.end();
		dstar.terminate();

	}
	
	@Result
	public String defaultResult() {
		StringJoiner sj = new StringJoiner(" | ", "====== DStar ====== \n", "");
		markPath("ui.DStar", "on", "off");
		
		env.nodes()
			.filter(n -> n.getAttribute("ui.DStar").equals("on"))
			.forEach(n -> sj.add(n.getId()));
		
		return sj.toString();

	}
}
