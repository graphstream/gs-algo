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
package org.graphstream.algorithm.generator;

import org.graphstream.stream.Source;

/**
 * Graph generator.
 * 
 * <p>
 * A graph generator is an object that takes a graph as argument and
 * continuously create and evolve it. Some generators define an end to the
 * generation process, others may continue endlessly.
 * </p>
 * 
 * <p>
 * Generator are very similar to graph readers excepted they have a direct
 * access to the graph. They can be used instead of readers each time the
 * reading process needs to have a read access to the graph.
 * </p>
 */
public interface Generator extends Source {
	/**
	 * Begin the graph generation. This usually is the place for initialization
	 * of the generator. After calling this method, call the
	 * {@link #nextEvents()} method to add elements to the graph.
	 */
	void begin();

	/**
	 * Perform the next step in generating the graph. While this method returns
	 * true, there are still more elements to add to the graph to generate it.
	 * Be careful that some generators never return false here, since they can
	 * generate graphs of arbitrary size. For such generators, simply stop
	 * calling this method when enough elements have been generated.
	 * 
	 * A call to this method can produce an undetermined number of nodes and
	 * edges. Checking nodes count is advisable when generating the graph to
	 * avoid an unwanted big graph.
	 * 
	 * @return true while there are elements to add to the graph.
	 */
	boolean nextEvents();

	/**
	 * End the graph generation by finalizing it. Once the {@link #nextEvents()}
	 * method returned false (or even if you stop before), this method must be
	 * called to finish the graph.
	 */
	void end();
}