package org.graphstream.algorithm.randomWalk;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * A base entity for the {@link RandomWalk} algorithm. 
 */
public abstract class Entity {

	/**
	 * The shared information.
	 */
	protected RandomWalk.Context context;
	
	/**
	 * The current node the entity is on.
	 */
	protected Node current;
	
	/**
	 * Should initialize the entity, starting it on the given node.
	 * @param start The node on which the entity starts.
	 */
	void init(RandomWalk.Context context, Node start) {
		this.current = start;
		this.context = context;
	}
	
	/**
	 * Should move the entity from its current node to another.
	 */
	abstract void step();
	
	/**
	 * The weight of an edge.
	 * @param e The edge.
	 * @return The weight of the edge.
	 */
	protected double weight(Edge e) {
		if (!e.hasAttribute(context.weightAttribute))
			return 1.0;

		return e.getNumber(context.weightAttribute);
	}
}