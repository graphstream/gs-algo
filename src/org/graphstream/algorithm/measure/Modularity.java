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
package org.graphstream.algorithm.measure;

import java.util.HashMap;
import java.util.HashSet;

import org.graphstream.algorithm.DynamicAlgorithm;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.stream.SinkAdapter;

import static org.graphstream.algorithm.Toolkit.communities;
import static org.graphstream.algorithm.Toolkit.modularity;
import static org.graphstream.algorithm.Toolkit.modularityMatrix;

/**
 * Computes and update the modularity of a given graph as it evolves.
 *
 * <p>
 * TODO document this.
 * </p>
 */
public class Modularity
	extends SinkAdapter implements DynamicAlgorithm
{
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
	
	/**
	 * New modularity algorithm.
	 */
	public Modularity()
	{
		
	}

	/**
	 * New modularity algorithm with a given marker for communities.
	 * 
	 * @param marker
	 *            name of the attribute marking the communities.
	 */
	public Modularity( String marker )
	{
		this.marker = marker;
	}

	/**
	 * The last computed modularity.
	 * 
	 * @complexity O(1)
	 * @return The last computed modularity.
	 */
	public float getLastComputedValue()
	{
		return Q;
	}
	
	/**
	 * Compute the modularity (if the graph changed since the last computation).
	 * 
	 * @complexity O(n+m!+m!k)
	 * @return The current modularity.
	 */
	public float getModularity()
	{
		compute();
		return Q;
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init( Graph graph )
	{
		if( graph != this.graph )
		{
			if( this.graph != null )
			{
				this.graph.removeSink( this );
			}
			
			this.graph = graph;
			
			if( this.graph != null )
			{
				this.graph.addSink( this );
				initialize();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute()
	{
		if( graphChanged )
		{
			float[][] E = modularityMatrix( graph, communities );
			Q = modularity( E );
			graphChanged = false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graphstream.algorithm.DynamicAlgorithm#terminate()
	 */
	public void terminate()
	{
		// NOP.
	}
	
	protected void initialize()
	{
		communities = communities( graph, marker );
	}
/*
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
*/
	
	/*
	 * @see org.graphstream.stream.Sink#nodeAdded(java.lang.String, long, java.lang.String)
	 */
	public void nodeAdded( String graphId, long timeId, String nodeId )
    {
		// A node added, put it in the communities.

		Node node = graph.getNode( nodeId );
		
		if( node != null )
		{
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
    }

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.Sink#nodeRemoved(java.lang.String, long, java.lang.String)
	 */
	public void nodeRemoved( String graphId, long timeId, String nodeId )
    {
		Node node = graph.getNode( nodeId );
		
		if( node != null )
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
    }

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.Sink#edgeAdded(java.lang.String, long, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public void edgeAdded( String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed )
    {
		graphChanged = true;
    }

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.Sink#edgeRemoved(java.lang.String, long, java.lang.String)
	 */
	public void edgeRemoved( String graphId, long timeId, String edgeId )
    {
		graphChanged = true;
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.Sink#graphCleared(java.lang.String, long)
	 */
	public void graphCleared( String graphId, long timeId)
	{
		graphChanged = true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.Sink#nodeAttributeAdded(java.lang.String, long, java.lang.String, java.lang.String, java.lang.Object)
	 */
	public void nodeAttributeAdded( String graphId, long timeId,
			String nodeId, String attribute, Object value )
    {
		nodeAttributeChanged( graphId, timeId, nodeId, attribute, null, value );
    }

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.Sink#nodeAttributeChanged(java.lang.String, long, java.lang.String, java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void nodeAttributeChanged( String graphId, long timeId, String nodeId,
			String attribute, Object oldValue, Object newValue )
    {
		if( attribute.equals( marker ) )
		{
			Node node = graph.getNode( nodeId );
			graphChanged = true;
			
			// The node changed community.
			
			if( oldValue != newValue )
			{
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
}