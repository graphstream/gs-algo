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
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;

/**
 * <p>
 * The Bellman-Ford algorithm computes single-source shortest paths in a
 * weighted digraph (where some of the edge weights may be negative). Dijkstra's
 * algorithm accomplishes the same problem with a lower running time, but
 * requires edge weights to be non-negative. Thus, Bellman-Ford is usually used
 * only when there are negative edge weights (from the <a
 * href="http://en.wikipedia.org/wiki/Bellman-Ford_algorithm">Wikipedia</a>).
 * </p>
 * 
 * <h3>Warning</h3>
 * <p>
 * For the moment only attributes located on the edges are supported.
 * </p>
 * 
 * @complexity O(VxE) time, where V and E are the number of vertices and edges
 *             respectively.
 * 
 * @author Antoine Dutot
 * @author Yoann Pigné
 * 
 */
public class BellmanFord implements Algorithm {

	/**
	 * The graph to be computed for shortest path.
	 */
	protected Graph graph;

	/**
	 * ID of the source node.
	 */
	protected String source;

	/**
	 * Name of attribute used to get weight of edges.
	 */
	protected String weightAttribute;

	/**
	 * Build a new BellmanFord algorithm giving the name of the weight attribute
	 * for edges.
	 * 
	 * @param attribute
	 *            weight attribute of edges
	 */
	public BellmanFord(String attribute) {
		this(attribute, null);
	}

	/**
	 * Same that {@link #BellmanFord(String)} but setting the id of the source
	 * node.
	 * 
	 * @param attribute
	 *            weight attribute of edges
	 * @param sourceNode
	 *            id of the source node
	 */
	public BellmanFord(String attribute, String sourceNode) {
		this.source = sourceNode;
		this.weightAttribute = attribute;
	}

	/**
	 * Set the id of the node used as source.
	 * 
	 * @param nodeId
	 *            id of the source node
	 */
	public void setSource(String nodeId) {
		this.source = nodeId;
	}

	/**
	 * Get the id of node used as source.
	 * 
	 * @return id of the source node
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Constructs all the possible shortest paths from the source node to the
	 * destination (end). Warning: this construction is VERY HEAVY !
	 * 
	 * @param end
	 *            The destination to which shortest paths are computed.
	 * @return a list of shortest paths given with
	 *         {@link org.graphstream.graph.Path} objects.
	 */
	public List<Path> getPathSetShortestPaths(Node end) {
		ArrayList<Path> paths = new ArrayList<Path>();
		pathSetShortestPath_facilitate(end, new Path(), paths);
		return paths;
	}

	@SuppressWarnings("unchecked")
	private void pathSetShortestPath_facilitate(Node current, Path path,
			List<Path> paths) {
		Node source = graph.getNode(this.source);

		if (current != source) {
			Node next = null;
			ArrayList<? extends Edge> predecessors = (ArrayList<? extends Edge>) current
					.getAttribute("BellmanFord.predecessors");
			while (current != source && predecessors.size() == 1) {
				Edge e = predecessors.get(0);
				next = e.getOpposite(current);
				path.add(current, e);
				current = next;
				predecessors = (ArrayList<? extends Edge>) current
						.getAttribute("BellmanFord.predecessors");
			}
			if (current != source) {
				for (Edge e : predecessors) {
					Path p = path.getACopy();
					p.add(current, e);
					pathSetShortestPath_facilitate(e.getOpposite(current), p,
							paths);

				}
			}
		}
		if (current == source) {
			paths.add(path);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	@SuppressWarnings("unchecked")
	public void compute() {
		Node source = graph.getNode(this.source);

		// Step 1: Initialize graph

		for (Node n : graph) {
			if (n == source)
				n.addAttribute("BellmanFord.distance", 0.0);
			else
				n.addAttribute("BellmanFord.distance");

			n.addAttribute("BellmanFord.predecessors");
		}

		// Step 2: relax edges repeatedly

		for (int i = 0; i < graph.getNodeCount(); i++) {
			for (Edge e : graph.getEachEdge()) {
				Node n0 = e.getNode0();
				Node n1 = e.getNode1();
				Double d0 = (Double) n0.getAttribute("BellmanFord.distance");
				Double d1 = (Double) n1.getAttribute("BellmanFord.distance");

				Double we = (Double) e.getAttribute(weightAttribute);
				if (we == null)
					throw new NumberFormatException(
							"org.miv.graphstream.algorithm.BellmanFord: Problem with attribute \""
									+ weightAttribute + "\" on edge " + e);

				if (d0 != null) {
					if (d1 == null || d1 >= d0 + we) {
						n1.addAttribute("BellmanFord.distance", d0 + we);
						ArrayList<Edge> predecessors = (ArrayList<Edge>) n1
								.getAttribute("BellmanFord.predecessors");

						if (d1 != null && d1 == d0 + we) {
							if (predecessors == null) {
								predecessors = new ArrayList<Edge>();
							}
						} else {
							predecessors = new ArrayList<Edge>();
						}
						if (!predecessors.contains(e)) {
							predecessors.add(e);
						}

						n1.addAttribute("BellmanFord.predecessors",
								predecessors);
					}
				}
			}
		}

		// Step 3: check for negative-weight cycles

		for (Edge e : graph.getEachEdge()) {
			Node n0 = e.getNode0();
			Node n1 = e.getNode1();
			Double d0 = (Double) n0.getAttribute("BellmanFord.distance");
			Double d1 = (Double) n1.getAttribute("BellmanFord.distance");

			Double we = (Double) e.getAttribute(weightAttribute);

			if (we == null) {
				throw new NumberFormatException(
						String.format(
								"%s: Problem with attribute \"%s\" on edge \"%s\"",
								BellmanFord.class.getName(), weightAttribute,
								e.getId()));
			}

			if (d1 > d0 + we) {
				throw new NumberFormatException(
						String.format(
								"%s: Problem: negative weight, cycle detected on edge \"%s\"",
								BellmanFord.class.getName(), e.getId()));
			}
		}
	}
}
