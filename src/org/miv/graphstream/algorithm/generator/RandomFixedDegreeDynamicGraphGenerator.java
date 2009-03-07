/*
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 */

package org.miv.graphstream.algorithm.generator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import static org.miv.graphstream.algorithm.Toolkit.*;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.Node;
import org.miv.graphstream.graph.implementations.SingleGraph;
import org.miv.graphstream.io.DynamicGraphWriterHelper;
import org.miv.util.Environment;

/**
 * This is a graph generator that generates dynamic random graphs.
 * 
 * <u>The principle:</u>
 * 
 * <p>
 * A graph consists in a set of vertices and a set of edges. The dynamic relies on 4 kinds of steps
 * of events: at each step:
 * <ul>
 * 		<li> a subset of nodes is removed</li>
 * 		<li> a subset of nodes is added</li>
 * 		<li> a subset of edges is removed (in the current version, edges that are removed are those
 *			that were attached to nodes that disappear)</li>
 * 		<li> a subset of edges is added</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This generator is characterized by:
 * <ul>
 * 		<li>The parameters:
 * 		<ul>
 * 			<li>number of vertices</li>
 * 			<li>maximum mean degree</li>
 * 		</ul>
 * 		</li>
 * 		<li>The constraints:
 * 		<ul>
 * 			<li>graph nervosity</li>
 * 			<li>creation links rules</li>
 * 		</ul>
 * 		</li>
 * 		<li>The metrics:
 * 		<ul>
 * 			<li>mean number of vertices and edges</li>
 * 			<li>mean age of vertices and edges</li>
 * 			<li>mean distribution of degree</li>
 * 			<li>mean number of connected components</li>
 * 			<li>...</li>
 * 			<li>...</li>
 * 		</ul>
 * 		</li>
 * </ul>
 * </p>
 * 
 * <p>
 * How to build such graphs ? There exist at least one mathematical function for doing that f(step) =
 * nbVertices*log(step)/log(step+"Pente") the larger "Pente", the softer the "pente" of the curve.
 * Given f(step), it is possible to compute nbCreations and nbDeletions together with the graph
 * nervosity. f(step) represents the number of vertices that should be present within the graph
 * given the step and the value of the parameter "Pente". However, as our graph is dynamic, some
 * vertices may be deleted while some other vertices may be added to the graph.
 * 
 * Question: could it be possible to build a dynamic graph that reaches a stable state
 * (stabilization of the number of vertices, and stabilization of some other properties), just by
 * adding some constraints/characteristics on each node?
 * 
 * @author Fr&eacute;d&eacute;ric Guinand
 * @since 20080616
 * @version 20080616
 */
public class RandomFixedDegreeDynamicGraphGenerator
{
// Attributes

	protected Graph dynagraph;
	protected int nbSteps = 1000;
	protected int nbVertices;
	protected double meanDegreeLimit;
	protected double nervousness;
	protected String graphName;

	/**
	 * Constructor that reads its parameters from the Environment and output the graph to a DGS
	 * file.
	 * 
	 * The DGS file name is as follows : "morphoGraph_steps" + nbSteps + "_vertices" + nbVertices
	 * + "_degree" + meanDegreeLimit + "_nervouness" + nervousness.
	 * 
	 * The parameters needed are obtained from the Environment :
	 * <ul>
	 * 		<li>nbVertices : integer</li>
	 * 		<li>meanDegreeLimit : double</li>
	 * 		<li>nervousness : double</li>
	 * 		<li>nbSteps : integer</li>
	 * </ul>
	 *  
	 * @throws IOException If an error occurs during output.
	 * @throws IllegalArgumentException If one of the parameters if not given or is zero.
	 * @see #RandomFixedDegreeDynamicGraphGenerator(String)
	 */
	public RandomFixedDegreeDynamicGraphGenerator() throws IOException
	{
		this( null );
	}
	
	/**
	 * Like {@link #RandomFixedDegreeDynamicGraphGenerator()} but specifies the graph output file
	 * name.
	 * @param fileName The output file name for the DGS file.
	 * @throws IOException If an error occurs during output.
	 * @throws IllegalArgumentException If one of the parameters if not given or is zero.
	 */
	public RandomFixedDegreeDynamicGraphGenerator( String fileName ) throws IOException
	{
		Environment env = Environment.getGlobalEnvironment();
		nbVertices = (int) env.getNumberParameter( "nbVertices" );
		meanDegreeLimit = env.getNumberParameter( "meanDegreeLimit" );
		nervousness = env.getNumberParameter( "nervousness" );
		nbSteps = (int) env.getNumberParameter( "nbSteps" );
		
		if( nbVertices == 0 || meanDegreeLimit == 0 || nervousness == 0 || nbSteps == 0 )
			throw new IllegalArgumentException( usage );
		
		generateGraph();
	}
	
	/**
	 * Setup the generator but do not generate the graph. You can generate the graph using
	 * {@link #generate(Graph)} or generate the graph and output it in DGS using {@link #generateGraph()}.
	 * @param nbVertices The number of vertices.
	 * @param meanDegreeLimit The average degree.
	 * @param nervousness The nervousness.
	 * @param nbSteps The number of steps.
	 */
	public RandomFixedDegreeDynamicGraphGenerator( int nbVertices, double meanDegreeLimit, double nervousness, int nbSteps )
	{
		this.nbVertices      = nbVertices;
		this.meanDegreeLimit = meanDegreeLimit;
		this.nervousness     = nervousness;
		this.nbSteps         = nbSteps;
	}

	/**
	 * Generate the graph and output it to a DGS file.
	 * 
	 * The DGS file name is as follows : "morphoGraph_steps" + nbSteps + "_vertices" + nbVertices
	 * + "_degree" + meanDegreeLimit + "_nervouness" + nervousness.
	 * 
	 * @throws IOException If an error occurs during file output.
	 */
	public void generateGraph() throws IOException
	{
		graphName = "morphoGraph_steps" + nbSteps + "_vertices" + nbVertices + "_degree"
		        + meanDegreeLimit + "_nervousness" + nervousness;
		
		generateGraph( graphName );
	}
	
	/**
	 * Same as {@link #generateGraph()} but specifies the graph output file name. The graph will
	 * be output in DGS format.
	 * @param graphName The graph output file name.
	 */
	public void generateGraph( String graphName ) throws IOException
	{
		dynagraph = new SingleGraph( graphName );
		DynamicGraphWriterHelper dgwh = new DynamicGraphWriterHelper();
		dgwh.begin( dynagraph, graphName + ".dgs" );
		generate( dynagraph );
		dgwh.end();
	}
	
	@SuppressWarnings("all")
	public void generate( Graph graph )
	{
		dynagraph = graph;
		
		int numeroSommet = 0;
		int numeroLien = 0;
		Node dead, source, dest;
		Edge newEdge;
		int nbCreations, nbSuppressions, nbCreationsEdges, age;

		// pour l'ensemble des étapes, on créé des sommets et des liens
		for( int s = 1; s < nbSteps; s++ )
		{

			// Graph Writer
			dynagraph.stepBegins( s );
			// -----------------

			nbSuppressions = (int) ( Math.random() * ( dynagraph.getNodeCount() * nervousness ) );
			for( int r = 1; r <= nbSuppressions; r++ )
			{
				dead = (Node) ( dynagraph.getNodeSet().toArray()[(int) ( Math.random() * dynagraph
				        .getNodeCount() )] );
				age = s - (Integer) dead.getAttribute( "birth" );
				System.out.println( "node age = " + age );
				// avant de flinguer le noeud, il faut lister ses arêtes de façon
				// à récupérer leur âge.
				for( Edge e : dead.getEdgeSet() )
				{
					age = s - (Integer) e.getAttribute( "birth" );
					System.out.println( "edge age = " + age );
				}
				dynagraph.removeNode( dead.getId() );
			}

			// la vitesse de création des sommets initiaux dépend de la pente ici 10000
			nbCreations = (int) ( Math.random() * ( ( nbVertices - dynagraph.getNodeCount() )
			        * Math.log( s ) / Math.log( s + 10000 ) ) );
			for( int c = 1; c <= nbCreations; c++ )
			{
				numeroSommet = numeroSommet + 1;
				String nomSommet = new String( "v_" + numeroSommet );
				dynagraph.addNode( nomSommet );
				Node newNode = dynagraph.getNode( nomSommet );
				newNode.addAttribute( "birth", new Integer( s ) );
			}

			// on s'occupe maintenant des liens, toujours avec la nervosity
			// la limite maximale du degré moyen est donnée par l'utilisateur, c'est
			// un parametre.
			double degreMoyen = meanDegree();

			nbCreationsEdges = (int) ( Math.random() * ( ( ( meanDegreeLimit - degreMoyen ) * ( dynagraph
			        .getNodeCount() / 2 ) )
			        * Math.log( s ) / Math.log( s + 10000 ) ) );
			for( int c = 1; c <= nbCreationsEdges; c++ )
			{
				numeroLien = numeroLien + 1;
				source = (Node) ( dynagraph.getNodeSet().toArray()[(int) ( Math.random() * dynagraph
				        .getNodeCount() )] );
				dest = (Node) ( dynagraph.getNodeSet().toArray()[(int) ( Math.random() * dynagraph
				        .getNodeCount() )] );
				String idEdge = source.getId() + "__" + dest.getId();
				boolean found = ( ( dynagraph.getEdge( idEdge ) == null ) && ( source.getId() != dest
				        .getId() ) );
				while( !found )
				{
					dest = (Node) ( dynagraph.getNodeSet().toArray()[(int) ( Math.random() * dynagraph
					        .getNodeCount() )] );
					idEdge = source.getId() + "__" + dest.getId();
					if( ( dynagraph.getEdge( idEdge ) == null )
					        && ( source.getId() != dest.getId() ) )
					{
						found = true;
					}
				}
				newEdge = dynagraph.addEdge( idEdge, source.getId(), dest.getId() );
				newEdge.addAttribute( "birth", new Integer( s ) );
			}
			displayMeasures( nbCreations, nbSuppressions, nbCreationsEdges );
		}
	}

	public void displayMeasures( int nbC, int nbS, int nbCE )
	{
		System.out.print( "Creations:: " + nbC + " Suppressions:: " + nbS + " Edges:: " + nbCE
		        + " --> " );
		System.out.println( "Total:: " + dynagraph.getNodeCount() );
		System.out.println( "MeanDegree:: " + meanDegree() );
		displayDistribution();
	}

	public void displayDistribution()
	{
//		int[] distrib = dynagraph.getDegreeDistribution();
		int[] distrib = degreeDistribution( dynagraph );
		System.out.print( "Distribution:: " );
		for( int d = 0; d < distrib.length; d++ )
		{
			System.out.print( distrib[d] + " " );
		}
		for( int d = distrib.length; d < nbVertices; d++ )
		{
			System.out.print( 0 + " " );
		}
		System.out.println();
	}

	/**
	 * This method computes the mean degree of the graph.
	 */
	@SuppressWarnings("all")
	public double meanDegree()
	{
		int total = 0;
		double deg = 0;
		if( dynagraph.getNodeCount() != 0 )
		{
			for( Node n : dynagraph.getNodeSet() )
			{
				total = total + n.getDegree();
			}
			deg = (double) total / dynagraph.getNodeCount();
		}
		return deg;
	}

	/**
	 * Dynamic edge generator.
	 */
	@SuppressWarnings("all")
	public void generateDGSvertices()
	{

		graphName = "morphoGraph_" + nbVertices + "_" + meanDegreeLimit;

		String intro = "DGS002 \n" + graphName + " 0 0\n" + "st 0\n";
		String initNode = "an ";
		String initEdge = "ae ";

		System.out.print( "dgs " + intro );

		int nbSommets = 0;
		int numeroSommet = 0;
		int nbCreations, nbSuppressions;
		Integer leSommet;
		ArrayList<Integer> vertices = new ArrayList<Integer>();
		Hashtable<Integer,Integer> naissances = new Hashtable<Integer,Integer>();

		for( int s = 1; s < nbSteps; s++ )
		{
			nbSuppressions = (int) ( Math.random() * ( nbSommets * nervousness ) );
			for( int r = 1; r <= nbSuppressions; r++ )
			{
				int sommetSupprime = (int) ( Math.random() * vertices.size() );
				leSommet = vertices.get( sommetSupprime );
				vertices.remove( sommetSupprime );
				int age = s - naissances.get( leSommet );
				System.out.println( "age de v_" + leSommet + " = " + age );
				System.out.println( "dgs dn v_" + leSommet );
			}
			// la vitesse de création des sommets initiaux dépend de la pente ici 10000
			nbCreations = (int) ( Math.random() * ( ( nbVertices - nbSommets ) * Math.log( s ) / Math
			        .log( s + 10000 ) ) );
			for( int c = 1; c <= nbCreations; c++ )
			{
				numeroSommet = numeroSommet + 1;
				System.out.println( "dgs an v_" + numeroSommet );
				vertices.add( new Integer( numeroSommet ) );
				naissances.put( new Integer( numeroSommet ), new Integer( s ) );
			}
			nbSommets = nbSommets + nbCreations - nbSuppressions;
			System.out.print( "Creations:: " + nbCreations + " Suppressions:: " + nbSuppressions
			        + " --> " );
			System.out.println( "Total:: " + nbSommets );
			System.out.println( "dgs st " + s );
		}
	}

	/**
	 * Allow to test the generator.
	 */
	public static void main( String args[] )
	{

		org.miv.util.Environment.getGlobalEnvironment().readCommandLine( args );
		try
		{
			new RandomFixedDegreeDynamicGraphGenerator();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}

	}

	/**
	 * This method describes how the class should be used.
	 */
	public static String usage = "\nRandomFixedDegreeDynamicGraphGenerator \n"
	        + "\t\t-nbVertices=<nombre de sommets (entier)>\n"
	        + "\t\t-meanDegreeLimit=<degré moyen max (réel)>\n"
	        + "\t\t-nervousness=<nervosité (réel)>\n" + "\t\t-nbSteps=<nombre d'étapes (entier)>\n";
}