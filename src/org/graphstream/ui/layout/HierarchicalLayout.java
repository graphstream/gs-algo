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
 * @since 2012-01-24
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.ui.layout;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import org.graphstream.algorithm.Prim;
import org.graphstream.algorithm.SpanningTree;
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator;
import org.graphstream.algorithm.util.FibonacciHeap;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AdjacencyListGraph;
import org.graphstream.stream.PipeBase;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.view.Viewer;

public class HierarchicalLayout extends PipeBase implements Layout {

	public static enum Rendering {
		VERTICAL, HORIZONTAL, DISK
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
	final Graph internalGraph;

	boolean structureChanged;

	Rendering renderingType;

	Point3 hi, lo;

	long lastStep;

	int nodeMoved;

	double distanceBetweenLevels = 1;
	double levelWidth = 1, levelHeight = 1;

	public HierarchicalLayout() {
		roots = new LinkedList<String>();
		// listeners = new LinkedList<LayoutListener>();
		nodesPosition = new HashMap<String, Position>();
		internalGraph = new AdjacencyListGraph("hierarchical_layout-intern");
		hi = new Point3();
		lo = new Point3();
		renderingType = Rendering.VERTICAL;
	}

	public void setRoots(String... rootsId) {
		roots.clear();

		if (rootsId != null) {
			for (String id : rootsId)
				roots.add(id);
		}
	}

	// public void addListener(LayoutListener listener) {
	// listeners.add(listener);
	// }

	public void clear() {
		// TODO Auto-generated method stub

	}

	public void compute() {
		nodeMoved = 0;

		if (structureChanged) {
			structureChanged = false;
			computePositions();
		}

		publishPositions();
		lastStep = System.currentTimeMillis();
	}

	protected void computePositions() {
		final int[] levels = new int[internalGraph.getNodeCount()];
		Arrays.fill(levels, -1);

		final int[] columns = new int[internalGraph.getNodeCount()];

		LinkedList<Node> roots = new LinkedList<Node>(), roots2 = new LinkedList<Node>();

		if (this.roots.size() > 0) {
			for (int i = 0; i < this.roots.size(); i++)
				roots.add(internalGraph.getNode(this.roots.get(i)));
		}

		SpanningTree tree = new Prim("weight", "inTree");
		tree.init(internalGraph);
		tree.compute();

		if (roots.size() == 0) {
			int max = internalGraph.getNode(0).getDegree();
			int maxIndex = 0;

			for (int i = 1; i < internalGraph.getNodeCount(); i++)
				if (internalGraph.getNode(i).getDegree() > max) {
					max = internalGraph.getNode(i).getDegree();
					maxIndex = i;
				}

			roots.add(internalGraph.getNode(maxIndex));
		}

		Box rootBox = new Box();
		LevelBox rootLevelBox = new LevelBox(0);
		LinkedList<LevelBox> levelBoxes = new LinkedList<LevelBox>();

		rootLevelBox.add(rootBox);
		levelBoxes.add(rootLevelBox);

		for (int i = 0; i < roots.size(); i++) {
			Node n = roots.get(i);
			levels[n.getIndex()] = 0;
			columns[n.getIndex()] = i;
			setBox(rootBox, n);
		}

		do {
			while (roots.size() > 0) {
				Node root = roots.poll();
				int level = levels[root.getIndex()] + 1;
				Box box = getChildrenBox(root);
				
				root.edges()
					.filter(e -> e.getAttribute(tree.getFlagAttribute()).equals(tree.getFlagOn()))
					.forEach(e -> {
						Node op = e.getOpposite(root);

						if (levels[op.getIndex()] < 0 || level < levels[op.getIndex()]) {
							levels[op.getIndex()] = level;
							roots2.add(op);
							op.setAttribute("parent", root);
							setBox(box, op);
						}
					});
				
			}

			roots.addAll(roots2);
			roots2.clear();
		} while (roots.size() > 0);

		FibonacciHeap<Integer, Box> boxes = new FibonacciHeap<Integer, Box>();
		boxes.add(0, rootBox);

		for (int i = 0; i < internalGraph.getNodeCount(); i++) {
			Box box = getChildrenBox(internalGraph.getNode(i));

			if (box != null) {
				boxes.add(box.level, box);

				while (levelBoxes.size() <= box.level)
					levelBoxes.add(new LevelBox(levelBoxes.size()));

				levelBoxes.get(box.level).add(box);
			}
		}

		for (int i = 0; i < levelBoxes.size(); i++)
			levelBoxes.get(i).sort();

		while (boxes.size() > 0)
			renderBox(boxes.extractMin());

		hi.x = hi.y = Double.MIN_VALUE;
		lo.x = lo.y = Double.MAX_VALUE;

		for (int idx = 0; idx < internalGraph.getNodeCount(); idx++) {
			Node n = internalGraph.getNode(idx);
			double y = n.getNumber("y");
			double x = n.getNumber("x");

			if (!n.hasNumber("oldX") || n.getNumber("oldX") != x
					|| !n.hasNumber("oldY") || n.getNumber("oldY") != y) {
				n.setAttribute("oldX", x);
				n.setAttribute("oldY", y);
				n.setAttribute("changed");
				nodeMoved++;
			}

			hi.x = Math.max(hi.x, x);
			hi.y = Math.max(hi.y, y);
			lo.x = Math.min(lo.x, x);
			lo.y = Math.min(lo.y, y);
		}
	}

	protected void setBox(Box box, Node node) {
		if (node.hasAttribute("box"))
			getBox(node).remove(node);

		box.add(node);
		node.setAttribute("box", box);

		if (!node.hasAttribute("children"))
			node.setAttribute("children", new Box(node, 1));

		getChildrenBox(node).level = box.level + 1;
	}

	protected static Box getBox(Node node) {
		Box box = (Box) node.getAttribute("box");
		return box;
	}

	protected static Box getChildrenBox(Node node) {
		Box box = (Box) node.getAttribute("children");
		return box;
	}

	protected void renderBox(Box box) {
		if (box.size() == 0)
			return;

		for (int i = 0; i < box.size(); i++) {
			Node n = box.get(i);

			switch (renderingType) {
			case VERTICAL:
				n.setAttribute("x", box.width * i / (double) box.size());
				n.setAttribute("y", box.height / 2);
				break;
			case DISK:
			case HORIZONTAL:
				n.setAttribute("x", box.width / 2);
				n.setAttribute("y", box.height * i / (double) box.size());
				break;
			}
		}

		double sx = 1, sy = 1;
		double dx = 0, dy = 0;

		if (box.parent != null) {
			Box parentBox = getBox(box.parent);

			switch (renderingType) {
			case VERTICAL:
				sx = 1 / (double) parentBox.size();
				sy = 1 / Math.pow(2, box.level);
				break;
			case DISK:
			case HORIZONTAL:
				sx = 1 / Math.pow(2, box.level);
				sy = 1 / (double) parentBox.size();
				break;
			}
		}

		box.scale(sx, sy);

		if (box.parent != null) {
			Box parentBox = getBox(box.parent);

			dx = box.parent.getNumber("x");
			dy = box.parent.getNumber("y");

			switch (renderingType) {
			case VERTICAL:
				dx -= box.width / 2;
				dy += parentBox.height / 2;
				break;
			case DISK:
			case HORIZONTAL:
				dx += parentBox.width / 2;
				dy -= box.height / 2;
				break;
			}
		}

		box.translate(dx, dy);
	}

	protected void explore(Node parent, Node who, SpanningTree tree,
			int[] levels) {

	}

	protected void publishPositions() {
		
		internalGraph.nodes()
			.filter(n -> n.hasAttribute("changed"))
			.forEach(n -> {
				n.removeAttribute("changed");

				sendNodeAttributeChanged(sourceId, n.getId(), "xyz", null,
						new double[] { n.getNumber("x"), n.getNumber("y"), 0 });
			});
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

	// /*
	// * (non-Javadoc)
	// *
	// * @see
	// *
	// org.graphstream.ui.layout.Layout#removeListener(org.graphstream.ui.layout
	// * .LayoutListener)
	// */
	// public void removeListener(LayoutListener listener) {
	// listeners.remove(listener);
	// }

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

	static class Box extends LinkedList<Node> {
		private static final long serialVersionUID = -1929536876444346726L;

		Node parent;
		int level;
		double x, y;
		double width, height;
		int order;

		Box() {
			this(null, 0);
		}

		Box(Node parent, int level) {
			this.parent = parent;
			this.level = level;
			this.width = 5;
			this.height = 1;
			this.order = 0;
			this.x = 0;
			this.y = 0;
		}

		void scale(double sx, double sy) {
			width *= sx;
			height *= sy;

			for (int i = 0; i < size(); i++) {
				get(i).setAttribute("x", sx * get(i).getNumber("x"));
				get(i).setAttribute("y", sy * get(i).getNumber("y"));
			}
		}

		void translate(double dx, double dy) {
			for (int i = 0; i < size(); i++) {
				get(i).setAttribute("x", dx + get(i).getNumber("x"));
				get(i).setAttribute("y", dy + get(i).getNumber("y"));
			}
		}
	}

	static class LevelBox extends LinkedList<Box> {
		private static final long serialVersionUID = -5818919480025868466L;

		int level;

		LevelBox(int level) {
			this.level = level;
		}

		void sort() {
			if (level > 0) {
				Collections.sort(this, new Comparator<Box>() {
					public int compare(Box b0, Box b1) {
						Box pb0 = getBox(b0.parent);
						Box pb1 = getBox(b1.parent);

						if (pb0.order < pb1.order)
							return -1;
						else if (pb0.order > pb1.order)
							return 1;

						return 0;
					}
				});
			}

			for (int i = 0; i < size(); i++)
				get(i).order = i;
		}
	}

	public static void main(String... args) {
		Graph g = new AdjacencyListGraph("g");
		BarabasiAlbertGenerator gen = new BarabasiAlbertGenerator();
		HierarchicalLayout hl = new HierarchicalLayout();
		gen.addSink(g);
		gen.begin();
		for (int i = 0; i < 200; i++)
			gen.nextEvents();
		gen.end();

		Viewer v = g.display(false);
		v.enableAutoLayout(hl);
	}
}
