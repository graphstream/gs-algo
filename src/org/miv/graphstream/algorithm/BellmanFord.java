/**
 * 
 */
package org.miv.graphstream.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.Path;

/**
 * <p>
 * The Bellman�Ford algorithm computes single-source shortest paths in a
 * weighted digraph (where some of the edge weights may be negative). Dijkstra's
 * algorithm accomplishes the same problem with a lower running time, but
 * requires edge weights to be non-negative. Thus, Bellman�Ford is usually used
 * only when there are negative edge weights (from the <a href="http://en.wikipedia.org/wiki/Bellman-Ford_algorithm">Wikipedia</a>).
 * </p>
 * 
 * <h3>Warning</h3>
 * <p>
 * For the moment only attributes located on the edges are supported.
 * </p> 
 * 
 * @complexity O(V�E) time, where V and E are the number of vertices and edges respectively.
 * 
 * @author Antoine Dutot
 * @author Yoann Pign�
 * 
 */
public class BellmanFord
{

	/**
	 * The graph to be computed for shortest path.
	 */
	Graph graph;
	Node source;
	@SuppressWarnings("all")
	public BellmanFord(Graph graph,  String attribute, Node source )
	{
		this.graph = graph;
		this.source= source;

		// Step 1: Initialize graph
		for(Node n : graph.getNodeSet())
		{
			if(n==source)
			{
				n.addAttribute( "BellmanFord.distance", 0.0 );
			}
			else
			{
				n.addAttribute( "BellmanFord.distance", null );
			}
			n.addAttribute( "BellmanFord.predecessors", null );
		}
		
		// Step 2: relax edges repeatedly
		for(int i = 0; i < graph.getNodeCount(); i++)
		{
			System.out.println();
			for(Edge e : graph.getEdgeSet())
			{
				Node n0 = e.getNode0();
				Node n1 = e.getNode1();
				Double d0 = (Double) n0.getAttribute( "BellmanFord.distance" );
				Double d1 = (Double) n1.getAttribute( "BellmanFord.distance" );
				
				Double we = (Double) e.getAttribute( attribute );
				if(we ==null)
					throw new NumberFormatException("org.miv.graphstream.algorithm.BellmanFord: Problem with attribute \""+attribute+"\" on edge "+e);
				
//				Double w0 = (Double) n0.getAttribute( attribute );
//				Double w1 = (Double) n1.getAttribute( attribute );
				
//				if( w0==null || w1==null)
//					throw new NumberFormatException("org.miv.graphstream.algorithm.BellmanFord: Problem with attribute \""+attribute+"\" on edge "+e);
				if( d0 != null )
				{
					if( d1 == null || d1 >= d0 + we )
					{
						n1.addAttribute( "BellmanFord.distance", d0 + we );
						ArrayList<Edge> predecessors = (ArrayList<Edge>) n1.getAttribute( "BellmanFord.predecessors" );
						if( d1!=null && d1 == d0 + we )
						{
							if( predecessors == null )
							{
								predecessors = new ArrayList<Edge>();
							}
						}
						else
						{
							predecessors = new ArrayList<Edge>();
						}
						if(!predecessors.contains( e ))
						{
							predecessors.add( e );
						}
						// --- DEGUG ---
					//	System.out.printf("Edge %s --- Node %s predecessors.size()=%d%n",e.toString(),n1.toString(), predecessors.size());
						
						n1.addAttribute( "BellmanFord.predecessors" ,predecessors);
						
					}
				}
			}
		}
	

		// Step 3: check for negative-weight cycles
		for(Edge e : graph.getEdgeSet()) {
			Node n0 = e.getNode0();
			Node n1 = e.getNode1();
			Double d0 = (Double) n0.getAttribute( "BellmanFord.distance" );
			Double d1 = (Double) n1.getAttribute( "BellmanFord.distance" );
	
			Double we = (Double) e.getAttribute( attribute );
			if(we ==null)
				throw new NumberFormatException("org.miv.graphstream.algorithm.BellmanFord: Problem with attribute \""+attribute+"\" on edge "+e);
			if( d1 > d0 + we )
			{
				throw new NumberFormatException("org.miv.graphstream.algorithm.BellmanFord: Problem: negative weight  cycle detected. Edge "+e);
			}
		}
	}
	

	/**
	 * Constructs all the possible shortest paths from the source node to the
	 * destination (end). Warning: this construction is VERY HEAVY !
	 * 
	 * @param end The destination to which shortest paths are computed.
	 * @return a list of shortest paths given with
	 *         {@link org.miv.graphstream.graph.Path} objects.
	 */
	public List<Path> getPathSetShortestPaths( Node end )
	{
		ArrayList<Path> paths = new ArrayList<Path>();
		pathSetShortestPath_facilitate( end, new Path(), paths );
		return paths;
	}

	@SuppressWarnings("all")
	private void pathSetShortestPath_facilitate( Node current, Path path, List<Path> paths )
	{
		if( current != source )
		{
			Node next = null;
			ArrayList<? extends Edge> predecessors = (ArrayList<? extends Edge>) current.getAttribute( "BellmanFord.predecessors" );
			while( current != source && predecessors.size() == 1 )
			{
				Edge e = predecessors.get( 0 );
				next = e.getOpposite( current );
				path.add( current, e );
				current = next;
				predecessors = (ArrayList<? extends Edge>) current.getAttribute( "BellmanFord.predecessors" );
			}
			if( current != source )
			{
				for( Edge e: predecessors )
				{
					Path p = path.getACopy();
					p.add( current, e );
					pathSetShortestPath_facilitate( e.getOpposite( current ), p, paths );

				}
			}
		}
		if( current == source )
		{
			paths.add( path );
		}
	}

	
	
	
}


