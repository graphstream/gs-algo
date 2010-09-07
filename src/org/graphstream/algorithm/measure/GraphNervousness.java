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
package org.graphstream.algorithm.measure;

import org.graphstream.algorithm.Algorithm;
import org.graphstream.graph.Graph;
import org.graphstream.stream.ElementSink;

/**
 * The <b>graph nervousness</b> is a measure that give for each step of a
 * dynamic graph a ratio between the number of structural events ( addition and
 * removal of nodes and edges) and the number of elements (nodes and edges) in
 * the graph.
 * 
 * This measure is different from the Element Nervousness even different from
 * the average element nervousness.
 */
public class GraphNervousness
	implements Algorithm, ElementSink
{
	/**
	 * The working graph.
	 */
	protected Graph graph = null;
	
	/**
	 * Number of structural events (add/remove node/edge).
	 */
	protected int nbStructuralevents = 0;
	
	/**
	 * The computed graph nervousness.
	 */
	protected double graphNervousness = 0;

	/**
	 * Default constructor.
	 */
	public GraphNervousness()
	{

	}

	private void initialCondition(Graph graph)
	{
		if( this.graph != null )
			this.graph.removeElementSink(this);

		this.nbStructuralevents = 0;
		this.graphNervousness = 0;
		this.graph = graph;
		graph.addElementSink(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init( Graph graph )
	{
		initialCondition(graph);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute()
	{
		
	}
	
	/**
	 * Get the computed graph nervousness.
	 * 
	 * @return graph nervousness
	 */
	public double getGraphNervousness()
	{
		return graphNervousness;
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.ElementSink#edgeAdded(java.lang.String, long, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public void edgeAdded( String graphId, long timeId, String edgeId,
			String fromNodeId, String toNodeId, boolean directed )
    {
		nbStructuralevents++;
    }

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.ElementSink#edgeRemoved(java.lang.String, long, java.lang.String)
	 */
	public void edgeRemoved( String graphId, long timeId, String edgeId )
    {
		nbStructuralevents++;
    }

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.ElementSink#nodeAdded(java.lang.String, long, java.lang.String)
	 */
	public void nodeAdded( String graphId, long timeId, String nodeId )
    {
		nbStructuralevents++;
    }

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.ElementSink#nodeRemoved(java.lang.String, long, java.lang.String)
	 */
	public void nodeRemoved( String graphId, long timeId, String nodeId )
    {
		nbStructuralevents++;
    }
	
	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.ElementSink#graphCleared(java.lang.String, long)
	 */
	public void graphCleared( String graphId, long timeId )
	{
		nbStructuralevents++;
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.ElementSink#stepBegins(java.lang.String, long, double)
	 */
	public void stepBegins( String graphId, long timeId, double time )
    {
		if (nbStructuralevents > 0)
		{
			graphNervousness = nbStructuralevents
					/ (double) (graph.getNodeCount() + graph.getEdgeCount());
			nbStructuralevents = 0;
		}
    }
}