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
package org.graphstream.algorithm.generator;

import java.util.ArrayList;

/**
 * Scale-free graph (tree) generator using the preferential attachement rule.
 * 
 * <p>
 * This is a very simple graph generator that generates a tree using the
 * preferential attachement rule: nodes are generated one by one, and each time
 * attached by an edge to another node that has more chance to choosed if it
 * already has lots of nodes attached to it.
 * </p>
 * 
 * <p>
 * The more this generator is iterated, the more nodes are generated. It can
 * therefore generate trees of any size.
 * </p>
 * 
 * @since 20061128
 */
public class PreferentialAttachmentGenerator extends BaseGenerator {
	/**
	 * Degree of each node.
	 */
	protected ArrayList<Integer> degrees;

	/**
	 * Maximal degree at time t.
	 */
	protected int degreeMax = 0;

	/**
	 * Number of edges.
	 */
	protected int edgesCount = 0;

	/**
	 * New generator.
	 */
	public PreferentialAttachmentGenerator() {
		directed = false;
	}

	/**
	 * Start the generator. A single node is added.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
		this.degrees = new ArrayList<Integer>();
		this.degreeMax = 0;

		addNode("0");
		degrees.add(0);
	}

	/**
	 * Step of the generator. Add a node and try to connect it with some others.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		// Generate a new node.

		int index = degrees.size();
		String id = Integer.toString(index);

		addNode(id);
		degrees.add(0);

		// Compute the attachment probability of each previouly added node

		int sumDeg = edgesCount * 2;

		// Choose the node to attach to.

		float sumProba = 0;
		float rnd = (float) Math.random();
		int otherIdx = -1;

		for (int i = 0; i < index; ++i) {
			float proba = sumDeg == 0 ? 1 : ((float) degrees.get(i))
					/ ((float) sumDeg);

			sumProba += proba;

			if (sumProba > rnd) {
				otherIdx = i;
				break;
			}
		}

		// Attach to the other node.

		if (otherIdx >= 0) {
			String oid = Integer.toString(otherIdx);
			String eid = id + "_" + oid;

			addEdge(eid, oid, id);
			edgesCount++;
			degrees.set(otherIdx, degrees.get(otherIdx) + 1);
			degrees.set(index, degrees.get(index) + 1);
		} else {
			System.err.printf("PreferentialAttachmentGenerator: Aieuu!%n");
		}

		// It is always possible to add an element.

		return true;
	}

	/**
	 * Clean degrees.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#end()
	 */
	public void end() {
		degrees.clear();
		degrees = null;
		degreeMax = 0;
	}
}