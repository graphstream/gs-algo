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

package org.miv.graphstream.algorithm.coloring;

import java.util.*;
import java.awt.*;

import org.miv.graphstream.graph.*;
import org.miv.graphstream.algorithm.*;

/**
 * Welsh Powell static colorating algorithm.
 * 
 * <p>
 * This class is intended to give some algorithm for computing the well-known
 * coloring problem. It provides the Welsh and Powell greedy algorithm that may
 * used as a static method.
 * </p>
 * 
 * <p>
 * This is an iterative greedy algorithm:
 * <ul>
 * 		<li>Step 1: All vertices are sorted according to the decreasing value of
 * 			their degree in a list V.
 * 		<li>Step 2: Colors are ordered in a list C.
 * 		<li>Step 3: The first non colored vertex v in V is colored with the
 * 			first available color in C. <i>available</i> means a color that was
 * 			not previously used by the algorithm.
 * 		<li>Step 4: The remaining part of the ordered list V is traversed and
 * 			the same color is allocated to every vertex for which no adjacent
 * 			vertex has the same color.
 * 		<li>Step 5: Steps 3 and 4 are applied iteratively until all the vertices
 * 			have been colored.
 * 		</ul>
 * <p>
 * 
 * <p>
 * This algorithm is known to use at most d(G)+1 colors where d(G)
 * represents the largest value of the degree in the graph G.
 * </p>
 * 
 * <p>
 * After computation (using {@link #compute()}, the algorithm returns its result
 * using the {@link #getLastComputedResult()} method. If you passed "true" to
 * the <tt>modify</tt> parameter, the colors are stored in the graph as
 * attributes. By default the attribute name is "color", so that the viewer
 * displays them accordingly, but you can optionnaly choose the attribute name.
 * </p>
 * 
 * <p>
 * This algorithm uses the <i>std-algo-1.0</i> algorithm's standard.
 * </p>
 * 
 * @version 0.1 30/08/2007
 * @author Frédéric Guinand
 * @author Antoine Dutot
 * @author Yoann Pigné
 */
public class WelshPowell
	implements Algorithm
{
// Attributes
	
	/**
	 * Modify the graph ?.
	 */
	protected boolean modify = false;
	
	/**
	 * Name of the attributes added to the graph.
	 */
	protected String attrName = "color";
	
	/**
	 * The graph.
	 */
	protected Graph g;

	/**
	 * The algorithm result (number of colors). 
	 */
	protected int result;
	
// Constructors
	
	/**
	 * New Welsh and Powell coloring algorithm.
	 * @param g An instance of a graph.
	 * @param modify A boolean equals to true if the graph has to be modified,
	 *        false otherwise
	 * @param attrName If modify is true, then attrName may be indicated as the
	 *        name of the attribute corresponding to the color allocated by this
	 *        algorithm. Note that if attrName is "color", then the color of the
	 *        nodes will be modified accordingly.
	 */
	public WelshPowell( Graph g, boolean modify, String attrName )
	{
		this.g        = g;
		this.modify   = modify;
		this.attrName = attrName;
	}
	
	/**
	 * New Welsh and Powell coloring algorithm, using "color" as the attribute
	 * name. If the graph is to be modified, the modification will store colors
	 * in it and the viewer should display them accordingly.
	 * @param g An instance of a graph.
	 * @param modify A boolean equals to true if the graph has to be modified,
	 *        false otherwise
	 */
	public WelshPowell( Graph g, boolean modify )
	{
		this.g        = g;
		this.modify   = modify;
	}
	
// Accessors
	/*
	public Graph getGraph()
	{
		return g;
	}
	*/
	/**
	 * Return the last computed result of the algorithm.
	 * @return The number of colors.
	 * @see #compute()
	 */
	public int getLastComputedResult()
	{
		return result;
	}
	
// Commands
	
	/**
	 * Set the name of the attribute to put in the graph if it is modified.
	 * @param attrName An attribute name.
	 */
	public void setAttributeName( String attrName )
	{
		this.attrName = attrName;
	}
	
	/**
	 * Modify the graph when computing the algorithm?.
	 * @param modify If true, attributes are stored in the graph.
	 */
	public void setModify( boolean modify )
	{
		this.modify = modify;
	}
	
	public void init( Graph g )
	{
		this.g = g;
	}
	
	public void compute()
	{
		String attributeName = "welshpowell";

		if( modify && ( attrName != null ) )
			attributeName = attrName;
	
		// ------- STEP 1 -----------
		// the algorithm requires the use of a sorted list using
		// degree values for sorting them.

		Comparator<Node> degreeComparator = new Comparator<Node>()
		{
			public int compare( Node ni, Node nj )
			{
				int returnValue = 0;
				int diff = ni.getDegree() - nj.getDegree();
				
				if( diff > 0 )
				{
					returnValue = -1;
				}
				else if( diff < 0 )
				{
					returnValue = 1;
				}
				
				return returnValue;
			}
		};

		PriorityQueue<Node> pq = new PriorityQueue<Node>( g.getNodeCount(), degreeComparator );
		Iterator<? extends Node> nodes = g.getNodeIterator();
		
		while( nodes.hasNext() )
		{
		    pq.add( nodes.next() );
		}

		ArrayList<Node> sortedNodes = new ArrayList<Node>();

		for( int i = 0; i < g.getNodeCount(); i++ )
		{
			sortedNodes.add( pq.poll() );
		}

		// ------ STEP 2 --------
		// color initialization

		ArrayList<Color> allColors = new ArrayList<Color>();
		Color col;
		int nbColors = 0;

		for( int i = 0; i < g.getNodeCount(); i++ )
		{
			col = Color.getHSBColor( (float) ( Math.random() ), 0.8f, 0.9f );
			allColors.add( col );
		}

		// ------- STEP 3 --------

		Color currentColor = allColors.remove( 0 );
		nbColors++;

		while( !sortedNodes.isEmpty() )
		{
			int index = 0;

			while( index < sortedNodes.size() )
			{
				Node n = sortedNodes.get( index );
				Iterator<? extends Node> neighbors = n.getNeighborNodeIterator();
				boolean conflict = false;

				while( neighbors.hasNext() && !conflict )
				{
					Node neighb = neighbors.next();

					if( neighb.hasAttribute( attributeName ) )
					{
						if( ( (Color) ( neighb.getAttribute( attributeName ) ) ).equals( currentColor ) )
						{
							conflict = true;
						}
					}
				}

				if( !conflict )
				{
					n.addAttribute( attributeName, currentColor );
					sortedNodes.remove( index );
				}
				else
				{
					index++;
				}
			}

			currentColor = allColors.remove( 0 );
			nbColors++;
		}

		if( !modify )
		{
		    nodes = g.getNodeIterator();
		    
		    while( nodes.hasNext() )
		    {
			nodes.next().removeAttribute( attributeName );
		    }
		}

		result = nbColors;
	}
}