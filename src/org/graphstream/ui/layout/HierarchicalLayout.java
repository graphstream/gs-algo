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
package org.graphstream.ui.layout;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.PipeBase;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swingViewer.Viewer;

public class HierarchicalLayout extends PipeBase implements Layout {

	public static enum Rendering {
		UP_TO_DOWN, DOWN_TO_UP, LEFT_TO_RIGHT, RIGHT_TO_LEFT, DISK
	}

	static class Position {
		int level;
		int order;
		String parent;
		boolean changed;
		double x, y;

		Position(int level, int order) {
			this.level = level;
			this.order = order;
			this.changed = true;
		}
	}

	final HashMap<String, Position> nodesPosition;
	final LinkedList<String> roots;
//	final LinkedList<LayoutListener> listeners;
	final Graph internalGraph;

	boolean structureChanged;

	Rendering renderingType;

	Point3 hi, lo;

	long lastStep;

	int nodeMoved;

	public HierarchicalLayout() {
		roots = new LinkedList<String>();
//		listeners = new LinkedList<LayoutListener>();
		nodesPosition = new HashMap<String, Position>();
		internalGraph = new AdjacencyListGraph("hierarchical_layout-intern");
		hi = new Point3();
		lo = new Point3();
	}

	public void setRoots(String... rootsId) {
		roots.clear();

		if (rootsId != null) {
			for (String id : rootsId)
				roots.add(id);
		}
	}

//	public void addListener(LayoutListener listener) {
//		listeners.add(listener);
//	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public void compute() {
		nodeMoved = 0;

		if (structureChanged)
			computePositions();

		publishPositions();
		lastStep = System.currentTimeMillis();
	}

	protected void computePositions() {
		LinkedList<String> currentLevel = new LinkedList<String>();
		LinkedList<String> nextLevel = new LinkedList<String>();
		HashSet<Node> visited = new HashSet<Node>();

		int level = 0;
		int order = 0;

		int[] levels = {};
		double r, d;

		currentLevel.addAll(roots);

		while (currentLevel.size() > 0) {
			for (order = 0; order < currentLevel.size(); order++) {
				Node n = internalGraph.getNode(currentLevel.get(order));
				Position p = nodesPosition.get(n.getId());

				visited.add(n);

				if (p.level != 0 || p.order != order) {
					p.level = 0;
					p.order = order;
					p.changed = true;
				}

				for (int i = 0; i < n.getOutDegree(); i++) {
					Edge e = n.getLeavingEdge(i);
					Node o = e.getOpposite(n);

					if (!visited.contains(o)
							&& !currentLevel.contains(o.getId())
							&& !nextLevel.contains(o)) {
						Position op = nodesPosition.get(o.getId());

						op.parent = n.getId();
						nextLevel.add(o.getId());
					}
				}
			}

			level++;
		}

		for (Position p : nodesPosition.values()) {
			if (p.level > levels.length)
				levels = Arrays.copyOf(levels, p.level);

			levels[p.level] = Math.max(p.order, levels[p.level]);
		}

		hi.x = hi.y = Double.MIN_VALUE;
		lo.x = lo.y = Double.MAX_VALUE;

		for (Node n : internalGraph) {
			Position p = nodesPosition.get(n.getId());

			if (p != null) {
				if (p.changed) {
					p.changed = false;
					nodeMoved++;

					if (roots.size() > 1)
						r = p.level + 1;
					else
						r = p.level;

					d = p.order * 2 * Math.PI / (levels[p.level] + 1);

					p.x = r * Math.cos(d);
					p.y = r * Math.sin(d);
				}

				hi.x = Math.max(hi.x, p.x);
				hi.y = Math.max(hi.y, p.y);
				lo.x = Math.min(lo.x, p.x);
				lo.y = Math.min(lo.y, p.y);
			}
		}
	}

	protected void publishPositions() {
		for (Node n : internalGraph) {
			Position p = nodesPosition.get(n.getId());

			if (p != null && p.changed) {
				p.changed = false;

				sendNodeAttributeChanged(sourceId, n.getId(), "xyz", null,
						new double[] { p.x, p.y, 0 });
				
			//	for (int i = 0; i < listeners.size(); i++)
			//		listeners.get(i).nodeMoved(n.getId(), p.x, p.y, 0);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#freezeNode(java.lang.String,
	 * boolean)
	 */
	public void freezeNode(String id, boolean frozen) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getForce()
	 */
	public double getForce() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getHiPoint()
	 */
	public Point3 getHiPoint() {
		return hi;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getLastStepTime()
	 */
	public long getLastStepTime() {
		return lastStep;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getLayoutAlgorithmName()
	 */
	public String getLayoutAlgorithmName() {
		return "Hierarchical";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getLowPoint()
	 */
	public Point3 getLowPoint() {
		return lo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getNodeMoved()
	 */
	public int getNodeMovedCount() {
		return nodeMoved;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getQuality()
	 */
	public double getQuality() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getStabilization()
	 */
	public double getStabilization() {
		return 1 - nodeMoved / (double) internalGraph.getNodeCount();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getStabilizationLimit()
	 */
	public double getStabilizationLimit() {
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#getSteps()
	 */
	public int getSteps() {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#inputPos(java.lang.String)
	 */
	public void inputPos(String filename) throws IOException {
		throw new UnsupportedOperationException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#moveNode(java.lang.String, double,
	 * double, double)
	 */
	public void moveNode(String id, double x, double y, double z) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#outputPos(java.lang.String)
	 */
	public void outputPos(String filename) throws IOException {
		throw new UnsupportedOperationException();
	}

//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see
//	 * org.graphstream.ui.layout.Layout#removeListener(org.graphstream.ui.layout
//	 * .LayoutListener)
//	 */
//	public void removeListener(LayoutListener listener) {
//		listeners.remove(listener);
//	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#setForce(double)
	 */
	public void setForce(double value) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#setQuality(int)
	 */
	public void setQuality(double qualityLevel) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#setSendNodeInfos(boolean)
	 */
	public void setSendNodeInfos(boolean send) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#setStabilizationLimit(double)
	 */
	public void setStabilizationLimit(double value) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.ui.layout.Layout#shake()
	 */
	public void shake() {
		// No, I will not shake my work
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.PipeBase#nodeAdded(java.lang.String, long,
	 * java.lang.String)
	 */
	public void nodeAdded(String sourceId, long timeId, String nodeId) {
		internalGraph.addNode(nodeId);
		structureChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.PipeBase#nodeRemoved(java.lang.String, long,
	 * java.lang.String)
	 */
	public void nodeRemoved(String sourceId, long timeId, String nodeId) {
		internalGraph.removeNode(nodeId);
		structureChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.PipeBase#edgeAdded(java.lang.String, long,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String fromId, String toId, boolean directed) {
		internalGraph.addEdge(edgeId, fromId, toId, directed);
		structureChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.PipeBase#edgeRemoved(java.lang.String, long,
	 * java.lang.String)
	 */
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		internalGraph.removeEdge(edgeId);
		structureChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.PipeBase#graphCleared(java.lang.String, long)
	 */
	public void graphCleared(String sourceId, long timeId) {
		internalGraph.clear();
		structureChanged = true;
	}
	
	public static void main(String ... args) {
		Graph g = new AdjacencyListGraph("g");
		g.addNode("root");
		g.addNode("0");
		g.addNode("1");
		g.addNode("2");
		g.addNode("0-0");
		g.addNode("0-1");
		g.addNode("1-0");
		g.addNode("1-1");
		g.addNode("1-2");
		g.addNode("2-0");
		
		Viewer v = g.display(false);
		v.enableAutoLayout(new HierarchicalLayout());
	}
}
