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
import java.util.List;

import org.miv.graphstream.algorithm.AStar;
import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Path;
import org.miv.graphstream.graph.implementations.DefaultGraph;
import org.miv.graphstream.io.GraphParseException;
import org.miv.util.NotFoundException;

/**
 * Test the A* algorithm with any graph and any nodes.
 * @author Antoine Dutot
 */
public class TestAStar2
{
	public static void main( String args[] )
	{
		if( args.length >= 3 )
		{
			try
			{
				new TestAStar2( args[0], args[1], args[2] );
			}
			catch( Exception e )
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.err.printf( "Usage: TestAStar2 <graph-name> <start-node-name> <end-node-name>%n" );
		}
	}
	
	public TestAStar2( String graphName, String source, String target )
		throws IOException, NotFoundException, GraphParseException
	{
		Graph graph = new DefaultGraph();
		
		graph.read( graphName );
		
		AStar astar = new AStar();
		
		astar.setGraph( graph );
		astar.compute( source, target );
		
		Path path = astar.getShortestPath();
		
		List<Edge> edges = path.getEdgePath();
		
		for( Edge edge: edges )
			edge.addAttribute( "color", "red" );
	
		graph.display();
	}
}
