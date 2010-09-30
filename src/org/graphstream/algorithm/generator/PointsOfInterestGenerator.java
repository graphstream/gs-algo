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
 * Copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 */
package org.graphstream.algorithm.generator;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Generate a <b>social<b> dynamic graph. Graph is composed of high-connected
 * group of nodes, modeling organizations, and few connections between
 * organizations.
 * 
 * This is done by creating <i>points of interest</i>. Nodes can be interested
 * by these points or loose them interest. When two nodes are interested by at
 * least one common point, then there are connected.
 * 
 * Some probabilities can be set defining the following events :
 * <ul>
 * <li>remove a node ;</li>
 * <li>add a node ;</li>
 * <li>remove a point of interest ;</li>
 * <li>add a point of interest.</li>
 * </ul>
 * 
 * Initial parameters are :
 * <ul>
 * <li>initial count of points of interest ;</li>
 * <li>initial count of nodes ;</li>
 * </ul>
 * 
 * @author Guilhelm Savin
 * 
 */
public class PointsOfInterestGenerator
	extends BaseGenerator
{
	/**
	 * Defines a point of interest. It is just a set of <i>addicted</i> nodes.
	 */
	protected class PointOfInterest
	{
		/**
		 * Set of nodes interested by this point.
		 */
		Set<Addict> addict;
		
		PointOfInterest()
		{
			addict = new HashSet<Addict>();
		}
		
		/**
		 * Registers a node as an addict of this point. The node will be linked
		 * to all nodes already addict of this point. The list of points of
		 * interest of the node will be updated.
		 * 
		 * @param addictA
		 *            the addicted node
		 */
		void newAddict( Addict addictA )
		{
			if( ! addict.contains(addictA) )
			{
				for( Addict addictB: addict )
					addictA.link(addictB);
			
				addict.add(addictA);
				addictA.pointsOfInterest.add(this);
			}
		}
		
		/**
		 * Unregisters a node. The node will be unlinked to all nodes already
		 * addict of this point. The list of points of interest of the node will
		 * be updated.
		 * 
		 * @param addictA
		 *            the addicted node
		 */
		void delAddict( Addict addictA )
		{
			if( addict.contains(addictA) )
			{
				addict.remove(addictA);
				addictA.pointsOfInterest.remove(this);
				
				for( Addict addictB: addict )
					addictA.unlink(addictB);
			}
		}
		
		/**
		 * Check is a node is addict of this point.
		 * 
		 * @param a
		 *            the addict
		 * @return true if a is addict of this point
		 */
		boolean isAddict( Addict a )
		{
			return addict.contains(a);
		}
	}
	
	/**
	 * Defines data of a node. We have to keep id of the node and to backup
	 * points of interest of this node and neighbor of the node.
	 */
	protected class Addict
	{
		/**
		 * Id of the node.
		 */
		String id;
		
		/**
		 * List of points of interest of this node.
		 */
		LinkedList<PointOfInterest> pointsOfInterest;
		
		/**
		 * List of neighbors.
		 */
		Map<Addict,AtomicInteger> neighbor;
		
		Addict( String id )
		{
			this.id = id;
			pointsOfInterest = new LinkedList<PointOfInterest>();
			neighbor = new HashMap<Addict,AtomicInteger>();
		}
		
		/**
		 * Defines a step for a node. Node will iterate over points-of-interest.
		 * For each point p, if node is already interest by p, node will check
		 * if it is still interested by this point (according to
		 * <i>lostInterestProbability</i> probability). Else, node will checked if it
		 * can be interested by p, according to <i>haveInterestProbability</i>
		 * probability and its points count (the probability will decrease when
		 * the count of points increases).
		 */
		void step()
		{
			//
			// Avoid that all nodes are interested by the same point.
			//
			Collections.shuffle( PointsOfInterestGenerator.this.pointsOfInterest, random );
			
			for( PointOfInterest poi: PointsOfInterestGenerator.this.pointsOfInterest )
			{
				if( pointsOfInterest.contains(poi) )
				{
					if( random.nextFloat() < lostInterestProbability )
						poi.delAddict(this);
				}
				else
				{
					if( random.nextFloat() < Math.pow( haveInterestProbability, 1.2 * pointsOfInterest.size() ) )
						poi.newAddict(this);
				}
			}
		}

		/**
		 * Link this node to another. Both nodes will share a common counter.
		 * Links these two nodes will increase the counter and so unlink will
		 * decrease the counter. If counter not exists, it is initialized and
		 * edge is created. Else, if counter is equal to zero, counter is
		 * removed and edge is removed too.
		 * 
		 * @param a
		 *            the node to link
		 */
		void link( Addict a )
		{
			if( ! neighbor.containsKey(a) )
			{
				PointsOfInterestGenerator.this.addEdge( getEdgeId(id,a.id), id, a.id );
				AtomicInteger i = new AtomicInteger(0);
				neighbor.put(a,i);
				a.neighbor.put(this,i);
			}
			
			neighbor.get(a).incrementAndGet();
		}
		
		/**
		 * Unlink this node with another. Links-counter between these two nodes
		 * is decreased and edge is removed is needed.
		 * 
		 * @param a
		 *            the node to unlink
		 */
		void unlink( Addict a )
		{
			if( neighbor.containsKey(a) )
			{
				if( neighbor.get(a).decrementAndGet() <= 0 )
				{
					neighbor.remove(a);
					a.neighbor.remove(this);
					PointsOfInterestGenerator.this.delEdge( getEdgeId(id,a.id) );
				}
			}
		}
		
		/**
		 * Unlink all neighbor.
		 */
		void fullUnlink()
		{
			for( Addict a: neighbor.keySet() )
			{
				a.neighbor.remove(this);
				PointsOfInterestGenerator.this.delEdge( getEdgeId(id,a.id) );
			}
		}
	}
	
	protected static String getEdgeId( String nodeA, String nodeB )
	{
		return nodeA.compareTo(nodeB) < 0 ?
				String.format( "%s---%s", nodeA, nodeB ) :
				String.format( "%s---%s", nodeB, nodeA ); 
	}
	
	/**
	 * Initial count of nodes.
	 */
	protected int initialPeopleCount;
	
	/**
	 * Probability to add a node during a step.
	 */
	protected float addPeopleProbability;
	
	/**
	 * Probability to remove a node during a step.
	 */
	protected float delPeopleProbability;
	
	/**
	 * Probability that a node becomes interested in a point-of-interest it was
	 * not already interested.
	 */
	protected float haveInterestProbability;
	
	/**
	 * Probability that a node looses its interest for
	 * a point-of-interest.
	 */
	protected float lostInterestProbability;
	
	/**
	 * Initial count of point-of-interest.
	 */
	protected int initialPointOfInterestCount;
	
	/**
	 * Probability to add a new point-of-interest.
	 */
	protected float addPointOfInterestProbability;
	
	/**
	 * Probability to remove a point-of-interest.
	 */
	protected float delPointOfInterestProbability;
	
	/**
	 * List of addicts.
	 */
	protected LinkedList<Addict> addicts;
	/**
	 * List of point-of-interest.
	 */
	protected LinkedList<PointOfInterest> pointsOfInterest;
	
	private long currentId;
	
	public PointsOfInterestGenerator()
	{
		disableKeepNodesId();
		disableKeepEdgesId();
		
		initialPeopleCount = 500;
		addPeopleProbability = delPeopleProbability = 0.001f;
		
		haveInterestProbability = 0.001f;
		lostInterestProbability = 0.005f;
		
		initialPointOfInterestCount = 15;
		addPointOfInterestProbability = delPointOfInterestProbability = 0.001f;
		
		addicts = new LinkedList<Addict>();
		pointsOfInterest = new LinkedList<PointOfInterest>();
	}
	
	/**
	 * Add initial count of points of interest, and initial count of people.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin()
	{
		pointsOfInterest.clear();
		
		for( int i = 0; i < initialPointOfInterestCount; i++ )
			addPointOfInterest();
		
		for( int i = 0; i < initialPeopleCount; i++ )
			addAddict();
	}

	/**
	 * Step of the generator. Try to remove a node according to the
	 * {@link #delPeopleProbability}. Try to add a node according to the
	 * {@link #addPeopleProbability}. Try to remove a point of interest
	 * according to the {@link #delPointOfInterestProbability}. Try to add a
	 * point of interest according to the {@link #addPointOfInterestProbability}
	 * . Then, step of <i>addicts</i>.
	 * 
	 * @see PointsOfInterestGenerator.Addict#step()
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents()
	{
		if( random.nextDouble() < delPeopleProbability )
			killSomeone();
		
		if( random.nextDouble() < addPeopleProbability )
			addAddict();
		
		if( random.nextDouble() < delPointOfInterestProbability )
			removeRandomPointOfInterest();
		
		if( random.nextDouble() < addPointOfInterestProbability )
			addPointOfInterest();
		
		for( Addict a : addicts )
			a.step();
		
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	public void end()
	{
		// Nothing to do
	}

	protected void addPointOfInterest()
	{
		pointsOfInterest.add( new PointOfInterest() );
	}
	
	protected void removePointOfInterest( PointOfInterest poi )
	{
		pointsOfInterest.remove(poi);
		
		for( Addict a : poi.addict )
			poi.delAddict(a);
	}
	
	protected void removeRandomPointOfInterest()
	{
		pointsOfInterest.remove( random.nextInt(pointsOfInterest.size()) );
	}
	
	protected void addAddict()
	{
		Addict a  = new Addict( String.format( "%08x", currentId++ ) );
		
		addicts.add(a);
		addNode(a.id);
	}
	
	protected void killAddict( Addict a )
	{
		while( a.pointsOfInterest.size() > 0 )
			a.pointsOfInterest.peek().delAddict(a);
		
		a.fullUnlink();
		
		addicts.remove(a);
		delNode(a.id);
		
		a.id = null;
		a.pointsOfInterest.clear();
		a.pointsOfInterest = null;
	}
	
	protected void killSomeone()
	{
		killAddict( addicts.get( random.nextInt( addicts.size() ) ) );
	}
}
