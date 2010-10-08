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
 * Project copyright 2006 - 2010
 * 	Julien Baudry
 * 	Antoine Dutot
 * 	Yoann Pigné
 * 	Guilhelm Savin
 *
 * This file is copyright 2010
 *  Guillaume-Jean Herbiet
 */
package org.graphstream.algorithm.community;

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
		return this.id == c.id();
	}

	/**
	 * Stringification method for Community object.
	 */
	public String toString() {
		return getId();
	}

	/**
	 * Comparison method for two Community objects, based on the value of their
	 * identfiers.
	 */
	@Override
	public int compareTo(Community o) {
		return this.id.compareTo(o.id());
	}
}
