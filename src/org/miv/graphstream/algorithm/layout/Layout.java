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
package org.miv.graphstream.algorithm.layout;

import org.miv.util.*;
import org.miv.util.geom.*;

/**
 * Layout algorithm interface.
 * 
 * <p>
 * The layout algorithm role is to compute the best possible positions of nodes
 * in a given space (2D or 3D). As there are many such algorithms with distinct
 * qualities and uses, this interface defines what is awaited from a layout
 * algorithm.
 * </p>
 * 
 * <p>
 * The algorithm takes a graph as input. However, at the contrary of several
 * other algorithms, it does not work on the graph itself. It works on a
 * description of the graph and maintains its own vision of this graph. In
 * return, it does not modify the graph, but sends events to listeners telling
 * the new positions of nodes in the graph.
 * </p>
 * 
 * <p>
 * Here a layout algorithm continuously updates its internal representation of
 * the graph following a given method and outputs its computations to a listener
 * for each element of the graph (iterative algorithm). Such a layout algorithm
 * is not made to compute a layout once and for all. This is the best way to
 * handle evolving graphs.
 * </p>
 * 
 * <p>
 * This behaviour has been chosen because this algorithm is often run aside the
 * main thread that works on the graph. We want a thread to be able to compute a
 * new layout on its side, without disturbing the main algorithm run on the
 * graph. See the {@link org.miv.graphstream.algorithm.layout.LayoutRunner} for
 * an helper class allowing to create such a thread.
 * </p>
 * 
 * <p>
 * To describe the graph, you input the graph using methods like
 * {@link #addNode(String)}, {@link #removeNode(String)},
 * {@link #addEdge(String,String,String,boolean)} and
 * {@link #removeEdge(String)}. These inputs can be triggered by a
 * GraphListener for example.
 * </p>
 * 
 * <p>
 * To be notified of the layout changes dynamically, you must register a
 * {@link LayoutListener} that will be called each time a node changes its
 * position.
 * </p>
 * 
 * <p>
 * The graph viewers in the UI package often use a layout algorithm to present
 * graphs on screen.
 * </p>
 * 
 * <p>
 * TODO: would it be interesting, for some layouts, to have edges that contain
 * "break points" or curve points (Bezier for example) that are also moved by
 * the layout algorithm ?
 * </p>
 * 
 * @author Antoine Dutot
 * @author Yoann Pigné
 * @since 20050706
 */
public interface Layout
{
// Accessors

	/**
	 * Name of the layout algorithm. Can be used to determine how to interpret
	 * broken edges points and draw them.
	 */
	String getLayoutAlgorithmName();
	
	/**
	 * How many nodes moved during the last step?. When this method returns zero,
	 * the layout stabilised.
	 */
	int getNodeMoved();

	/**
	 * Percent of nodes moving (between [0..1]).
	 */
	double getStabilization();

	/**
	 * Smallest point in space of the layout bounding box.
	 */
	Point3 getLowPoint();

	/**
	 * Largest point in space of the layout bounding box.
	 */
	Point3 getHiPoint();

	/**
	 * Number of calls made to step() so far.
	 */
	int getSteps();

	/**
	 * Time in nanoseconds used by the last call to step().
	 */
	long getLastStepTime();

// Commands

	/**
	 * Clears the whole nodes and edges structures
	 */
	void clear();

	/**
	 * Add a listener for layout events.
	 */
	void addListener( LayoutListener listener );

	/**
	 * Remove a listener for layout events.
	 */
	void removeListener( LayoutListener listener );

	/**
	 * Add a node id to the graph description.
	 * @param id Identifier of the node.
	 */
	void addNode( String id ) throws SingletonException;

	/**
	 * Remove a node from the graph description.
	 * @param id Identifier of the node.
	 * @throws NotFoundException If no node matches id.
	 */
	void removeNode( String id ) throws NotFoundException;

	/**
	 * Add an edge id between node from and node to.
	 * @param id Identifier of the edge.
	 * @param from Identifier of the source node.
	 * @param to Identifier of the destination node.
	 * @param directed True if the edge is directed from the from node to the to
	 *        node.
	 */
	void addEdge( String id, String from, String to, boolean directed )
			throws NotFoundException, SingletonException;
	
	/**
	 * Declare that an edge is broken in segments and allow the layout to also
	 * compute "curved" edges position and deformation. This is an optional
	 * feature.
	 * TODO: how to tell that points are on the edge (segmented edge) or out of
	 * the edge (bezier edge for example)... Interpolation or approximation ?.
	 * There may exist several interpolation/approximation methods. Given them a
	 * name ?
	 * @param edgeId The edge to consider.
	 * @param points The number of break points per edge.
	 */
	void addEdgeBreakPoint( String edgeId, int points );

	/**
	 * Ignore or consider the given edge for the layout computation. By
	 * default edges are not ignored. This is an optional feature.
	 * @param id The edge identifier.
	 * @param on If true, the edge is ignored, else it is considered.
	 * @throws NotFoundException
	 */
	void ignoreEdge( String id, boolean on ) throws NotFoundException;
	
	/**
	 * Remove the edge id.
	 * @param id Identifier of the edge.
	 */
	void removeEdge( String id ) throws NotFoundException;

	/**
	 * Forcedly move a node by a vector. The vector is made of scalar that represent a
	 * portion of the graph display bounds. For example the x component of the
	 * vector represent x% of the graph width.
	 * @param id Node identifier.
	 * @param dx X component of the vector.
	 * @param dy Y component of the vector.
	 * @param dz Z component of the vector.
	 */
	void moveNode( String id, float dx, float dy, float dz );

	/**
	 * Change a node importance.
	 * @param id The node identifier.
	 * @param weight The relative node importance in [0..1].
	 */
	void setNodeWeight( String id, float weight );
	
	/**
	 * Change the edge importance.
	 * @param id The edge identifier.
	 * @param weight The relative edge importance in [0..1].
	 */
	void setEdgeWeight( String id, float weight );

	/**
	 * Allow or disallow a node to move.
	 * @param id The node identifier.
	 * @param on If true, the node stops moving.
	 */
	void freezeNode( String id, boolean on );
	
	/**
	 * The general "speed" of the algorithm.
	 * @param value A number in [0..1].
	 */
	void setForce( float value );
	
	/**
	 * Add a random vector whose length is 10% of the size of
	 * the graph to all node positions.
	 */
	void shake();

	/**
	 * Method to call repeatedly to compute the layout.
	 * 
	 * <p>
	 * This method implements
	 * the layout algorithm proper. It must be called in a loop, until the
	 * layout stabilises. You can know if the layout is stable by using the
	 * {@link #getNodeMoved()} method that returns the number of node that have
	 * moved during the last call to step().
	 * </p>
	 * 
	 * <p>
	 * The listener is called by this method, therefore each call to step() will
	 * also trigger layout events, allowing to reproduce the layout process
	 * graphically for example. You can insert the listener only when the layout
	 * stabilised, and then call step() anew if you do not want to observe the
	 * layout process.
	 * </p>
	 */
	void compute();

// Output

	/**
	 * Read the nodes positions from a file. See {@link #outputPos(String)} for
	 * the file format.
	 */
	void inputPos( String filename ) throws java.io.IOException;

	/**
	 * Output the nodes positions to a file. The file format is
	 * <ul>
	 * 		<li>each line gives the position of one node.</li>
	 * 		<li>the list starts with the node identifier (maybe between quotes
	 * 		    if needed).</li>
	 * 		<li>a colon.<li>
	 * 		<li>and a  list of two to three float numbers indicating the
	 * 		    position of the node in a given space.</li>
	 * 		<li>Empty lines are ignored.</li>
	 * 		<li>Lines beginning with an arbitrary number of spaces and then a
	 *		    sharp sign (#) are ignored.</li>
	 * </ul>
	 */
	void outputPos( String filename ) throws java.io.IOException;
	
	/**
	 * Set the overal quality level. There are five quality levels.
	 * @param qualityLevel The quality level in [0..4].
	 */
	void setQuality( int qualityLevel );
}