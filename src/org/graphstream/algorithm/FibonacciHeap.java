package org.graphstream.algorithm;

import java.util.ArrayList;


public class FibonacciHeap<K extends Comparable<K>, V> {
	
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
		
		public K getKey() {
			return key;
		}
		
		public V getValue() {
			return value;
		}
	}
	
	protected Node min;
	protected int size;
	protected ArrayList<Node> degList;
	
	public FibonacciHeap() {
		min = null;
		size = 0;
		degList = new ArrayList<Node>();
	}
	
	public boolean isEmpty() {
		return size == 0;
	}
	
	public int size() {
		return size;
	}
	
	public void clear() {
		if (!isEmpty()) {
			min.clear();
			min = null;
			size = 0;
		}
	}
	
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
	
	public K getMinKey() {
		return min.key;
	}
	
	public V getMinValue() {
		return min.value;
	}
	
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
		for (Node s : degList)
			if (s != null) {
				s.left = s.right = s;
				if (min == null)
					min = s;
				else {
					min.concatLists(s);
					if (s.key.compareTo(min.key) < 0)
						min = s;
				}
			}
	}
	
	public void decreaseKey(Node x, K key) {
		if (key.compareTo(x.key) > 0)
			throw new IllegalArgumentException("The new key must be less than the old");
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
		}
		else
			x.lostChild = true;
	}
}