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

package org.miv.graphstream.algorithm.layout.elasticbox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import org.miv.graphstream.algorithm.layout.Layout;
import org.miv.graphstream.algorithm.layout.LayoutListener;
import org.miv.pherd.Particle;
import org.miv.pherd.ParticleBox;
import org.miv.pherd.ParticleBoxListener;
import org.miv.pherd.ntree.Anchor;
import org.miv.pherd.ntree.BarycenterCellData;
import org.miv.pherd.ntree.Cell;
import org.miv.pherd.ntree.CellSpace;
import org.miv.pherd.ntree.OctreeCellSpace;
import org.miv.pherd.ntree.QuadtreeCellSpace;
import org.miv.util.Environment;
import org.miv.util.NotFoundException;
import org.miv.util.SingletonException;
import org.miv.util.geom.Point3;
import org.miv.util.geom.Vector2;
import org.miv.util.geom.Vector3;

/**
 * An implementation of a graph layout that mostly follows the Fruchterman-Reingold
 * algorithm with the addition of a recursive space decomposition approximation. 
 *
 * <p>
 * In the Fruchterman-Reingold algorithm a global temperature parameter is used
 * to constrain the displacement of nodes in order to attain stability. The
 * problem here is that the graph can evolve with time. Therefore the temperature
 * is elevated each time the graph changes so that the layout can adapt.
 * </p>
 * 
 * <p>
 * Another difference of this algorithm is that it does not put "walls"
 * around the graph. Here, the graph "floats" in space. Nodes do not stick
 * to the walls. This produces more aesthetic drawings.
 * </p>
 * 
 * <p>
 * The original article: Fruchterman, T. M. J., & Reingold, E. M. (1991).
 * Graph Drawing by Force-Directed Placement. Software: Practice and
 * Experience, 21(11). 
 * </p>
 * 
 * TODO: regler la decroissance de la temperature en fonction de la taille du graphe ??
 * 
 * @author Antoine Dutot
 * @since 2007
 */
public class ElasticBox implements Layout, ParticleBoxListener
{
// Attributes
	
	/**
	 * The node representation and the n-tree.
	 */
	protected ParticleBox nodes;
	
	/**
	 * The set of edges.
	 */
	protected HashMap<String,Edge> edges = new HashMap<String,Edge>();
	
	/**
	 * Random number generator.
	 */
	protected Random random;
	
// Attributes
	
	/**
	 * The optimal distance between nodes. We start with an initial space of
	 * [-1..1] therefore, the optimal edge length should be 1/nodeCount however
	 * as the graph is dynamic, this is difficult to compute or change.
	 */
	protected float k = 1f;
	
	/**
	 * Constant used in determining the optimal distance between nodes.
	 */
	protected static float C = 1;
	
	/**
	 * Compute the third coordinate ?.
	 */
	protected boolean is3D = false;
	
	/**
	 * The lowest node position.
	 */
	protected Point3 lo = new Point3( 0, 0, 0 );
	
	/**
	 * The highest node position.
	 */
	protected Point3 hi = new Point3( 1, 1, 1 );
	
	/**
	 * Global force strength.
	 */
	protected float force = 0.1f;
	
	/**
	 * Global temperature.
	 */
	protected float temperature = 1f;
	
	/**
	 * The cool factor. This factor is reset according to the number of nodes.
	 * The larger the number of nodes, the closer to one.
	 */
	protected float coolFactor = 0.99999f;
	
	/**
	 * The view distance at which the cells of the n-tree are explored exhaustively,
	 * after this the poles are used. This is a factor of k. 
	 */
	protected int viewZone = 2;

	/**
	 * The diagonal of the graph area at the current step.
	 */
	protected float area;
	
	/**
	 * The maximum length of a node displacement at the current step.
	 * This is set in the node#move(int) method.
	 */
	protected float maxMoveLength;
	
// Attributes
	
	/**
	 * The duration of the last step in milliseconds.
	 */
	protected long lastStepTime;
	
	/**
	 * Number of nodes that moved during last step.
	 */
	protected int nodesMoved;
	
	/**
	 * Current step.
	 */
	protected int time;
	
	/**
	 * Set of listeners.
	 */
	protected ArrayList<LayoutListener> listeners = new ArrayList<LayoutListener>();
	
	/**
	 * How much very connected clusters must be grouped.
	 */
	protected float clusteringCoef = 1;
	
// Constructors
	
	public ElasticBox()
	{
		this( false );
	}
	
	public ElasticBox( boolean is3D )
	{
		this( is3D, new Random( System.currentTimeMillis() ) );
	}
	
	public ElasticBox( boolean is3D, Random randomNumberGenerator )
	{
		CellSpace space;
		
		this.is3D   = is3D;
		this.random = randomNumberGenerator;

		checkEnvironment();
		
		if( is3D )
		     space = new OctreeCellSpace( new Anchor( -1, -1, -1 ), new Anchor( 1, 1, 1 ) );
		else space = new QuadtreeCellSpace( new Anchor( -1, -1, -0.01f ), new Anchor( 1, 1, 0.01f ) );
		
		this.nodes = new ParticleBox( 10, space, new BarycenterCellData() );
		
		nodes.addParticleBoxListener( this );
	}
	
	protected void checkEnvironment()
	{
		Environment env = Environment.getGlobalEnvironment();
		
		if( env.hasParameter( "Layout.3d" ) )
			this.is3D = env.getBooleanParameter( "Layout.3d" );
	}
	
// Accessors

	public Point3 getLowPoint()
	{
		return nodes.getNTree().getLowestPoint();
	}

	public Point3 getHiPoint()
	{
		return nodes.getNTree().getHighestPoint();
	}

	public long getLastStepTime()
	{
		return lastStepTime;
	}

	public String getLayoutAlgorithmName()
	{
		return "Fruchterman-Reingold (modified)";
	}

	public int getNodeMoved()
	{
		return nodesMoved;
	}

	public double getStabilization()
	{
		float pc = nodes.getParticleCount();
		
		if( pc > 0 )
			return( 1 - (nodesMoved / pc) );
		
		return 1;
	}

	public int getSteps()
	{
		return time;
	}

// Commands

	public void addListener( LayoutListener listener )
	{
		listeners.add( listener );
	}

	public void removeListener( LayoutListener listener )
	{
		int pos = listeners.indexOf( listener );
		
		if( pos >= 0 )
		{
			listeners.remove( pos );
		}
	}

	public void setForce( float value )
	{
		this.force = value;
	}

	public void setQuality( int qualityLevel )
	{
		switch( qualityLevel )
		{
			case 0:
				viewZone = 1;
				coolFactor = 0.999f;
				heat();
				break;
			case 1:
				viewZone = 2;
				coolFactor = 0.9999f;
				heat();
				break;				
			case 2:
				viewZone = 4;
				coolFactor = 0.99999f;
				heat();
				break;
			case 3:
				viewZone = 8;
				coolFactor = 0.999999f;
				heat();
				break;
			case 4:
				viewZone = -1;
				coolFactor = 1f;
				heat();
				break;
			default:
				System.err.printf( "invalid quality level %d%n", qualityLevel );
				break;
		}
	}

	public void clear()
	{
		// TODO
	}

	public void compute()
	{
		long  t1;
//		float area;
		
		computeArea();

		maxMoveLength = Float.MIN_VALUE;

//		k          = C * ((float) Math.sqrt( area / nodes.getParticleCount() ));
		k          = 0.1f;
		t1         = System.currentTimeMillis();
		nodesMoved = 0;

		// Loop on edges to compute edge attraction.
		
		for( Edge edge : edges.values() )
			edge.attraction();
		
		// Loop on nodes to compute node repulsion.
		
		nodes.step();
		
		// Ready for next step.

		cool();
		time++;
		lastStepTime = System.currentTimeMillis() - t1;
		
		for( LayoutListener listener: listeners )
			listener.stepCompletion( (float)getStabilization() );
	}
	
	protected void computeArea()
	{
		float w = getHiPoint().x - getLowPoint().x;
		float h = getHiPoint().y - getLowPoint().y;
		float d = getHiPoint().z - getLowPoint().z;

		area = (float) Math.sqrt( w*w + h*h + d*d );
	}
	
	protected void heat()
	{
		temperature += 0.1f;
		
		if( temperature > 1f )
			temperature = 1f;
	}
	
	protected void cool()
	{
		temperature *= coolFactor;
		
		if( temperature <= 0 )
			temperature = 0.1f;
	}
	
	protected void reevaluateCoolFactor()
	{
		coolFactor = 1 - (1/(nodes.getParticleCount()*10));
	}
	
	public void shake()
	{
	/*	Iterator<Object> i = nodes.getParticleIdIterator();
		
		while( i.hasNext() )
		{
			Node node = (Node) nodes.getParticle( i.next() );
			node.shake();
		}
	*/	
		temperature = 1f;
	}

	public void addNode( String id ) throws SingletonException
	{
		nodes.addParticle( new Node( id ) );
		
		heat();
		reevaluateCoolFactor();
	}

	public void moveNode( String id, float dx, float dy, float dz )
	{
		Node node = (Node) nodes.getParticle( id );
		
		if( node != null )
		{
			node.move( dx, dy, dz );
			heat();
		}
	}

	public void freezeNode( String id, boolean on )
	{
		Node node = (Node) nodes.getParticle( id );
		
		if( node != null )
		{
			node.frozen = on;
			
			if( on == false )
				heat();
		}
	}

	public void setNodeWeight( String id, float weight )
	{
		Node node = (Node) nodes.getParticle( id );
		
		if( node != null )
			node.setWeight( weight );
	}

	public void removeNode( String id ) throws NotFoundException
	{
		Node node = (Node) nodes.removeParticle( id );
		
		if( node != null )
		{
			node.removeNeighborEdges();
			heat();
			reevaluateCoolFactor();
		}
	}

	public void addEdge( String id, String from, String to, boolean directed )
			throws NotFoundException, SingletonException
	{
		Node n0 = (Node) nodes.getParticle( from );
		Node n1 = (Node) nodes.getParticle( to );
		
		if( n0 != null && n1 != null )
		{
			Edge e = new Edge( id, n0, n1 );
			Edge o = edges.put( id, e );
			
			if( o != null )
			{
				//throw new SingletonException( "edge '"+id+"' already exists" );
				System.err.printf( "edge '%s' already exists%n", id );
			}
			else
			{
				n0.registerEdge( e );
				n1.registerEdge( e );
			}
			
			heat();
		}
	}

	public void addEdgeBreakPoint( String edgeId, int points )
	{
		System.err.printf( "edge break points are not handled yet." );
	}
	
	public void ignoreEdge( String edgeId, boolean on )
	{
		Edge edge = edges.get( edgeId );
		
		if( edge != null )
		{
			edge.ignored = on;
		}
	}

	public void setEdgeWeight( String id, float weight )
	{
		Edge edge = edges.get( id );
		
		if( edge != null )
			edge.weight = weight;
	}

	public void removeEdge( String id ) throws NotFoundException
	{
		Edge e = edges.remove( id );
		
		if( e != null )
		{
			e.node0.unregisterEdge( e );
			e.node1.unregisterEdge( e );
			heat();
		}
	}

	public void outputPos( String filename ) throws IOException
	{
		// TODO Auto-generated method stub
	}

	public void inputPos( String filename ) throws IOException
	{
		// TODO Auto-generated method stub
	}

// Particle box listner

	public void particleAdded( Object id, float x, float y, float z, Object mark )
	{
	}

	public void particleMarked( Object id, Object mark )
	{
	}

	public void particleMoved( Object id, float x, float y, float z )
	{
		for( LayoutListener listener: listeners )
			listener.nodeMoved( (String)id, x, y, z );
	}

	public void particleRemoved( Object id )
	{
	}

	public void stepFinished( int time )
	{
	}
	
// Nested classes
	
/**
 * Node representation. 
 */
protected class Node extends Particle
{
// Attributes
	
	/**
	 * Set of edge connected to this node.
	 */
	public ArrayList<Edge> neighbours = new ArrayList<Edge>();
	
	/**
	 * Should the node move?.
	 */
	public boolean frozen = false;
	
	/**
	 * Displacement vector.
	 */
	public Vector2 disp;
	
	public float len;
	
// Constructors
	
	/**
	 * New node.
	 * @param id The node identifier.
	 */
	public Node( String id )
	{
		this( id, random.nextFloat()*2-1, random.nextFloat()*2-1, is3D ? random.nextFloat()*2-1 : 0 );
	}
	
	/**
	 * New node at a given position.
	 * @param id The node identifier.
	 * @param x The abscissa.
	 * @param y The ordinate.
	 * @param z The depth.
	 */
	public Node( String id, float x, float y, float z )
	{
		super( id, x, y, is3D ? z : 0 );
		//disp = is3D ? new Vector3() : new Vector2();
		disp = new Vector2();
	}

// Accessors
	
	/**
	 * All the edges connected to this node.
	 * @return A set of edges.
	 */
	public Collection<Edge> getEdges()
	{
		return neighbours;
	}
	
// Commands

	@Override
	public void move( int time )
	{
		if( ! frozen )
		{
			disp.fill( 0 );
			
			Vector3 delta = new Vector3();

			if( viewZone < 0 )
				repulsionN2( delta );
			else repulsionNLogN( delta );

			attraction();

			len = disp.length();

			if( len > maxMoveLength )
				maxMoveLength = len;			
		}
	}
	
	@Override
	public void nextStep( int time )
	{
		float ratio = 1;
		
		if( maxMoveLength > area )
			ratio = area / maxMoveLength;
		
		disp.scalarMult( ratio * force * temperature );
		
		len = disp.length();
		
		if( len > 0.0001f )// area*0.0001f )
		{
			nextPos.x = pos.x + disp.data[0];
			nextPos.y = pos.y + disp.data[1];
			
			if( is3D )
			     nextPos.z = pos.z + disp.data[2];
			
			nodesMoved++;
			moved = true;
		}

		super.nextStep( time );
	}
	
	public void move( float dx, float dy, float dz )
	{
		pos.set( pos.x + dx, pos.y + dy, pos.z + dz );
	}
	
	protected void repulsionN2( Vector3 delta )
	{
		Iterator<Object> i = nodes.getParticleIdIterator();
		
		while( i.hasNext() )
		{
			Node node = (Node) nodes.getParticle( i.next() );
			
			if( node != this )
			{
/*				delta.set( node.pos.x - pos.x, node.pos.y - pos.y, is3D ? node.pos.z - pos.z : 0 );
				
				float len = delta.length();
				
				delta.scalarDiv( len*len );
				delta.scalarMult( k*k * (float)Math.pow(weight * node.weight, clusteringCoef) );
				
				disp.sub( delta );
*/
				delta.set( node.pos.x - pos.x, node.pos.y - pos.y, is3D ? node.pos.z - pos.z : 0 );
				
				float len = delta.normalize();
				
				delta.scalarMult( -(k*k)/len * (float)Math.pow( weight * node.weight, clusteringCoef ) );
				disp.add( delta );
			}
		}
	}
	
	protected void repulsionNLogN( Vector3 delta )
	{
		// Explore the n-tree from the root cell and consider the contents
		// of one cell only if it does intersect an area around the current
		// node. Else take its (weighted) barycenter into account.
		
		recurseRepulsion( nodes.getNTree().getRootCell(), delta );
	}
	
	protected void recurseRepulsion( Cell cell, Vector3 delta )
	{
		if( intersection( cell ) )
		{
			if( cell.isLeaf() )
			{
				Iterator<? extends Particle> i = cell.getParticles();
				
				while( i.hasNext() )
				{
					Node node = (Node) i.next();
					
					if( node != this )
					{
						delta.set( node.pos.x - pos.x, node.pos.y - pos.y, is3D ? node.pos.z - pos.z : 0 );
						float len = delta.normalize();
						delta.scalarMult( -(k*k)/len  * (float)Math.pow( weight * node.weight, clusteringCoef ) );
						disp.add( delta );
					}
				}
			}
			else
			{
				int div = cell.getSpace().getDivisions();
				
				for( int i=0; i<div; i++ )
					recurseRepulsion( cell.getSub( i ), delta );
			}
		}
		else
		{
			BarycenterCellData bary = (BarycenterCellData) cell.getData();
			
			delta.set( bary.center.x - pos.x,
			           bary.center.y - pos.y,
			    is3D ? bary.center.z - pos.z : 0 );
			float len = delta.normalize();
			delta.scalarMult( (-(k*k)/len) * (float)Math.pow( bary.weight * weight, clusteringCoef ) );
			disp.add( delta );
		}
	}
	
	protected boolean intersection( Cell cell )
	{
		float x1 = cell.getSpace().getLoAnchor().x;
		float y1 = cell.getSpace().getLoAnchor().y;
		float z1 = cell.getSpace().getLoAnchor().z;
		float x2 = cell.getSpace().getHiAnchor().x;
		float y2 = cell.getSpace().getHiAnchor().y;
		float z2 = cell.getSpace().getHiAnchor().z;
		
		float X1 = pos.x - viewZone*k;
		float Y1 = pos.y - viewZone*k;
		float Z1 = pos.z - viewZone*k;
		float X2 = pos.x + viewZone*k;
		float Y2 = pos.y + viewZone*k;
		float Z2 = pos.z + viewZone*k;
		
		// Only when the area is before or after the cell there cannot
		// exist an intersection (case a and b). Else there must be an
		// intersection (cases c, d, e and f).
		//
		// |-a-|   +---------+   |-b-|
		//         |         |
		//       |-c-|     |-d-|
		//         |         |
		//         |  |-e-|  |
		//         |         |
		//       |-+----f----+-|
		//         |         |
		//         +---------+
		
		if( X2 < x1 || X1 > x2 )
			return false;
		
		if( Y2 < y1 || Y1 > y2 )
			return false;
				
		if( Z2 < z1 || Z1 > z2 )
			return false;
		
		return true;
	}
	
	protected void attraction()
	{
		for( Edge edge : neighbours )
		{
			if( ! edge.ignored )
			{
				if( this == edge.node0 )
				{
					disp.add( edge.spring );
				}
				else
				{
					disp.sub( edge.spring );
				}
			}
		}
	}

	/**
	 * The given edge is connected to this node.
	 * @param e The edge to connect.
	 */
	public void registerEdge( Edge e )
	{
		neighbours.add( e );
	}

	/**
	 * The given edge is no more connected to this node.
	 * @param e THe edge to disconnect.
	 */
	public void unregisterEdge( Edge e )
	{
		int i = neighbours.indexOf( e );

		if( i >= 0 )
		{
			neighbours.remove( i );
		}
	}
	
	/**
	 * Remove all edges connected to this node.
	 */
	public void removeNeighborEdges()
	{
		for( Edge edge: neighbours )
		{
			removeEdge( edge.id );
		}
	}
	
	/**
	 * Move the node by a random vector.
	 */
	public void shake()
	{
		pos.x += random.nextFloat() * k * 2 - 1;
		pos.y += random.nextFloat() * k * 2 - 1;
		
		if( is3D )
			pos.z += random.nextFloat() * k * 2 - 1;
	}
}

/**
 * Edge representation.
 */
protected class Edge
{
	/**
	 * The edge identifier.
	 */
	public String id;
	
	/**
	 * Source node.
	 */
	public Node node0;
	
	/**
	 * Target node.
	 */
	public Node node1;
	
	/**
	 * Edge weight.
	 */
	public float weight = 1f;
	
	/**
	 * The attraction force on this edge.
	 */
	public Vector3 spring = new Vector3();
	
	/**
	 * Make this edge ignored by the layout algorithm ?.
	 */
	public boolean ignored = false;
	
	/**
	 * New edge between two given nodes.
	 * @param id The edge identifier.
	 * @param n0 The first node.
	 * @param n1 The second node.
	 */
	public Edge( String id, Node n0, Node n1 )
	{
		this.id    = id;
		this.node0 = n0;
		this.node1 = n1;
	}

	/**
	 * Considering the two nodes of the edge, return the one that was not
	 * given as argument.
	 * @param node One of the nodes of the edge.
	 * @return The other node.
	 */
	public Node getOpposite( Node node )
	{
		if( node0 == node )
			return node1;

		return node0;
	}
	
	/**
	 * Compute the attraction force on this edge.
	 */
	public void attraction()
	{
		if( ! ignored )
		{
			Point3 p0 = node0.getPosition();
			Point3 p1 = node1.getPosition();
		
			spring.set( p1.x - p0.x, p1.y - p0.y, is3D ? p1.z - p0.z : 0 );
			float len = spring.normalize();
		
			spring.scalarMult( ( (len*len)/k ) * weight * (float)Math.pow( node0.getWeight() * node1.getWeight(), clusteringCoef ) );
		}
	}
}

public void particleAdded( Object id, float x, float y, float z )
{
}

public void particleAttributeChanged( Object id, String attribute, Object newValue, boolean removed )
{
}

}