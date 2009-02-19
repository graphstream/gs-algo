/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.miv.graphstream.algorithm.test;

import java.awt.Color;
import java.io.IOException;

import org.miv.graphstream.algorithm.ConnectedComponents;
import org.miv.graphstream.algorithm.measure.test.TestElementNervousness;
import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Element;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.GraphListener;
import org.miv.graphstream.graph.GraphListenerHelper;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.implementations.SingleGraph;
import org.miv.graphstream.io.GraphParseException;
import org.miv.util.Environment;
import org.miv.util.NotFoundException;

public class TestConnectedComponents
{
	protected static Color COLORS[] = { Color.BLACK, Color.RED, Color.GREEN,
		Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.PINK };
	
	public static void main( String args[] )
	{
		org.miv.util.Environment.getGlobalEnvironment().readCommandLine(args);
		try
		{
			new TestConnectedComponents();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public TestConnectedComponents() throws NotFoundException, IOException, GraphParseException
	{
		if( ! org.miv.util.Environment.getGlobalEnvironment().getParameter("input").equals("") )
		{
			newTest();
			return;
		}
		Graph graph = new org.miv.graphstream.graph.implementations.DefaultGraph();
		
		Environment.getGlobalEnvironment().setParameter( "Layout.3d", "0" );
		
		graph.display();
		
		ConnectedComponents cc = new ConnectedComponents( graph );
		
		int sleep = 5000;
		
		cc.setCutAttribute( "cut" );
		cc.begin();
		
		sleep( sleep );
		
		graph.addNode( "A" );
		graph.addNode( "B" );
		graph.addNode( "C" );
		
		System.err.printf( "CC == 3 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );
		
		graph.addEdge( "AB", "A", "B" );
		System.err.printf( "CC == 2 == %d%n", cc.getConnectedComponentsCount() );
		graph.addEdge( "BC", "B", "C" );
		System.err.printf( "CC == 1 == %d%n", cc.getConnectedComponentsCount() );
		graph.addEdge( "CA", "C", "A" );
		System.err.printf( "CC == 1 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );
		
		graph.addNode( "X" );
		graph.addNode( "Y" );
		graph.addNode( "Z" );
		
		System.err.printf( "CC == 4 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );
		
		graph.addEdge( "XY", "X", "Y" );
		System.err.printf( "CC == 3 == %d%n", cc.getConnectedComponentsCount() );
		graph.addEdge( "YZ", "Y", "Z" );
		System.err.printf( "CC == 2 == %d%n", cc.getConnectedComponentsCount() );
		graph.addEdge( "ZX", "Z", "X" );
		System.err.printf( "CC == 2 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );
		
		graph.addEdge( "AX", "A", "X" );
		System.err.printf( "CC == 1 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );
	
		graph.removeEdge( "AX" );
//		graph.getEdge( "AX" ).addAttribute( "cut", "X" );
//		graph.getEdge( "AX" ).addAttribute( "style", "dashed" );
		System.err.printf( "CC == 2 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );
		
		graph.addEdge( "AX", "A", "X" );
//		graph.getEdge( "AX" ).removeAttribute( "cut" );
//		graph.getEdge( "AX" ).removeAttribute( "style" );
		System.err.printf( "CC == 1 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );

//		graph.removeEdge( "AX" );
		graph.getEdge( "AX" ).addAttribute( "cut", "X" );
		graph.getEdge( "AX" ).addAttribute( "style", "dashed" );
		
		sleep( sleep );

//		graph.removeEdge( "AB" );
		graph.getEdge( "AB" ).addAttribute( "cut", "X" );
		graph.getEdge( "AB" ).addAttribute( "style", "dashed" );		
		System.err.printf( "CC == 2 == %d%n", cc.getConnectedComponentsCount() );
		
		sleep( sleep );
		
//		graph.removeEdge( "CA" );
		graph.getEdge( "CA" ).addAttribute( "cut", "X" );
		graph.getEdge( "CA" ).addAttribute( "style", "dashed" );		
		System.err.printf( "CC == 3 == %d%n", cc.getConnectedComponentsCount() );

		
		
		System.err.printf( "Finished!!%n" );
	}
	
	private void newTest() throws NotFoundException, IOException, GraphParseException
	{
		Graph graph = new SingleGraph();
		final ConnectedComponents cc = new ConnectedComponents(graph);
		graph.addGraphListener(new GraphListenerHelper(){
			public void stepBegins(Graph graph, double time)
			{System.out.println(cc.getConnectedComponentsCount());}});
		graph.read(org.miv.util.Environment.getGlobalEnvironment().getParameter("input"));
	}

	protected void sleep( int ms )
	{
		try
		{
			Thread.sleep( ms );
		}
		catch( InterruptedException e )
		{
			e.printStackTrace();
		}
	}
}