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
 * Copyright 2006 - 2009
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm.generator;

import java.util.*;

//import org.graphstream.graph.*;
import org.graphstream.io.SourceBase;

/**
 * Base graph generator.
 *
 * <p>
 * This class is a base to implement generators. It it has facilities to
 * generate edges or nodes, and provides services to add attributes on them
 * and to choose if the edge is directed or not.
 * </p>
 * 
 * <p>
 * Indeed, This generator has the ability to add randomly choosed numerical
 * values on arbitrary attributes on edges or nodes of the graph, and to
 * randomly choose a direction for edges.
 * </p>
 * 
 * <p>
 * A list of attributes can be given for nodes and edges. In this case each
 * new node or edge added will have this attribute and the value will be a
 * randomly choosed number. The range in which these numbers are choosed can be
 * specified.
 * </p>
 * 
 * <p>
 * By default, edges are not oriented. It is possible to ask orientation, and in
 * addition to ask that the direction be choosed randomly (by default, if edges
 * must be oriented, the order given for the two nodes to connect is used). 
 * </p>
 *
 * @since 2007
 */
public abstract class BaseGenerator
	extends SourceBase
	implements Generator
{
	protected class NodeKeepData
	{
		LinkedList<String> edges;
		
		void keepEdge( String id )
		{
			if( edges == null )
				edges = new LinkedList<String>();
			
			if( ! edges.contains(id) )
				edges.add(id);
		}
		
		void unkeepEdge( String id )
		{
			if( edges != null )
				edges.remove(id);
		}
	}
	
	protected class EdgeKeepData
	{
		String src, trg;
		
		EdgeKeepData( String src, String trg )
		{
			keepNodes(src,trg);
		}
		
		void keepNodes( String src, String trg )
		{
			this.src = src;
			this.trg = trg;
		}
	}
	
// Attributes
	
	/**
	 * Are edges directed ?.
	 */
	protected boolean directed = false;
	
	/**
	 * If directed, choose the direction randomly?.
	 */
	protected boolean randomlyDirected = false;
	
	/**
	 * List of attributes to put on nodes with a randomly choosed numerical value.
	 */
	protected ArrayList<String> nodeAttributes = new ArrayList<String>();
	
	/**
	 * List of attributes to put on edges with a randomly choosed numerical value.
	 */
	protected ArrayList<String> edgeAttributes = new ArrayList<String>();
	
	/**
	 * If node attributes are added, in which range are the numbers choosed ?.
	 */
	protected float[] nodeAttributeRange = new float[2];
	
	/**
	 * If edge attributes are added, in which range are the numbers choosed ?.
	 */
	protected float[] edgeAttributeRange = new float[2];
	
	/**
	 * List of all generated nodes so far. Used to create edges toward all other
	 * nodes at each step.
	 */
	protected ArrayList<String> 			nodes 		= new ArrayList<String>();
	protected ArrayList<String> 			edges 		= new ArrayList<String>();
	protected HashMap<String,NodeKeepData>	nodesData 	= new HashMap<String,NodeKeepData>();
	protected HashMap<String,EdgeKeepData>	edgesData 	= new HashMap<String,EdgeKeepData>();
	
	protected boolean keepNodesId = false;
	protected boolean keepEdgesId = false;
	
	/**
	 * The random number generator.
	 */
	protected Random random = new Random();

	/**
	 * Seth the node label attribute using the identifier?.
	 */
	protected boolean addNodeLabels = false;

	/**
	 * Seth the edge label attribute using the identifier?.
	 */
	protected boolean addEdgeLabels = false;
	
// Constructors
	
	/**
	 * New base graph generator. By default no attributes are added to nodes and
	 * edges, and edges are not directed.
	 */
	public BaseGenerator()
	{
		this( false, false );
	}
	
	/**
	 * New base graph generator. By default no attributes are added to nodes and
	 * edges. It is possible to make edge randomly directed.
	 * @param directed If true the edges are directed.
	 * @param randomlyDirectedEdges If true edge, are directed and the direction
	 * is choosed randomly.
	 */
	public BaseGenerator( boolean directed, boolean randomlyDirectedEdges )
	{
		super("generator");
		setDirectedEdges( directed, randomlyDirectedEdges );
		
		nodeAttributeRange[0] = 0;
		nodeAttributeRange[1] = 1;
		edgeAttributeRange[0] = 0;
		edgeAttributeRange[1] = 1;
	}
	
	/**
	 * New random graph generator.
	 * @param directed If true the edges are directed.
	 * @param randomlyDirectedEdges It true, edges are directed and the direction is choosed randomly.
	 * @param nodeAttribute put an attribute by that name on each node with a random numeric value.
	 * @param edgeAttribute put an attribute by that name on each edge with a random numeric value.
	 */
	public BaseGenerator( boolean directed, boolean randomlyDirectedEdges, String nodeAttribute, String edgeAttribute )
	{
		this( directed, randomlyDirectedEdges );

		addNodeAttribute( nodeAttribute );
		addEdgeAttribute( edgeAttribute );
	}
	
// Access
	
// Commands
	
	protected void enableKeepNodesId()
	{
		keepNodesId = true;
	}
	
	protected void disableKeepNodesId()
	{
		keepNodesId = false;
	}
	
	protected void enableKeepEdgesId()
	{
		keepEdgesId = true;
	}
	
	protected void disableKeepEdgesId()
	{
		keepEdgesId = false;
	}
	
	/**
	 * Set the random seed used for random number generation.
	 * @param seed The seed.
	 */
	public void setRandomSeed( long seed )
	{
		random.setSeed( seed );
	}
	
	/**
	 * Allow to add label attributes on nodes. The label is the identifier of
	 * the node.
	 * @param on If true labels are added.
	 */
	public void addNodeLabels( boolean on )
	{
		addNodeLabels = on;
	}
	
	/**
	 * Allow to add label attributes on edges. The label is the identifier of
	 * the edge.
	 * @param on If true labels are added.
	 */
	public void addEdgeLabels( boolean on )
	{
		addEdgeLabels = on;
	}
	
	/**
	 * Make each generated edge directed or not. If the new edge created are
	 * directed, the direction is chosen randomly.
	 * @param directed It true, edge will be directed. 
	 * @param randomly If true, not only edges are directed, but the direction
	 * is chosen randomly.
	 */
	public void setDirectedEdges( boolean directed, boolean randomly )
	{
		this.directed = directed;
		
		if( directed && randomly )
			randomlyDirected = randomly;
	}
	
	/**
	 * Add this attribute on all nodes generated. This attribute will have a
	 * numerical value chosen in a range that is by default [0-1].
	 * @param name The attribute name.
	 * @see #setNodeAttributesRange(float, float)
	 * @see #removeNodeAttribute(String)
	 */
	public void addNodeAttribute( String name )
	{
		nodeAttributes.add( name );
	}
	
	/**
	 * Remove an automatic attribute for nodes.
	 * @param name The attribute name.
	 * @see #addNodeAttribute(String)
	 */
	public void removeNodeAttribute( String name )
	{
		int pos = nodeAttributes.indexOf( name );
		
		if( pos >= 0 )
			nodeAttributes.remove( pos );
	}
	
	/**
	 * Add this attribute on all edges generated. This attribute will have a
	 * numerical value chosen in a range that is by default [0-1].
	 * @param name The attribute name.
	 * @see #setEdgeAttributesRange(float, float)
	 * @see #removeEdgeAttribute(String)
	 */
	public void addEdgeAttribute( String name )
	{
		edgeAttributes.add( name );
	}
	
	/**
	 * Remove an automatic attribute for edges.
	 * @param name The attribute name.
	 * @see #addEdgeAttribute(String)
	 */
	public void removeEdgeAttribute( String name )
	{
		int pos = edgeAttributes.indexOf( name );
		
		if( pos >= 0 )
			edgeAttributes.remove( pos );
	}
	
	/**
	 * If node attributes are added automatically, choose in which range the values are choosed.
	 * @see #addNodeAttribute(String)
	 */
	public void setNodeAttributesRange( float low, float hi )
	{
		nodeAttributeRange[0] = low;
		nodeAttributeRange[1] = hi;
	}
	
	/**
	 * If edge attributes are added automatically, choose in which range the values are choosed.
	 * @see #addEdgeAttribute(String)
	 */
	public void setEdgeAttributesRange( float low, float hi )
	{
		edgeAttributeRange[0] = low;
		edgeAttributeRange[1] = hi;
	}

	/**
	 * Same as {@link #addNode(String)} but specify attributes to position the node on a plane.
	 * @param id The node identifier.
	 * @param x The node abscissa.
	 * @param y The node ordinate.
	 * @return The create node.
	 */
	protected void addNode( String id, float x, float y )
	{
		addNode(id);
		sendNodeAttributeAdded(sourceId,id,"xy",new float [] {x,y});
	}
	
	/**
	 * Add a node and put attributes on it if needed.
	 * @param id The new node identifier.
	 */
	protected void addNode( String id )
	{
		sendNodeAdded(sourceId,id);
		
		if( addNodeLabels )
			sendNodeAttributeAdded(sourceId,id,"label",id);
		
		float value;
		
		for( String attr: nodeAttributes )
		{
			value = ( random.nextFloat() * ( nodeAttributeRange[1] - nodeAttributeRange[0] ) ) + nodeAttributeRange[0];
			sendNodeAttributeAdded(sourceId,id,attr,value);
		}
		
		if( keepNodesId )
		{
			nodes.add(id);
			nodesData.put(id,new NodeKeepData());
		}
	}
	
	protected void delNode( String id )
	{
		if( keepNodesId )
		{
			if( keepEdgesId )
			{
				NodeKeepData nkd = nodesData.get(id);
				
				if( nkd.edges != null )
					while( nkd.edges.size() > 0 ) delEdge(nkd.edges.peek());
			}
			
			nodes.remove(id);
			nodesData.remove(id);
		}
		
		sendNodeRemoved( sourceId, id );
	}
	
	/**
	 * Add an edge, choosing randomly its orientation if needed and putting
	 * attribute on it if needed.
	 * @param id The edge identifier, if null, the identifier is created from the nodes identifiers.
	 * @param from The source node (can be inversed randomly with the target node).
	 */
	protected void addEdge( String id, String from, String to )
	{
		if( directed && randomlyDirected && ( random.nextFloat() > 0.5f ) )
		{
			String tmp = from;
			from       = to;
			to         = tmp;
		}

		if( id == null )
			id = from + "_" + to;
		
		sendEdgeAdded(sourceId,id,from,to,directed);
		
		if( addEdgeLabels )
			sendEdgeAttributeAdded(sourceId,id,"label",id);
		
		for( String attr: edgeAttributes )
		{
			float value = ( random.nextFloat() * ( edgeAttributeRange[1] - edgeAttributeRange[0] ) ) + edgeAttributeRange[0];
			sendEdgeAttributeAdded(sourceId,id,attr,value);
		}
		
		if( keepEdgesId )
		{
			edges.add(id);
			
			if( keepNodesId )
			{
				edgesData.put(id,new EdgeKeepData(from,to));
				nodesData.get(from).keepEdge(id);
				nodesData.get(to).keepEdge(id);
			}
		}
	}
	
	protected void delEdge( String edgeId )
	{
		sendEdgeRemoved(sourceId,edgeId);
		
		if( keepEdgesId )
		{
			edges.remove(edgeId);
			
			if( keepNodesId )
			{
				EdgeKeepData ekd = edgesData.get(edgeId);
				
				nodesData.get(ekd.src).unkeepEdge(edgeId);
				nodesData.get(ekd.trg).unkeepEdge(edgeId);
			}
		}
	}
}
