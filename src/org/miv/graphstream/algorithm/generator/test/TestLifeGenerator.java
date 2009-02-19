package org.miv.graphstream.algorithm.generator.test;

import org.miv.graphstream.graph.*;
import org.miv.graphstream.algorithm.generator.*;
import org.miv.miterator.*;

public class TestLifeGenerator
{
	protected MIterator miterator;
	
	protected LifeGenerator generator;
	
	protected Graph graph;
	
	public static void main( String args[] )
	{
		new TestLifeGenerator( args );
	}
	
	public TestLifeGenerator( String args[] )
	{
		miterator = new MIterator( 100, 100 );
		generator = new LifeGenerator( miterator );
		graph     = new CheckedGraph( "life!" );

		// The R pentamino.
		
		miterator.setCell( 50, 50, 1 );
		miterator.setCell( 49, 50, 1 );
		miterator.setCell( 50, 51, 1 );
		miterator.setCell( 51, 51, 1 );
		miterator.setCell( 50, 49, 1 );
		
		// Start the loop!
		
		int i = 0;
		
		generator.begin( graph );
		graph.display();
		
		while( i < 1000000 )
		{
			generator.nextElement();
			sleep( 40 );
			i++;
		}
		
		generator.end();
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