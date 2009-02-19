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

import java.util.List;

import org.miv.graphstream.algorithm.AStar;
import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Path;
import org.miv.graphstream.graph.implementations.DefaultGraph;

/**
 * Simple test of the A* algorithm.
 * 
 * @author Antoine Dutot
 */
public class TestAStar
{
	public static void main( String args[] )
	{
		new TestAStar();
	}
	
	public TestAStar()
	{
		Graph graph = new DefaultGraph( false, true );
		
		graph.addEdge( "AB", "A", "B" );
		graph.addEdge( "BC", "B", "C" );
		graph.addEdge( "CD", "C", "D" );
		graph.addEdge( "DE", "D", "E" );
		graph.addEdge( "EF", "E", "F" );
		graph.addEdge( "BF", "B", "F" );
		
		graph.getNode("A").addAttribute( "label", "A" );
		graph.getNode("B").addAttribute( "label", "B" );
		graph.getNode("C").addAttribute( "label", "C" );
		graph.getNode("D").addAttribute( "label", "D" );
		graph.getNode("E").addAttribute( "label", "E" );
		graph.getNode("F").addAttribute( "label", "F" );
		
		AStar astar = new AStar();
		
		astar.setGraph( graph );
		astar.compute( "A", "F" );
		
		Path path = astar.getShortestPath();
		
		List<Edge> edges = path.getEdgePath();
		
		for( Edge edge: edges )
		{
			edge.addAttribute( "color", "red" );
		}
		
		graph.display();
	}
}
