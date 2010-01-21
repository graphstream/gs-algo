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
import org.graphstream.oldUi.GraphViewerRemote;

import java.util.LinkedList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Kruskal's algorithm is a greedy algorithm which allows to find a minimal 
 * spanning tree in a weighted connected graph. More informations on 
 * <a href="http://en.wikipedia.org/wiki/Kruskal%27s_algorithm">Wikipedia</a>.
 * 
 * <p>
 * This algorithm uses the <i>std-algo-1.0</i> algorithm's standard.
 * </p>
 * 
 * @complexity m*(log(m)+3)+n+n<sup>2</sup>, m = |E|, n = |V|
 * 
 * @author Guilhelm Savin
 *
 */
public class Kruskal extends AbstractSpanningTree
{
	/**
	 * Attribute which will be used to compare edges.
	 */
	protected String		weightAttribute;
	/**
	 * Attribute used to clusterize the graph.
	 */
	protected String		clusterAttribute = "Kruskal.cluster";
	/**
	 * List of edges that will be added to the tree.
	 */
	protected LinkedList<Edge>	edgesToTreat;
	
	/**
	 * Create a new Kruskal's algorithm.
	 * Graph will be set to null.
	 */
	public Kruskal()
	{
		this( null );
	}
	/**
	 * Create a new Kruskal's algorithm.
	 * 
	 * @param graph used graph
	 */
	public Kruskal( Graph graph )
	{
		this( graph, "weight", "Kruskal.flag" );
	}
	/**
	 * Create a new Kruskal's algorithm.
	 * 
	 * @param graph used graph
	 * @param weightAttribute attribute used to compare edges
	 * @param flagAttribute attribute used to set if an edge is in the spanning tree
	 */
	public Kruskal( Graph graph, String weightAttribute, String flagAttribute )
	{
		this( graph, weightAttribute, flagAttribute, true, false );
	}
	/**
	 * Create a new Kruskal's algorithm.
	 * 
	 * @param graph used graph
	 * @param weightAttribute attribute used to compare edges
	 * @param flagAttribute attribute used to set if an edge is in the spanning tree
	 * @param flagOn value of the <i>flagAttribute</i> if edge is in the spanning tree
	 * @param flagOff value of the <i>flagAttribute</i> if edge is not in the spanning tree
	 */
	public Kruskal( Graph graph, String weightAttribute, String flagAttribute, 
			Object flagOn, Object flagOff )
	{
		super( graph, flagAttribute, flagOn, flagOff );
		
		this.weightAttribute = weightAttribute;
		this.edgesToTreat = new LinkedList<Edge>();
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
	
// Protected Access
	
	/**
	 * Sort edges using <i>weightAttribute</i> to compare.
	 */
	protected void sortEdgesByWeight()
	{
		Collections.sort( edgesToTreat, new WeightEdgeComparator() );
	}
	/**
	 * Create the <i>edgesToTreat</i> list. Also check if all edges as
	 * a <i>weightAttribute</i> which is an instance of Comparable.
	 * 
	 * @see java.lang.Comparable
	 */
	protected void buildAndCheck()
	{
		Iterator<? extends Edge> iteE;
		boolean error = false;
		
		edgesToTreat.clear();
		
		iteE = this.graph.getEdgeIterator();
		
		while( iteE.hasNext() )
		{
			edgesToTreat.addLast( iteE.next() );
			if( ! edgesToTreat.getLast().hasAttribute( weightAttribute, Comparable.class ) )
			{
				error = true;
			}
		}
		
		if( error )
		{
			System.err.printf( "*** error *** Kruskal's algorithm: some weight are not comparable%n" );
		}
	}
	/**
	 * Reset cluster and flag attribute values.
	 */
	@Override
	protected void resetFlags()
	{
		super.resetFlags();
		
		Iterator<? extends Node> iteN;
		int cluster = 0;
		
		iteN = this.graph.getNodeIterator();
		
		while( iteN.hasNext() )
		{
			iteN.next().setAttribute( clusterAttribute, cluster++ );
		}
	}
	/**
	 * Get weight of an edge.
	 * 
	 * @param e an edge
	 * @return weight of <i>e</i>
	 */
	@SuppressWarnings("all")
	protected Comparable getWeight( Edge e )
	{
		return (Comparable) e.getAttribute( weightAttribute, Comparable.class );
	}
	/**
	 * Get cluster of a node.
	 * 
	 * @param n a node
	 * @return cluster of <i>n</i>
	 */
	protected int getCluster( Node n )
	{
		return (Integer) n.getAttribute( clusterAttribute );
	}
	/**
	 * Build the spanning tree.
	 */
	@Override
	protected void makeTree()
	{
		buildAndCheck();
		sortEdgesByWeight();
		
		int treeSize = 0, c1, c2;
		Edge e = null;
		
		while( treeSize < graph.getNodeCount() - 1 )
		{
			if( edgesToTreat.size() == 0 )
			{
				System.err.printf( "*** warning *** Kruskal's algorithm: error while making tree%n" );
				break;
			}
			
			e = edgesToTreat.poll();
			c1 = getCluster( e.getNode0() );
			c2 = getCluster( e.getNode1() );
			
			if( c1 != c2 )
			{
				edgeOn( e );
				treeSize++;
				mergeClusters( e.getNode0(), e.getNode1() );
			}
		}
	}
	/**
	 * Merge two clusters.
	 * 
	 * @param n0 first node
	 * @param n1 second node
	 */
	protected void mergeClusters( Node n0, Node n1 )
	{
		int c1 = getCluster( n0 );
		int c2 = getCluster( n1 );
		
		LinkedList<Node> pool = new LinkedList<Node>();
		Node current = null;
		Iterator<? extends Node> iteN = null;
		
		
		pool.add( n1 );
		
		while( pool.size() > 0 )
		{
			current = pool.poll();
			current.setAttribute( clusterAttribute, c1 );
			
			iteN = current.getNeighborNodeIterator();
			while( iteN.hasNext() )
			{
				current = iteN.next();
				if( getCluster( current ) == c2 && ! pool.contains( current ) )
				{
					pool.add( current );
				}
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
	@SuppressWarnings("all")
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
		
		public boolean equals( Object o )
		{
			return o instanceof WeightEdgeComparator;
		}
	}
	
// Main for tests

	public static void main( String [] args )
	{
		Graph graph = new DefaultGraph( "Kruskal's algorithm" );

		GraphViewerRemote remote = graph.oldDisplay( false );
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
		
		Kruskal k = new Kruskal( graph, "weight", "color", "red", "black" );
		k.compute();
	}
}
