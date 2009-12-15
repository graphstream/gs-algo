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

/**
 * Generator for grids.
 *
 * @since 2007
 */
public class GridGenerator
	extends BaseGenerator
{
// Attributes
	
	/**
	 * Create diagonal links.
	 */
	protected boolean cross = false;
	
	/**
	 * Close the grid as a tore.
	 */
	protected boolean tore = false;
	
	/**
	 * generate x and y attributes on a plane.
	 */
	protected boolean generateXY = false;
	
	/**
	 * Current width and height of the grid.
	 */
	protected int currentSize = 0;
	
	/**
	 * Used to generate edge names.
	 */
	protected int edgeNames = 0;
	
// Constructors
	
	/**
	 * New grid generator. By default no diagonal links are
	 * made and the grid is not a tore.
	 */
	public GridGenerator()
	{
		this( false, false );
	}
	
	/**
	 * New grid generator.
	 * @param cross Create diagonal links?.
	 * @param tore Close the grid as a tore?.
	 */
	public GridGenerator( boolean cross, boolean tore )
	{
		this( cross, tore, false );
	}
	
	public GridGenerator( boolean cross, boolean tore, boolean generateXY )
	{
		this( cross, tore, false, false );
	}
	
	public GridGenerator( boolean cross, boolean tore, boolean generateXY, boolean directed )
	{
		this.cross      = cross;
		this.tore       = tore;
		this.generateXY = generateXY;
		this.directed   = directed;
	}
	
// Attributes
	
// Commands
	
	public void begin()
	{
		addNode( nodeName( 0, 0 ), 0, 0 );
	}

	public void end()
	{
		if( tore )
		{
			if( currentSize > 0 )
			{				
				for( int y=0; y<=currentSize; ++y )
				{
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( currentSize, y ), nodeName( 0, y ) );
					
					if( cross )
					{
						if( y > 0 )
						{
							addEdge( Integer.toString( edgeNames++ ),
								nodeName( currentSize, y ), nodeName( 0, y-1 ) );
							addEdge( Integer.toString( edgeNames++ ),
									nodeName( currentSize, y-1 ), nodeName( 0, y ) );
						}
					}
				}
				
				for( int x=0; x<=currentSize; ++x )
				{
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( x, currentSize ), nodeName( x, 0 ) );
					
					if( cross )
					{
						if( x > 0 )
						{
							addEdge( Integer.toString( edgeNames++ ),
									nodeName( x, currentSize ), nodeName( x-1, 0 ) );
							addEdge( Integer.toString( edgeNames++ ),
									nodeName( x-1, currentSize ), nodeName( x, 0 ) );							
						}
					}
				}
				
				if( cross )
				{
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( currentSize, 0 ), nodeName( 0, currentSize ) );
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( 0, 0 ), nodeName( currentSize, currentSize ) );
				}
			}
		}
	}

	public boolean nextElement()
	{
		currentSize++;
		
		for( int y=0; y<currentSize; ++y )
		{
			String id = nodeName( currentSize, y );
			
			addNode( id, currentSize, y );
			addEdge( Integer.toString( edgeNames++ ),
					nodeName( currentSize-1, y ), id );

			if( y > 0 )
			{
				addEdge( Integer.toString( edgeNames++ ),
						nodeName( currentSize, y-1 ), id );
				
				if( cross )
				{
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( currentSize-1, y-1 ), id );
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( currentSize, y-1 ), nodeName( currentSize-1, y ) );
				}
			}
		}
		
		for( int x=0; x<=currentSize; ++x )
		{
			String id = nodeName( x, currentSize );
			
			addNode( id, x, currentSize );
			addEdge( Integer.toString( edgeNames++ ),
					nodeName( x, currentSize-1 ), id );
			
			if( x > 0 )
			{
				addEdge( Integer.toString( edgeNames++ ),
						nodeName( x-1, currentSize ), id );
				
				if( cross )
				{		
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( x-1, currentSize-1 ), id );
					addEdge( Integer.toString( edgeNames++ ),
							nodeName( x-1, currentSize ), nodeName( x, currentSize-1 ) );
				}
			}
		}
		
		return false;
	}
	
	protected String nodeName( int x, int y )
	{
		return Integer.toString( x ) + "_" + Integer.toString( y );
	}
}