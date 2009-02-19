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

package org.miv.graphstream.algorithm;

import java.util.*;

import org.miv.graphstream.graph.*;

/**
 * Computes and update the modularity of a given graph as it evolves.
 *
 * <p>
 * TODO document this.
 * </p>
 *
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since 2007
 */
public class Modularity implements DynamicAlgorithm, GraphListener
{
// Attributes
	
	/**
	 * The graph.
	 */
	protected Graph graph;
	
	/**
	 * Name of the attribute marking the communities.
	 */
	protected String marker;
	
	/**
	 * All communities indexed by their marker value.
	 */
	protected HashMap<Object,HashSet<Node>> communities;

	/**
	 * Set to false after {@link #compute()}.
	 */
	protected boolean graphChanged = true;
	
	/**
	 * Last value computed.
	 */
	protected float Q;
	
// Construction
	
	public Modularity( Graph graph, String marker )
	{
		this.marker = marker;
		setGraph( graph );
	}

// Access
	
	/**
	 * The last computed modularity.
	 * @complexity O(1)
	 * @return The last computed modularity.
	 */
	public float getLastComputedValue()
	{
		return Q;
	}
	
	/**
	 * Compute the modularity (if the graph changed since the last computation).
	 * @complexity O(n+m²+m²k)
	 * @return The current modularity.
	 */
	public float getModularity()
	{
		compute();
		return Q;
	}
	
// Command
	
	public void begin()
	{
		// NOP.
	}

	public void end()
	{
		// NOP.
	}

	public void compute()
	{
		if( graphChanged )
		{
			float[][] E = graph.algorithm().modularityMatrix( communities );
			Q = graph.algorithm().modularity( E );
			graphChanged = false;
		}
	}

	public Graph getGraph()
	{
		return graph;
	}

	public void setGraph( Graph graph )
	{
		if( graph != this.graph )
		{
			if( this.graph != null )
			{
				this.graph.removeGraphListener( this );
			}
			
			this.graph = graph;
			
			if( this.graph != null )
			{
				this.graph.addGraphListener( this );
				initialize();
			}
		}
	}
	
	protected void initialize()
	{
		communities = graph.algorithm().communities( marker );
	}

	public void beforeGraphClear( Graph graph )
	{
		graphChanged = true;
	}

	public void afterEdgeAdd( Graph graph, Edge edge )
	{
		graphChanged = true;
	}

	public void beforeEdgeRemove( Graph graph, Edge edge )
	{
		graphChanged = true;
	}

	public void afterNodeAdd( Graph graph, Node node )
	{
		// A node added, put it in the communities.
		
		Object communityKey = node.getAttribute( marker );
		
		if( communityKey == null )
			communityKey = "NULL_COMMUNITY";
		
		HashSet<Node> community = communities.get( communityKey );
			
		if( community == null )
		{
			community = new HashSet<Node>();
			communities.put( communityKey, community );
		}
			
		community.add( node );

		graphChanged = true;		
	}

	public void beforeNodeRemove( Graph graph, Node node )
	{
		Object communityKey = node.getAttribute( marker );
		
		if( communityKey == null )
			communityKey = "NULL_COMMUNITY";
		
		HashSet<Node> community = communities.get( communityKey );
		
		assert community != null : "Removing a node that was not placed in any community !!";
		
		if( community != null )
		{
			community.remove( node );
			
			if( community.size() == 0 )
			{
				communities.remove( communityKey );
			}
		}
		
		graphChanged = true;
	}

	public void attributeChanged( Element element, String attribute,
			Object oldValue, Object newValue )
	{
		if( attribute.equals( marker ) && element instanceof Node )
		{
			graphChanged = true;
			
			// The node changed community.
			
			if( oldValue != newValue )
			{
				Node node = (Node) element;
				
				HashSet<Node> communityFrom = communities.get( oldValue );
				HashSet<Node> communityTo   = communities.get( newValue );
				
				if( communityFrom != null )
				{
					communityFrom.remove( node );
					
					if( communityFrom.size() == 0 )
						communities.remove( oldValue );
				}
				
				if( communityTo == null )
				{
					communityTo = new HashSet<Node>();
					communities.put( newValue, communityTo );
				}
				
				communityTo.add( node );
			}
		}
	}

	public void stepBegins(Graph graph, double time)
	{
	}
}