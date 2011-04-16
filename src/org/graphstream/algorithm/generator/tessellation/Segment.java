package org.graphstream.algorithm.generator.tessellation;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a segment around an added point.
 * 
 * <p>
 * Such a segments will have an existing vertex as target and as source a new vertex. The new vertex
 * will have as forming point the newly added point when tessellating.
 * </p>
 * 
 * @author Antoine Dutot
 */
class Segment {

	public Vertex source;
	public Vertex newSource;
	public Vertex target;

	public PointPool points;
	
	public int p0 = -1;
	public int p1 = -1;
	public int p2 = -1;
	
	public Segment(Vertex source, Vertex target, PointPool points) {
		this.source = source;
		this.target = target;
		this.points = points;
	}

	/** Compute the points that will be forming the new source vertex of this segment. */
	public void associateWithPoints(int pp, VertexPool vertices, PointPool points) {
		p0 = pp;
		
		if(target.incomplete) {
			p1 = target.p0;
			p2 = target.p1;
		} else {
			List<Integer> common = source.commonPoints(target);
			if(common.size() == 2) {
				p1 = common.get(0);
				p2 = common.get(1);
			} else {
				throw new RuntimeException(String.format("%nAh ah ah ah: invalid common points count %d!=2%n  => %s != %s", common.size(), source, target));
			}
		}
		
		assert(p1 != p2);
	}
	
	@Override
	public String toString() {
		if(p0>=0 && p1>=0 && p2>=0) {
			return String.format("{segment %s --(%d,%d,%d)-- %s}", source.toString(), p0, p1, p2, target.toString());
		} else {
			return String.format("{segment %s ---- %s}", source.toString(), target.toString());
		}
	}
	
	/** Create the new source vertex of the segment. */
	public Vertex createVertex(VertexPool vertices, ArrayList<Vertex> del) {
		int v = -1;
		
		if(del.size()>0) {
			Vertex old = del.remove(del.size()-1);
			v = vertices.replace(old, p0, p1, p2, target.id, -1, -1);
		} else {
			v = vertices.add(p0, p1, p2, target.id, -1, -1);
		}
			
		newSource = vertices.get(v);

		return newSource;
	}
	
	public void tieWithNewVertices(ArrayList<Segment> seg, VertexPool vertices) {
		// We ignore point p0, already set to the new point.
		// We also ignore v0, already set.
		// We want to set v1 and v2. They are new vertices (newSource in seg, that have a common
		// point (p1 or p2) with us.
		newSource.v1 = commonPointWith(p0, p1, seg, vertices);
		newSource.v2 = commonPointWith(p0, p2, seg, vertices);
		vertices.update(newSource);
		
		assert((newSource.v1 >= 0) && (newSource.v2 >= 0));
		assert(newSource.v0 != newSource.v1 && newSource.v1 != newSource.v2 && newSource.v0 != newSource.v2);
	}
	
	protected int commonPointWith(int p0, int p, ArrayList<Segment> seg, VertexPool vertices) {
		for(Segment segment: seg) {
			Vertex v = segment.newSource;
			if(v != newSource) {
				if((v.p0 == p0 || v.p1 == p0 || v.p2 == p0) && (v.p0 == p || v.p1 == p || v.p2 == p))
					return v.id;
			}
		}
		
System.err.printf("no common point (%d %d)%n", p0, p);
		
		return -1;
	}
	
	protected void tieWithOldVertices(ArrayList<Segment> seg, VertexPool vertices) {
		if(target.v0 == source.id) target.v0 = newSource.id;
		else if(target.v1 == source.id) target.v1 = newSource.id;
		else if(target.v2 == source.id) target.v2 = newSource.id;
		vertices.update(target);
	}
}
