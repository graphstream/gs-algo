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

package org.miv.graphstream.algorithm.generator.test;

import org.miv.graphstream.algorithm.generator.*;
import org.miv.graphstream.graph.*;

/**
 * Test the random graph generator.
 *
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since 2007
 */
public class TestRandomGenerator
{
	public static void main( String args[] )
	{
		if( args.length > 0 )
		     new TestRandomGenerator( args );
		else new TestRandomGenerator( null );
	}
	
	public TestRandomGenerator( String args[] )
	{
		int    size     = 500;
		String filename = null;
		
		if( args != null && args.length > 0 )
		{
			try
			{
				size = Integer.parseInt( args[0] );
			}
			catch( NumberFormatException e )
			{
				usage( "invalid number %s", args[0] );
			}

			if( args.length > 1 )
				filename = args[1];
		}
		
		Graph g = new CheckedGraph( "Full Graph" );

		g.display( true );
		
		RandomGenerator rg = new RandomGenerator( 10, true, true );
		
		rg.addNodeLabels( true );
		rg.begin( g );
		
		for( int i=0; i<size-1; ++i )
		{
			rg.nextElement();
		}
		
		rg.end();
		
		if( filename != null )
		{
			try
			{
				g.write( filename );
			}
			catch( java.io.IOException e )
			{
				e.printStackTrace();
			}
		}
	}
	
	protected void usage( String message, Object ... params )
	{
		if( message != null )
		{
			System.err.printf( message, params );
		}
		
		System.err.printf( "Usage: %s <size> <outputFileName>%n", getClass().getName() );
		System.err.printf( "%n" );
		System.err.printf( "   you can pass no argument, one or two.%n" );
		System.err.printf( "   <size> .............. if specified, the size of the full graph.%n" );
		System.err.printf( "   <outputFileName> .... if specified, a file that will contain the generated graph.%n" );
		System.err.printf( "%n" );
		System.exit( 1 );
	}
}