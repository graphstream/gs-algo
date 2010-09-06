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

import org.graphstream.graph.*;

/**
 * This interface defines algorithms which can be run on a graph.
 * Such algorithms are divided in two step :
 * <ol>
 * <li>an initialization step</li>
 * <li>a computing step</li>
 * </ol>
 * 
 * Complexity of algorithms can be specify in the documentation with the
 * help of the "@complexity" tag.
 */
public interface Algorithm
{
	/**
	 * Initialization of the algorithm.
	 * @param graph The graph this algorithm is using.
	 */
	void init( Graph graph );

	/**
	 * Launch the algorithm on the previously specified graph.
	 * @see #init(Graph)
	 */
	void compute();
}