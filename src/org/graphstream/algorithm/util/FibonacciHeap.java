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
 * @since 2011-07-10
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.util;

import java.util.ArrayList;

/**
 * <p>
 * Fibonacci heap is a data structure used mainly to implement priority queues.
 * It stores pairs (key, value) in nodes. The structure is designed so that the
 * following operations can be implemented efficiently:
 * <ul>
 * <li>Finding the node with minimal key</li>
 * <li>Extracting the node with minimal key</li>
 * <li>Decreasing the key of a given node</li>
 * <li>Adding a new node</li>
 * <li>Merging two heaps</li>
 * <li>Deleting a node</li>
 * </ul>
 * 
 * <p>
 * This implementation does not support directly the last operation, but there
 * is a workaround. To delete a node, first decrease its key to something less
 * than the current minimum, then just extract the minimum.
 * 
 * <p>
 * In order to compare keys, this implementation uses their natural order: they
 * must implement {@link java.lang.Comparable} interface. The values can be of
 * any type.
 * 
 * <h3>Example</h3>
 * 
 * <pre>
 * FibonacciHeap&lt;Integer, String&gt; heap = new FibonacciHeap&lt;Integer, String&gt;();
 * // add some nodes and keep their references in order to be able to decrease
 * // their keys later
 * FibonacciHeap&lt;Integer, String&gt;.Node nodeA = heap.add(20, &quot;A&quot;);
 * FibonacciHeap&lt;Integer, String&gt;.Node nodeB = heap.add(10, &quot;B&quot;);
 * FibonacciHeap&lt;Integer, String&gt;.Node nodeC = heap.add(30, &quot;C&quot;);
 * FibonacciHeap&lt;Integer, String&gt;.Node nodeD = heap.add(50, &quot;D&quot;);
 * FibonacciHeap&lt;Integer, String&gt;.Node nodeE = heap.add(40, &quot;E&quot;);
 * 
 * String s1 = heap.extractMin(); // &quot;B&quot;
 * String s2 = heap.getMinValue(); // &quot;A&quot;
 * heap.decreaseKey(nodeD, 5);
 * String s3 = heap.extractMin(); // &quot;D&quot;
 * //delete nodeC
 * int kMin = heap.getMinKey(); // 20
 * heap.decreaseKey(nodeC, kMin - 1);
 * String s4 = heap.extractMin(); // C
 * </pre>
 * 
 * @author Stefan Balev
 * 
 * @param <K>
 *            the type of the keys
 * @param <V>
 *            the type of the values
 */
public class FibonacciHeap<K extends Comparable<K>, V> {

	/**
	 * This class encapsulates pairs (key, value) in nodes stored in Fibonacci
	 * heaps. Objects of this class cannot be instantiated directly. The only
	 * way to obtain a node reference is the return value of
	 * {@link FibonacciHeap#add(Comparable, Object)}. Typically these references
	 * are stored and then used in calls to
	 * {@link FibonacciHeap#decreaseKey(Node, Comparable)}.
	 * 
	 * @author Stefan Balev
	 * 
	 */
	public class Node {
		protected K key;
		protected V value;
		protected Node parent;
		protected Node child;
		protected Node left;
		protected Node right;
		protected int degree;
		protected boolean lostChild;

		protected Node(K key, V value) {
			this.key = key;
			this.value = value;
			parent = child = null;
			left = right = this;
			degree = 0;
			lostChild = false;
		}

		protected void clear() {
			parent = null;
			if (child != null) {
				child.clear();
				child = null;
			}
			left.right = null;
			left = null;
			if (right != null)
				right.clear();
		}

		protected void concatLists(Node y) {
			Node r = right;
			Node l = y.left;
			right = y;
			y.left = this;
			l.right = r;
			r.left = l;
		}

		protected void addChild(Node y) {
			y.parent = this;
			y.left = y.right = y;
			y.lostChild = false;
			degree++;
			if (child == null)
				child = y;
			else
				child.concatLists(y);
		}

		/**
		 * Returns the key stored in this node.
		 * @return the key stored in this node
		 */
		public K getKey() {
			return key;
		}

		/**
		 * Returns the value stored in this node.
		 * @return the value stored in this node
		 */
		public V getValue() {
			return value;
		}
	}

	protected Node min;
	protected int size;
	protected ArrayList<Node> degList;

	/**
	 * Creates a new empty Fibonacci heap.
	 */
	public FibonacciHeap() {
		min = null;
		size = 0;
		degList = new ArrayList<>();
	}

	/**
	 * Checks if the heap is empty.
	 * @return {@code true} if the heap is empty
	 * @complexity O(1)
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/**
	 * Returns the number of nodes in the heap.
	 * @return the number of nodes in the heap
	 * @complexity O(1)
	 */
	public int size() {
		return size;
	}

	/**
	 * Removes all the nodes in the heap
	 * @complexity This operation can be done in O(1) but this implementation takes O(<em>n</em>)
	 * in order to facilitate the garbage collection.
	 */
	public void clear() {
		if (!isEmpty()) {
			min.clear();
			min = null;
			size = 0;
		}
	}

	/**
	 * Adds a new node containing the given key and value to the heap.
	 * @param key the key of the new node
	 * @param value the value of the new node
	 * @return a reference to the new node. Typically this reference is stored and used in subsequent calls to
	 * {@link #decreaseKey(Node, Comparable)}
	 * @complexity O(1)
	 */
	public Node add(K key, V value) {
		Node node = new Node(key, value);
		if (isEmpty())
			min = node;
		else {
			min.concatLists(node);
			if (node.key.compareTo(min.key) < 0)
				min = node;
		}
		size++;
		return node;
	}

	/**
	 * Merges two heaps. Warning: <b>this operation is destructive</b>. This method empties the parameter {@code heap}.
	 * @param heap the heap to be merged with this heap
	 * @complexity O(1)
	 */
	public void addAll(FibonacciHeap<K, V> heap) {
		if (isEmpty())
			min = heap.min;
		else if (!heap.isEmpty()) {
			min.concatLists(heap.min);
			if (heap.min.key.compareTo(min.key) < 0)
				min = heap.min;
		}
		size += heap.size;
		heap.min = null;
		heap.size = 0;
	}

	/**
	 * Returns the minimal key in the heap.
	 * @return the minimal key in the heap
	 * @complexity O(1)
	 */
	public K getMinKey() {
		return min.key;
	}

	/**
	 * Returns the value stored in the node containing the minimal key.
	 * @return the value stored in the node containing the minimal key
	 * @complexity O(1)
	 * 
	 */
	public V getMinValue() {
		return min.value;
	}

	/**
	 * Removes the node containing the minimal key from the heap.
	 * @return the value stored in the removed node
	 * @complexity O(log<em>n</em>) amortized running time
	 */
	public V extractMin() {
		Node z = min;
		Node x = z.child;
		if (x != null) {
			do {
				x.parent = null;
				x = x.right;
			} while (x != z.child);
			z.concatLists(x);
			z.child = null;
		}
		if (z == z.right)
			min = null;
		else {
			z.left.right = z.right;
			z.right.left = z.left;
			min = z.right;
			consolidate();
		}
		z.left = z.right = null;
		size--;
		return z.value;
	}

	protected void consolidate() {
		Node w, x, y, t;
		int d;
		w = min;
		degList.clear();
		do {
			x = w;
			w = w.right;
			d = x.degree;
			while (d >= degList.size())
				degList.add(null);
			y = degList.get(d);
			while (y != null) {
				if (x.key.compareTo(y.key) > 0) {
					t = x;
					x = y;
					y = t;
				}
				x.addChild(y);
				degList.set(d, null);
				d++;
				if (d >= degList.size())
					degList.add(null);
				y = degList.get(d);
			}
			degList.set(d, x);
		} while (w != min);

		min = null;
		
		degList.stream()
			.filter(s -> s != null)
			.forEach(s -> {
				s.left = s.right = s;
				if (min == null)
					min = s;
				else {
					min.concatLists(s);
					if (s.key.compareTo(min.key) < 0)
						min = s;
				}
			});
			
	}

	/** Decreases the key of a given node
	 * @param x the node whose key is to be decreased. Must be a reference returned by {@link #add(Comparable, Object)}.
	 * @param key the new key
	 * @throws IllegalArgumentException if the new key is greater than the current.
	 * @complexity O(1) amortized running time
	 */
	public void decreaseKey(Node x, K key) {
		if (key.compareTo(x.key) > 0)
			throw new IllegalArgumentException(
					"The new key must be less than the old");
		x.key = key;
		Node y = x.parent;
		if (y != null && x.key.compareTo(y.key) < 0) {
			detach(x);
			multiDetach(y);
		}
		if (key.compareTo(min.key) < 0)
			min = x;
	}

	protected void detach(Node x) {
		Node y = x.parent;
		y.degree--;
		if (x == x.right)
			y.child = null;
		else {
			x.left.right = x.right;
			x.right.left = x.left;
			if (y.child == x)
				y.child = x.right;
			x.left = x.right = x;
		}
		min.concatLists(x);
		x.parent = null;
		x.lostChild = false;
	}

	protected void multiDetach(Node x) {
		if (x.parent == null)
			return;
		if (x.lostChild) {
			Node z = x.parent;
			detach(x);
			multiDetach(z);
		} else
			x.lostChild = true;
	}
}
