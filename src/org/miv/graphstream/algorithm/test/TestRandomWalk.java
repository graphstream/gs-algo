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

import java.io.IOException;
import java.util.Iterator;

import org.miv.graphstream.algorithm.RandomWalk;
import org.miv.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.miv.graphstream.algorithm.generator.Generator;
import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Element;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.implementations.MultiGraph;
import org.miv.graphstream.io.GraphParseException;
import org.miv.graphstream.ui.GraphViewer;
import org.miv.graphstream.ui.GraphViewerRemote;
import org.miv.util.NotFoundException;

/**
 * Test the random walk algorithm.
 */
public class TestRandomWalk
{
	protected static long SLEEP_TIME = 2;
	
	public static void main( String args[] )
	{
		String graphName  = null;
		String weightName = null;
		
		if( args.length > 0 )
			graphName = args[0];
		
		if( args.length > 1 )
			weightName = args[1];
		
		try
		{
			new TestRandomWalk( graphName, weightName, 40 );
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	protected Graph graph;
	
	public TestRandomWalk( String graphName, String weightName, int mem ) throws NotFoundException, IOException, GraphParseException
	{
		graph = new MultiGraph( graphName );
		
		if( graphName == null )
		{
			Generator g = new DorogovtsevMendesGenerator();
			
			g.begin( graph );
			for( int i=0; i<200; i++ )
				g.nextElement();
			g.end();
		}
		else
		{
			graph.read( graphName );
		}
		
		graph.addAttribute( "stylesheet", styleSheet );
		
		boolean haveXY = graph.algorithm().getRandomNode().hasAttribute( "x" )
		              || graph.algorithm().getRandomNode().hasAttribute( "xy" )
                      || graph.algorithm().getRandomNode().hasAttribute( "xyz" );
		
		GraphViewerRemote remote = graph.display( ! haveXY );
		
		remote.setQuality( 0 );
		remote.waitForLayoutStabilisation( 5000 );
		
		RandomWalk rwalk = new RandomWalk();

		rwalk.setEntityMemory( mem );
		rwalk.setWeightAttribute( weightName );
		rwalk.setPassesAttribute( "passes" );
		rwalk.begin( graph, graph.getNodeCount()/2 );
		
		boolean loop  = true;
		int     steps = 0;
		
		while( loop )
		{
			rwalk.step();
			int nreds = 0;
			int ereds = 0;
			nreds = colorNodes();
			ereds = colorEdges();
			if( ( steps % 10 ) == 0 )
				System.out.printf( "Step %d [red=%d]%n", steps, nreds );
			sleep();
			if( nreds >= 3 ) loop = false;
			if( ereds >= 3 ) loop = false;
			steps++;
		}
			
		rwalk.end();

		String baseOutputName = outputGraphName( graphName, weightName, mem, rwalk.getRandomSeed(), steps ); 
			
		screenShot( remote, baseOutputName );
		saveGraph( graph, baseOutputName );
		
		System.out.printf( "OK!%n" );
	}
	
	protected void screenShot( GraphViewerRemote remote, String baseOutputName )
	{
		remote.screenShot( baseOutputName, GraphViewer.ScreenshotType.SVG );
		
		System.out.printf( "Screenshot in «%s»%n", baseOutputName+".svg" );
		System.out.flush();
	}

	protected void saveGraph( Graph graph, String baseOutputName )
	{
		try
        {
			baseOutputName = String.format( "%s.dgs", baseOutputName );

	        graph.write( baseOutputName );
			System.out.printf( "Graph with weights in «%s»%n", baseOutputName );
			System.out.flush();
        }
        catch( IOException e )
        {
	        e.printStackTrace();
        }
	}
	
	protected String outputGraphName( String graphName, String weightName, int mem, long seed, int steps )
	{
		int    pos     = graphName.lastIndexOf( '.' );
		String outName = null;
		
		if( pos > 0 )
			outName = graphName.substring( 0, pos );
		
		pos = outName.lastIndexOf( '/' );
		
		if( pos >= 0 )
			outName = outName.substring( pos+1 );
		
		return String.format( "%s_m%03d_s%05d_%s_%d", outName, mem, steps, weightName != null ? "_W" : "", seed );
	}
	
	protected void sleep()
	{
		try { Thread.sleep( SLEEP_TIME ); } catch( InterruptedException e ) {}
	}
	
	protected int colorEdges()
	{
		int reds = 0;
		
		Iterator<? extends Edge> edges = graph.getEdgeIterator();
		
		while( edges.hasNext() )
		{
			Edge edge = edges.next();
		
			if( colorElement2( edge ) )
				reds++;
		}
		
		return reds;
	}
	
	protected int colorNodes()
	{
		int reds = 0;
	
		Iterator<? extends Node> nodes = graph.getNodeIterator();
		
		while( nodes.hasNext() )
		{
			Node node = nodes.next();
		
			if( colorElement1( node ) )
				reds++;
		}
		
		return reds;
	}
	
	protected boolean colorElement1( Element e )
	{
		int passes = (int) e.getNumber( "passes" );
		String style = null;
		boolean red = false;
		
		// Scale :
		// <256		-> cyan
		// <512		-> blue
		// <1024	-> black
		// <2048	-> green point
		// <4096	-> orange point
		// >=4096	-> red point
		
		if( passes < 256 )
		{
			e.addAttribute( "ui.color" , String.format( "rgb(%d,255,255)", 255-passes ) );
		}
		else if( passes < 512 )
		{
			passes -= 256;
			e.addAttribute( "ui.color" , String.format( "rgb(0,%d,255)", 255-passes ) );
		}
		else if( passes < 1024 )
		{
			passes -= 512;
			passes /= 2f;
			e.addAttribute( "ui.color" , String.format( "rgb(0,0,%d)", 255-passes ) );
		}
		else if( passes < 2048 )
		{
			e.addAttribute( "class", "point1" );
		}
		else if( passes < 4096 )
		{
			e.addAttribute( "class", "point2" );
		}
		else
		{
			e.addAttribute( "class", "point3" );
			red = true;
		}
		
		e.addAttribute( "style", style );
		
		return red;
	}

	protected boolean colorElement2( Element e )
	{
		int passes = (int) e.getNumber( "passes" );
		String style = null;
		boolean red = false;
		
		if( passes < 256 )
		{
			e.addAttribute( "ui.color" , String.format( "rgb(%d,255,255)", 255-passes ) );
		}
		else if( passes < 512 )
		{
			passes -= 256;
			e.addAttribute( "ui.color" , String.format( "rgb(0,%d,255)", 255-passes ) );
		}
		else if( passes < 768 )
		{
			passes -= 512;
			e.addAttribute( "ui.color" , String.format( "rgb(%d,%d,%d)", passes, passes, 255-passes ) );
		}
		else if( passes < 1024 )
		{
			passes -= 768;
			e.addAttribute( "ui.color", String.format( "rgb(255,%d,0)", 255-passes ) );
		}
		else if( passes < 2048 )
		{
			e.addAttribute( "class", "point1" );
		}
		else if( passes < 4096 )
		{
			e.addAttribute( "class", "point2" );
		}
		else
		{
			e.addAttribute( "class", "point3" );
			red = true;
		}
		
		e.addAttribute( "style", style );
		
		return red;
	}

	protected static String styleSheet =
		"node { width: 2px; }" +
//		"node.point1 { border-width: 1px; border-color: green;  shadow-style: simple; shadow-width: 2px; shadow-color: green; }" +
//		"node.point2 { border-width: 1px; border-color: orange; shadow-style: simple; shadow-width: 3px; shadow-color: orange; }" +
//		"node.point3 { border-width: 1px; border-color: red;    shadow-style: simple; shadow-width: 4px; shadow-color: red; }" +
//		"edge.point1 { border-width: 1px; border-color: green;  shadow-style: simple; shadow-width: 2px; shadow-color: green; }" +
//		"edge.point2 { border-width: 1px; border-color: orange; shadow-style: simple; shadow-width: 3px; shadow-color: orange; }" +
//		"edge.point3 { border-width: 1px; border-color: red;    shadow-style: simple; shadow-width: 4px; shadow-color: red; }" +
		"node.point1 { border-width: 1px; border-color: green;  shadow-style: simple; shadow-width:  3px; shadow-color: #99FF99; }" +
		"node.point2 { border-width: 1px; border-color: orange; shadow-style: simple; shadow-width:  4px; shadow-color: #FFEE99; }" +
		"node.point3 { border-width: 1px; border-color: red;    shadow-style: simple; shadow-width:  8px; shadow-color: #FF9999; }" +
		"edge.point1 { shadow-style: simple; shadow-width:  1px; shadow-color: #BBFFBB; }" +
		"edge.point2 { shadow-style: simple; shadow-width:  2px; shadow-color: #FFEEBB; }" +
		"edge.point3 { shadow-style: simple; shadow-width:  3px; shadow-color: #FFBBBB; }" +
		"edge { width: 1px; color: #808080; arrow-shape: none; }";
}