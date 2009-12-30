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
	extends BaseGenerator
{
// Attributes

	protected int nbVertices;
	protected double meanDegreeLimit;
	protected double nervousness;
	protected String graphName;

	protected int step = 1;
	
	protected int deltaStep = 100;
	
	protected int currentNodeId = 0;
	
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
	 * @see #RandomFixedDegreeDynamicGraphGenerator(int, double, double)
	 */
	public RandomFixedDegreeDynamicGraphGenerator()
	{
		this(50,5,0.1f);
	}
	
	/**
	 * Setup the generator but do not generate the graph.
	 * @param nbVertices The number of vertices.
	 * @param meanDegreeLimit The average degree.
	 * @param nervousness The nervousness.
	 */
	public RandomFixedDegreeDynamicGraphGenerator( int nbVertices, double meanDegreeLimit, double nervousness )
	{
		enableKeepNodesId();
		enableKeepEdgesId();
		
		this.nbVertices      = nbVertices;
		this.meanDegreeLimit = meanDegreeLimit;
		this.nervousness     = nervousness;
	}
	
	/**
	 * This method computes the mean degree of the graph.
	 */
	public double meanDegree()
	{
		return 2.0 * edges.size() / (double) nodes.size();
	}
	
	protected String getEdgeId( String src, String trg )
	{
		if( src.compareTo(trg) < 0 )
			return String.format( "%s_%s", src, trg );
		
		return String.format( "%s_%s", trg, src );
	}

	public void begin()
	{
		step = 0;
	}

	public boolean nextElement()
	{
		int nbCreations, nbSuppressions, nbCreationsEdges;
		String dead, source, dest;
		
		sendStepBegins(sourceId,step);
		
		nbSuppressions = (int) ( random.nextFloat() * ( nodes.size() * nervousness ) );
		
		for( int r = 1; r <= nbSuppressions; r++ )
		{
			dead = nodes.get(random.nextInt(nodes.size()));
			delNode(dead);
		}

		nbCreations = (int) ( random.nextFloat() * ( ( nbVertices - nodes.size() )
		        * Math.log( step ) / Math.log( step + deltaStep ) ) );
		
		for( int c = 1; c <= nbCreations; c++ )
		{
			String nodeId = String.format( "%d", currentNodeId++ );
			
			addNode(nodeId);
		}

		double degreMoyen = meanDegree();

		nbCreationsEdges = (int) ( random.nextFloat() * ( ( ( meanDegreeLimit - degreMoyen ) * ( nodes.size() / 2 ) )
		        * Math.log( step ) / Math.log( step + deltaStep ) ) );
		
		if( nodes.size() > 1 )
		{
			for( int c = 1; c <= nbCreationsEdges; c++ )
			{
				do
				{
					source 	= nodes.get(random.nextInt(nodes.size()));
					dest 	= nodes.get(random.nextInt(nodes.size()));
				}
				while( source.equals(dest) );

				String idEdge = getEdgeId( source, dest );

				while( edges.contains(idEdge) || source.equals(dest) )
				{
					dest 	= nodes.get(random.nextInt(nodes.size()));
					idEdge 	= getEdgeId( source, dest );
				}

				addEdge( idEdge, source, dest );
			}
		}
		
		step++;
		
		return false;
	}

	public void end()
	{
		
	}
}