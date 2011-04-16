package org.graphstream.algorithm.generator.tessellation.test;

import org.graphstream.algorithm.generator.tessellation.DelaunayGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point2;

import static org.graphstream.algorithm.Toolkit.*;

public class TestLeHavreDelaunay {
	public static void main(String args[]) {
		new TestLeHavreDelaunay();
	}
	
	public TestLeHavreDelaunay() {
		Graph src = new MultiGraph("LeHavreSrc");
		Graph del = new MultiGraph("LeHavreDelaunay");
		
		try {
			System.out.printf("reading ...%n");
			src.read("/home/antoine/Documents/Programs/gs-geography/LeHavre.dgs");
			
			Point2 min = new Point2(Float.MAX_VALUE, Float.MAX_VALUE);
			Point2 max = new Point2(Float.MIN_VALUE, Float.MIN_VALUE);
			
			System.out.printf("min max (%s %s)...%n", min, max);
			for(Node node: src) {
				double xyz[] = nodePosition(node);

				if(xyz[0] < min.x) min.x = xyz[0];
				if(xyz[0] > max.x) max.x = xyz[0];
				if(xyz[1] < min.y) min.y = xyz[1];
				if(xyz[1] > max.y) max.y = xyz[1];
			}
			
			System.out.printf("triangulation ...%n");
			DelaunayGenerator gen = new DelaunayGenerator(del, min, max);
			
//			del.addAttribute("ui.antialias");
			del.addAttribute("ui.stylesheet", stylesheet);
			del.display(false);
			
			gen.begin();

			for(Node node: src) {
				double xyz[] = nodePosition(node);
				System.out.printf("    p(%f, %f)%n",xyz[0], xyz[1]);
				gen.addPoint(xyz[0], xyz[1]);
			}

			gen.end();
			System.out.printf("ok ...%n");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String stylesheet = 
		"graph { padding: 20px; }" +
		"edge { fill-color: rgb(150,150,150); }" +
		"node { fill-color: rgb(50,50,50); size: 2px; }" +
		"node.voronoi { fill-color: green; size: 5px; }";
}
