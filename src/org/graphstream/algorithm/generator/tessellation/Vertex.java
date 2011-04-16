package org.graphstream.algorithm.generator.tessellation;

import java.util.LinkedList;
import java.util.List;

import org.graphstream.ui.geom.Point2;

/**
 * A vertex of the Voronoï/Dirchlet tessellation, representing a Voronoï territory.
 * 
 * <p>
 * A vertex is formed by three points (p0, p1, p2) that make the Delaunay triangle dual of
 * the Voronoï/Dirichlet territory of the vertex. Through the edges of this triangle, the
 * vertex is neighbor of three other vertices (v0, v1, v2).
 * </>
 * 
 * <p>
 * This class data structure is the one described in the Boyer article (See VoronoiTessellation).
 * </p>
 */
public class Vertex {
	
	public int id;
	
	public int p0, p1, p2;
	
	public int v0, v1, v2;
	
	/** Is this vertex an incomplete vertex (having only two forming points and one neighbour)? */
	public boolean incomplete = false;
	
	/** The vertex position. */
	public Point2 position = null;

	/** The radius of the circumcircle (circle passing by the three forming points whose
	 * centre is the position of this vertex). */
	public double radius = 0.0;
	
	/**
	 * Build a vertex.
	 *
	 * @param id The index of this vertex in the vertex pool.
	 * @param p0 The first forming point of the vertex.
 	 * @param p1 The second forming point of the vertex.
	 * @param p2 The third forming point of the vertex.
 	 * @param v0 The first neighbour vertex.
 	 * @param v1 The second neighbour vertex.
 	 * @param v2 The third neighbour vertex.
 	 * @param points The point pool where point coordinates are stored.
	 */ 
	public Vertex(int id, int p0, int p1, int p2, int v0, int v1, int v2, PointPool points) {
		this.id = id;
		this.p0 = p0;
		this.p1 = p1;
		this.p2 = p2;
		this.v0 = v0;
		this.v1 = v1;
		this.v2 = v2;
		
		assert p1 != p0 && p1 != p2 && p0 != p2 : String.format("vertex(%s) bad points(%d %d %d)", id, p0, p1, p2);
//		assert v0 != v1 && v1 != v2 && v2 != v0 : String.format("vertex(%s) bad vertices(%d %d %d)", id, v0, v1, v2);
		
		position = computeCircumcircle(points);
		radius = computeRadius(points);
	}

	/**
	 * Build an incomplete vertex, that is one that points outside of the considered area.
	 * 
	 * <p>
	 * An incomplete vertex "leaves" the tessellation area. It defines an "open" area and is thus
	 * formed by only two points, and has only one neighbour vertex.
	 * </p>
	 * 
	 * @param id The index of this vertex in the vertex pool.
	 * @param p0 The first forming point of the vertex.
	 * @param p1 The second forming point of the vertex.
	 * @param v0 The unique neighbour vertex.
	 */
	public Vertex(int id, int p0, int p1, int v0, PointPool points) {
		this(id, p0, p1, -1, v0, -1, -1, points);
		incomplete = true;
	}
	
	/** True if the given point p is closer to this vertex position than its three
	 * forming points. */
	public boolean pointCloserThanFormingPoints(Point2 p, PointPool points) {
		if(incomplete) {
			return false;
		} else {
			Point2 pp0 = points.get(p0);
			Point2 pp1 = points.get(p1);
			Point2 pp2 = points.get(p2);
			double len = position.distance(p);
				
			return (position.distance(pp0)>len || position.distance(pp1)>len || position.distance(pp2)>len);
		}
	}
	
	/** Compute the circumcircle radius. The circumcircle is the circle whose centre is this
	 * vertex position and that passes by the three forming points. */
	protected double computeRadius(PointPool points) {
		if(position != null) {
			return points.get(p0).distance(position);
		} else {
			return 0.0;
		}
	}
	
	/** Compute the position of this vertex, by computing the circumcircle of the three
	 * forming points. The result is the centre of this circumcircle. */
	protected Point2 computeCircumcircle(PointPool points) {
		if(p0>=0 && p1>=0 && p2>=0) {
			// We compute a circumcircle as the intersection point of the three median of each
			// edge of the triangle.
		
			double ax  = points.get(p0).x;
			double ay  = points.get(p0).y;
			double aax = ax * ax;
			double aay = ay * ay;
			double bx  = points.get(p1).x;
			double by  = points.get(p1).y;
			double bbx = bx * bx;
			double bby = by * by;
			double cx  = points.get(p2).x;
			double cy  = points.get(p2).y;
			double ccx = cx * cx;
			double ccy = cy * cy;
			
			double D = 2 * ( ax * ( by - cy ) + bx * ( cy - ay ) + cx * ( ay - by ) );
			double A = ( (aax+aay)*(by-cy) + (bbx+bby)*(cy-ay) + (ccx+ccy)*(ay-by) ) / D;
			double B = ( (aax+aay)*(cx-bx) + (bbx+bby)*(ax-cx) + (ccx+ccy)*(bx-ax) ) / D;
			
			return new Point2((float)A, (float)B);
		} else { 
			return null;
		}
	}
	
	/**
	 * Common points between the two vertices.
	 * @param other The other vertex.
	 * @return A list of points forming the two vertices.
	 */
	public List<Integer> commonPoints(Vertex other) {
		List<Integer> common = new LinkedList<Integer>();
		
		if(p0==other.p0 || p0==other.p1 || p0==other.p2) common.add( p0 );
		if(p1==other.p0 || p1==other.p1 || p1==other.p2) common.add( p1 );
		if(p2==other.p0 || p2==other.p1 || p2==other.p2) common.add( p2 );
		
		return common;
	}
	
	/**
	 * True if this vertex has the two points p0 and p1.
	 * This can be used to quickly check if two vertices share a point contiguity (an edge of
	 * a Delaunay triangle).
	 */
	public boolean sharePoints(int p0, int p1) {
		return ((p0 == this.p0 || p0 == this.p1 || p0 == this.p2)
		      &&(p1 == this.p0 || p1 == this.p1 || p1 == this.p2));
	}
	
	@Override
	public String toString() {
		if(incomplete) {
			return String.format("[#vertex %d (%d %d) (%d)]", id, p0, p1, v0);
		} else {
			return String.format("[vertex %d (%d %d %d) (%d %d %d)]", id, p0, p1, p2, v0, v1, v2);
		}
	}
}