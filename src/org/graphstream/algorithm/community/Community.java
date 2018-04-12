/*
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
 *
 *
 * @since 2010-10-01
 * 
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.community;

import java.util.Objects;

/**
 * Basic community class. Ensures that each created community object has a
 * unique identifier throughout the simulation. This class also allows
 * comparison and easy stringification of communities.
 * 
 * @author Guillaume-Jean Herbiet
 * 
 */
public class Community implements Comparable<Community> {
	/**
	 * Counter of the generated communities. Ensure the uniqueness of the
	 * identifiers of the generated communities.
	 */
	protected static Integer NEXT_COMMUNITY_ID = 0;

	/**
	 * Id of the current community as an Integer.
	 */
	protected Integer id;

	/**
	 * New community instance with unique identifier.
	 */
	public Community() {
		this.id = NEXT_COMMUNITY_ID;
		NEXT_COMMUNITY_ID++;
	}

	/**
	 * Return the community identifier.
	 * 
	 * @return community identifier as an Integer
	 */
	public Integer id() {
		return this.id;
	}

	/**
	 * Return the community identifier as a String.
	 * 
	 * @return community identifier as a String
	 */
	public String getId() {
		return this.id.toString();
	}

	/**
	 * Tell if two Community objects are equal, based on their identifier.
	 * 
	 * @param c
	 *            The Community to compare to.
	 * @return True if the two communities are equal, false otherwise.
	 */
	public boolean equals(Community c) {
		return Objects.equals(this.id, c.id());
	}

	/**
	 * Stringification method for Community object.
	 */
	@Override
	public String toString() {
		return getId();
	}

	/**
	 * Comparison method for two Community objects, based on the value of their
	 * identfiers.
	 */
//	@Override
	public int compareTo(Community o) {
		return this.id.compareTo(o.id());
	}
}
