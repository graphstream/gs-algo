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
 * @since 2012-07-23
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.util;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This data structure is used to maintain disjoint sets. It supports limited
 * number of operations, but they are all executed in constant amortized time.
 * The supported operations are:
 * </p>
 * <ul>
 * <li>Adding a new set containing a single element</li>
 * <li>Determining if two elements belong to the same set</li>
 * <li>Union of the sets containing two elements</li>
 * </ul>
 * 
 * <p>
 * The space taken by this structure is O(n), where n is the number of elements.
 * </p>
 * 
 * @param <E>
 *            the type of the elements
 */
public class DisjointSets<E> {

	/**
	 * Disjoint sets are represented as a forest. This class presents the tree
	 * node associated to each element.
	 */
	protected static class Node {
		Node parent;
		int rank;

		protected Node() {
			parent = this;
			rank = 0;
		}

		protected Node root() {
			if (this != parent)
				parent = parent.root();
			return parent;
		}

		protected boolean join(Node node) {
			Node x = root();
			Node y = node.root();
			if (x == y)
				return false;
			if (x.rank > y.rank)
				y.parent = x;
			else {
				x.parent = y;
				if (x.rank == y.rank)
					y.rank++;
			}
			return true;
		}
	}

	/**
	 * Correspondence between elements and nodes.
	 */
	protected Map<E, Node> map;

	/**
	 * Creates a new instance containing no sets and no elements
	 */
	public DisjointSets() {
		map = new HashMap<E, Node>();
	}

	/**
	 * Creates a new instance containing no sets and no elements. If the total
	 * number of elements to add is known in advance, this constructor is more
	 * efficient than the default.
	 * 
	 * @param initialCapacity
	 *            Initial capacity (in number of elements) of the structure. The
	 *            structure grows dynamically and new elements can be added even
	 *            if this capacity is exceeded.
	 */
	public DisjointSets(int initialCapacity) {
		map = new HashMap<E, Node>(4 * initialCapacity / 3 + 1);
	}

	/**
	 * Adds a new set containing only {@code e} to the structure. If {@code e}
	 * already belongs to some of the disjoint sets, nothing happens.
	 * 
	 * @param e
	 *            The element to add as a singleton
	 * @return True if the new set is added and false if {@code e} already
	 *         belongs to some set.
	 */
	public boolean add(E e) {
		Node x = map.get(e);
		if (x != null)
			return false;
		map.put(e, new Node());
		return true;
	}

	/**
	 * Checks if two elements belong to the same set.
	 * 
	 * @param e1
	 *            An element
	 * @param e2
	 *            An element
	 * @return True if and only if belong to the same set. Note that if
	 *         {@code e1} or {@code e2} do not belong to any set, false is
	 *         returned.
	 */
	public boolean inSameSet(Object e1, Object e2) {
		Node x1 = map.get(e1);
		if (x1 == null)
			return false;
		Node x2 = map.get(e2);
		if (x2 == null)
			return false;
		return x1.root() == x2.root();
	}

	/**
	 * Union of the set containing {@code e1} and the set containing {@code e2}.
	 * After this operation {@code inSameSet(e1, e2)} will return true. If
	 * {@code e1} or {@code e2} do not belong to any set or if they already
	 * belong to the same set, nothing happens.
	 * 
	 * @param e1
	 *            An element
	 * @param e2
	 *            An element
	 * @return {@code true} if and only if {@code e1} and {@code e2} belong to different sets at the beginning
	 */
	public boolean union(Object e1, Object e2) {
		Node x1 = map.get(e1);
		if (x1 == null)
			return false;
		Node x2 = map.get(e2);
		if (x2 == null)
			return false;
		return x1.join(x2);
	}

	/**
	 * Checks if an element belongs to some of the disjoint sets.
	 * 
	 * @param e
	 *            An element
	 * @return True if {@code e} belongs to some set.
	 */
	public boolean contains(Object e) {
		return map.get(e) != null;
	}

	/**
	 * Reinitializes the structure. After this operation the structure contains
	 * no sets.
	 */
	public void clear() {
		map.values().forEach(node -> node.parent = null);
		
		map.clear();
	}
}
