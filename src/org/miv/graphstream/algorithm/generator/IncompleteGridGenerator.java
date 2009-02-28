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

package org.miv.graphstream.algorithm.generator;

import java.util.ArrayList;
import java.util.Random;

import org.miv.graphstream.graph.Graph;

/**
 * This class allow the user to generate 2D-grid graph into the dgs format
 * 
 * <p>
 * This is a very simple graph generator that generates a grid of size nXm.
 * However, four different versions are possible:
 * <ol>
 * <li> The simple 2D-grid, in such a case, two parameters (width and height) are enough
 * <li> The 2D-Cross-Grid, in which inner nodes have 8 neighbors (North, South, East, West, 
 *      North-West, North-East, South-West and South-East).
 * <li> The 2D-Torus, in which all nodes have 4 neighbors, and,
 * <li> The 2D-Cross-Torus, in which all nodes have 8 neighbors.
 * </ol>
 * </p>
 * 
 * @author Fr&eacute;d&eacute;ric Guinand
 * @since  20070722
 * @version 20080524
 */

public class IncompleteGridGenerator
	implements Generator
{

// Attributes
	
    /** 
     * Some constant integer values
     */
    final static int DEFAULT_DIM = 2;
    final static int DEFAULT_SIZE = 20;
    final static int GRID = 0;
    final static int TORUS = 1;
    final static int CROSS_GRID = 2;
    final static int CROSS_TORUS = 3;

    /**
     * The graph to grow.
     */
    protected Graph graph;
    
    /**
     * dimension of the grid
     */
    protected int dimension;
    protected int type;

    protected int startLine;
    protected int startCol;
    /**
     * The sizes
     */
    protected int width;
    protected int height;
    protected int nbObstacles;
    protected int obstacleSize;
    protected String graphName;

    protected Random alea;
    protected boolean generateDGS = false;

    // Constructors

    /**
     * Default constructor.
     */
    public IncompleteGridGenerator()
    {
    	this( DEFAULT_DIM, DEFAULT_SIZE, DEFAULT_SIZE, false, false, 1, 5 );
    }
    /**
     * Build a new IncompleteGridGenerator.
     * 
     * @param dimension
     * @param width
     * @param height
     * @param cross
     * @param torus
     * @param nbObstacles
     * @param obstaclesSize
     */
    public IncompleteGridGenerator( int dimension, int width, int height,
    		boolean cross, boolean torus, int nbObstacles, int obstaclesSize )
    {
    	this.dimension 		= dimension;
    	this.width			= width;
    	this.height			= height;
    	this.nbObstacles 	= nbObstacles;
    	this.obstacleSize	= obstaclesSize;
    	this.startLine		= 0;
    	this.startCol		= 0;
    	this.graphName		= String.format( "incomplete-grid-%dx%d", width, height );
    	this.alea			= new Random();
    	
    	if( cross && torus )
    		type = CROSS_TORUS;
    	else if( cross )
    		type = CROSS_GRID;
    	else if( torus )
    		type = TORUS;
    	else
    		type = GRID;
    }
    /**
     * @see org.miv.graphstream.algorithm.generator.Generator
     */
    public void begin( Graph graph )
    {
    	if( graph == null )
    		generateDGS = true;
    	
    	if( generateDGS )
    		System.out.printf( "DGS002 \n%s 0 0\n" +
    				"nodes x:number y:number \n" +
    				"st 0\n", graphName );
    	
    	this.graph = graph;
    }
    /**
     * @see org.miv.graphstream.algorithm.generator.Generator
     */
    public boolean nextElement()
    		//int dim, int width, int height, int type,boolean event, int sl, int sc, String gName)
    {
    	for(int l=0;l<height;l++)
    		for(int c=0;c<width;c++)
    			addNode( l + "_" + c, l, c );

    	// dans tous les cas, il faut mettre les liens pour la grille
    	for(int l=0;l<height;l++)
    	{
    		int lplus1 = l+1;

    		for(int c=0;c<width;c++)
    		{
    			int cplus1 = c+1;
    			int cmoins1 = c-1;
    			String src = l+"_"+c;
    			String est = l+"_"+cplus1;
    			String sud = lplus1+"_"+c;
    			String sw = lplus1+"_"+cmoins1;
    			String se = lplus1+"_"+cplus1;
    			// dans tous les cas 

    			if(cplus1 < width)
    				addEdge( src + ":" + est, src, est );

    			if(lplus1 < height)
    				addEdge( src + ":" + sud, src, sud );

    			// cas du tore 
    			if((cplus1 == width) && ((type == TORUS) || (type == CROSS_TORUS)))
    			{
    				est = l+"_0";
    				addEdge( src + ":" + est, src, est );
    			}

    			if((lplus1 == height) && ((type == TORUS) || (type == CROSS_TORUS)))
    			{
    				sud = "0_"+c;
    				addEdge( src + ":" + sud, src, sud );
    			}

    			// dans tous les cas de croix 
    			if((type == CROSS_GRID) || (type == CROSS_TORUS))
    			{
    				if((cmoins1 >= 0) && (lplus1 < height))
    					addEdge( src + ":" + sw, src, sw );

    				if((cplus1 < width) && (lplus1 < height))
    					addEdge( src + ":" + se, src, se );
    			}

    			// dans le cas du tore-croix 
    			if(type == CROSS_TORUS)
    			{
    				int ligne = lplus1;
    				int col = cmoins1;
    				if(cmoins1 < 0)
    					col = width-1;

    				if(lplus1 == height)
    					ligne = 0;

    				if((cmoins1 < 0) || (lplus1 == height))
    				{
    					sw = ligne+"_"+col;
    					addEdge( src + ":" + sw, src, sw );
    				}

    				col = cplus1;
    				if(cplus1 == width)
    					col = 0;

    				if(lplus1 == height)
    					ligne = 0;

    				if((cplus1 == width) || (lplus1 == height))
    				{
    					se = ligne+"_"+col;
    					addEdge( src + ":" + se, src, se );
    				}
    			}
    		}
    	}
	
	// add some obstacles ==> remove some nodes
	// starting point randomly chosen as well as length and width
    	ArrayList<String> removed = new ArrayList<String>();
    	
    	for(int nbObs = 0; nbObs < nbObstacles; nbObs++)
    	{
    		int xstart = alea.nextInt(width);
    		int ystart = alea.nextInt(height);
    		int larg = alea.nextInt(obstacleSize)+1;
    		int lon = alea.nextInt(obstacleSize)+1;
    		for(int i=xstart;i<=xstart+larg;i++)
    		{
    			for(int j=ystart;j<=ystart+lon;j++)
    			{
    				if((i < width) && (j < height))
    				{
    					String idfRemNode = String.format( "%d_%d", i, j);
    					if(!removed.contains(idfRemNode))
    					{
    						delNode( idfRemNode );
    						removed.add(idfRemNode);
    					}
    				}
    			}
    		}
    	}
    	
    	return false;
    }
    /**
     * @see org.miv.graphstream.algorithm.generator.Generator
     */
    public void end()
    {
    	
    }
    /**
     * Method used to create a node.
     * @param id
     * @param x
     * @param y
     */
    protected void addNode( String id, int x, int y )
    {
    	if( generateDGS )
    		System.out.printf( "an \"%s\" %d %d\n", id, x, y );
    	else
    		graph.addNode(id).addAttribute( "xy", x, y );
    }
    /**
     * Method used to delete a node.
     * @param id
     */
    protected void delNode( String id )
    {
    	if( generateDGS )
    		System.out.printf( "dn \"%s\"\n", id );
    	else
    		graph.removeNode(id);
    }
    /**
     * Method used to create an edge.
     * @param id
     * @param src
     * @param trg
     */
    protected void addEdge( String id, String src, String trg )
    {
    	if( generateDGS )
    		System.out.printf( "ae \"%s\" \"%s\" \"%s\"\n", id, src, trg );
    	else
    		graph.addEdge( id, src, trg );
    }
    /**
     * This method describes how the class should be used.
     */
    public static void usage()
    {
    	System.out.println("java org.miv.graphstream.algorithm.generator.IncompleteGridGenerator [paramètres]");
    	System.out.println("\t\t dimension: dimension de la grille. Cette version n'accepte que la dimension 2.");
    	System.out.println("\t\t largeur: largeur de la grille (entier. Par défaut 20)");
    	System.out.println("\t\t hauteur: hauteur de la grille (entier. Par défaut 20)");
    	System.out.println("\t\t type: type appartient à l'ensemble {grille,tore,croix,tore-croix}");
    	System.out.println("\t\t nbObstacles : Nombre d'obstacles");
    	System.out.println("\t\t sizeObstacles : taille max des obstacles");
    }
    /**
     * Main method used to generate DGS output.
     */
    public static void main( String args[] )
    {
    	if(args.length >= 5)
    	{
    		int width = Integer.parseInt(args[1]);
    		int height = Integer.parseInt(args[2]);
    		boolean tore	= false;
    		boolean cross	= false;
    		
    		if(args[3].equalsIgnoreCase("tore"))
    		{
    			tore = true;
    			cross = false;
    		}
    		else
    		{
    			if(args[3].equalsIgnoreCase("croix"))
    			{
    				cross	= true;
    				tore	= false;
    			} 
    			else
    			{
    				if(args[3].equalsIgnoreCase("tore-croix"))
    				{
    					cross	= true;
    					tore	= true;
    				} 
    			}
    		}
    		
    		int nbObstacles = Integer.parseInt(args[4]);
    		int obstacleSize = Integer.parseInt(args[5]);
    		
    		Generator gen = new IncompleteGridGenerator(2,width,height,tore,cross,
    				nbObstacles,obstacleSize);
    		
    		gen.begin(null);
    		gen.nextElement();
    		gen.end();
    	} 
    	else
    	{
    		usage();
    	}
    }
}
