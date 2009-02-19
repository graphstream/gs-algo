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

import java.util.*;
import org.miv.graphstream.graph.*;

/**
 * Lots of small usefull algorithms on graphs.
 * 
 * <p>This class is automatically instanced by the various implementations of
 * the Graph interface. Furthermore, the various {@link org.miv.graphstream.graph.Graph} implementations are
 * free to refine several algorithms to optimize them for their internal
 * representation.</p>
 * 
 * @see org.miv.graphstream.graph.Graph#algorithm()
 * @author Antoine Dutot
 * @author Yoann Pigné
 */
public class Algorithms implements Algorithm
{
// Attributes
	
	/**
	 * The graph to operate uppon.
	 */
	protected Graph graph;
	
// Constructors
	
	public Algorithms()
	{
	}
	
	public Algorithms( Graph graph )
	{
		setGraph( graph );
	}
	
// Accessors

	public Graph getGraph()
	{
		return graph;
	}
	
	/**
	 * Compute the degree distribution of this graph. Each cell of the returned
	 * array contains the number of nodes having degree n where n is the index
	 * of the cell. For example cell 0 counts how many nodes have zero edges,
	 * cell 5 counts how many nodes have five edges. The last index indicates
	 * the maximum degree.
	 * 
	 * @complexity O(n) where n is the number of nodes.
	 */
	public int[] getDegreeDistribution()
	{
		if(graph.getNodeCount() == 0)
			return null;
		int      max = 0;
		int[]    dd;
		int      d;

		for( Node n: graph.getNodeSet() )
		{
			d = n.getDegree();

			if( d > max )
			{
				max = d;
			}
		}

		dd = new int[max+1];

		for( Node n: graph.getNodeSet() )
		{
			d = n.getDegree();

			dd[d] += 1;
		}

		return dd;
	}
	
	/**
	 * Return a list of nodes sorted by degree, the larger first.
	 * @return The degree map.
	 * @complexity O(n log(n)) where n is the number of nodes.
	 */
	public ArrayList<Node> getDegreeMap()
	{
		ArrayList<Node> map = new ArrayList<Node>();
		
		map.addAll( graph.getNodeSet() );
	
		Collections.sort( map, new Comparator<Node>() {
			public int compare( Node a, Node b )
			{
				return b.getDegree() - a.getDegree();
			}
		});
		
		return map;
	}

	/**
	 * Returns the value of the average degree of the graph.
	 * @return The average degree of the graph.
	 * @complexity O(n) where n is the number of nodes.
	 */
	public float  getAverageDegree()
	{
		int degree=0;
		for(Node n : graph.getNodeSet())
		{
			degree+=n.getDegree();
		}
		return degree / (float)graph.getNodeCount();
	}
	
	/**
	 * The density is the number of links in the graph divided by the total number of possible links.
	 * @return The density of the graph 
	 */
	public float getDensity()
	{
		float m = (float) graph.getEdgeCount();
		float n = (float) graph.getNodeCount();
		return ( (2*m) / ( n*(n-1) ) );
	}
	
	/**
	 * Returns the value of the degree average deviation of the graph.
	 * @return The degree average deviation.
	 * @complexity O(n) where n is the number of nodes.
	 */
	public float getDegreeAverageDeviation()
	{
		float average = getAverageDegree();
		float  sum=0;
		for(Node n  : graph.getNodeSet())
		{
			sum +=Math.pow( (n.getDegree() - average), 2.0 );
		}
		return (float) Math.sqrt( sum/(float)graph.getNodeCount() );
	}
	
	/**
	 * Clustering coefficient for each node of the graph.
	 * @return An array whose size correspond to the number of nodes, where each
	 * element is the clustering coefficient of a node.
	 * @complexity at worse O(n d^2) where n is the number of nodes and d the
	 *             average or maximum degree of nodes.
	 */
	public double[] getClusteringCoefficients()
	{
		int n = graph.getNodeCount();

		if( n > 0 )
		{
			int j = 0;
			double[] coefs= new double[n];

			for( Node node: graph.getNodeSet() )
			{
				coefs[j++] = getClusteringCoefficient( node );
			}

			assert( j == n );

			return coefs;
		}
		
		return null;
	}

	/**
	 * Clustering coefficient for one node of the graph.
	 * @param node The node to compute the clustering coefficient for.
	 * @return The clustering coefficient for this node.
	 * @complexity O(d^2) where d is the degree of the given node.  
	 */
	public double getClusteringCoefficient( Node node )
	{
		double coef = 0.0;
		int    n    = node.getDegree();

		if( n > 1 )
		{
			// Collect the neighbor nodes.

			Node[]          nodes = new Node[n];
			HashSet<Edge> set     = new HashSet<Edge>();
			int           i       = 0;

			for( Edge e: node.getEdgeSet() )
			{
				nodes[i++] = e.getOpposite( node );
			}

			// Count the number of edges between these nodes.

			for( i=0; i<n; ++i )	// For all neighbor nodes.
			{
				for( int j=0; j<n; ++j )	// For all other nodes of this clique.
				{
					if( j != i )
					{
						Edge e = nodes[j].getEdgeToward( nodes[i].getId() );

						if( e != null )
						{
							if( ! set.contains( e ) )
								set.add( e );
						}
					}
				}
			}

			double ne = set.size();
			double max = ( n * ( n - 1 ) ) / 2.0;

			coef = ne / max;
		}

		return coef;
	}
	
	/**
	 * Choose a node at random.
	 * @return A node chosen at random.
	 * @complexity at worse O(n) where n is the number of nodes.
	 */
	public Node getRandomNode()
	{
		return getRandomNode( new Random() );
	}
	
	/**
	 * Choose a node at random.
	 * @param random The random number generator to use.
	 * @return A node chosen at random.
	 * @complexity at worse O(n) where n is the number of nodes.
	 */
	public Node getRandomNode( Random random )
	{
		int n = graph.getNodeCount();
		int r = random.nextInt( n );
		int i = 0;
		
		for( Node node: graph.getNodeSet() )
		{
			if( r == i )
			{
				return node;
			}
			
			i++;
		}
		
		throw new RuntimeException( "Outch !!" );
	}
	
	/**
	 * Return set of nodes grouped by the value of one attribute (the marker).
	 * For example, if the marker is "color" and in the graph there are nodes
	 * whose "color" attribute value is "red" and others with value "blue", this
	 * method will return two sets, one containing all nodes corresponding to
	 * the nodes whose "color" attribute is red, the other with blue nodes. If
	 * some nodes do not have the "color" attribute, a third set is returned.
	 * The returned sets are stored in a hash map whose keys are the values
	 * of the marker attribute (in our example, the keys would be "red" and
	 * "blue", and if there are nodes that do not have the "color" attribute,
	 * the third set will have key "NULL_COMMUNITY").
	 * @param marker The attribute that allows to group nodes.
	 * @return The communities indexed by the value of the marker.
	 * @complexity O(n) with n the number of nodes.
	 */
	public HashMap<Object,HashSet<Node>> communities( String marker )
	{
		HashMap<Object,HashSet<Node>> communities = new HashMap<Object,HashSet<Node>>();
		
		for( Node node: graph.getNodeSet() )
		{
			Object communityMarker = node.getAttribute( marker );
			
			if( communityMarker == null )
				communityMarker = "NULL_COMMUNITY";
				
			HashSet<Node> community = communities.get( communityMarker );
				
			if( community == null )
			{
				community = new HashSet<Node>();
				communities.put( communityMarker, community );
			}
				
			community.add( node );
		}

		return communities;
	}
	
	/**
	 * Create the modularity matrix E from the communities. The given communities
	 * are set of nodes forming the communities as produced by the
	 * {@link #communities(String)} method.
	 * @param communities Set of nodes.
	 * @return The E matrix as defined by Newman and Girvan.
	 * @complexity O(m²k) with m the number of communities and k the average
	 * number of nodes per community.
	 */
	public float[][] modularityMatrix( HashMap<Object,HashSet<Node>> communities )
	{
		int    edgeCount      = graph.getEdgeCount();
		int    communityCount = communities.size(); 
		float  E[][]          = new float[communityCount][];
		Object keys[]         = new Object[communityCount];
		
		int k = 0;
		
		for( Object key: communities.keySet() )
			keys[k++] = key;
		
		for( int i=0; i<communityCount; ++i )
			E[i] = new float[communityCount];
		
		for( int y=0; y<communityCount; ++y )
		{
			for( int x=y; x<communityCount; ++x )
			{
				E[x][y]  = modularityCountEdges( communities.get( keys[x] ), communities.get( keys[y] ) );
				E[x][y] /= edgeCount;
				
				if( x != y )
				{
					E[y][x] = E[x][y] / 2;
					E[x][y] = E[y][x];
				}
			}
		}
		
		return E;
	}
	
	/**
	 * Compute the modularity of the graph from the E matrix.
	 * @param E The E matrix given by {@link #modularityMatrix(HashMap)}.
	 * @return The modularity of the graph.
	 * @complexity O(m²) with m the number of communities.
	 */
	public float modularity( float[][] E )
	{
		float sumE = 0, Tr = 0;
		float communityCount = E.length;
		
		for( int y=0; y<communityCount; ++y )
		{
			for( int x=y; x<communityCount; ++x )
			{
				if( x == y )
					Tr += E[x][y];
				
				sumE += E[x][y]*E[x][y];
			}
		}

		return( Tr - sumE );
	}
	
	/**
	 * Computes the modularity as defined by Newman and Girvan in "Finding and
	 * evaluating community structure in networks". This algorithm traverses
	 * the graph to count nodes in communities. For this to work, there must
	 * exist an attribute on each node whose value define the community the
	 * node pertains to (see {@link #communities(String)}).
	 * 
	 * This method is an utility method that call:
	 * <ul>
	 * 	<li>{@link #communities(String)}</li>
	 * 	<li>{@link #modularityMatrix(HashMap)}</li>
	 * 	<li>{@link #modularity(float[][])}</li>
	 * </ul>
	 * in order to produce the modularity value.
	 *  
	 * @param marker The community attribute stored on nodes.
	 * @return The graph modularity.
	 * @complexity 0(n + m² + m²k) with n the number of nodes, m the number of
	 * communities and k the average number of nodes per communities.
	 * @see org.miv.graphstream.algorithm.Modularity
	 */
	public float modularity( String marker )
	{
		HashMap<Object,HashSet<Node>> communities = communities( marker );
		
		int    communityCount = communities.size();
		float  E[][]          = modularityMatrix( communities );
		
		for( Object key: communities.keySet() )
		{
			HashSet<Node> com = communities.get( key );
			
			System.err.printf( "    community '%s': %d%n", key.toString(), com.size() );
		}
		
		for( int y=0; y<communityCount; ++y )
		{
			System.err.printf( "    %3d  ", y );
			
			for( int x=0; x<communityCount; ++x )
			{
				System.err.printf( " %03.3f", E[x][y] );
			}
			
			System.err.printf( "%n" );
		}
		
		// Compute the modularity.
		
		return modularity( E );
	}
	
	/**
	 * Count the number of edges between the two communities (works if the two
	 * communities are the same).
	 * @param community The first community.
	 * @param otherCommunity The second community.
	 * @return The number of edges between the two communities.
	 */
	protected int modularityCountEdges( HashSet<Node> community, HashSet<Node> otherCommunity )
	{
		HashSet<Edge> marked = new HashSet<Edge>();
		
		int edgeCount = 0;
		
		if( community != otherCommunity )
		{
			// Count edges between the two communities
			
			for( Node node: community )
			{
				for( Edge edge: node.getEdgeSet() )
				{
					if( ! marked.contains( edge ) )
					{
						marked.add( edge );
						
						if( ( community.contains( edge.getNode0() ) && otherCommunity.contains( edge.getNode1() ) )
						||  ( community.contains( edge.getNode1() ) && otherCommunity.contains( edge.getNode0() ) ) )
						{
							edgeCount++;
						}
					}
				}
			}
		}
		else
		{
			// Count inner edges.
			
			for( Node node: community )
			{
				for( Edge edge: node.getEdgeSet() )
				{
					if( ! marked.contains( edge ) )
					{
						marked.add( edge );
						
						if( community.contains( edge.getNode0() ) && community.contains( edge.getNode1() ) )
						{
							edgeCount++;
						}
					}
				}
			}
		}
		
		return edgeCount;
	}
	
// Commands
	
	/**
	 * Does nothing, this implementation of Algorithm is a set of useful algorithms!.
	 */
	public void compute()
	{
		// NOP!
	}
	
	public void setGraph( Graph graph )
	{
		this.graph = graph;
	}
}