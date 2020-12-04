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
 * @since 2011-06-16
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import java.util.ArrayDeque;
import java.util.HashMap;

import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;


/**
 * Utility Class
 */
class Pair<X, Y> {
	final X x;
	final Y y;

	public Pair(X x, Y y) {
		this.x = x;
		this.y = y;
	}
}



/**
 * Iterative version of Tarjan's graph theory algorithm for finding the strongly
 * connected components of a graph.
 * 
 * See #TarjanStronglyConnectedComponents for details and usage. 
 * 
 * This implementation relies on a stack of nodes to be visited rather than a stack 
 * of function calls. This is an iterative implementation of the recursive original
 * one. 
 * 
 * @reference Tarjan, R. E. (1972),
 *            "Depth-first search and linear graph algorithms", SIAM Journal on
 *            Computing 1 (2): 146–160, doi:10.1137/0201010
 * @complexity O( | V | + | E | )
 * 
 */
public class IterativeTarjanStronglyConnectedComponents implements Algorithm {

	/**
	 * Associate some data with each node. Each node has an index and a low
	 * link.
	 */
	protected HashMap<Node, NodeData> data;
	/**
	 * The current index.
	 */
	protected int index;
	/**
	 * Stack used in computation.
	 */
	protected ArrayDeque<Node> S;
	/**
	 * Object used to generate component indexes.
	 */
	protected IndexGenerator sccIndex;
	/**
	 * Attribute key defining where component index is stored in node.
	 */
	protected String sccAttribute;
	/**
	 * Graph uses in computation. It is set when {@link #init(Graph)} is called.
	 */
	protected Graph graph;


	/**
	 * Recursion Stack
	 */
	protected ArrayDeque<Pair<Node,Long>> work; 

	/**
	 * Build a new Tarjan algorithm.
	 */
	public IterativeTarjanStronglyConnectedComponents() {
		this.data = new HashMap<Node, NodeData>();
		this.S = new ArrayDeque<Node>();
		this.sccIndex = new IntegerIndexGenerator();
		this.sccAttribute = "scc";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
	 */
	public void init(Graph graph) {
		this.graph = graph;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.Algorithm#compute()
	 */
	public void compute() {
		data.clear();
		index = 0;
		S.clear();
		
		graph.nodes()
			.filter(v -> !data.containsKey(v))
			.forEach(v -> strongConnect(v));
		
	}

	/**
	 * Set the generator of components indexes.
	 * 
	 * @param gen
	 *            the new generator
	 */
	@Parameter
	public void setIndexGenerator(IndexGenerator gen) {
		if (gen == null)
			throw new NullPointerException();

		this.sccIndex = gen;
	}

	/**
	 * Set the node attribute key where component index is stored.
	 * 
	 * @param key
	 *            attribute key of component index
	 */
	@Parameter
	public void setSCCIndexAttribute(String key) {
		if (key == null)
			throw new NullPointerException();

		this.sccAttribute = key;
	}

	/**
	 * Get the node attribute key where component index is stored.
	 * 
	 * @return the attribute key
	 */
	public String getSCCIndexAttribute() {
		return this.sccAttribute;
	}
	
	@Result
	public String defaultMessage() {
		return "Result stored in \""+this.sccAttribute+"\" attribute";
	}
	

	protected void strongConnect(Node v) {
		NodeData nd = new NodeData();
		data.put(v, nd);

		work = new ArrayDeque<>();
		work.push(new Pair<Node, Long>(v,0L));//[(v, 0)] # NEW: Recursion ArrayDeque.
		while (!work.isEmpty()){
			Pair<Node, Long> p = work.pop();
			v = p.x;
			nd = data.get(v);
			long i = p.y;
			if (i == 0L) {
				if(nd == null) {
					nd = new NodeData();
				}
				nd.index = index;
				nd.lowlink = index;
				data.put(v, nd);
				index++;
				S.push(v);
				
			}
			boolean recurse = false;
			for( long j = i ; j <v.getOutDegree(); j++) {
				Node w = v.getLeavingEdge((int)j).getOpposite(v);
				if (!data.containsKey(w)) {
					work.push(new Pair<>(v,j+1));
					work.push(new Pair<>(w,0L));
					recurse = true;
					break;
				} else if(S.contains(w)) {
					nd.lowlink = Math.min(nd.lowlink, data.get(w).index);
				}
			}
			if (recurse) {
				continue;
			}
			if (nd.index == nd.lowlink) {
				Node w;
				Object currentSCCIndex = sccIndex.nextIndex();
	
				do {
					w = S.pop();
					w.setAttribute(sccAttribute, currentSCCIndex);
				} while (w != v);
			}
			if(!work.isEmpty()){
				NodeData ndv = data.get(work.peek().x);
				ndv.lowlink =  Math.min(ndv.lowlink, nd.lowlink);
			}
		}
	}

	/**
	 * Internal data associated to nodes in computation.
	 */
	protected static class NodeData {
		int index;
		int lowlink;
	}

	/**
	 * Defines objects able to generator index.
	 */
	public static interface IndexGenerator {
		/**
		 * Create a new index.
		 * 
		 * @return a new index object that has to be unique according to
		 *         previous indexes.
		 */
		Object nextIndex();
	}

	/**
	 * Defines an index generator producing a sequence of integer as indexes.
	 * 
	 */
	public static class IntegerIndexGenerator implements IndexGenerator {
		private int index;

		public IntegerIndexGenerator() {
			index = 0;
		}

		public Object nextIndex() {
			return Integer.valueOf(index++);
		}
	}
}
