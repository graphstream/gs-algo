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

/**
 * Various graph generators with specific properties.
 * 
 * <p>
 * Graph generators are classes that can be used as iterators, but instead of
 * iterating on already existing elements, they create them on the fly. Often
 * the more one iterates on the genrator, the larger the graph becomes. Some
 * generator are finite, whereas others are not.
 * </p>
 * 
 * <p>
 * Generators have very few methods (apart from the generation parameter setup
 * methods that are specific to each generator). They are:
 * <ul>
 * 		<li>{@link org.miv.graphstream.algorithm.generator.Generator#begin(org.miv.graphstream.graph.Graph)}
 * 			to start the generation of elements in the given graph. This graph
 * 			may or not already contain elements.</li>
 * 		<li>{@link org.miv.graphstream.algorithm.generator.Generator#nextElement()}
 * 			to generate one or a set of elements in the graph. The more this
 * 			method is iterated, the more the graph grows. This method returns
 * 			false when no more elements can be generated. BE CAREFUL: some
 * 			generators never return false: they can generate graphs of arbitrary
 * 			size.</li>
 * 		<li>{@link org.miv.graphstream.algorithm.generator.Generator#end()} to
 * 			finish the graph generation process, either once
 * 			{@link org.miv.graphstream.algorithm.generator.Generator#nextElement()}
 * 			returned false, or one chooses to stop the generation process.</li>
 * </ul>
 * </p>
 * 
 * @author Antoine Dutot
 * @author Yoann Pigné
 */
package org.miv.graphstream.algorithm.generator;