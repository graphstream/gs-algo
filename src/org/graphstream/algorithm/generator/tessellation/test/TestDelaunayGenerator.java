package org.graphstream.algorithm.generator.tessellation.test;

import org.graphstream.algorithm.generator.tessellation.DelaunayGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.geom.Point2;

public class TestDelaunayGenerator {
	protected Point2 p0 = new Point2(0, 0);
	protected Point2 p1 = new Point2(100, 100);
	protected Point2 p2 = new Point2(100, 0);
	protected long sleepTime = 0;
	
	public static void main(String args[]) {
		new TestDelaunayGenerator();
	}
	
	public TestDelaunayGenerator() {
		Graph graph = new DefaultGraph("delaunay");
//		DelaunayGenerator gen = new DelaunayGenerator(graph, p0, p1, p2);
		DelaunayGenerator gen = new DelaunayGenerator(graph, p0, p1);
		
		graph.addAttribute("ui.antialias");
		graph.addAttribute("ui.stylesheet", stylesheet);
		graph.display(false);
		
		gen.begin();
		sleep();
		for(int i=0; i<1000; i++) {
//			Point2 p = randomPosInTriangle(p0, p1, p2);
			Point2 p = randomPosInRectangle(p0, p1);
			gen.addPoint(p.x, p.y);
			sleep();
		}
		
		gen.end();
	}
	
	protected void sleep() {
		if(sleepTime > 0 ) try { Thread.sleep(sleepTime); } catch(Exception e) {}
	}
	
	public Point2 randomPosInTriangle(Point2 p0, Point2 p1, Point2 p2) {
		double r0 = Math.random();
		double r1 = Math.random();
		double r2 = Math.random();
		double sum = r0+r1+r2;
			
		r0 /= sum;// r0 -= 0.00001;
		r1 /= sum;// r1 -= 0.00001;
		r2 /= sum;// r2 -= 0.00001;
			
			
		assert((r0+r1+r2 <= 1) && (r0+r1+r2 >= 0.999)): String.format("%f invalide", r0+r1+r2);
			
		return new Point2(
			((p0.x*r0)+(p1.x*r1)+(p2.x*r2)),
			((p0.y*r0)+(p1.y*r1)+(p2.y*r2)));
	}

	public Point2 randomPosInRectangle(Point2 p0, Point2 p1) {
		double r0 = Math.random();
		double r1 = Math.random();
		double w = p1.x - p0.x;
		double h = p1.y - p0.y;
		
		return new Point2(p0.x + w*r0, p0.y + h*r1);
	}
	
	public static String stylesheet = 
		"graph { padding: 20px; }" +
		"edge { fill-color: rgb(150,150,150); }" +
		"node { fill-color: rgb(50,50,50); size: 2px; }" +
		"node.voronoi { fill-color: green; size: 5px; }";
}