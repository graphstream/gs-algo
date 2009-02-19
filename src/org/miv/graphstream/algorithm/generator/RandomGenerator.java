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

import org.miv.graphstream.graph.*;

/**
 * Random graph generator.
 *
 * <p>
 * This generator creates random graphs of any size. Calling
 * {@link #begin(Graph)} put one unique node in the graph, then
 * {@link #nextElement()} will add a new node each time it is called and
 * connect this node randomly to others.
 * </p>
 * 
 * <p>
 * The generator tried to generate nodes with random connections, with each node
 * having in average a given degree. The law in a Poisson law, however, the
 * way this generator works, adding node after node, perturbate this process.
 * We should first allocate all the needed nodes, then create edges, and we
 * create nodes at the same rate as edges. The more nodes are added the more
 * the degree distribution curve is shifted toward the right.
 * </p>
 * 
 * <p>
 * This generator has the ability to add randomly choosed numerical values
 * on arbitrary attributes on edges or nodes of the graph, and to randomly
 * choose a direction for edges.
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
 * By default, edges are not oriented. It is possible to ask orientation, in 
 * which case the direction is choosed randomly.
 * </p>
 *
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since 2007
 */
public class RandomGenerator extends BaseGenerator
{
// Attributes

	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;

	/**
	 * The average degree of each node.
	 */
	protected int averageDegree = 1;
	
// Constructors
	
	/**
	 * New full graph generator. By default no attributes are added to nodes and
	 * edges, and edges are not directed.
	 * @param averageDegree The average degree of nodes.
	 */
	public RandomGenerator( int averageDegree )
	{
		super();
		this.averageDegree = averageDegree;
	}

	/**
	 * @param averageDegree The average degree of nodes.
	 * @param directed Are edges directed?.
	 * @param randomlyDirectedEdges randomly direct generated edges.
	 */
	public RandomGenerator( int averageDegree, boolean directed, boolean randomlyDirectedEdges )
	{
		super( directed, randomlyDirectedEdges );
		this.averageDegree = averageDegree;
	}
	
	/**
	 * New random graph generator.
	 * @param averageDegree The average degree of nodes.
	 * @param directed Are edges directed?.
	 * @param randomlyDirectedEdges randomly direct generated edges.
	 * @param nodeAttribute put an attribute by that name on each node with a random numeric value.
	 * @param edgeAttribute put an attribute by that name on each edge with a random numeric value.
	 */
	public RandomGenerator( int averageDegree, boolean directed, boolean randomlyDirectedEdges, String nodeAttribute, String edgeAttribute )
	{
		super( directed, randomlyDirectedEdges, nodeAttribute, edgeAttribute );
		this.averageDegree = averageDegree;
	}
	
// Accessors
	
// Commands
	
	public void begin( Graph graph )
	{
		this.graph  = graph;
		
		String id = Integer.toString( nodeNames++ );
		
		addNode( id );
	}
	
	public void end()
	{
	}

	public boolean nextElement()
	{
		String id = Integer.toString( nodeNames++ );
		
		addNode( id );
		
		// Choose the degree of the node randomly, centered around the
		// predefined average degree.
		
		int degree = poisson( averageDegree );
		
		// For this degree, we choose randomly degree other nodes to be linked
		// to the new node.
		
		for( int i=0; i<degree; ++i )
		{
			int    n       = random.nextInt( nodes.size() );
			String otherId = nodes.get( n );
			
			if( otherId != id )
			{
				Node node = graph.getNode( otherId );
				
				if( ! node.hasEdgeToward( id ) && ! node.hasEdgeFrom( id ) )
				{
					addEdge( null, id, otherId );
				}
			}
		}
/*		
		for( String otherId: nodes )
		{
			if( otherId != id )		// We can compare refs safely here.
			{
				if( random.nextFloat() > 0.5f )
					addEdge( null, id, otherId );
			}
		}
*/		
		return false;
	}
	
	/**
	 * Generate a random integer centered around p.
	 * @param p The average value of the random number.
	 * @return A random int.
	 */
	protected int
	poisson( float p )
	{
		double a = Math.exp( -p );
		int    n = 0;
		double u = random.nextFloat();

		while( u > a )
		{
			u *= random.nextFloat();
			n++;
		}

		return n;
	}
}