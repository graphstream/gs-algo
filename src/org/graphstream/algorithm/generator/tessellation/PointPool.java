package org.graphstream.algorithm.generator.tessellation;

import java.util.ArrayList;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point2;

/**
 * Set of points of the tessellation.
 */
class PointPool {
	ArrayList<Point2> points = new ArrayList<Point2>();
	
	protected Graph graph;
	
	public PointPool(Graph graph) {
		this.graph = graph;
	}
	
	public int add(Point2 p) {
		int id = points.size();
		points.add(p);
		Node node = graph.addNode(nodeId(id));
		node.setAttribute("xy", p.x, p.y);
		return id;
	}
	
	public Point2 get(int i) {
		return points.get(i);
	}
	
	protected String nodeId(int id) {
		return String.format("%d", id);
	}
}