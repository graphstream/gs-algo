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

import java.util.Random;

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
 * @since 20070117
 */
public class DorogovtsevMendesGenerator
	extends BaseGenerator
{
// Attributes
	
	/**
	 * The graph to grow.
	 */
	//protected Graph graph;
	
	/**
	 * Random number generator.
	 */
	//protected Random random;
	
	/**
	 * Set of edges.
	 */
	
	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;
	
// Constructors

	public DorogovtsevMendesGenerator()
	{
		keepEdgesId = true;
	}
	
	/**
	 * New generator with the given random number generator.
	 * @param random The number generator to use.
	 */
	public DorogovtsevMendesGenerator( Random random )
	{
		this();
		
		this.random = random;
	}
	
// Access
// Commands

	public void begin()// Graph graph )
	{
		//this.graph = graph;
		this.random = this.random == null ? new Random( System.currentTimeMillis() ) : this.random;
		
		//Edge edge;
		
		/*
		graph.addNode( "0" );
		graph.addNode( "1" );
		graph.addNode( "2" );
		*/
		addNode("0");
		addNode("1");
		addNode("2");
		
		/*edge = graph.addEdge( "0-1", "0", "1" ); edges.add( edge );
		edge = graph.addEdge( "1-2", "1", "2" ); edges.add( edge );
		edge = graph.addEdge( "2-0", "2", "0" ); edges.add( edge );*/
		addEdge("0-1","0","1"); edges.add("0-1");
		addEdge("1-2","1","2"); edges.add("1-2");
		addEdge("2-0","2","0"); edges.add("2-0");
	
		nodeNames = 3;
	}

	public void end()
	{
		//graph = null;
	}

	public boolean nextElement()
	{
		int    rand = random.nextInt( edges.size() );
		String name = Integer.toString( nodeNames++ );
		String edge = edges.get( rand );
		String   n0 = edge.substring(0,edge.indexOf('-'));//, endIndex)edge.getNode0();
		String   n1 = edge.substring(edge.indexOf('-')+1);//edge.getNode1();
		//Edge   e    = null;

		//graph.addNode( name );
		addNode(name);

		//e = graph.addEdge( n0.getId() + "-" + name, n0.getId(), name ); edges.add( e );
		//e = graph.addEdge( n1.getId() + "-" + name, n1.getId(), name ); edges.add( e );
		addEdge( n0 + "-" + name, n0, name );
		addEdge( n1 + "-" + name, n1, name );
		
		return false;
	}
}