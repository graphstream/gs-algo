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

package org.graphstream.algorithm;

import java.util.*;

import org.graphstream.graph.*;
import org.graphstream.stream.Sink;

/**
 * All-pair shortest paths lengths.
 *
 * <p>
 * This class defines algorithms that compute all shortest paths lengths between
 * all pair of nodes in a given graph. This algorithm uses the Floyd-Warshal
 * algorithm, that effectively runs in O(n^3). This may seems a very large
 * complexity, however this algorithm may perform better than running several
 * Dijkstra on all node pairs of the graph when the graph becomes dense.
 * </p>
 * 
 * <p>
 * Note that as is, this algorithm does not store the paths it only stores the
 * lengths of the minimum paths (however there is an option that allows to
 * reconstruct paths in linear time, taking advantage of the fact we computed
 * all the shortest paths). The storage is made directly in the graph.
 * For each node of the graph, a
 * {@link org.graphstream.algorithm.APSP.APSPInfo} attribute is stored. The
 * name of this attribute is
 * {@link org.graphstream.algorithm.APSP.APSPInfo#ATTRIBUTE_NAME}.
 * </p>
 * 
 * <p>
 * This algorithm can use directed graphs and only compute paths according to
 * this direction. You can choose to ignore edge orientation by calling
 * {@link #setDirected(boolean)} method with "false" as value (or use the
 * appropriate constructor).
 * </p>
 * 
 * <p>
 * You can also specify that edges have "weights" or "importance" that value
 * them. You store these values as attributes on the edges. The default name
 * for these attributes is "weight" but you can specify it using the
 * {@link #setWeightAttributeName(String)} method (or by using the appropriate
 * constructor). The weight attribute must contain an object that implements
 * java.lang.Number.
 * </p>
 * 
 * <p>
 * How to rebuild the shortest path without storing them: we use the fact that
 * we compute ALL the shortest paths between ALL pairs of nodes. Therefore
 * instead of storing in each node the complete shortest path toward each
 * other node, we only store the target node name and if the path is made of
 * more than one edge, one "pass-by" node. As all shortest path that is made of
 * more than one edge is necessarily made of two other shortest paths, it is
 * easy to reconstruct a shortest path between two arbitrary nodes knowing only
 * a pass-by node.
 * </p>
 */
public class APSP
	implements Algorithm, Sink
{
// Attribute
	
	/**
	 * The graph to use.
	 */
	protected Graph graph;
	
	/**
	 * Does the graph changed between two calls to {@link #compute()}?.
	 */
	protected boolean graphChanged = true;
	
	/**
	 * If false, do not take edge orientation into account.
	 */
	protected boolean directed = true;
	
	/**
	 * Name of the attribute on each edge indicating the weight of the edge.
	 * This attribute must contain a descendant of Number.
	 */
	protected String weightAttributeName;
	
// Construction

	public APSP()
	{
		this(null);
	}
	
	/**
	 * New APSP algorithm working on the given graph. The edge weight attribute
	 * name by default is "weight" and edge orientation is taken into account.
	 * @param graph The graph to use.
	 */
	public APSP( Graph graph )
	{
		this( graph, "weight", true );
	}
	
	/**
	 * New APSP algorithm working on the given graph. To fetch edges importance,
	 * the algorithm use the given string as attribute name for edge weights.
	 * Weights must be a descendant of Number.
	 * @param graph The graph to use.
	 * @param weightAttributeName The edge weight attribute name.
	 * @param directed If false, edge orientation is ignored.
	 */
	public APSP( Graph graph, String weightAttributeName, boolean directed )
	{
		this.graph               = graph;
		this.weightAttributeName = weightAttributeName;
		this.directed            = directed;
		
		init( graph );
	}
	
// Access
	
	/**
	 * True if the algorithm must take edge orientation into account.
	 * @return True if directed.
	 */
	public boolean isDirected()
	{
		return directed;
	}
	
	/**
	 * The name of the attribute to use for retrieving edge weights.
	 * @return An attribute name.
	 */
	public String getWeightAttributeName()
	{
		return weightAttributeName;
	}
	
	public Graph getGraph()
	{
		return graph;
	}
	
// Commands
	
	/**
	 * Choose to use or ignore edge orientation.
	 * @param on If true edge orientation is used.b
	 */
	public void setDirected( boolean on )
	{
		directed = on;
	}
	
	/**
	 * Choose the name of the attribute used to retrieve edge weights. Edge
	 * weights attribute must contain a value that inherit Number.
	 * @param name The attribute name.
	 */
	public void setWeightAttributeName( String name )
	{
		weightAttributeName = name;
	}

	public void init( Graph graph )
	{
		if( this.graph != null )
			this.graph.removeSink( this );
		
		this.graph = graph;
		graphChanged = true;
		
		if( this.graph != null )
			this.graph.addSink( this );
	}
	
	/**
	 * Run the APSP computation. When finished, the graph is equipped with
	 * specific attributes of type
	 * {@link org.graphstream.algorithm.APSP.APSPInfo}. These attributes
	 * contain a map of length toward each other attainable node. The attribute
	 * name is given by
	 * {@link org.graphstream.algorithm.APSP.APSPInfo#ATTRIBUTE_NAME}.
	 * 
	 * @complexity O(n^3) where n is the number of nodes in the graph.
	 */
	public void compute()
	{
		if( graphChanged )
		{
			// Make a list of all nodes, and equip them with APSP informations.
			// The APSPInfo constructor add in each info item all the paths from
			// the node to all its neighbour. It set the distance to 1 if there
			// are no weights on edges.
			
			ArrayList<Node> nodeList = new ArrayList<Node>();

			for( Node node: graph )
			{
			    node.addAttribute( APSPInfo.ATTRIBUTE_NAME, new APSPInfo( node, weightAttributeName, directed ) );
			    nodeList.add( node );
			}
			
			// The Floyd-Warshal algorithm. You can easily see it is in O(n^3)..
			
			int z = 0;
			
			for( Node k: nodeList )
			{
				for( Node i: nodeList )
				{
					for( Node j: nodeList )
					{
						APSPInfo I   = (APSPInfo) i.getAttribute( APSPInfo.ATTRIBUTE_NAME, APSPInfo.class );
						APSPInfo J   = (APSPInfo) j.getAttribute( APSPInfo.ATTRIBUTE_NAME, APSPInfo.class );
						APSPInfo K   = (APSPInfo) k.getAttribute( APSPInfo.ATTRIBUTE_NAME, APSPInfo.class );
						float    Dij = I.getLengthTo( J.source.getId() );		// Distance between i and j.
						float    Dik = I.getLengthTo( K.source.getId() );		// Distance between i and k.
						float    Dkj = K.getLengthTo( J.source.getId() );		// Distance between k and j.
						
						// Take into account non-existing paths.
						
						if( Dik >= 0 && Dkj >= 0 )
						{
							float sum = Dik + Dkj;
							
							if( Dij >= 0 )
							{
								if( sum < Dij )
								{
									I.setLengthTo( J, sum, K );
								}
							}
							else
							{
								I.setLengthTo( J, sum, K );
							}
						}
					}
				}
			
				z++;
//				System.err.printf( "%3.2f%%%n", (z/((float)n))*100 );
			}
		}

		graphChanged = false;
	}
	
	/**
	 * Information stored on each node of the graph giving the length of the
	 * shortest paths toward each other node.
	 */
	public static class APSPInfo
	{
		public static final String ATTRIBUTE_NAME = "APSPInfo";
		
		/**
		 * The start node name. This information is stored inside this node.
		 */
		public Node source;
		
		/**
		 * Maximum number of hops to attain another node in the graph from the
		 * "from" node. 
		 */
		public float maxLength;
		
		/**
		 * Minimum number of hops to attain another node in the graph from the
		 * "from" node.
		 */
		public float minLength;
		
		/**
		 * Shortest paths toward all other accessible nodes.
		 */
		public HashMap<String,TargetPath> targets = new HashMap<String,TargetPath>(); 
		
		/**
		 * Create the new information and put in it all the paths between this
		 * node and all its direct neighbours.
		 * @param node The node to start from.
		 * @param weightAttributeName The key used to retrieve the weight
		 * 		  attributes of edges. This attribute but store a value that
		 *        inherit Number.
		 * @param directed If false, the edge orientation is not taken into
		 *        account.
		 */
		public APSPInfo( Node node, String weightAttributeName, boolean directed )
		{
			float weight = 1;
			Iterable<? extends Edge> edges = node.getLeavingEdgeSet();
			
			source = node;
			
			if( ! directed )
				edges = node.getEdgeSet();
				
			for( Edge edge: edges )
			{
				Node other = edge.getOpposite( node );
				
				if( edge.hasAttribute( weightAttributeName ) )
					weight = (float) edge.getNumber( weightAttributeName );
				
				targets.put( other.getId(), new TargetPath( other, weight, null ) );
			}
		}

		/**
		 * The node represented by this APSP information.
		 * @return A node identifier.
		 */
		public String getNodeId()
		{
			return source.getId();
		}
		
		/**
		 * Minimum distance between this node and another. This returns -1 if
		 * there is no path stored yet between these two nodes.
		 * @param other The other node identifier.
		 * @return The distance or -1 if no path is stored yet between the two
		 *         nodes.
		 */
		public float getLengthTo( String other )
		{
			if( targets.containsKey( other ) )
				return targets.get( other ).distance;
			
			return -1;
		}
		
		/**
		 * The minimum distance between this node and another.
		 * @return A distance.
		 */
		public float getMinimumLength()
		{
			return minLength;
		}
		
		/**
		 * The maximum distance between this node and another.
		 * @return A distance.
		 */
		public float getMaximumLength()
		{
			return maxLength;
		}
		
		/**
		 * Add or change the length between this node and another and update the
		 * minimum and maximum lengths seen so far.
		 * @param other The other node APSP info.
		 * @param length The new minimum path lengths between these nodes.
		 */
		public void setLengthTo( APSPInfo other, float length, APSPInfo passBy )
		{
			targets.put( other.source.getId(), new TargetPath( other.source, length, passBy ) );
			
			if( length < minLength )
				minLength = length;
			
			if( length > maxLength )
				maxLength = length;
		}
		
		public Path getShortestPathTo( String other )
		{
			TargetPath tpath = targets.get( other );
			
			// XXX Probably a bug here in the Path class usage.
			// TODO update this to create an edge path to be compatible with multi-graphs.
			
			if( tpath != null )
			{
				Path path = new Path();	// XXX use the Path object directly.
				ArrayList<Node> nodePath = new ArrayList<Node>();

				nodePath.add( source );
				nodePath.add( tpath.target );
				
				// Recursively build the path between the source and target node
				// by exploring pass-by nodes.
				
				expandPath( 1, this, tpath, nodePath );
				
				// Build a Path object.
				
				for( int i=0; i<nodePath.size()-1; ++i )
				{
					// XXX XXX complicated ?
					
					path.add( nodePath.get( i ), nodePath.get( i ).getEdgeToward( nodePath.get(i+1).getId() ) );
				}
				
				return path;
			}
			
			return null;
		}
		
		protected int expandPath( int pos, APSPInfo source, TargetPath path, ArrayList<Node> nodePath )
		{
			// result      = will contain the expanded path.
			// source      = A.
			// path.passBy = X.
			// path.target = B.
			// pos         = position of insertion of X inside result.
			
			if( path.passBy != null )
			{
				// We want to insert X between A and B.
				
				nodePath.add( pos, path.passBy.source );

				// We build paths between A and X and between X and B.
				
				TargetPath path1 = source.targets.get( path.passBy.source.getId() );	// path from A -> X 
				TargetPath path2 = path.passBy.targets.get( path.target.getId() );		// path from X -> B
				
				// Now we recurse the path expansion.
				
				int added1 = expandPath( pos, source, path1, nodePath );
				int added2 = expandPath( pos + 1 + added1, path.passBy, path2, nodePath );
				
				// Return the number of elements added at pos.
				
				return added1 + added2 + 1;
			}
			else
			{
				// These is no more intermediary node X, stop the recursion.
				
				return 0;
			}
		}
	}
	
	/**
	 * Description of a path to a target node.
	 * 
	 * <p>
	 * This class is made to be used by the APSPInfo class, which references a
	 * source node. This class describes a target node, the length of the
	 * shortest path to it and, if the path is made of more than only one edge,
	 * an intermediary node (pass-by), used to reconstruct recursively the 
	 * shortest path.
	 * </p>
	 * 
	 * <p>
	 * This representation avoids to store each node of each shortest path,
	 * since this would consume a too large memory area. This way, a shortest
	 * path is stored at constant size (this is possible since we computed all
	 * the shortest paths and, knowing that a path of more than one edge is
	 * always made of the sum of two shortest paths, and knowing only one
	 * "pass-by" node in the shortest path, it is possible to rebuild it). 
	 * </p>
	 *
	 * @author Antoine Dutot
	 * @since 2007
	 */
	public static class TargetPath
	{
		/**
		 * A distant other node.
		 */
		public Node target;
		
		/**
		 * The distance to this other node.
		 */
		public float distance;
		
		/**
		 * An intermediary other node on the minimum path to the other node.
		 * Used to reconstruct the path between two nodes.
		 */
		public APSPInfo passBy;
		
		public TargetPath( Node other, float distance, APSPInfo passBy )
		{
			this.target   = other;
			this.distance = distance;
			this.passBy   = passBy;
		}
	}

// Graph Listener
	
	public void nodeAdded( String graphId, long timeId, String nodeId )
    {
		graphChanged = true;
    }

	public void nodeRemoved( String graphId, long timeId, String nodeId )
    {
		graphChanged = true;
    }

	public void edgeAdded( String graphId, long timeId, String edgeId, String fromNodeId, String toNodeId,
            boolean directed )
    {
		graphChanged = true;
    }

	public void edgeRemoved( String graphId, long timeId, String edgeId )
    {
		graphChanged = true;
    }
	
	public void graphCleared( String graphId, long timeId )
	{
		graphChanged = true;
	}

	public void stepBegins( String graphId, long timeId, double time )
    {
    }

	public void graphAttributeAdded( String graphId, long timeId, String attribute, Object value )
    {
    }

	public void graphAttributeChanged( String graphId, long timeId, String attribute, Object oldValue, Object value )
    {
    }

	public void graphAttributeRemoved( String graphId, long timeId, String attribute )
    {
    }

	public void nodeAttributeAdded( String graphId, long timeId, String nodeId, String attribute, Object value )
    {
    }

	public void nodeAttributeChanged( String graphId, long timeId, String nodeId, String attribute, Object oldValue, Object value )
    {
    }

	public void nodeAttributeRemoved( String graphId, long timeId, String nodeId, String attribute )
    {
    }

	public void edgeAttributeAdded( String graphId, long timeId, String edgeId, String attribute, Object value )
    {
		if( attribute.equals( weightAttributeName ) )
		{
			graphChanged = true;
		}
    }

	public void edgeAttributeChanged( String graphId, long timeId, String edgeId, String attribute, Object oldValue, Object value )
    {
		if( attribute.equals( weightAttributeName ) )
		{
			graphChanged = true;
		}
    }

	public void edgeAttributeRemoved( String graphId, long timeId, String edgeId, String attribute )
    {
    }
}