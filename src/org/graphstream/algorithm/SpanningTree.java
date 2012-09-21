package org.graphstream.algorithm;

import java.util.Iterator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;

/**
 * This interface defines the basic functionalities of a spanning tree algorithm.
 * 
 * <p> It defines methods related to tagging the edges of the spanning tree and for iterating on them.</p>
 */
public interface SpanningTree extends Algorithm {
	/**
	 * Get key attribute which will be used to set if edges are in the spanning
	 * tree, or not.
	 * 
	 * @return flag attribute
	 */
	String getFlagAttribute();
	
	/**
	 * Set the flag attribute.
	 * 
	 * @param flagAttribute
	 *            New attribute used. If {@code null} edges are not tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	void setFlagAttribute(String flagAttribute);
	
	/**
	 * Get value used to set that an edge is in the spanning tree.
	 * 
	 * @return on value
	 */
	Object getFlagOn();
	
	/**
	 * Set value used to set that an edge is in the spanning tree.
	 * 
	 * @param flagOn
	 *            on value. If {@code null} edges in the tree are not tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	void setFlagOn(Object flagOn);
	
	/**
	 * Get value used to set that an edge is not in the spanning tree.
	 * 
	 * @return off value
	 */
	Object getFlagOff();
	
	
	/**
	 * Set value used to set that an edge is not in the spanning tree.
	 * 
	 * @param newFlagOff
	 *            off value. If {@code null} edges out of the tree are not
	 *            tagged.
	 * @throws IllegalStateException
	 *             if {@link #init(Graph)} is already called
	 */
	void setFlagOff(Object flagOff);
	
	/**
	 * Removes the tags of all edges. Use this method to save memory if the
	 * spanning tree is used no more.
	 */
	void clear();

	/**
	 * An iterator on the tree edges.
	 * 
	 * @return An iterator on the tree edges
	 */
	<T extends Edge> Iterator<T> getTreeEdgesIterator();
	
	
	/**
	 * Iterable view of the spanning tree edges. This implementation uses
	 * {@link #getTreeEdgesIterator()}.
	 * 
	 * @return Iterable view of the tree edges.
	 */
	<T extends Edge> Iterable<T> getTreeEdges();
}
