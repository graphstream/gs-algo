package org.graphstream.algorithm.generator.tessellation;

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.ui.geom.Point2;

import static org.graphstream.algorithm.generator.tessellation.Ids.*;

public class DelaunayGenerator {

	protected PointPool points;
	
	protected VertexPool vertices;
	
	protected int first = -1;
	
	protected Graph graph = null;
	
	protected Point2 p0, p1, p2;
	
	protected int pp0, pp1, pp2;

	/**
	 * @param graph
	 * @param p0
	 * @param p1
	 * @param p2
	 */
	public DelaunayGenerator(Graph graph, Point2 p0, Point2 p1, Point2 p2) {
		this.graph = graph;
		this.points = new PointPool(graph);
		this.vertices = new VertexPool(points, graph);
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
	}
	
	/**
	 * @param graph
	 * @param bottomLeft
	 * @param topRight
	 */
	public DelaunayGenerator(Graph graph, Point2 bottomLeft, Point2 topRight) {
		this.graph = graph;
		this.points = new PointPool(graph);
		this.vertices = new VertexPool(points, graph);

		// We find an englobing triangle using a bounding box of the points.
		//
		//           b           The bounding box is defined by points d and x.
		//          /|\          We use the distance ac (height of the box) and double it to form
		//         / | \         bc. Then using Thales we know that ec = 2*dc. Having point b and e
		//        /  |  \        we now have our triangle.
		//       f---a---x
		//      /|   |   |\
		//     / |   |   | \
		//    /  |   |   |  \
		//   e---d---c---+---g
		//
		double width = (topRight.x-bottomLeft.x);
		double height = (topRight.y-bottomLeft.y);
		double gapx = width/100f;
		double gapy = height/100f;
		
		p0 = new Point2(bottomLeft.x+width/2, topRight.y+height+gapy);
		p1 = new Point2(bottomLeft.x-(width/2+gapx), bottomLeft.y-gapy);
		p2 = new Point2(topRight.x+(width/2+gapx), bottomLeft.y-gapy);
	}
	
	public void begin() {
		pp0 = points.add(p0);
		pp1 = points.add(p1);
		pp2 = points.add(p2);
		
		int none0 = vertices.addIncomplete(pp0, pp1, 3);
		int none1 = vertices.addIncomplete(pp1, pp2, 3);
		int none2 = vertices.addIncomplete(pp2, pp0, 3);

		first = vertices.add(pp0, pp1, pp2, none0, none1, none2);
	}
	
	public void addPoint(double x, double y) {
		Point2 p = new Point2(x, y);
		
		if(! inInitialTriangle(p)) 
			throw new RuntimeException(String.format("bad point (%s), out of initial triangle (%s, %s, %s)", p, p0, p1, p2));
		
		int pp = points.add(p);
		ArrayList<Vertex> del = vertices.computeListOfDeletedVertices(p);
		ArrayList<Segment> seg = computeSegments(del);
		
		removeSharedEdges(del);
		
		associateSegmentsWithPoints(pp, seg);
		associateSegmentsWithAdjVertices(seg, del);
		tieNewVertices(seg);
		tieOldVertices(seg);
	}
	
	public void end() {
		graph.removeNode(nodeId(pp0));
		graph.removeNode(nodeId(pp1));
		graph.removeNode(nodeId(pp2));
	}
	
	protected void removeSharedEdges(ArrayList<Vertex> del) {
		// The algorithms says : 4. an old contiguity between [...] points will
		// be removed [...] if all its vertices [...] are in the list of
		// deleted vertices. So say we all.
	
		ArrayList<Edge> toRemove = new ArrayList<Edge>();
		
		for(Vertex vertex: del) {
			// see all pairs p0--p1, p1--p2, p0--p2
			// Find their other vertex (one of v0, v1 or v2).
			// If the other vertex is in del.
			// place the corresponding edge in the deleted list.
			
			Edge edge = getEdge(vertex.p0, vertex.p1, graph);
			if(edge!=null && edgeMustDisapear(vertex, vertex.p0, vertex.p1, del)) toRemove.add(edge);
			edge = getEdge(vertex.p1, vertex.p2, graph);
			if(edge!=null && edgeMustDisapear(vertex, vertex.p1, vertex.p2, del)) toRemove.add(edge);  
			edge = getEdge(vertex.p0, vertex.p2, graph);
			if(edge!=null && edgeMustDisapear(vertex, vertex.p0, vertex.p2, del)) toRemove.add(edge);
		}

		for(Edge edge: toRemove)
			graph.removeEdge(edge.getId());

	}
	
	protected boolean edgeMustDisapear(Vertex vertex, int from, int to, ArrayList<Vertex> del) {
		Vertex other = vertices.get(vertex.v0);
		
		if(other != null && other.sharePoints(from, to)) {
			if(del.contains(other)) return true;
		}
		
		other = vertices.get(vertex.v1);
		
		if(other != null && other.sharePoints(from, to)) {
			if(del.contains(other)) return true;
		}
		
		other = vertices.get(vertex.v2);
		
		if(other != null && other.sharePoints(from, to)) {
			if(del.contains(other)) return true;
		}
		
		return false;
	}
	
	protected ArrayList<Segment> computeSegments(ArrayList<Vertex> del) {
		ArrayList<Segment> seg = new ArrayList<Segment>();

		for(Vertex source: del) {
			assert(source.v0 != source.v1 && source.v1 != source.v2 && source.v0 != source.v2):String.format("bad vertex %d linked vertices (%d %d %d)",source.id, source.v0, source.v1, source.v2);
			assert(! source.incomplete);
			
			try {
			Vertex target0 = vertices.get(source.v0);
			Vertex target1 = vertices.get(source.v1);
			Vertex target2 = vertices.get(source.v2);
				
			if(!del.contains(target0)) seg.add(new Segment(source, target0, points));
			if(!del.contains(target1)) seg.add(new Segment(source, target1, points));
			if(!del.contains(target2)) seg.add(new Segment(source, target2, points));
			} catch(Exception e) {
				e.printStackTrace();
				
				System.err.printf("%s%n", source);
			}
		}
			
		return seg;
	}
		
	protected void associateSegmentsWithPoints(int pp, ArrayList<Segment> seg) {
		for(Segment segment: seg) {	
			segment.associateWithPoints(pp, vertices, points);
		}
	}
		
	protected void associateSegmentsWithAdjVertices(ArrayList<Segment> seg, ArrayList<Vertex> del) {
		for(Segment segment: seg) {
			segment.createVertex(vertices, del);
		}
	}

	protected void tieNewVertices(ArrayList<Segment> seg) {
		for(Segment segment: seg) {
			segment.tieWithNewVertices(seg, vertices);
		}
	}
		
	protected void tieOldVertices(ArrayList<Segment> seg) {
		for(Segment segment: seg) {
			segment.tieWithOldVertices(seg, vertices);
		}
	}
		
	public boolean inInitialTriangle(Point2 p) {
		// We transform the point p into barycentric coordinates. Barycentric coordinates
		// express a point position according to a family of other points. For a triangle
		// made of the three points p1, p2 and p3,
		// the barycentric coordinates of a point p are (b1*p1), (b2*p2), (b3*p3), where
		// b1, b2, b3 are the coefficients of the barycentric coordinates of point p.
		//
		// If all barycentric coordinates are > 0 and < 1 then we are
		// in the triangle. If one of the coordinates is == 0 or == 1 it is along an edge
		// of the triangle. Else it is out of the triangle.
			
		Point2 p1 = this.p0; // points.get(vertices.get(first).p0);
		Point2 p2 = this.p1; //points.get(vertices.get(first).p1);
		Point2 p3 = this.p2; //points.get(vertices.get(first).p2);
		double b1 = ( (p2.y-p3.y)*(p.x-p3.x) + (p3.x-p2.x)*(p.y-p3.y) ) / ( (p2.y-p3.y)*(p1.x-p3.x) + (p3.x-p2.x)*(p1.y-p3.y) );
		double b2 = ( (p3.y-p1.y)*(p.x-p3.x) + (p1.x-p3.x)*(p.y-p3.y) ) / ( (p3.y-p1.y)*(p2.x-p3.x) + (p1.x-p3.x)*(p2.y-p3.y) );
		double b3 = ( 1 - b1 - b2 );

		return ( (b1 > 0 && b1 < 1) && (b2 > 0 && b2 < 1) && (b3 > 0 && b3 < 1) );
	}
}