package org.graphstream.algorithm.generator.tessellation;

import java.util.ArrayList;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.geom.Point2;

import static org.graphstream.algorithm.generator.tessellation.Ids.*;

/**
 * Set of vertices of the Voronoï/Dirichlet tessellation.
 */
class VertexPool {
	protected PointPool points = null;
	protected ArrayList<Vertex> vertices = new ArrayList<Vertex>();
	protected Graph graph;
	
	public VertexPool(PointPool points, Graph graph) {
		this.points = points;
		this.graph = graph;
		
		graph.setStrict(false);
		graph.setAutoCreate(true);
	}
	
	public int addIncomplete(int p0, int p1, int v0 ) {
		int id = vertices.size();
		vertices.add( new Vertex(id, p0, p1, v0, points) );
		return id;
	}
		
	public int add(int p0, int p1, int p2, int v0, int v1, int v2) {
		int id = vertices.size();
		vertices.add( new Vertex(id, p0, p1, p2, v0, v1, v2, points ) );

		addEdges(p0, p1, p2);
//		addVoronoiNode(id);
		
		return id;
	}
		
	public int replace(Vertex old, int p0, int p1, int  p2, int v0, int v1, int v2) {
//		graph.removeEdge(edgeId(old.p0, old.p1));
//		graph.removeEdge(edgeId(old.p1, old.p2));
//		graph.removeEdge(edgeId(old.p2, old.p0));
		
		vertices.set(old.id, new Vertex(old.id, p0, p1, p2, v0, v1, v2, points) );
		
		addEdges(p0, p1, p2);
		
		
		return old.id;
	}

	protected void addEdges(int p0, int p1, int p2) {
		Node n0 = graph.getNode(nodeId(p0));
		Node n1 = graph.getNode(nodeId(p1));
		Node n2 = graph.getNode(nodeId(p2));
		
		Edge n0n1 = n0.getEdgeBetween(nodeId(p1));
		Edge n1n2 = n1.getEdgeBetween(nodeId(p2));
		Edge n2n0 = n2.getEdgeBetween(nodeId(p0));
		
		if(n0n1 == null) graph.addEdge(edgeId(p0,p1), n0.getId(), n1.getId(), false);
		if(n1n2 == null) graph.addEdge(edgeId(p1,p2), n1.getId(), n2.getId(), false);
		if(n2n0 == null) graph.addEdge(edgeId(p2,p0), n2.getId(), n0.getId(), false);
	}
	
	protected void addVoronoiNode(int v) {
		Vertex vertex = vertices.get(v);
		if(!vertex.incomplete) {
			Node node = graph.getNode(String.format("voronoi_%d", v));
			if (node==null)
				node = graph.addNode(String.format("voronoi_%d", v));
			node.setAttribute("xy", vertex.position.x, vertex.position.y);
			node.setAttribute("ui.class", "voronoi");
		}
	}
	
	public void update(Vertex v) {
		// The data in v changed.
	}
		
	public Vertex get(int i) {
		return vertices.get(i);
	}
	
	public ArrayList<Vertex> computeListOfDeletedVertices(Point2 p) {
		ArrayList<Vertex> del = new ArrayList<Vertex>();
		
		for(Vertex vertex: vertices) {
			if( vertex.pointCloserThanFormingPoints(p, points) )
				del.add(vertex);
		}
		
		assert(del.size() > 0);
		
		return del;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format("Vertices:%n"));

		for(Vertex vertex:vertices) {
				sb.append(String.format("    %s%n", vertex.toString()));
		}
		
		return sb.toString();
	}
}
