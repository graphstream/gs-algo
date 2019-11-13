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

import java.util.HashMap;
import java.util.Stack;

import org.graphstream.algorithm.util.Parameter;
import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * Tarjan's Algorithm is a graph theory algorithm for finding the strongly
 * connected components of a graph.
 * 
 * <h2>Overview from <a href=
 * "http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm"
 * >Wikipedia</a></h2>
 * 
 * <p>
 * The algorithm takes a directed graph as input, and produces a partition of
 * the graph's vertices into the graph's strongly connected components. Every
 * vertex of the graph appears in a single strongly connected component, even if
 * it means a vertex appears in a strongly connected component by itself (as is
 * the case with tree-like parts of the graph, as well as any vertex with no
 * successor or no predecessor).
 * </p>
 * 
 * <p>
 * The basic idea of the algorithm is this: a depth-first search begins from an
 * arbitrary start node (and subsequent depth-first searches are conducted on
 * any nodes that have not yet been found). The search does not explore any node
 * that has already been explored. The strongly connected components form the
 * subtrees of the search tree, the roots of which are the roots of the strongly
 * connected components.
 * </p>
 * 
 * <p>
 * The nodes are placed on a stack in the order in which they are visited. When
 * the search returns from a subtree, the nodes are taken from the stack and it
 * is determined whether each node is the root of a strongly connected
 * component. If a node is the root of a strongly connected component, then it
 * and all of the nodes taken off before it form that strongly connected
 * component.
 * </p>
 * 
 * <h2>Usage</h2>
 * 
 * <p>
 * This algorithm use an attribute to store the component's index of each node.
 * This attribute can be customized using {@link #setSCCIndexAttribute(String)}.
 * Index is generate with an index generator that can be customized using
 * {@link #setIndexGenerator(IndexGenerator)}
 * </p>
 * 
 * @reference Tarjan, R. E. (1972),
 *            "Depth-first search and linear graph algorithms", SIAM Journal on
 *            Computing 1 (2): 146–160, doi:10.1137/0201010
 * @complexity O( | V | + | E | )
 * 
 */
public class TarjanStronglyConnectedComponents implements Algorithm {

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
	protected Stack<Node> S;
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
	 * Build a new Tarjan algorithm.
	 */
	public TarjanStronglyConnectedComponents() {
		this.data = new HashMap<Node, NodeData>();
		this.S = new Stack<Node>();
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

		nd.index = index;
		nd.lowlink = index;

		index++;
		S.push(v);
		
		v.leavingEdges().forEach(vw -> {
			Node w = vw.getOpposite(v);

			if (!data.containsKey(w)) {
				strongConnect(w);
				nd.lowlink = Math.min(nd.lowlink, data.get(w).lowlink);
			} else if (S.contains(w)) {
				nd.lowlink = Math.min(nd.lowlink, data.get(w).index);
			}
		});

		if (nd.index == nd.lowlink) {
			Node w;
			Object currentSCCIndex = sccIndex.nextIndex();

			do {
				w = S.pop();
				w.setAttribute(sccAttribute, currentSCCIndex);
			} while (w != v);
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
