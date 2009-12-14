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
 * A grid generator with holes.
 * 
 * @author Guilhelm Savin
 */

public class IncompleteGridGenerator
	extends BaseGenerator
{

	// Attributes
    
	protected int 		currentWidth 	= 0;
    protected int 		currentHeight 	= 0;
    protected float 	holeProbability = 0.5f;
    protected int 		holeMaxSize 	= 5;
    protected int 		holesPerStep 	= 3;
    protected boolean 	cross 			= true;
	
    // Constructors

    /**
     * Default constructor.
     */
    public IncompleteGridGenerator()
    {
    	this( true, 0.5f, 5, 3 );
    }
    
	public IncompleteGridGenerator(boolean cross, float holeProbability,
			int holeMaxSize, int holesPerStep)
    {
    	enableKeepNodesId();   
    	
    	this.cross 				= cross;
    	this.holeProbability 	= holeProbability;
    	this.holeMaxSize 		= holeMaxSize;
    	this.holesPerStep 		= holesPerStep;
    }
    
    /**
     * @see org.graphstream.algorithm.generator.Generator
     */
    public void begin()
    {
    	
    }
    
    protected String getNodeId( int x, int y )
    {
    	return String.format("%d_%d",x,y);
    }
    
    protected String getEdgeId( String n1, String n2 )
    {
    	if( n1.compareTo(n2) < 0 )
    	{
    		String tmp = n2;
    		n2 = n1;
    		n1 = tmp;
    	}
    	
    	return String.format("%s-%s", n1, n2 );
    }
    
    protected void connectNode( int x, int y )
    {
    	String nodeId = getNodeId(x,y);
    	String neigh;
    	
    	if( x > 0 )
    	{
    		neigh = getNodeId(x-1,y);

    		if( nodes.contains(neigh) )
    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    	}

    	if( x < currentWidth - 1 )
    	{
    		neigh = getNodeId(x+1,y);

    		if( nodes.contains(neigh) )
    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    	}
    	
    	if( y > 0 )
    	{
    		neigh = getNodeId(x,y-1);

    		if( nodes.contains(neigh) )
    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    	}

    	if( y < currentHeight - 1 )
    	{
    		neigh = getNodeId(x,y+1);

    		if( nodes.contains(neigh) )
    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    	}
    	
    	if( cross )
    	{
    		if( x > 0 )
    		{
    			if( y > 0 )
    			{
    				neigh = getNodeId(x-1,y-1);

    	    		if( nodes.contains(neigh) )
    	    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    			}
    			
    			if( y < currentHeight - 1 )
    			{
    				neigh = getNodeId(x-1,y+1);

    	    		if( nodes.contains(neigh) )
    	    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    			}
    		}
    		
    		if( x < currentWidth - 1 )
    		{
    			if( y > 0 )
    			{
    				neigh = getNodeId(x+1,y-1);

    	    		if( nodes.contains(neigh) )
    	    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    			}
    			
    			if( y < currentHeight - 1 )
    			{
    				neigh = getNodeId(x+1,y+1);

    	    		if( nodes.contains(neigh) )
    	    			addEdge(getEdgeId(nodeId,neigh),nodeId,neigh);
    			}
    		}
    	}
    }
    
    protected void disconnectNode( int x, int y )
    {
    	String nodeId = getNodeId(x,y);
    	String neigh;
    	
    	if( x > 0 )
    	{
    		neigh = getNodeId(x-1,y);

    		if( nodes.contains(neigh) )
    			delEdge(getEdgeId(nodeId,neigh));
    	}

    	if( x < currentWidth - 1 )
    	{
    		neigh = getNodeId(x+1,y);

    		if( nodes.contains(neigh) )
    			delEdge(getEdgeId(nodeId,neigh));
    	}
    	
    	if( y > 0 )
    	{
    		neigh = getNodeId(x,y-1);

    		if( nodes.contains(neigh) )
    			delEdge(getEdgeId(nodeId,neigh));
    	}

    	if( y < currentHeight - 1 )
    	{
    		neigh = getNodeId(x,y+1);

    		if( nodes.contains(neigh) )
    			delEdge(getEdgeId(nodeId,neigh));
    	}
    	
    	if( cross )
    	{
    		if( x > 0 )
    		{
    			if( y > 0 )
    			{
    				neigh = getNodeId(x-1,y-1);

    	    		if( nodes.contains(neigh) )
    	    			delEdge(getEdgeId(nodeId,neigh));
    			}
    			
    			if( y < currentHeight - 1 )
    			{
    				neigh = getNodeId(x-1,y+1);

    	    		if( nodes.contains(neigh) )
    	    			delEdge(getEdgeId(nodeId,neigh));
    			}
    		}
    		
    		if( x < currentWidth - 1 )
    		{
    			if( y > 0 )
    			{
    				neigh = getNodeId(x+1,y-1);

    	    		if( nodes.contains(neigh) )
    	    			delEdge(getEdgeId(nodeId,neigh));
    			}
    			
    			if( y < currentHeight - 1 )
    			{
    				neigh = getNodeId(x+1,y+1);

    	    		if( nodes.contains(neigh) )
    	    			delEdge(getEdgeId(nodeId,neigh));
    			}
    		}
    	}
    }
    
    /**
     * @see org.graphstream.algorithm.generator.Generator
     */
    public boolean nextElement()
    {
    	for( int i = 0; i < currentWidth; i++ )
    	{
    		addNode( getNodeId(i,currentHeight) );
    		connectNode(i,currentHeight);
    	}
    
    	for( int i = 0; i < currentHeight; i++ )
    	{
    		addNode( getNodeId(currentWidth,i) );
    		connectNode(currentWidth,i);
    	}
    	
    	addNode( getNodeId(currentWidth,currentHeight) );
    	connectNode(currentWidth,currentHeight);
    	
    	currentWidth++;
    	currentHeight++;
    	
    	for( int k = 0; k < holesPerStep; k++ )
    	{
    		if( random.nextFloat() < holeProbability )
    		{
    			int x1, y1;
    			int sizeX, sizeY;

    			do
    			{
    				x1 = random.nextInt(currentWidth);
    				y1 = random.nextInt(currentHeight);
    			}
    			while( ! nodes.contains(getNodeId(x1,y1)) );

    			sizeX = random.nextInt(holeMaxSize);
    			sizeY = random.nextInt(holeMaxSize-sizeX);

    			for( int i = 0; i < sizeX; i++ )
    				for( int j = 0; j < sizeY; j++ )
    				{
    					if( nodes.contains(getNodeId(x1+i,y1+j)) )
    					{
    						disconnectNode(x1+i,y1+j);
    						delNode(getNodeId(x1+i,y1+j));
    					}
    				}
    		}
    	}
    	
    	return false;
    }
    
    /**
     * @see org.graphstream.algorithm.generator.Generator
     */
    public void end()
    {
    	
    }
}
