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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.Path;

/**
 * An implementation of the A* algorithm.
 *  
 *  <p>
 *  A* computes the shortest path from a node to another in a graph.
 *  It can eventually fail if the two nodes are in two distinct connected
 *  components.
 *  </p>
 *  
 *  <p>
 *  In this A* implementations, the various costs (often called g, h and f)
 *  are given by a {@link org.miv.graphstream.algorithm.AStar.Costs} class.
 *  This class must provide a way to compute :
 *  <ul>
 *  	<li>The cost of moving from a node to another, often called g ;</li>
 *  	<li>The estimated cost from a node to the destination, the heuristic, often noted h ;</li>
 *  	<li>f is the sum of g and h and is computed automatically.</li>
 *  </ul>
 *  </p>
 *  
 *  <p>
 *  By default the {@link org.miv.graphstream.algorithm.AStar.Costs} implementation
 *  used uses an heuristic that returns 0 for any heuristic. This makes A* an
 *  equivalent of the Dijkstra algorithm, but also makes it far less efficient.
 *  </p>
 *  
 *  <p>
 *  The basic usage of this algorithm is as follows :
 *  <pre>
 *  	AStart astar = new AStar( graph );
 *  	astar.compute( "A", "Z" );	// with A and Z node identifiers in the graph.
 *  	Path path = astar.getShortestPath();
 *  </pre>
 *  </p>
 *  
 * @complexity The complexity of A* depends on the heuristic.
 * @author Antoine Dutot
 * @author Yoann Pigné
 */
public class AStar implements Algorithm
{
// Attributes
	
	/**
	 * The graph.
	 */
	protected Graph graph;

	/**
	 * The source node id.
	 */
	protected String source;
	
	/**
	 * The target node id.
	 */
	protected String target;
	
	/**
	 * How to compute the path cost, the cost between two nodes and the heuristic.
	 * The heuristic to estimate the distance from the current position to the target.
	 */
	protected Costs costs = new DefaultCosts();
	
	/**
	 * The open set.
	 */
	protected HashMap<Node,AStarNode> open = new HashMap<Node,AStarNode>();
	
	/**
	 * The closed set.
	 */
	protected HashMap<Node,AStarNode> closed = new HashMap<Node,AStarNode>();
	
	/**
	 * If found the shortest path is stored here.
	 */
	protected Path result;
	
	/**
	 * Set to true if the algorithm run, but did not found any path from the source
	 * to the target.
	 */
	protected boolean noPathFound;
	
// Constructors

	/**
	 * New A* algorithm.
	 */
	public AStar()
	{
	}
	
	/**
	 * New A* algorithm on a given graph.
	 * @param graph The graph where the algorithm will compute shortest paths.
	 */
	public AStar( Graph graph )
	{
		setGraph( graph );
	}
	
// Accessors

    public Graph getGraph()
    {
	    return graph;
    }
	
// Commands
	
	/**
	 * Change the source node. This clears the already computed path, but
	 * preserves the target node name.
	 * @param nodeName Identifier of the source node.
	 */
	public void setSource( String nodeName )
	{
		clearAll();
		source = nodeName; 
	}
	
	/**
	 * Change the target node. This clears the already computed path, but
	 * preserves the source node name.
	 * @param nodeName Identifier of the target node.
	 */
	public void setTarget( String nodeName )
	{
		clearAll();
		target = nodeName;
	}
	
	/**
	 * Specify the how various costs are computed. The costs object is in charge
	 * of computing the cost of displacement from one node to another (and therefore
	 * allows to compute the cost from the source node to any node). It also allows
	 * to compute the heuristic to use for evaluating the cost from
	 * the current position to the target node. Calling this DOES NOT clear
	 * the currently computed paths. 
	 * @param costs The cost method to use.
	 */
	public void setCosts( Costs costs )
	{
		this.costs = costs;
	}
	
    public void compute()
    {
		if( source != null && target != null )
		{
			Node sourceNode = graph.getNode( source );
			Node targetNode = graph.getNode( target );
			
			if( sourceNode == null )
				throw new RuntimeException( "source node '"+source+"' does not exist in the graph" );
			
			if( targetNode == null )
				throw new RuntimeException( "target node '"+target+"' does not exist in the graph" );
			
			aStar( sourceNode, targetNode );
		}
    }
	
	/**
	 * The computed path, or null if nor result was found.
	 * @return The computed path, or null if no path was found.
	 */
	public Path getShortestPath()
	{
		return result;
	}
	
	/**
	 * After having called {@link #compute()} or {@link #compute(String, String)},
	 * if the {@link #getShortestPath()} returns null, or this method return true,
	 * there is no path from the given source node to the given target node. In
	 * other words, the graph as several connected components.
	 * @return True if there is no possible path from the source to the destination.
	 */
	public boolean noPathFound()
	{
		return noPathFound;
	}
	
	/**
	 * Build the shortest path from the target/destination node, following the parent links.
	 * @param target The destination node.
	 * @return The path.
	 */
	public Path buildPath( AStarNode target )
	{
		Path path = new Path();
		
		ArrayList<Node> thePath = new ArrayList<Node>();
		AStarNode       node    = target;
		
		while( node != null )
		{
			thePath.add( node.node );
			node = node.parent;
		}
		
		int n = thePath.size();
		
		if( n > 1 )
		{
			Node current = thePath.get( n-1 );
			Node follow  = thePath.get( n-2 );
		
			path.add( current, current.getEdgeToward( follow.getId() ) );
		
			current = follow;
			
			for( int i=n-3; i>=0; i-- )
			{
				follow = thePath.get( i );
				path.add( current.getEdgeToward( follow.getId() ) );
				current = follow;
			}
		}
		
		return path;
	}
	
	/**
	 * Call {@link #compute()} after having called {@link #setSource(String)}
	 * and {@link #setTarget(String)}.
	 * @param source Identifier of the source node.
	 * @param target Identifier of the target node.
	 */
	public void compute( String source, String target )
	{
		setSource( source );
		setTarget( target );
		compute();
	}

    public void setGraph( Graph graph )
    {
		clearAll();
		this.graph = graph;
    }
	
	/**
	 * Clear the already computed path. This does not clear the source node
	 * name, the target node name and the weight attribute name.
	 */
	protected void clearAll()
	{
		open.clear();
		closed.clear();

		result      = null;
		noPathFound = false;
	}
	
	/**
	 * The A* algorithm proper.
	 * @param sourceNode The source node.
	 * @param targetNode The target node.
	 */
	protected void aStar( Node sourceNode, Node targetNode )
	{
		// TODO: use a priority queue.
		// TODO: make the computation interruptible.
		
		clearAll();
		open.put( sourceNode, new AStarNode( sourceNode, null, 0, costs.heuristic( sourceNode, targetNode ) ) );
		
		while( ! open.isEmpty() )
		{
			AStarNode current = getNextBetterNode();

			assert( current != null );
			
			System.err.printf( "OPEN=%d%n", open.size() );
			System.err.printf( "CUR %s [%f | %f | %f]%n", current.node.getId(), current.g, current.h, current.rank );
			
			if( current.node == targetNode )
			{
				// We found it !
				result = buildPath( current );
				return;
			}
			else
			{
				open.remove( current.node );
				closed.put( current.node, current );
				
				// For each successor of the current node :
				
				Iterator<? extends Node> nexts = current.node.getNeighborNodeIterator();
				
				while( nexts.hasNext() )
				{
					Node  next = nexts.next();
					float h    = costs.heuristic( next, targetNode );
					float g    = current.g + costs.cost(  current.node, next );
					float f    = g + h;
					
					// If the node is already in open with a better rank, we skip it.
					
					AStarNode alreadyInOpen = open.get( next );
					
					if( alreadyInOpen != null && alreadyInOpen.rank <= f )
						continue;
					
					// If the node is already in closed with a better rank; we skip it.
					
					AStarNode alreadyInClosed = closed.get( next );
					
					if( alreadyInClosed != null && alreadyInClosed.rank <= f )
						continue;

					closed.remove( next );
					open.put( next, new AStarNode( next, current, g, h ) );
					System.err.printf( "   PUT %s [%f | %f | %f]%n", next.getId(), g, h, g+h );
				}
			}
		}
	}
	
	/**
	 * Find the node with the lowest rank in the open list.
	 * TODO replace this by a priority queue.
	 * @return The node of open that has the lowest rank.
	 */
	protected AStarNode getNextBetterNode()
	{
		float min = Float.MAX_VALUE;
		AStarNode theChosenOne = null;
		
		for( AStarNode node: open.values() )
		{
			if( node.rank < min )
			{
				theChosenOne = node;
				min          = node.rank;
			}
		}
		
		return theChosenOne;
	}
	
// Nested classes
	
	/**
	 * The definition of an heuristic. The heuristic is in charge of evaluating
	 * the distance between the current position and the target.
	 */
	public interface Costs
	{
		/**
		 * Estimate cost from the given node to the target node. 
		 * @param node A node. 
		 * @param target The target node.
		 * @return The estimated cost between a node and a target node.
		 */
		float heuristic( Node node, Node target );
		
		/**
		 * Cost of displacement from parent to next. The next node must be
		 * directly connected to parent, or -1 is returned.
		 * @param parent The node we come from.
		 * @param next The node we go to.
		 * @return The real cost of moving from parent to next, or -1 is next
		 * is not directly connected to parent by an edge.
		 */
		float cost( Node parent, Node next );
	}
	
	public class DefaultCosts implements Costs
	{
		/**
		 * The attribute used to retrieve the cost of an edge cross.
		 */
		protected String weightAttribute = "weight";
		
		/**
		 * New default costs for the A* algorithm. The cost of each
		 * edge is obtained from a numerical attribute stored under
		 * the name "weight". This attribute must be a descendant of
		 * Number (Double, Float, Integer, etc.).
		 */
		public DefaultCosts()
		{
		}

		/**
		 * New default costs for the A* algorithm. The cost of each
		 * edge is obtained from the attribute stored on each edge
		 * under the "weightAttributeName". This attribute must be a
		 * descendant of Number (Double, Float, Integer, etc.).
		 * @param weightAttributeName The name of cost attributes on edges.
		 */
		public DefaultCosts( String weightAttributeName )
		{
			weightAttribute = weightAttributeName;
		}
		
		/**
		 * The heuristic. This one always returns zero, therefore transforming
		 * this A* into the Dijkstra algorithm.
		 * @return The estimation.
		 */
		public float heuristic( Node node, Node target )
		{
			return 0;
		}
		
		/**
		 * The cost of moving from parent to next.  If there
		 * is no cost attribute, the edge is considered to cost value "1".
		 * @param parent The node we come from.
		 * @param next The node we go to.
		 * @return The movement cost.
		 */
		public float cost( Node parent, Node next )
		{
			Edge choice = parent.getEdgeToward( next.getId() );

			if( choice != null && choice.hasNumber( weightAttribute ) )
				return ((Number)choice.getNumber( weightAttribute )).floatValue();
			
			return 1;
		}
	}
	
	/**
	 * Representation of a node in the A* algorithm.
	 * 
	 * <p>
	 * This representation contains :
	 * <ul>
	 * 		<li>the node itself;</li>
	 * 		<li>its parent node (to reconstruct the path) ;</li>
	 * 		<li>the g value (cost from the source to this node) ;</li>
	 * 		<li>the h value (estimated cost from this node to the target) ;</li>
	 * 		<li>the f value or rank, the sum of g and h.</li>
	 * </ul>
	 * </p>
	 * @author Antoine Dutot
	 * @author Yoann Pigné
	 */
	protected class AStarNode
	{
		/**
		 * The node.
		 */
		public Node node;
		
		/**
		 * The node's parent.
		 */
		public AStarNode parent;
		
		/**
		 * Cost from the source node to this one.
		 */
		public float g;
		
		/**
		 * Estimated cost from this node to the destination.
		 */
		public float h;
		
		/**
		 * Sum of g and h.
		 */
		public float rank;
		
		/**
		 * New A* node.
		 * @param node The node.
		 * @param parent It's parent node.
		 * @param g The cost from the source to this node.
		 * @param h The estimated cost from this node to the target.
		 */
		public AStarNode( Node node, AStarNode parent, float g, float h )
		{
			this.node   = node;
			this.parent = parent;
			this.g      = g;
			this.h      = h;
			this.rank   = g + h;
		}
	}
}