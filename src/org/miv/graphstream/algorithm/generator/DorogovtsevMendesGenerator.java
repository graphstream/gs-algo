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
 * Dorogovtsev - Mendes graph generator.
 *
 * <p>
 * Generates a graph using the Dorogovtsev - Mendes algorithm. This starts by
 * creating three nodes and tree edges, making a triangle, and then add one
 * node at a time. Each time a node is added, an edge is choosed randomly and
 * the node is connected to the two extremities of this edge.
 * </p>
 * 
 * <p>
 * This process generates a power-low degree distribution, as nodes that have
 * more edges have more chances to be selected since their edges are more
 * represented in the edge set.
 * </p>
 * 
 * <p>
 * This algorithm often generates graphs that seem more suitable than the
 * simple preferential attachment implemented in the PreferentialAttachmentGenerator
 * class (despite the fact more complex and useful preferential attachment
 * generators could be realized in the future).
 * </p>
 * 
 * </p>
 * The Dorogovtsev - Mendes algorithm always produce planar graphs.
 * </p>
 * 
 * <p>
 * The more this generator is iterated, the more nodes are generated. It can
 * therefore generate trees of any size.
 * </p>
 *
 * @author Antoine Dutot
 * @author Yoann Pigné 
 * @since 20070117
 */
public class DorogovtsevMendesGenerator implements Generator
{
// Attributes
	
	/**
	 * The graph to grow.
	 */
	protected Graph graph;
	
	/**
	 * Random number generator.
	 */
	protected Random random;
	
	/**
	 * Set of edges.
	 */
	protected ArrayList<Edge> edges = new ArrayList<Edge>();
	
	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;
	
// Constructors

	public DorogovtsevMendesGenerator()
	{
	}
	
	/**
	 * New generator with the given random number generator.
	 * @param random The number generator to use.
	 */
	public DorogovtsevMendesGenerator( Random random )
	{
		this.random = random;
	}
	
// Access
// Commands

	public void begin( Graph graph )
	{
		this.graph = graph;
		this.random = this.random == null ? new Random( System.currentTimeMillis() ) : this.random;
		
		Edge edge;
		
		graph.addNode( "0" );
		graph.addNode( "1" );
		graph.addNode( "2" );
		
		edge = graph.addEdge( "0-1", "0", "1" ); edges.add( edge );
		edge = graph.addEdge( "1-2", "1", "2" ); edges.add( edge );
		edge = graph.addEdge( "2-0", "2", "0" ); edges.add( edge );
	
		nodeNames = 3;
	}

	public void end()
	{
		graph = null;
	}

	public boolean nextElement()
	{
		int    rand = random.nextInt( edges.size() );
		String name = Integer.toString( nodeNames++ );
		Edge   edge = edges.get( rand );
		Node   n0   = edge.getNode0();
		Node   n1   = edge.getNode1();
		Edge   e    = null;

		graph.addNode( name );

		e = graph.addEdge( n0.getId() + "-" + name, n0.getId(), name ); edges.add( e );
		e = graph.addEdge( n1.getId() + "-" + name, n1.getId(), name ); edges.add( e );
				
		return false;
	}
}