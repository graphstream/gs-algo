/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
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
 * Base for spanning tree algorithms. In this implementation, you can specify
 * the attribute and the values which will define if an edge is in the spanning
 * tree or not. You can easily use this feature to colorize the spanning by
 * specifying the color attribute as <i>flagAttribute</i> and color of tree as
 * <i>flagOn</i>.
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
	 * Create a new SpanningTree algorithm.
	 */
	public AbstractSpanningTree() {
		this("SpanningTree.flag");
	}

	/**
	 * Create a new SpanningTree algorithm.
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
		return this.flagAttribute;
	}

	/**
	 * Set the flag attribute.
	 * 
	 * @param newFlagAttribute
	 *            new attribute used
	 */
	public void setFlagAttribute(String newFlagAttribute) {
		this.flagAttribute = newFlagAttribute;
	}

	/**
	 * Get value used to set that an edge is in the spanning tree.
	 * 
	 * @return on value
	 */

	public Object getFlagOn() {
		return this.flagOn;
	}

	/**
	 * Set value used to set that an edge is in the spanning tree.
	 * 
	 * @param newFlagOn
	 *            on value
	 */
	public void setFlagOn(Object newFlagOn) {
		if (!this.flagOff.equals(newFlagOn))
			this.flagOn = newFlagOn;
	}

	/**
	 * Get value used to set that an edge is not in the spanning tree.
	 * 
	 * @return off value
	 */
	public Object getFlagOff() {
		return this.flagOff;
	}

	/**
	 * Set value used to set that an edge is not in the spanning tree.
	 * 
	 * @param newFlagOff
	 *            off value
	 */
	public void setFlagOff(Object newFlagOff) {
		if (!this.flagOn.equals(newFlagOff))
			this.flagOff = newFlagOff;
	}

	// Protected Access

	/**
	 * Add an edge to the spanning tree.
	 * 
	 * @param e
	 *            edge to add
	 */
	protected void edgeOn(Edge e) {
		e.changeAttribute(flagAttribute, flagOn);
	}

	/**
	 * Remove an edge of the spanning tree.
	 * 
	 * @param e
	 *            edge to remove
	 */
	protected void edgeOff(Edge e) {
		e.changeAttribute(flagAttribute, flagOff);
	}

	/**
	 * Reset cluster and flag attribute values.
	 */
	protected void resetFlags() {
		Iterator<? extends Edge> iteE;

		iteE = graph.getEdgeIterator();

		while (iteE.hasNext())
			edgeOff(iteE.next());
	}

	/**
	 * Method that will be implemented by spanning tree's algorithms to build
	 * the tree.
	 */
	protected abstract void makeTree();

	// Algorithm interface

	public void init(Graph graph) {
		this.graph = graph;
	}

	/**
	 * Compute the spanning tree using Kruskal's algorithm.
	 */
	public void compute() {
		if (this.graph == null) {
			return;
		}

		resetFlags();
		makeTree();
	}
}
