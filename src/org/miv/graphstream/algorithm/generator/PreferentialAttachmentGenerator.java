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

package org.miv.graphstream.algorithm.generator;

import java.util.*;

import org.miv.graphstream.graph.*;

/**
 * Scale-free graph (tree) generator using the preferential attachement rule.
 * 
 * <p>
 * This is a very simple graph generator that generates a tree using the
 * preferential attachement rule: nodes are generated one by one, and each time
 * attached by an edge to another node that has more chance to choosed if it
 * already has lots of nodes attached to it.
 * </p>
 * 
 * <p>
 * The more this generator is iterated, the more nodes are generated. It can
 * therefore generate trees of any size.
 * </p>
 *
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since  20061128
 */
public class
	PreferentialAttachmentGenerator
implements
	Generator
{
// Attributes
	
	/**
	 * The graph to grow.
	 */
	protected Graph graph;
	
	/**
	 * Degree of each node.
	 */
	protected ArrayList<Integer> degrees;
	
	/**
	 * Maximal degree at time t.
	 */
	protected int degreeMax = 0;
	
// Constructors
	
// Accessors
	
// Commands
	
	public void
	begin( Graph graph )
	{
		this.graph     = graph;
		this.degrees   = new ArrayList<Integer>();
		this.degreeMax = 0;
		
		graph.addNode( "0" );
		degrees.add( 0 );
	}

	public void
	end()
	{
		graph     = null;
		degrees   = null;
		degreeMax = 0;
	}

	public boolean
	nextElement()
	{
		// Generate a new node.
		
		int    index = degrees.size();
		String id    = Integer.toString( index );
		
		graph.addNode( id );
		degrees.add( 0 );
		
		// Compute the attachment probability of each previouly added node
		
		int sumDeg = graph.getEdgeCount() * 2;
		
		// Choose the node to attach to.
		
		float sumProba = 0;
		float rnd      = (float) Math.random();
		int   otherIdx = -1;
		
		for( int i=0; i<index; ++i )
		{
			float proba = sumDeg == 0 ? 1 : ((float)degrees.get( i )) / ((float)sumDeg);
			
			sumProba += proba;
			
			if( sumProba > rnd )
			{
				otherIdx = i;
				break;
			}
		}
		
		// Attach to the other node.
		
		if( otherIdx >= 0 )
		{
			String oid = Integer.toString( otherIdx );
			String eid = id +  "_" + oid;
			
			graph.addEdge( eid, oid, id, false );
			degrees.set( otherIdx, degrees.get( otherIdx ) + 1 );
			degrees.set( index, degrees.get( index ) + 1 );
		}
		else
		{
			System.err.printf( "PreferentialAttachmentGenerator: Aieuu!%n" );
		}
		
		// It is always possible to add an element.
		
		return true;
	}
}