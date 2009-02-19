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
package org.miv.graphstream.algorithm.measure;

import org.miv.graphstream.graph.Edge;
import org.miv.graphstream.graph.Element;
import org.miv.graphstream.graph.Graph;
import org.miv.graphstream.graph.GraphListener;
import org.miv.graphstream.graph.Node;

/**
 * The <b>graph nervousness</b> is a measure that give for each step of a dynamic graph a ratio
 * between the number of structural events ( addition and removal of nodes and edges) and the number
 * of elements (nodes and edges) in the graph.
 * 
 * This measure is different from the Element Nervousness even different from the average element
 * nervousness.
 * 
 * @author Yoann Pigné
 * @since 2008/07/31
 * 
 */
public class GraphNervousness implements GraphListener
{

	Graph graph = null;
	int nbStructuralevents = 0;
	double graphNervousness = 0;

	/**
	 * Default constructor with no graph given. The reference to the graph will be recorded as soon
	 * as a method form the GraphListener interface is called on this class.
	 */
	public GraphNervousness()
	{

	}

	/**
	 * Constructor with a given graph. Any event from another graph from this one will produce an
	 * exception. The method can only work with one graph.
	 * 
	 * @param graph
	 * @throws Exception
	 */
	public GraphNervousness(Graph graph) throws Exception
	{
		initialCondition(graph);
	}

	private void initialCondition(Graph graph)
	{
		if (this.graph == null)
		{
			this.graph = graph;
			graph.addGraphListener(this);

		} else if (this.graph != graph)
			try
			{
				throw new Exception(
						"The GraphNervousness only listen to one graph. Two or more direfent Graph instances are sending events to the algorithm.");
			} catch (Exception e)
			{
				e.printStackTrace();
				System.exit(0);
			}

	}

	public double getGraphNervousness()
	{
		return graphNervousness;
	}

	public void afterEdgeAdd(Graph graph, Edge edge)
	{
		initialCondition(graph);
		nbStructuralevents++;
	}

	public void afterNodeAdd(Graph graph, Node node)
	{
		initialCondition(graph);
		nbStructuralevents++;
	}

	public void attributeChanged(Element element, String attribute, Object oldValue, Object newValue)
	{

	}

	public void beforeEdgeRemove(Graph graph, Edge edge)
	{
		initialCondition(graph);
		nbStructuralevents++;
	}

	public void beforeGraphClear(Graph graph)
	{

	}

	public void beforeNodeRemove(Graph graph, Node node)
	{
		initialCondition(graph);
		nbStructuralevents++;
	}

	public void stepBegins(Graph graph, double time)
	{
		initialCondition(graph);
		if (nbStructuralevents > 0)
		{
			graphNervousness = nbStructuralevents
					/ (double) (graph.getNodeCount() + graph.getEdgeCount());
			nbStructuralevents = 0;
		}
	}

}
