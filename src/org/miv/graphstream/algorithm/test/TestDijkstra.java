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
import java.util.Locale;

import org.miv.graphstream.algorithm.Dijkstra;
import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.CheckedGraph;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.Path;
import org.miv.graphstream.io.GraphParseException;
import org.miv.graphstream.ui.viewer.GraphViewer;

import org.miv.util.NotFoundException;

/**
 * A simple test that compute a shortest path with Dijkstra's algorithm. It considers only the number of edges between two nodes.  
 * @author Yoann Pigné
 */
public class TestDijkstra
{

	Dijkstra dijkstra;
	Graph graph;
	Node source;
	Node target;

	/**
	 * @param strs
	 * @throws GraphParseException 
	 * @throws IOException 
	 * @throws NotFoundException 
	 */
	public TestDijkstra(String[] strs) throws NotFoundException, IOException, GraphParseException
	{
		graph = new CheckedGraph("TestDijkstra  - "+strs[0]);
		new GraphViewer(graph,true);
//		org.miv.util.ui.MemoryMonitor.show();
		graph.read(strs[0]);
		System.out.printf(" - The graph is read : %d nodes and %d links%n",graph.getNodeCount(), graph.getEdgeCount());
		source = graph.getNode(strs[1]);
		target = graph.getNode(strs[2]);
		long timestamp = System.currentTimeMillis();
		long time = System.nanoTime();
		dijkstra = new Dijkstra(graph,Dijkstra.Element.edge, "truc",source);
		System.out.printf(Locale.US,"%d ms %n", (System.nanoTime() - time) / 1000000 );
		
		System.out.printf(" - Dijkstra's covering tree computed from source %s in %d ms%n",source.getId(), (int)(System.currentTimeMillis() - timestamp));
		System.out.printf(" - The shortest path from node %s to node %s in %d hop(s) long%n",source.getId(), target.getId(), (int)dijkstra.getShortestPathValue(target));
		System.out.printf(" - Now I am gona try to show you this (or these) shortest path(s) on a graphical display...%n");

		List<Path> paths =  dijkstra.getPathSetShortestPaths( target ); 
		System.out.printf( "%d paths%n", paths.size() );
		for(Path p : paths)
		{
			System.out.println(p.toString());
			for(Edge e : graph.getEdgeSet())
			{
				e.addAttribute("color", java.awt.Color.gray);
			}
			for(Edge e : p.getEdgePath())
			{
				e.addAttribute( "color", java.awt.Color.red );
			}
		}
		graph.write( "out.dgs" );
	}


	public static void main(String[] strs) throws NotFoundException, IOException, GraphParseException
	{
		if (strs.length != 3)
		{
			usage();
			System.exit(-1);
		}
		new TestDijkstra(strs);
	}
	

	public static void usage()
	{
		System.out
				.printf("USAGE : TestDijkstra <filename> <source> <target>%nwhere :%n   <filename>  is the file that contains your graph%n   <source>    is the id of the source node%n   <taget>     is the id of the target node ;-)%n");
	}
}
