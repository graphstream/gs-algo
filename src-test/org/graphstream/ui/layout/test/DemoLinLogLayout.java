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
 * @since 2012-06-24
 * 
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.ui.layout.test;

import java.io.IOException;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.measure.Modularity;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.stream.file.FileSourceEdge;
import org.graphstream.stream.file.FileSourceGML;
import org.graphstream.stream.file.FileSourcePajek;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphPosLengthUtils;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.layout.springbox.implementations.LinLog;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.view.Viewer;

/**
 * This test creates a layout (instead of using the default layout of
 * the viewer) and creates a loop :
 * 
 * <pre>
 *   Graph ------> Layout --+
 *     ^                    |
 *     |                    |
 *     +--------------------+
 * </pre>
 */
public class DemoLinLogLayout {
//	public static final String GRAPH = "data/dorogovtsev_mendes6000.dgs"; public static final double a= 0; public static final double r=-1.3; public static double force = 3;
	public static final String GRAPH = "data/karate.gml";		public static double a= 0; public static double r=-1.3; public static double force = 3;
//	public static final String GRAPH = "data/dolphins.gml";		public static double a= 0; public static double r=-1.2; public static double force = 8;
//	public static final String GRAPH = "data/polbooks.gml";		public static double a= 0; public static double r=-1.9; public static double force = 5;
//	public static final String GRAPH = "data/triangles.dgs";	public static double a= 1; public static double r=-1; public static double force = 0.5;
//	public static final String GRAPH = "data/FourClusters.dgs";	public static double a= 0; public static double r=-1; public static double force = 3;
//	public static final String GRAPH = "data/grid7x7.dgs";		public static double a= 0; public static double r=-1; public static double force = 100;
//	public static final String GRAPH = "data/celegansneural.gml";public static double a= 0; public static double r=-1; public static double force = 5;
//	public static final String GRAPH = "data/USAir97.net";		public static double a= 0; public static double r=-1.9; public static double force = 8;
//	public static final String GRAPH = "data/WorldImport1999.edge";		public static double a= 0; public static double r=-2; public static double force = 5;

	protected Graph graph;
	
	protected Viewer viewer;
	
	protected LinLog layout;
	
	protected Sprite CC, M;
	
	protected ConnectedComponents cc;
	
	protected Modularity modularity;
	
	public static void main(String args[]) {
		new DemoLinLogLayout();
	}

	public DemoLinLogLayout() {
		boolean loop = true;
		graph = new MultiGraph("test");
		viewer = graph.display(false);
		ProxyPipe fromViewer = viewer.newThreadProxyOnGraphicGraph();
		layout = new LinLog(false);
		SpriteManager sm = new SpriteManager(graph);
		CC = sm.addSprite("CC");
		M  = sm.addSprite("M");
		cc = new ConnectedComponents(graph);
		modularity = new Modularity("component");
		
		modularity.init(graph);
		cc.setCutAttribute("cut");
		cc.setCountAttribute("component");
		
		CC.setPosition(Units.PX, 20, 20, 0);
		M.setPosition(Units.PX, 20, 40, 0);
		
		layout.configure(a, r, true, force);
		layout.setQuality(1);
		layout.setBarnesHutTheta(0.5);

		graph.setAttribute("ui.antialias");
		graph.setAttribute("ui.stylesheet", styleSheet);
		fromViewer.addSink(graph);
		//viewer.addDefaultView(true);
		graph.addSink(layout);
		layout.addAttributeSink(graph);

		FileSource dgs = null;
		if(GRAPH.endsWith(".gml")) dgs = new FileSourceGML();
		else if(GRAPH.endsWith(".dgs")) dgs= new FileSourceDGS();
		else if(GRAPH.endsWith(".net")) dgs = new FileSourcePajek();
		else if(GRAPH.endsWith(".edge")) dgs = new FileSourceEdge();
		else throw new RuntimeException("WTF?");

		double cutThreshold = 0.8;
		
		dgs.addSink(graph);
		try {
			dgs.begin(getClass().getResourceAsStream(GRAPH));
			for (int i = 0; i < 5000 && dgs.nextEvents(); i++) {
				fromViewer.pump();
				layout.compute();
				findCommunities(cutThreshold);
				updateCC();
				updateM();
			}
			dgs.end();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Finished creating the graph.");

		while (loop) {
			fromViewer.pump();

			if (graph.hasAttribute("ui.viewClosed")) {
				loop = false;
			} else {
				layout.compute();
				findCommunities(cutThreshold);
				updateCC();
				updateM();
			}
		}

		System.exit(0);
	}
	
	protected void findCommunities(double threshold) {
		int nedges = graph.getEdgeCount();
		double avgDist = 0;
		double edgesDists[] = new double[nedges];
		for(int i=0; i<nedges; i++) {
			Edge edge = graph.getEdge(i);
			Point3 posFrom = GraphPosLengthUtils.nodePointPosition(edge.getNode0());
			Point3 posTo   = GraphPosLengthUtils.nodePointPosition(edge.getNode1());
			edgesDists[i]  = posFrom.distance(posTo);
			avgDist       += edgesDists[i];
		}
		avgDist /= nedges;
		// Nothing happened to the graph so the order remains.
		for(int i=0; i<nedges; i++) {
			Edge edge = graph.getEdge(i);
			if(edgesDists[i] > avgDist*threshold) {
				edge.setAttribute("ui.class", "cut");
				edge.setAttribute("cut");
			} else {
				edge.removeAttribute("ui.class");
				edge.removeAttribute("cut");
			}
		}
	}
	
	protected void updateCC() {
		CC.setAttribute("ui.label", String.format("CC %d", cc.getConnectedComponentsCount()));
	}
	
	protected void updateM() {
		M.setAttribute("ui.label", String.format("M  %f", modularity.getMeasure()));
	}
	
	protected static void sleep(long ms) {
		try { Thread.sleep(ms); } catch(Exception e) {} 
	}

	protected static String styleSheet =
		"node { size: 7px; fill-color: rgb(150,150,150); }" +
		"edge { fill-color: rgb(255,50,50); size: 2px; }" +
		"edge.cut { fill-color: rgba(200,200,200,128); }" +
		"sprite#CC { size: 0px; text-color: rgb(150,100,100); text-size: 20; }" +
		"sprite#M  { size: 0px; text-color: rgb(100,150,100); text-size: 20; }";
}