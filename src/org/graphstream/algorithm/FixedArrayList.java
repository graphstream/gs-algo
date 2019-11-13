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
 * @since 2011-10-04
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * Array list with immutable element indices.
 * 
 * <p>A fixed array list is like an array list, but it ensures the property that
 * each element will always stay at the same index, even if elements are
 * removed in between. The counterpart of this property is that the array
 * handles by itself the insertion of new elements (since when an element is
 * removed in the middle, this position can be reused), and therefore indices
 * cannot be chosen (i.e. only the {@link #add(Object)} and
 * {@link #addAll(Collection)} methods are usable to insert new elements in the
 * array).</p>
 * 
 * <p>This is the reason why this does not implement the List interface, because
 * the add(int,E) method cannot be implemented.</p>
 * 
 * <p>Furthermore, this array cannot contain null values, because it marks
 * unused positions within the array using the null value.</p>
 * 
 * @author Antoine Dutot
 * @since 20040912
 */
public class FixedArrayList<E>
	implements Collection<E>, RandomAccess
{
// Attribute

	/**
	 * List of elements.
	 */
	protected ArrayList<E> elements = new ArrayList<E>();

	/**
	 * List of free indices.
	 */
	protected ArrayList<Integer> freeIndices = new ArrayList<Integer>();

	/**
	 * Last inserted element index.
	 */
	protected int lastIndex = -1;

// Construction

	public FixedArrayList() {
		elements = new ArrayList<E>();
		freeIndices = new ArrayList<Integer>(16);
	}

	public FixedArrayList(int capacity) {
		elements = new ArrayList<E>(capacity);
		freeIndices = new ArrayList<Integer>(16);
	}

// Access

	/**
	 * Number of elements in the array.
	 * @return The number of elements in the array.
	 */
	public int size() {
		return elements.size() - freeIndices.size();
	}

	/**
	 * Real size of the array, counting elements that have been erased.
	 * @see #unsafeGet(int)
	 */
	public int realSize() {
		return elements.size();
	}

	public boolean isEmpty() {
		return size() == 0;
	}
	
	/**
	 * True if the given index i references a value.
	 * @param i The index to test.
	 * @return True if a value exists at the given index.
	 */
	public boolean hasIndex(int i) {
		if(i>0 && i<elements.size()) {
			return elements.get(i) != null;
		}
		
		return false;
	}

	/**
	 * I-th element.
	 * @param i The element index.
	 * @return The element at index <code>i</code>.
	 */
	public E get(int i) {
		E e = elements.get(i);

		if(e == null)
			throw new NoSuchElementException( "no element at index " + i );

		return e;
	}

	/**
	 * I-th element. Like the {@link #get(int)} method but it does not check
	 * the element does not exists at the given index.
	 * @param i The element index.
	 * @return The element at index <code>i</code>.
	 */
	public E unsafeGet(int i) {
		return elements.get( i );
	}

	public boolean contains(Object o) {
		int n = elements.size();

		for(int i=0; i<n; ++i) {
			E e = elements.get(i);
	
			if(e != null) {
				if(e == o)
					return true;

				if(elements.equals(o))
					return true;
			}
		}

		return false;
	}

	public boolean containsAll(Collection<?> c) {
		for(Object o: c) {
			if(! contains(o))
				return false;
		}

		return true;
	}

	@Override
    @SuppressWarnings("unchecked")
	public boolean equals(Object o) {
		if(o instanceof FixedArrayList) {
			FixedArrayList<? extends E> other = (FixedArrayList<? extends E>) o;

			int n = size();

			if(other.size() == n) {
				for(int i=0; i<n; ++i) {
					E e0 = elements.get(i);
					E e1 = other.elements.get(i);

					if(e0 != e1) {
						if(e0 == null && e1 != null)
							return false;

						if(e0 != null && e1 == null)
							return false;

						if(! e0.equals(e1))
							return false;
					}
				}

				return true;
			}
		}

		return false;
	}

	public java.util.Iterator<E> iterator() {
		return new FixedArrayIterator();
	}

	/**
	 * Last index used by the {@link #add(Object)} method.
	 * @return The last insertion index.
	 */
	public int getLastIndex() {
		return lastIndex;
	}
	
	/**
	 * The index that will be used in case of a next insertion in this array.
	 * @return next add index
	 */
	public int getNextAddIndex() {
		int n = freeIndices.size();
		
		if(n > 0)
		     return freeIndices.get(n - 1);
		else return elements.size();
	}

	public Object[] toArray() {
		int n = size();
		int m = elements.size();
		int j = 0;
		Object a[] = new Object[n];

		for(int i=0; i<m; ++i) {
			E e = elements.get(i);

			if(e != null)
				a[j++] = e;
		}

		assert(j == n);
		return a;
	}

	public <T> T[] toArray(T[] a) {
		// TODO
		throw new RuntimeException( "not implemented yet" );
	}

// Commands

	/**
	 * Add one <code>element</code> in the array. The index used for inserting
	 * the element is then available using {@link #getLastIndex()}. This method
	 * complexity is O(1).
	 * @see #getLastIndex()
	 * @param element The element to add.
	 * @return Always true.
	 * @throws NullPointerException If a null value is inserted.
	 */
	public boolean add(E element) throws java.lang.NullPointerException {
		if(element == null)
			throw new java.lang.NullPointerException( "this array cannot contain null value" );

		int n = freeIndices.size();

		if(n > 0) {
			int i = freeIndices.remove(n - 1);
			elements.set(i, element);
			lastIndex = i;
		} else {
			elements.add(element);
			lastIndex = elements.size() - 1;
		}

		return true;
	}

	public boolean addAll(Collection<? extends E> c) throws UnsupportedOperationException {
		java.util.Iterator<? extends E> k = c.iterator();
		
		while(k.hasNext()) {
			add(k.next());
		}

		return true;
	}
	
	/**
	 * This operation set the i-th cell with the given value.
	 * 
	 * This works only
	 * if the cell is empty, or if i is larger or equal to the size of the
	 * array (if larger, empty cells are added to fill the gap, and free
	 * indices will be used by the add() method).
	 * 
	 * If the cell is not empty, the return value is false.
	 * 
	 * This method is a convenience method, and its complexity is not O(1)
	 * like the add() and remove() methods. At worse the complexity is O(n).
	 * It is optimized so that when adding the element whose id is the one
	 * given by {@link FixedArrayList#getNextAddIndex()} its complexity is O(1).
	 * 
	 * @param i The index of the cell to change.
	 * @param element The value to set.
	 * @return false If the insertion was not successful (there was already
	 * something in the set).
	 */
	public boolean addAt(int i, E element) {
		if(i >= elements.size()) {
			// Add at the end or at a non existent position at the end.
			
			int n = elements.size();
			int d = i - n;
			
			for(int j=0; j<d; j++) {
				elements.add(null);
				freeIndices.add(n+j);
				
			}
			elements.add(element);
			assert(elements.size()-1 == i);
			lastIndex = i;
		} else {
			// Add at an existing position
			if(elements.get(i) == null) {
				// Add the element and release the index in the index list.
				elements.set(i, element);
				freeIndices.remove(freeIndices.lastIndexOf(i));	// O(n)
			} else {
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Remove the element at index <code>i</code>. This method complexity
	 * is O(1).
	 * @param i Index of the element to remove.
	 * @return The removed element.
	 */
	public E remove(int i) {
		int n = elements.size();

		if(i < 0 || i >= n)
			throw new ArrayIndexOutOfBoundsException("index "+i+" does not exist");

		if(n > 0) {
			if( elements.get( i ) == null )
				throw new NullPointerException("no element stored at index " + i);

			if(i == (n - 1)) {
				return elements.remove( i );
			} else {
				E e = elements.get(i);
				elements.set(i, null);
				freeIndices.add(i);
				return e;
			}
		}

		throw new ArrayIndexOutOfBoundsException("index "+i+" does not exist");
	}

	protected void removeIt(int i) {
		remove(i);
	}

	/**
	 * Remove the element <code>e</code>. At worse the complexity is
	 * O(n).
	 * @param e The element to remove.
	 * @return True if removed.
	 */
	public boolean remove(Object e) {
		int n = elements.size();

		for(int i=0; i<n; ++i) {
			if(elements.get(i) == e) {
				elements.remove(i);
				return true;
			}
		}

		return false;
	}

	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException( "not implemented yet" );
	}

	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException( "not implemented yet" );
	}

	public void clear() {
		elements.clear();
		freeIndices.clear();
	}

// Nested classes

protected class FixedArrayIterator implements java.util.Iterator<E> {
	int i;

	public FixedArrayIterator() {
		i = -1;
	}

	public boolean hasNext() {
		int n = elements.size();

		for(int j=i+1; j<n; ++j) {
			if(elements.get(j) != null)
				return true;
		}

		return false;
	}

	public E next() {
		int n = elements.size();

		for(int j=i+1; j<n; ++j) {
			E e = elements.get(j);

			if(e != null) {
				i = j;
				return e;
			}
		}

		throw new NoSuchElementException("no more elements in iterator");
	}

	public void remove() throws UnsupportedOperationException {
		if(i >= 0 && i < elements.size() && elements.get(i) != null) {
			removeIt(i);	// A parent class method cannot be called if it has
							// the same name as one in the inner class
							// (normal), but even if they have distinct
							// arguments types. Hence this strange removeIt()
							// method...
		} else {
			throw new IllegalStateException("no such element");
		}
	}
}
}