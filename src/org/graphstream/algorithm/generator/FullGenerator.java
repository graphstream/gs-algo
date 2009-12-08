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

import org.graphstream.graph.*;

/**
 * Full graph generator.
 *
 * <p>
 * Probably not very usefull, still sometimes needed. This genertor creates
 * fully connected graphs of any size. Calling {@link #begin(Graph)} put one
 * unique node in the graph, then {@link #nextElement()} will add a new node
 * each time it is called.
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
 * @author Yoann Pign�
 * @since 2007
 */
public class FullGenerator extends BaseGenerator
{
// Attributes

	/**
	 * Used to generate node names.
	 */
	protected int nodeNames = 0;
	
// Constructors
	
	/**
	 * New full graph generator. By default no attributes are added to nodes and
	 * edges, and edges are not directed.
	 */
	public FullGenerator()
	{
		super();
	}
	
	public FullGenerator( boolean directed, boolean randomlyDirectedEdges )
	{
		super( directed, randomlyDirectedEdges );
	}
	
	/**
	 * New full graph generator.
	 * @param directed Are edge directed?.
	 * @param randomlyDirectedEdges randomly direct generated edges.
	 * @param nodeAttribute put an attribute by that name on each node with a random numeric value.
	 * @param edgeAttribute put an attribute by that name on each edge with a random numeric value.
	 */
	public FullGenerator( boolean directed, boolean randomlyDirectedEdges, String nodeAttribute, String edgeAttribute )
	{
		super( directed, randomlyDirectedEdges, nodeAttribute, edgeAttribute );
	}
	
// Accessors
	
// Commands
	
	@Override
	public void begin( Graph graph )
	{
		this.graph  = graph;
		
		String id = Integer.toString( nodeNames++ );

		addNode( id );
	}
	
	@Override
	public void end()
	{
	}

	@Override
	public boolean nextElement()
	{
		String id = Integer.toString( nodeNames++ );
		
		addNode( id );
		
		for( String otherId: nodes )
		{
			if( otherId != id )		// We can compare refs safely here.
				addEdge( null, id, otherId );
		}
		
		return false;
	}
}