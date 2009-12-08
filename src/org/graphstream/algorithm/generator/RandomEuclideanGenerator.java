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

package org.graphstream.algorithm.generator;

import java.util.Iterator;

import org.graphstream.graph.*;

/**
 * Random Euclidean graph generator.
 * 
 * <p>
 * This generator creates random graphs of any size. Links of such graphs are
 * created according to a threshold. If the Euclidean distance between two nodes
 * is less than a given threshold, then a link is created between those 2 nodes.
 * Calling {@link #begin(Graph)} put one unique node in the graph, then
 * {@link #nextElement()} will add a new node each time it is called and connect
 * this node to its neighbors according to the threshold planar Euclidean
 * distance.
 * </p>
 * 
 * <p>
 * This generator has the ability to add randomly chosen numerical values on
 * arbitrary attributes on edges or nodes of the graph, and to randomly choose a
 * direction for edges.
 * </p>
 * 
 * <p>
 * A list of attributes can be given for nodes and edges. In this case each new
 * node or edge added will have this attribute and the value will be a randomly
 * chosen number. The range in which these numbers are chosen can be specified.
 * </p>
 * 
 * <p>
 * By default, edges are not oriented. It is possible to ask orientation, in
 * which case the direction is chosen randomly.
 * </p>
 *
 * <p>
 * By default, the graph is generated in the plane (2 dimensions) . Cartesian
 * coordinates on nodes will be generated at random. So, each node will
 * automatically be given two attributes: "x" and "y". If a dimension is
 * specified, then |dimension| attributes are generated, and the 2-norm distance (<a
 * href="http://en.wikipedia.org/wiki/Euclidean_distance">Euclidean distance</a>)
 * is considered in that dimension between the nodes.
 * </p>
 * 
 * <p>
 * If the dimension is 2, then attributes "x" and "y" are defined for each node.
 * If dimension is 3, then attributes "x", "y" and "z" are used. For other
 * values of dimension, |dimension| attributes are defined ("xi" with "i" \in
 * |dimension|) .
 * </p>
 * 
 * @author Antoine Dutot
 * @author Yoann Pign�
 * @since June 25 2007
 * @complexity For the construction of a n nodes graph, the complexity is about O(n^2).
 */
public class RandomEuclideanGenerator extends BaseGenerator
{
	// Attributes

	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;

	/**
	 * The dimension of the space.
	 */
	protected int dimension = 2;

	/**
	 * The threshold that defines whether or not a link is created between to
	 * nodes. Since the coordinate system is defined between 0 and 1, the
	 * threshold has to be set between these two bounds.
	 */
	protected float threshold=0.1f;
	
	// constructors
	private void initDimension( int dimension )
	{
		this.dimension = dimension;
		super.setNodeAttributesRange( 0f, 1f );
		if( dimension > 0 )
		{
			if( dimension == 2 )
			{
				super.addNodeAttribute( "x" );
				super.addNodeAttribute( "y" );
			}
			else if( dimension == 3 )
			{
				super.addNodeAttribute( "x" );
				super.addNodeAttribute( "y" );
				super.addNodeAttribute( "z" );
			}
			else
			{
				for( int i = 0; i < dimension; i++ )
					super.addNodeAttribute( "x" + i );
			}
		}
		else
			System.err.println( "dimension has to be higher that zero" );

	}
	/**
	 * New random Euclidean graph generator. By default no attributes are added
	 * to nodes and edges. Dimension of  the space is two.
	 */
	public RandomEuclideanGenerator( )
	{
		super( );
		initDimension(2);
	}

	/**
	 * New random Euclidean graph generator. By default no attributes are added
	 * to nodes and edges.  You
	 * may also specify a dimension for the space.
	 * @param dimension The dimension of the space for the graph. By default it is two.
	 */
	public RandomEuclideanGenerator( int dimension )
	{
		super( );
		initDimension(dimension);
	}

	/**
	 * New random Euclidean graph generator. By default no attributes are added
	 * to nodes and edges. It is possible to make edge randomly directed. You
	 * may also specify a dimension for the space.
	 * @param dimension The dimension of the space for the graph. By default it is two.
	 * @param directed If true the edges are directed.
	 * @param randomlyDirectedEdges If true edge, are directed and the direction
	 *        is chosen at randomly.
	 */
	public RandomEuclideanGenerator( int dimension, boolean directed, boolean randomlyDirectedEdges )
	{
		super( directed, randomlyDirectedEdges );
		initDimension(dimension);
	}

	/**
	 * New random Euclidean graph generator.
	 * @param dimension The dimension of the space for the graph. By default it is two.
	 * @param directed If true the edges are directed.
	 * @param randomlyDirectedEdges It true, edges are directed and the
	 *        direction is chosen at random.
	 * @param nodeAttribute put an attribute by that name on each node with a
	 *        random numeric value.
	 * @param edgeAttribute put an attribute by that name on each edge with a
	 *        random numeric value.
	 */
	public RandomEuclideanGenerator( int dimension, boolean directed, boolean randomlyDirectedEdges, String nodeAttribute,
			String edgeAttribute )
	{
		super( directed, randomlyDirectedEdges, nodeAttribute, edgeAttribute );
		initDimension(dimension);
	}

	// Commands

	
	@Override
	public void begin( Graph graph )
	{
		this.graph = graph;

		String id = Integer.toString( nodeNames++ );

		addNode( id );
	}

	@Override
	public void end()
	{}

	@Override
	public boolean nextElement()
	{
		String id = Integer.toString( nodeNames++ );

		addNode( id );

		Node n = graph.getNode(id);
		Iterator<? extends Node> nodes = graph.getNodeIterator();
		
		while( nodes.hasNext() )
		{
		    Node n2 = nodes.next();
		    
		    if( n != n2 && distance(n, n2) < threshold)
		    {
			super.addEdge( id+"-"+n2.getId() , id, n2.getId());
		    }
		}
		return true;
	}

	private float distance(Node n, Node n2)
	{
		float d =0f;
		if( dimension == 2 )
		{
			d = (float) Math.pow( Math.abs( (Float )n.getAttribute( "x" ) - (Float )n2.getAttribute( "x" ) ), 2 ) +  (float) Math.pow( Math.abs( (Float )n.getAttribute( "y" ) - (Float )n2.getAttribute( "y" ) ), 2 ); 
		}
		else if( dimension == 3 )
		{
			d = (float) Math.pow( Math.abs( (Float )n.getAttribute( "x" ) - (Float )n2.getAttribute( "x" ) ), 2 ) 
				+ (float) Math.pow( Math.abs( (Float )n.getAttribute( "y" ) - (Float )n2.getAttribute( "y" ) ), 2 )
				+ (float) Math.pow( Math.abs( (Float )n.getAttribute( "z" ) - (Float )n2.getAttribute( "z" ) ), 2 );
		}
		else
		{
			for( int i = 0; i < dimension; i++ )
				d+=  (float) Math.pow( Math.abs( (Float )n.getAttribute( "x"+i ) - (Float )n2.getAttribute( "x+i" ) ), 2 );
		}
		
		return (float) Math.sqrt( d );
	}
	
	/**
	 * Set the threshold that defines whether or not a link is created between to
	 * notes. Since the coordinate system is defined between 0 and 1, the
	 * threshold has to be set between these two bounds.
	 * @param threshold The defined threshold. 
	 */
	public void setThreshold(float threshold)
	{
		if(threshold <= 1f && threshold >= 0f)
		{
			this.threshold = threshold;
		}
	}
	

}