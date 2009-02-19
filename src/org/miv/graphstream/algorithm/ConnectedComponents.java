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

import java.util.Hashtable;
import java.util.LinkedList;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Element;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.GraphListener;
import org.miv.graphstream.graph.Node;

/**
 * <p>
 * This algorithm computes the connected components of one given graph. The
 * connected components are the set of its connected subgraphs. Two nodes belong
 * to the same connected component the there exists a path (without considering
 * the direction of the edges) between them. Therfore, the algorithm does not
 * consider the direction of the edges. The number of connected components of an
 * undirected graph is equal to the number of connected components of the same
 * directed graph. See <a
 * href="http://en.wikipedia.org/wiki/Connected_component_%28graph_theory%29">wikipedia</a>
 * for details.
 * </p>
 * <h2>Dynamics</h2>
 * <p>
 * This algorithm tries to handle the dynamics of the graph, trying not to
 * recompute all the algorithm from scratch at each change (kind of
 * re-optimization). In this way, each instance of the algorithm is registered
 * as a graph listener. Each change in the graph topology may affect the
 * algorithm.
 * </p>
 * <h2>Usage</h2>
 * <p>
 * To start using the algorithm, you first need an instance of
 * {@link org.miv.graphstream.graph.Graph}, then you only have to
 * instantiate the algorithm class. Whether you specify a reference to the graph
 * in the constructor or you set it with the {@link #setGraph(Graph)}
 * method.
 * </p>
 * <p>
 * The computation of the algorithm start only when the graph is specified with
 * the {@link #setGraph(Graph)} method or with the appropriated
 * constructor. In case of a static graph, you may call the {@link #compute()}
 * method. In case of a dynamic graph, the algorithm should compute itself
 * automatically when an event (node or edge added or removed) occurs.
 * </p>
 * <p>
 * Finally you may ask the algorithm for the number of connected components at
 * any moment with a call to the {@link #getConnectedComponentsCount()} method.
 * </p>
 * 
 * @author Yoann Pigné
 * @author Antoine Dutot
 * 
 * @since June 26 2007
 * 
 * @complexity For the initial computation, let n be the number of nodes, then
 *             the complexity is 0(n). For the re-optimization steps, let k be
 *             the number of nodes concerned by the changes (k <= n), the
 *             complexity is O(k).
 * 
 */
public class ConnectedComponents implements DynamicAlgorithm, GraphListener
{
	/**
	 * 
	 */
	private static final String CONNECTED_COMPONENT = "connectedComponent";

	private Hashtable<Node, Integer> connectedComponentsMap;

	/**
	 * The Graph the algorithm is working on.
	 */
	protected Graph graph;

	/**
	 * The number of connected components
	 */
	protected int connectedComponents = 0;

	/**
	 * Single IDs to identify the connected components.
	 */
	protected int connectedComponentsId = 0;

	/**
	 * A token to decide whether or not the algorithm is started.
	 */
	protected boolean started = false;

	/**
	 * Constructor with the given graph. The computation of the algorithm start
	 * only when the {@link #begin} method is invoked.
	 * @param graph The graph who's connected components will be computed.
	 */
	public ConnectedComponents( Graph graph )
	{
		this.graph = graph;
		graph.addGraphListener( this );
		connectedComponentsMap = new Hashtable<Node, Integer>();
	}

	/**
	 * Ask the algorithm for the number of connected components.
	 */
	public int getConnectedComponentsCount()
	{
		if( !started )
		{
			begin();
		}
		return connectedComponents;
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.algorithm.DynamicAlgorithm#begin()
	 */
	public void begin()
	{
		compute();
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.algorithm.DynamicAlgorithm#end()
	 */
	public void end()
	{}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute()
	{
		// initialization
		connectedComponents = 0;
		connectedComponentsId = 0;
		started = true;

		// Initialization
		for( Node v: graph.getNodeSet() )
		{
			connectedComponentsMap.put( v, 0 );
		}

		for( Node v: graph.getNodeSet() )
		{
			if( ( connectedComponentsMap.get( v ) == 0 ) )
			{
				connectedComponents++;
				connectedComponentsId++;
				computeConnectedComponent( v, connectedComponentsId, null );
			}

		}

	}

	/**
	 * Private method that goes recursively (depth first) into the connected
	 * component and assigns each node an id
	 * @param v The considered node.
	 * @param id The id to assign to the given node.
	 * @param exception An optional edge that may not by considered (useful when
	 *        receiving a {@link #beforeEdgeRemove(Graph, Edge)}
	 *        event.
	 * 
	 */
	private void computeConnectedComponent( Node v, int id, Edge exception )
	{
		LinkedList<Node> open = new LinkedList<Node>();
		open.add( v );
		while( !open.isEmpty() )
		{
			Node n = open.remove();
			connectedComponentsMap.put( n, id );

			for( Edge e: n.getEdgeSet() )
			{
				if( e != exception )
				{
					Node n2 = e.getOpposite( n );
					if(  connectedComponentsMap.get( n2 ) != id )
					{
						open.add( n2 );
						connectedComponentsMap.put( n2, id );
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.algorithm.Algorithm#getGraph()
	 */
	public Graph getGraph()
	{
		return graph;
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.algorithm.Algorithm#setGraph(org.miv.graphstream.graph.Graph)
	 */
	public void setGraph( Graph graph )
	{
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.graph.GraphListener#afterEdgeAdd(org.miv.graphstream.graph.Graph,
	 *      org.miv.graphstream.graph.Edge)
	 */
	public void afterEdgeAdd( Graph graph, Edge edge )
	{
		if( !started && graph != null )
		{
			begin();
		}
		else if( started )
		{
			if( !( connectedComponentsMap.get( edge.getNode0() ).equals( connectedComponentsMap.get( edge.getNode1() ) ) ) )
			{
				connectedComponents--;

				computeConnectedComponent( edge.getNode1(), connectedComponentsMap.get( edge.getNode0() ), edge );
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.graph.GraphListener#afterNodeAdd(org.miv.graphstream.graph.Graph,
	 *      org.miv.graphstream.graph.Node)
	 */
	public void afterNodeAdd( Graph graph, Node node )
	{
		if( !started && graph != null )
		{
			begin();
		}
		else if( started )
		{
			connectedComponents++;
			connectedComponentsId++;
			connectedComponentsMap.put( node, connectedComponentsId );
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.graph.GraphListener#attributeChanged(org.miv.graphstream.graph.Element,
	 *      java.lang.String, java.lang.Object, java.lang.Object)
	 */
	public void attributeChanged( Element element, String attribute, Object oldValue, Object newValue )
	{

	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.graph.GraphListener#beforeEdgeRemove(org.miv.graphstream.graph.Graph,
	 *      org.miv.graphstream.graph.Edge)
	 */
	public void beforeEdgeRemove( Graph graph, Edge edge )
	{
		if( !started && graph != null )
		{
			begin();
		}

		if( started )
		{
			connectedComponentsId++;
			computeConnectedComponent( edge.getNode0(), connectedComponentsId, edge );
			if( !( connectedComponentsMap.get( edge.getNode0() ).equals( connectedComponentsMap.get( edge.getNode1() ) ) ) )
			{
				connectedComponents++;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.graph.GraphListener#beforeGraphClear(org.miv.graphstream.graph.Graph)
	 */
	public void beforeGraphClear( Graph graph )
	{
		connectedComponents = 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.miv.graphstream.graph.GraphListener#beforeNodeRemove(org.miv.graphstream.graph.Graph,
	 *      org.miv.graphstream.graph.Node)
	 */
	public void beforeNodeRemove( Graph graph, Node node )
	{
		if( !started && graph != null )
		{
			begin();
		}

		if( started )
		{
			connectedComponents--;
		}
	}

}