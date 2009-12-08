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

package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.GraphViewerRemote;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;

/**
 * Prim's algorithm is an algorithm which allows to find a minimal 
 * spanning tree in a weighted connected graph. More informations on 
 * <a href="http://en.wikipedia.org/wiki/Prim%27s_algorithm">Wikipedia</a>.
 * 
 * <p>
 * This algorithm uses the <i>std-algo-1.0</i> algorithm's standard.
 * </p>
 * 
 * @complexity 0(m+m<sup>2</sup>log(m)), where m = |E|
 * 
 * @author Guilhelm Savin
 *
 */
public class Prim
	extends AbstractSpanningTree
{
	/**
	 * Attribute key which will be used to compare edges.
	 */
	protected String weightAttribute;

	/**
	 * Create a new Prim's algorithm.
	 * Graph will be set to null.
	 */
	public Prim()
	{
		this( null );
	}
	/**
	 * Create a new Prim's algorithm.
	 * 
	 * @param graph used graph
	 */
	public Prim( Graph graph )
	{
		this( graph, "weight", "Kruskal.flag" );
	}
	/**
	 * Create a new Prim's algorithm.
	 * 
	 * @param graph used graph
	 * @param weightAttribute attribute used to compare edges
	 * @param flagAttribute attribute used to set if an edge is in the spanning tree
	 */
	public Prim( Graph graph, String weightAttribute, String flagAttribute )
	{
		this( graph, weightAttribute, flagAttribute, true, false );
	}
	/**
	 * Create a new Prim's algorithm.
	 * 
	 * @param graph used graph
	 * @param weightAttribute attribute used to compare edges
	 * @param flagAttribute attribute used to set if an edge is in the spanning tree
	 * @param flagOn value of the <i>flagAttribute</i> if edge is in the spanning tree
	 * @param flagOff value of the <i>flagAttribute</i> if edge is not in the spanning tree
	 */
	public Prim( Graph graph, String weightAttribute, String flagAttribute, 
			Object flagOn, Object flagOff )
	{
		super( graph, flagAttribute, flagOn, flagOff );
		
		this.weightAttribute = weightAttribute;
	}
	
	/**
	 * Get key attribute used to compare edges.
	 * 
	 * @return weight attribute
	 */
	public String getWeightAttribute()
	{
		return this.weightAttribute;
	}
	/**
	 * Set the weight attribute.
	 * 
	 * @param newWeightAttribute new attribute used
	 */
	public void setWeightAttribute( String newWeightAttribute )
	{
		this.weightAttribute = newWeightAttribute;
	}
	/**
	 * Get weight of an edge.
	 * 
	 * @param e an edge
	 * @return weight of <i>n</i>
	 */
	@SuppressWarnings("all")
	protected Comparable getWeight( Edge e )
	{
		return (Comparable) e.getAttribute( weightAttribute, Comparable.class );
	}
	/**
	 * Check if all edges have a weight attribute and that this attribute is an
	 * instance of Comparable.
	 *
	 * @see java.lang.Comparable
	 */
	protected void checkWeights()
	{
		Iterator<? extends Edge> iteE;
		boolean error = false;
		
		iteE = this.graph.getEdgeIterator();
		
		while( iteE.hasNext() )
		{
			if( ! iteE.next().hasAttribute( weightAttribute, Comparable.class ) )
			{
				error = true;
			}
		}
		
		if( error )
		{
			System.err.printf( "*** error *** Prim's algorithm: some edges seem to not have a weigth." );
		}
	}
	/**
	 * Build the tree.
	 */
	@Override
	protected void makeTree()
	{
		checkWeights();
		
		Iterator<? extends Edge> iteE;
		Node current;
		Edge e;
		LinkedList<Node> pool = new LinkedList<Node>();
		LinkedList<Edge> epool = new LinkedList<Edge>();
		WeightEdgeComparator cmp = new WeightEdgeComparator();
		boolean resort = false;
		
		current = this.graph.getNodeIterator().next();
		
		pool.add( current );
		iteE = current.getLeavingEdgeIterator();
		while( iteE.hasNext() )
		{
			epool.add(iteE.next() );
		}
		
		Collections.sort( epool, cmp );
		
		while( pool.size() < graph.getNodeCount() )
		{
			e = epool.poll();
			
			current = null;
			
			if( ! pool.contains( e.getNode0() ) ) current = e.getNode0();
			if( ! pool.contains( e.getNode1() ) ) current = e.getNode1();
			
			if( current == null ) continue;
			
			edgeOn(e);
			pool.add(current);
			
			for( int i = 0; i < epool.size(); )
			{
				Edge tmp = epool.get(i);
				if( tmp.getNode0().equals(current) || tmp.getNode1().equals(current) )
				{
					epool.remove(i);
				}
				else i++;
			}
			
			iteE = current.getLeavingEdgeIterator();
			resort = false;
			while( iteE.hasNext() )
			{
				e = iteE.next();
				
				if( ( ! pool.contains(e.getNode0()) && pool.contains(e.getNode1()) ) ||
						( pool.contains(e.getNode0()) && ! pool.contains(e.getNode1()) ) )
				{
					epool.add(e);
					resort = true;
				}
			}
			
			if( resort )
			{
				Collections.sort( epool, cmp );
			}
		}
	}
	
// Stuff needed to work
	
	/**
	 * A comparator which uses the <i>weightAttribute</i> of its parent's class
	 * to compare edges.
	 * 
	 * @author Guilhelm Savin
	 */
	private final class WeightEdgeComparator implements Comparator<Edge>
	{
		/**
		 * Compare two edges.
		 * 
		 * @return an integer less than 0 if e1 less than e2, more than 0 if e1 more than e2
		 */
		@SuppressWarnings( "all" )
		public int compare( Edge e1, Edge e2 )
		{
			return getWeight( e1 ).compareTo( getWeight( e2 ) );
		}

		@Override
		public boolean equals( Object o )
		{
			return o instanceof WeightEdgeComparator;
		}
	}
	
//	 Main for tests

		public static void main( String [] args )
		{
			Graph graph = new DefaultGraph( "Kruskal's algorithm" );

			GraphViewerRemote remote = graph.display( false );
			remote.setQuality( 4 );
			
			graph.addNode( "A" ).addAttribute( "xy", 0, 0);
			graph.addNode( "B" ).addAttribute( "xy", 1, 0);
			graph.addNode( "C" ).addAttribute( "xy", 2, 0);
			graph.addNode( "D" ).addAttribute( "xy", 0, 1);
			graph.addNode( "E" ).addAttribute( "xy", 2, 1);
			graph.addNode( "F" ).addAttribute( "xy", 0, 2);
			graph.addNode( "G" ).addAttribute( "xy", 2, 2);
			
			graph.addEdge( "AB", "A", "B", false ).addAttribute( "weight",  7 );
			graph.getEdge("AB").addAttribute("label", "7");
			graph.addEdge( "AD", "A", "D", false ).addAttribute( "weight",  5 );
			graph.getEdge("AD").addAttribute("label", "5");
			
			graph.addEdge( "BC", "B", "C", false ).addAttribute( "weight",  8 );
			graph.getEdge("BC").addAttribute("label", "8");
			graph.addEdge( "BD", "B", "D", false ).addAttribute( "weight",  9 );
			graph.getEdge("BD").addAttribute("label", "9");
			graph.addEdge( "BE", "B", "E", false ).addAttribute( "weight",  7 );
			graph.getEdge("BE").addAttribute("label", "7");
			
			graph.addEdge( "CE", "C", "E", false ).addAttribute( "weight",  5 );
			graph.getEdge("CE").addAttribute("label", "5");
			
			graph.addEdge( "DE", "D", "E", false ).addAttribute( "weight",  15 );
			graph.getEdge("DE").addAttribute("label", "15");
			graph.addEdge( "DF", "D", "F", false ).addAttribute( "weight",  6 );
			graph.getEdge("DF").addAttribute("label", "6");
			
			graph.addEdge( "EF", "E", "F", false ).addAttribute( "weight",  8 );
			graph.getEdge("EF").addAttribute("label", "8");
			graph.addEdge( "EG", "E", "G", false ).addAttribute( "weight",  9 );
			graph.getEdge("EG").addAttribute("label", "9");
			
			graph.addEdge( "FG", "F", "G", false ).addAttribute( "weight",  11 );
			graph.getEdge("FG").addAttribute("label", "11");
			
			Prim p = new Prim( graph, "weight", "color", "red", "black" );
			p.compute();
		}
}
