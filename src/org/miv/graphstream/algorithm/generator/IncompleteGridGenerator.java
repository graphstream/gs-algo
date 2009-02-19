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

public class IncompleteGridGenerator {

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

    protected boolean evenementiel;
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

    protected boolean trace = false;
    public    Random alea;

    // Constructors

    public IncompleteGridGenerator(String args[]) {

	dimension = DEFAULT_DIM;
	width = DEFAULT_SIZE;
	height = DEFAULT_SIZE;
	nbObstacles = 1;
	obstacleSize = 5;
	graphName = "";
	type = GRID;
	evenementiel = false;
	startLine = 0;
	startCol = 0;
	alea = new Random();

	if(trace) {
	    for(int i=0;i<args.length;i++) {
		System.out.println("argument "+i+":"+args[i]+"__");
	    }
	    System.exit(0);
	}

	if(args.length >= 5) {
	    width = Integer.parseInt(args[1]);
	    height = Integer.parseInt(args[2]);
	    graphName="grid_"+width+"_"+height;
	    if(args[3].equalsIgnoreCase("tore")) {
		type = TORUS;
		graphName = "torus_"+width+"_"+height;
	    } else {
		if(args[3].equalsIgnoreCase("croix")) {
		    type = CROSS_GRID;
		    graphName = "cross-grid_"+width+"_"+height;
		} else {
		    if(args[3].equalsIgnoreCase("tore-croix")) {
			type = CROSS_TORUS;
			graphName = "cross-torus_"+width+"_"+height;
		    } 
		}
	    }
	    nbObstacles = Integer.parseInt(args[4]);
	    obstacleSize = Integer.parseInt(args[5]);
	} else usage();

	//String gName = new String(dimension+"_"+width+"_"+height);
	generateDGS(dimension,
		    width,
		    height,
		    type,
		    evenementiel,
		    startLine,
		    startCol,
		    graphName);
    }
	

    /**
     * main()
     */
	
    public static void main(String args[]) {
	if(args.length < 2) {
	    usage();
	} else {
	    IncompleteGridGenerator gg = new IncompleteGridGenerator(args);
	}
    }

    /**
     * Méthode 
     */
    public void generateDGS(int dim, int width, int height, int type, boolean event, int sl, int sc, String gName) {
	String intro = "DGS002 \n"+gName+ " 0 0\n"+"nodes x:number y:number \n"+"st 0\n";
	String initNode = "an ";
	String initEdge = "ae ";

	System.out.print(intro);

	if(!event) {
	    for(int l=0;l<height;l++) {
		for(int c=0;c<width;c++) {
		    System.out.println(initNode+"\""+l+"_"+c+"\""+" "+l+" "+c);
		}
	    }
	    // dans tous les cas, il faut mettre les liens pour la grille
	    for(int l=0;l<height;l++) {
		int lplus1 = l+1;
		for(int c=0;c<width;c++) {
		    int cplus1 = c+1;
		    int cmoins1 = c-1;
		    String src = l+"_"+c;
		    String est = l+"_"+cplus1;
		    String sud = lplus1+"_"+c;
		    String sw = lplus1+"_"+cmoins1;
		    String se = lplus1+"_"+cplus1;
		    // dans tous les cas 
		    if(cplus1 < width) {
			System.out.println(initEdge+"\""+src+":"+est+"\""+" "+"\""+src+"\""+" "+"\""+est+"\"");
		    }
		    if(lplus1 < height) {
			System.out.println(initEdge+"\""+src+":"+sud+"\""+" "+"\""+src+"\""+" "+"\""+sud+"\"");
		    }
		    // cas du tore 
		    if((cplus1 == width) && ((type == TORUS) || (type == CROSS_TORUS))) {
			est = l+"_0";
			System.out.println(initEdge+"\""+src+":"+est+"\""+" "+"\""+src+"\""+" "+"\""+est+"\"");
		    }
		    if((lplus1 == height) && ((type == TORUS) || (type == CROSS_TORUS))) {
			sud = "0_"+c;
			System.out.println(initEdge+"\""+src+":"+sud+"\""+" "+"\""+src+"\""+" "+"\""+sud+"\"");
		    }
		    // dans tous les cas de croix 
		    if((type == CROSS_GRID) || (type == CROSS_TORUS)) {
			if((cmoins1 >= 0) && (lplus1 < height)) {
			    System.out.println(initEdge+"\""+src+":"+sw+"\""+" "+"\""+src+"\""+" "+"\""+sw+"\"");
			}
			if((cplus1 < width) && (lplus1 < height)) {
			    System.out.println(initEdge+"\""+src+":"+se+"\""+" "+"\""+src+"\""+" "+"\""+se+"\"");
			}
		    }
                    // dans le cas du tore-croix 
		    if(type == CROSS_TORUS) {
			int ligne = lplus1;
			int col = cmoins1;
			if(cmoins1 < 0) {
			    col = width-1;
			} 
			if(lplus1 == height) {
			    ligne = 0;
			}
			if((cmoins1 < 0) || (lplus1 == height)) {
			    sw = ligne+"_"+col;
			    System.out.println(initEdge+"\""+src+":"+sw+"\""+" "+"\""+src+"\""+" "+"\""+sw+"\"");
			}
			col = cplus1;
			if(cplus1 == width) {
			    col = 0;
			}
			if(lplus1 == height) {
			    ligne = 0;
			}
			if((cplus1 == width) || (lplus1 == height)) {
			    se = ligne+"_"+col;
			    System.out.println(initEdge+"\""+src+":"+se+"\""+" "+"\""+src+"\""+" "+"\""+se+"\"");
			}
		    }
		}
	    }
	} else { // to be done
	}
	
	// add some obstacles ==> remove some nodes
	// starting point randomly chosen as well as length and width
	ArrayList<String> removed = new ArrayList<String>();
	for(int nbObs = 0; nbObs < nbObstacles; nbObs++) {
	    int xstart = alea.nextInt(width);
	    int ystart = alea.nextInt(height);
	    int larg = alea.nextInt(obstacleSize)+1;
	    int lon = alea.nextInt(obstacleSize)+1;
	    for(int i=xstart;i<=xstart+larg;i++) {
		for(int j=ystart;j<=ystart+lon;j++) {
		    if((i < width) && (j < height)) {
			String idfRemNode = new String(i+"_"+j);
			if(!removed.contains(idfRemNode)) {
			    System.out.println("dn "+"\""+i+"_"+j+"\"");
			    removed.add(idfRemNode);
			}
		    }
		}
	    }
	}
    }

    /**
     * Cette méthode créé un graphe qui est une grille ou un tore, qui est statique ou 
     * une suite d'événements de création, et retourne le graphe en question.
     */



    /**
     * This method describes how the class should be used.
     */
    public static void usage() {
	System.out.println("java org.miv.graphstream.algorithm.generator.IncompleteGridGenerator [paramètres]");
	System.out.println("\t\t dimension: dimension de la grille. Cette version n'accepte que la dimension 2.");
	System.out.println("\t\t largeur: largeur de la grille (entier. Par défaut 20)");
	System.out.println("\t\t hauteur: hauteur de la grille (entier. Par défaut 20)");
	System.out.println("\t\t type: type appartient à l'ensemble {grille,tore,croix,tore-croix}");
	System.out.println("\t\t nbObstacles : Nombre d'obstacles");
	System.out.println("\t\t sizeObstacles : taille max des obstacles");
    }

}
