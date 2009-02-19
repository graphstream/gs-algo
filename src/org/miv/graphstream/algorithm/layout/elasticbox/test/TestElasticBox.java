package org.miv.graphstream.algorithm.layout.elasticbox.test;

import java.io.IOException;

import org.miv.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.miv.graphstream.algorithm.generator.Generator;
import org.miv.graphstream.algorithm.layout2.elasticbox.ElasticBox;
//import org.miv.graphstream.algorithm.layout2.springbox.SpringBox;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.io.GraphParseException;
import org.miv.graphstream.ui.swing.SwingGraphViewer;
import org.miv.util.NotFoundException;
//import org.miv.graphstream.ui.swingviewer.SwingGraphViewer;


public class TestElasticBox
{
	public static void main( String args[] )
	{
		new TestElasticBox( args );
	}
	
	public TestElasticBox( String args[] )
	{
		Graph g = new org.miv.graphstream.graph.implementations.AdjacencyListGraph( false, true );
		
		new SwingGraphViewer( g, new ElasticBox( true ), false );

		g.addAttribute( "stylesheet", "node { width:3; } edge { color:grey; }" );
		
		if( args.length <= 0 )
		{
			Generator gg = new DorogovtsevMendesGenerator();
		
			gg.begin( g );
		
			for( int i=0; i < 2000; ++i )
				gg.nextElement();
		
			gg.end();
		}
		else
		{
			try
			{
				g.read( args[0] );
			}
			catch( NotFoundException e )
			{
				e.printStackTrace();
			}
			catch( IOException e )
			{
				e.printStackTrace();
			}
			catch( GraphParseException e )
			{
				e.printStackTrace();
			}
		}
	}
}