/*
 * Copyright 2006 - 2012
 *      Stefan Balev       <stefan.balev@graphstream-project.org>
 *      Julien Baudry	<julien.baudry@graphstream-project.org>
 *      Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *      Yoann Pigné	<yoann.pigne@graphstream-project.org>
 *      Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 *  
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.algorithm;

import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

/**
 * Base for spanning tree algorithms.
 * 
 * <p>
 * The result is stored in an edge attribute which name is defined by
 * {@link #flagAttribute} and value is {@link #flagOn} if the edge is in the
 * tree or {@link #flagOff} if not. If {@link #flagAttribute} is {@code null}
 * nothing is stored in the edges. If {@link #flagOn} is {@code null} edges in
 * the tree are not tagged. If {@link #flagOff} is {@code null} edges out of the
 * tree are not tagged.
 * </p>
 * 
 * 
 * <h2>Creating a spanning tree algorithm</h2>
 * 
 * <p>
 * Spanning tree algorithms have to extend this class and to implements the
 * {@link #makeTree()} and {@link #getTreeEdgesIterator()} methods.
 * {@link #edgeOn(Edge)} and {@link #edgeOff(Edge)} methods have to be used to
 * properly tag edges.
 * </p>
 * 
 * <p>
 * A call to compute reset the values of edges attribute. Then a call to
 * {@link #makeTree()} is made.
 * </p>
 * 
 * <h2>Highlight the spanning tree in viewer</h2>
 * 
 * <p>
 * Using the CSS, it is possible to highlight the spanning tree result using
 * classes. Considering two css edge classes have been defined in the CSS, for
 * example :
 * 
 * <pre>
 * edge .in {
 * 	 size: 3px;
 *   fill-color: black;
 * }
 * 
 * edge .notin {
 *   size: 2px;
 *   fill-color: gray;
 * }
 * </pre>
 * 
 * <p>
 * You can tell the algorithm to set up the value of the "ui.class" attribute of
 * edges to "in" when the edge is in the tree or "notin" when edge is not in the
 * tree.
 * </p>
 * 
 * <p>
 * This can be done by setting the {@link #flagAttribute} of the algorithm using
 * the setter {@link #setFlagAttribute(String)} and the flag values
 * {@link #flagOn} and {@link #flagOff} with {@link #setFlagOn(Object)} and
 * {@link #setFlagOff(Object)} setters.
 * </p>
 * 
 * <pre>
 * Graph graph = ...;
 * AbstractSpanningTree sp = ...;
 * 
 * ...
 * 
 * sp.setFlagAttribute("ui.class");
 * sp.setFlagOn("in");
 * sp.setFlagOff("notin");
 * 
 * sp.init(graph);
 * sp.compute();
 * 
 * graph.display();
 * 
 * ..
 * </pre>
 */
public abstract class AbstractSpanningTree implements Algorithm {
	/**
	 * The graph on which algorithm try to extract a spanning tree.
	 */
	protected Graph graph;

	/**
	 * Attribute which will be used to set is an edge is in the spanning tree or
	 * not.
	 */
	protected String flagAttribute;

	/**
	 * Value of the <i>flagAttribute</i> if the edge is in the spanning tree.
	 */
	protected Object flagOn;

	/**
	 * Value of the <i>flagAttribute</i> if the edge is not in the spanning
	 * tree.
	 */
	protected Object flagOff;

	/**
	 * Create a new SpanningTree algorithm. By default edges are not tagged.
	 */
	public AbstractSpanningTree() {
		this(null, null, null);
	}

	/**
	 * Create a new SpanningTree algorithm. Default flag attribute values are
	 * {@code true} for edges in the tree and {@code false} for the remaining
	 * edges.
	 * 
	 * @param flagAttribute
	 *            attribute used to compare edges
	 */
	public AbstractSpanningTree(String flagAttribute) {
		this(flagAttribute, true, false);
	}

	/**
	 * Create a new SpanningTree algorithm.
	 * 
	 * @param flagAttribute
	 *            attribute used to set if an edge is in the spanning tree
	 * @param flagOn
	 *            value of the <i>flagAttribute</i> if edge is in the spanning
	 *            tree
	 * @param flagOff
	 *            value of the <i>flagAttribute</i> if edge is not in the
	 *            spanning tree
	 */
	public AbstractSpanningTree(String flagAttribute, Object flagOn,
			Object flagOff) {
		graph = null;
		this.flagAttribute = flagAttribute;
		this.flagOn = flagOn;
		this.flagOff = flagOff;
	}

	/**
	 * Get key attribute which will be used to set if edges are in the spanning
	 * tree, or not.
	 * 
	 * @return flag attribute
	 */
	public String getFlagAttribute() {
		return flagAttribute;
	}

	/**
	 * Set the flag attribute.
	 * 
	 * @param flagAttribute
	 *            New attribute used. If {@code null} edges are not tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	public void setFlagAttribute(String flagAttribute) {
		if (graph != null)
			throw new IllegalStateException(
					"Flag attribute can be set only before the algorithm is initialized");
		this.flagAttribute = flagAttribute;
	}

	/**
	 * Get value used to set that an edge is in the spanning tree.
	 * 
	 * @return on value
	 */

	public Object getFlagOn() {
		return flagOn;
	}

	/**
	 * Set value used to set that an edge is in the spanning tree.
	 * 
	 * @param flagOn
	 *            on value. If {@code null} edges in the tree are not tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	public void setFlagOn(Object flagOn) {
		if (graph != null)
			throw new IllegalStateException(
					"Flag values can be set only before the algorithm is initialized");
		this.flagOn = flagOn;
	}

	/**
	 * Get value used to set that an edge is not in the spanning tree.
	 * 
	 * @return off value
	 */
	public Object getFlagOff() {
		return flagOff;
	}

	/**
	 * Set value used to set that an edge is not in the spanning tree.
	 * 
	 * @param newFlagOff
	 *            off value. If {@code null} edges out of the tree are not
	 *            tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	public void setFlagOff(Object flagOff) {
		if (graph != null)
			throw new IllegalStateException(
					"Flag values can be set only before the algorithm is initialized");
		this.flagOff = flagOff;
	}

	// Protected Access

	/**
	 * Add an edge to the spanning tree.
	 * 
	 * @param e
	 *            edge to add
	 */
	protected void edgeOn(Edge e) {
		if (flagAttribute != null) {
			if (flagOn != null)
				e.changeAttribute(flagAttribute, flagOn);
			else
				e.removeAttribute(flagAttribute);
		}
	}

	/**
	 * Remove an edge of the spanning tree.
	 * 
	 * @param e
	 *            edge to remove
	 */
	protected void edgeOff(Edge e) {
		if (flagAttribute != null) {
			if (flagOff != null)
				e.changeAttribute(flagAttribute, flagOff);
			else
				e.removeAttribute(flagAttribute);
		}
	}

	/**
	 * Reset flag attribute values. All edges are tagged as being out of the
	 * tree.
	 */
	protected void resetFlags() {
		for (Edge edge : graph.getEachEdge())
			edgeOff(edge);
	}

	// Abstract methods to be implemented by subclasses

	/**
	 * Method that will be implemented by spanning tree's algorithms to build
	 * the tree.
	 */
	protected abstract void makeTree();

	/**
	 * An iterator on the tree edges.
	 * 
	 * @return An iterator on the tree edges
	 */
	public abstract <T extends Edge> Iterator<T> getTreeEdgesIterator();

	/**
	 * Iterable view of the spanning tree edges. This implementation uses
	 * {@link #getTreeEdgesIterator()}.
	 * 
	 * @return Iterable view of the tree edges.
	 */
	public <T extends Edge> Iterable<T> getTreeEdges() {
		return new Iterable<T>() {
			public Iterator<T> iterator() {
				return getTreeEdgesIterator();
			}
		};
	}

	/**
	 * Removes the tags of all edges. Use this method to save memory if the
	 * spanning tree is used no more.
	 */
	public void clear() {
		if (flagAttribute != null)
			for (Edge edge : graph.getEachEdge())
				edge.removeAttribute(flagAttribute);
	}

	// Algorithm interface

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
	public void compute() {
		if (this.graph == null) {
			return;
		}

		resetFlags();
		makeTree();
	}
}
