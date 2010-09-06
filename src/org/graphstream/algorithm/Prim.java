/*
 * This file is part of GraphStream.
 * 
 * GraphStream is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * GraphStream is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with GraphStream.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;

/**
 * Prim's algorithm is an algorithm which allows to find a minimal spanning tree
 * in a weighted connected graph. More informations on <a
 * href="http://en.wikipedia.org/wiki/Prim%27s_algorithm">Wikipedia</a>.
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
	 */
	public Prim()
	{
		this( "weight", "Kruskal.flag" );
	}
	
	/**
	 * Create a new Prim's algorithm.
	 * 
	 * @param weightAttribute
	 *            attribute used to compare edges
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 */
	public Prim( String weightAttribute, String flagAttribute )
	{
		this( weightAttribute, flagAttribute, true, false );
	}
	
	/**
	 * Create a new Prim's algorithm.
	 * 
	 * @param weightAttribute
	 *            attribute used to compare edges
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 * @param flagOn
	 *            value of the <i>flagAttribute</i> if edge is in the spanning
	 *            tree
	 * @param flagOff
	 *            value of the <i>flagAttribute</i> if edge is not in the
	 *            spanning tree
	 */
	public Prim( String weightAttribute, String flagAttribute, 
			Object flagOn, Object flagOff )
	{
		super( flagAttribute, flagOn, flagOff );
		
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
	 * @param newWeightAttribute
	 *            new attribute used
	 */
	public void setWeightAttribute( String newWeightAttribute )
	{
		this.weightAttribute = newWeightAttribute;
	}
	
	/**
	 * Get weight of an edge.
	 * 
	 * @param e
	 *            an edge
	 * @return weight of <i>n</i>
	 */
	@SuppressWarnings("rawtypes")
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
}
