package org.miv.graphstream.algorithm.measure.test;

import org.miv.graphstream.algorithm.measure.ElementNervousness;
import org.miv.graphstream.algorithm.measure.GraphNervousness;
import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.implementations.DefaultGraph;
import org.miv.graphstream.io.GraphReader;
import org.miv.graphstream.io.GraphReaderFactory;
import org.miv.graphstream.io.GraphReaderListenerHelper;
import org.miv.graphstream.ui.GraphViewerRemote;

public class TestGraphNervousness
{

	private String input;
	
	public static void main(String args[])
	{
		org.miv.util.Environment.getGlobalEnvironment().readCommandLine(args);
		try
		{
			new TestGraphNervousness();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public TestGraphNervousness() throws Exception
	{
		input = org.miv.util.Environment.getGlobalEnvironment().getParameter("input");
		if (input == null || input == "")
		{
			throw new Exception("Usage: TestGraphNervousness -input=<graph-name>]%n");
		}
		
		Graph g = new DefaultGraph("624P#57234M");
		GraphNervousness nervousness = new GraphNervousness(g);
		
	
		
		GraphReader reader = GraphReaderFactory.readerFor(input);
		reader.addGraphReaderListener(new GraphReaderListenerHelper(g));
		reader.begin(input);
	
		while (reader.nextStep())
		{
				System.out.println(nervousness.getGraphNervousness());
		}
	
	}
}