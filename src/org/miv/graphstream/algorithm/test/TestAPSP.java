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

import java.io.*;

import org.miv.graphstream.algorithm.*;
import org.miv.graphstream.graph.*;
import org.miv.graphstream.io.*;
import org.miv.util.*;

/**
 * Test the APSP algorithm.
 *
 * @author Antoine Dutot
 * @since 2007
 */
public class TestAPSP
{
	public static void main( String args[] )
	{
		try
		{
			new TestAPSP( args );
		}
		catch( Exception e )
		{
			e.printStackTrace();
			System.exit( 1 );
		}
	}
	
	public TestAPSP( String args[] ) throws NotFoundException, IOException, GraphParseException
	{
		String filename = null;
		
		if( args.length > 0 )
			filename = args[0];
		
		Graph G = new CheckedGraph( "", false, true );
		
		if( filename == null )
			 buildGraph( G );
		else G.read( filename );
		
		APSP apsp = new APSP( G, "weight", true );
		
		apsp.compute();
		
		for( Node node: G.getNodeSet() )
		{
			printNode( node );
			/*
			float Dij = ((APSP.APSPInfo)node.getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME)).getLengthTo( "D" );
			
			if( Dij >= 0 )
				node.addAttribute( "label", node.getId()+" -("+Dij+")-> D" );
			*/
			
			node.addAttribute( "label", node.getId() );
		}
		
		if( G.getNode( "A" ) != null && G.getNode( "E" ) != null )
		{
			Path path = ((APSP.APSPInfo)(G.getNode("A").getAttribute(APSP.APSPInfo.ATTRIBUTE_NAME))).getShortestPathTo( "E" );
		
			System.out.printf( "Path A -> E:%n    " );
			for( Node node: path.getNodePath() )
				System.err.printf( " -> %s", node.getId() );
			System.out.printf( "%n" );
		}
		
		G.display();
	}
	
	protected void buildGraph( Graph G )
	{
		Edge AB = G.addEdge( "AB", "A", "B", true );
		Edge AC = G.addEdge( "AC", "C", "A", true );
		Edge BC = G.addEdge( "BC", "B", "C", true );
		Edge BD = G.addEdge( "BD", "D", "B", true );
		Edge CD = G.addEdge( "CD", "C", "D", true );
		Edge DE = G.addEdge( "DE", "D", "E", false );
		
		AB.addAttribute( "weight", 0.5f );
		AB.addAttribute( "label", "0.5" );
		AC.addAttribute( "weight", 0.5f );
		AC.addAttribute( "label", "0.5" );
		BC.addAttribute( "weight", 0.5f );
		BC.addAttribute( "label", "0.5" );
		BD.addAttribute( "weight", 0.5f );
		BD.addAttribute( "label", "0.5" );
		CD.addAttribute( "weight", 0.5f );
		CD.addAttribute( "label", "0.5" );
		DE.addAttribute( "weight", 0.5f );
		DE.addAttribute( "label", "0.5" );
	}
	
	protected void printNode( Node node )
	{
		APSP.APSPInfo info = (APSP.APSPInfo) node.getAttribute( APSP.APSPInfo.ATTRIBUTE_NAME );

		if( info == null )
			throw new RuntimeException( "Node "+node.getId()+" has no APSP info!!" );
		
		System.out.printf( "%s:%n", node.getId() );
		
		for( String other: info.targets.keySet() )
		{
			float Dij = info.targets.get( other ).distance;
			System.out.printf( "    -> %s = %4.3f%n", other, Dij );
		}
	}
}