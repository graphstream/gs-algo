package org.miv.graphstream.algorithm.measure.test;

import java.util.HashMap;
import java.util.Iterator;

import org.miv.graphstream.algorithm.measure.ElementNervousness;
import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Element;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.GraphListener;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.implementations.DefaultGraph;
import org.miv.graphstream.io.GraphReader;
import org.miv.graphstream.io.GraphReaderFactory;
import org.miv.graphstream.io.GraphReaderListenerHelper;
import org.miv.graphstream.ui.GraphViewerRemote;
import org.miv.graphstream.ui.Sprite;

public class TestElementNervousness implements GraphListener
{

	static String dgs = "" + "DGS003 \n " + "test 0 0 \n  " + "st 0 \n " + "an A  x:0 y:1\n "
			+ "an B  x:1 y:2\n " + "an C  x:1 y:1\n " + "an D  x:1 y:0\n " + "an E  x:2 y:1\n"
			+ "ae AB A B \n " + "ae AC A C \n " + "ae BE B E \n " + "ae CE C E \n" + "st 1 \n "
			+ "st 2 \n " + "st 3 \n " + "st 4 \n " + "st 5 \n " + "st 6 \n " + "st 7 \n "
			+ "st 8 \n " + "ae AD A D \n " + "ae DE D E \n" + "st 9 \n " + "de AC \n "
			+ "de CE \n " + "st 10 \n " + "de AB \n " + "de BE \n" + "st 11 \n" + "st 12 \n"
			+ "st 13 \n" + "st 14 \n" + "ae AB A B \n " + "ae AC A C \n " + "st 13 \n" + "st 14 \n"
			+ "";

	public static String styleSheet = "node { width:6px; color:grey; border-width:1px; border-color:black; text-color:black; }"
			+ "edge { color:black; text-color:black;}"
			// + "sprite { width: 3px; sprite-shape: arrow; border-width: 1px; sprite-orientation:
			// origin;}"
			+ "sprite { width: 15px; sprite-shape: pie-chart; border-width: 1px; }"
			// + "sprite { width: 5px; sprite-shape: flow; sprite-orientation: from; z-index:-1; }"
			+ "sprite { color: #F03050AA #F0F0F0AA; }" + "";
	Graph g;
	GraphViewerRemote remote;
	private String input;

	private double sleep;

	private boolean gui;

	private boolean layout;

	private HashMap<String,Sprite> sprites;
	
	public static void main(String args[])
	{
		org.miv.util.Environment.getGlobalEnvironment().readCommandLine(args);
		try
		{
			new TestElementNervousness();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public TestElementNervousness() throws Exception
	{
		input = org.miv.util.Environment.getGlobalEnvironment().getParameter("input");
		gui = org.miv.util.Environment.getGlobalEnvironment().getBooleanParameter("gui");
		sleep = org.miv.util.Environment.getGlobalEnvironment().getNumberParameter("sleep");
		layout = org.miv.util.Environment.getGlobalEnvironment().getBooleanParameter("layout");

		if (input == null || input == "")
		{
			throw new Exception(
					"\nUsage: TestElementNervousness -input=<graph-name> [ -gui  [ -sleep=<thread-sleep-duration>  -layout ] ]\n");
		}
		sprites = new HashMap<String, Sprite>();
		
		g = new DefaultGraph("624P#57234M");
		ElementNervousness nervousness = new ElementNervousness(g, Edge.class);
		g.addGraphListener(this);

		if (gui)
		{
			remote = g.display(layout);
			remote.setQuality(4);
			remote.setStepsVisible(true);

			g.addAttribute("ui.stylesheet", styleSheet);
		}
		GraphReader reader = GraphReaderFactory.readerFor(input);
		reader.addGraphReaderListener(new GraphReaderListenerHelper(g));
		reader.begin(input);
		
		while (reader.nextStep())
		{
			if (gui)
			{
				Thread.sleep((long) sleep);

				Iterator<? extends Edge> edgesIterator = g.getEdgeIterator();
				while (edgesIterator.hasNext())
				{
					Edge edge = edgesIterator.next();
					double nerv = nervousness.getElementNervousness(edge.getId());
					Sprite sprite = sprites.get(edge.getId());
					if (sprite == null)
					{
						sprite = remote.addSprite(edge.getId());
						sprite.attachToEdge(edge.getId());
						sprite.position(0.5f);
						sprites.put(sprite.getId(), sprite);
					}
					//double nerv2 = nerv * Math.exp(1 - nerv);
					sprite.addAttribute("pie-values", (float) nerv, (float) 1f - nerv);
				}
			}
			System.out.println(nervousness.getElementNervousness());
		}
	}


	public void afterEdgeAdd(Graph graph, Edge edge)
	{
		// TODO Auto-generated method stub

	}

	public void afterNodeAdd(Graph graph, Node node)
	{
		// TODO Auto-generated method stub

	}

	public void attributeChanged(Element element, String attribute, Object oldValue, Object newValue)
	{
		// TODO Auto-generated method stub

	}

	public void beforeEdgeRemove(Graph graph, Edge edge)
	{
		if(gui){
			sprites.remove(edge.getId());
			remote.removeSprite(edge.getId());
		}
	}

	public void beforeGraphClear(Graph graph)
	{
		// TODO Auto-generated method stub

	}

	public void beforeNodeRemove(Graph graph, Node node)
	{
		// remote.removeSprite(node.getId());

	}

	public void stepBegins(Graph graph, double time)
	{
		// TODO Auto-generated method stub

	}

}
