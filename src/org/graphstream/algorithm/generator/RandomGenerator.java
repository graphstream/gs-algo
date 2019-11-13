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
 * @since 2009-02-19
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.graphstream.algorithm.util.RandomTools;

/**
 * Random graph generator.
 * <p>
 * This generator creates random graphs of any size <em>n</em> with given
 * average degree <em>k</em> and binomial degree distribution
 * <em>B(n, k / (n - 1))</em>. Calling {@link #begin()} creates a clique of size
 * <em>k</em>. Each call of {@link #nextEvents()} removes a small fraction of
 * existing edges, adds a new node and connects it randomly with some of the old
 * nodes.
 * </p>
 * 
 * <p>
 * After <em>n - k</em> steps we obtain a Erd&#337;s–R&eacute;ny&iacute; random
 * graph <em>G(n, p)</em> with <em>p = k / (n - 1)</em>. In other words the
 * result is the same as if we started with <em>n</em> isolated nodes and
 * connected each pair of them with probability <em>p</em>.
 * </p>
 * 
 * <p>
 * This generator can work in &quot;non-destructive&quot; mode controlled by a
 * constructor parameter called <code>allowRemove</code> with default value
 * <code>true</code>. If this parameter is <code>false</code>, existing edges
 * are never removed. The obtained graph has the same average degree, but
 * different degree distribution. This distribution is asymmetric, more
 * stretched than the binomial distribution, with a peak on the left of the
 * average degree. This mode is slightly faster and more memory-efficient. Use
 * it if you do not want edges to be removed and if you do not care about the
 * degree distribution.
 * </p>
 * 
 * <p>
 * This generator has the ability to add randomly chosen numerical values on
 * arbitrary attributes on edges or nodes of the graph, and to randomly choose a
 * direction for edges. For more information see {@link BaseGenerator}.
 * </p>
 * 
 * <h2>Example</h2>
 * 
 * <pre>
 * Graph graph = new SingleGraph(&quot;Random&quot;);
 * Generator gen = new RandomGenerator();
 * gen.addSinkg(graph);
 * gen.begin();
 * while (graph.getNodeCount() &lt; 100 &amp;&amp; gen.nextEvents());
 * gen.end();
 * graph.display();
 * </pre>
 */
public class RandomGenerator extends BaseGenerator {
	/**
	 * the average degree
	 */
	protected double averageDegree;
	/**
	 * Can we remove existing edges?
	 */
	protected boolean allowRemove;
	/**
	 * List storing edge ids. Used to choose edges to remove
	 */
	protected List<String> edgeIds;
	/**
	 * For drawing random subsets
	 */
	protected Set<Integer> subset;

	/**
	 * The number of nodes generated so far.
	 */
	protected int nodeCount;

	/**
	 * Creates a generator with default average degree of 1.
	 */
	public RandomGenerator() {
		this(1, true, false);
	}

	/**
	 * Creates a generator with given average degree.
	 * 
	 * @param averageDegree
	 *            The average degree of the resulting graph.
	 * @throws IllegalArgumentException
	 *             if <code>averageDegree</code> is negative
	 */
	public RandomGenerator(double averageDegree) {
		this(averageDegree, true, false);
	}

	/**
	 * Creates a generator with given average degree.
	 * 
	 * @param averageDegree
	 *            The average degree of the resulting graph.
	 * @param allowRemove
	 *            If true, some existing edges are removed at each step.
	 * @throws IllegalArgumentException
	 *             if <code>averageDegree</code> is negative
	 */
	public RandomGenerator(double averageDegree, boolean allowRemove) {
		this(averageDegree, allowRemove, false);
	}

	/**
	 * Creates a generator with given average degree.
	 * 
	 * @param averageDegree
	 *            The average degree of the resulting graph.
	 * @param allowRemove
	 *            If true, some existing edges are removed at each step.
	 * @param directed
	 *            If true, generated edges are directed.
	 * @throws IllegalArgumentException
	 *             if <code>averageDegree</code> is negative
	 */
	public RandomGenerator(double averageDegree, boolean allowRemove,
			boolean directed) {
		super(directed, true);
		init(averageDegree, allowRemove);
	}

	/**
	 * Creates a generator with given average degree
	 * 
	 * @param averageDegree
	 *            The average degree of the resulting graph.
	 * @param allowRemove
	 *            If true, some existing edges are removed at each step.
	 * @param directed
	 *            If true, generated edges are directed.
	 * @param nodeAttribute
	 *            An attribute with this name and a random numeric value is put
	 *            on each node.
	 * @param edgeAttribute
	 *            An attribute with this name and a random numeric value is put
	 *            on each edge.
	 * @throws IllegalArgumentException
	 *             if <code>averageDegree</code> is negative
	 */
	public RandomGenerator(double averageDegree, boolean allowRemove,
			boolean directed, String nodeAttribute, String edgeAttribute) {
		super(directed, true, nodeAttribute, edgeAttribute);
		init(averageDegree, allowRemove);
	}

	/**
	 * Starts the generator. A clique of size equal to the average degree is
	 * added.
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 * @complexity O(k<sup>2</sup>) where k is the average degree
	 */
	public void begin() {
		if (allowRemove)
			edgeIds = new ArrayList<String>();
		subset = new HashSet<Integer>();
		for (nodeCount = 0; nodeCount <= (int) averageDegree; nodeCount++)
			addNode(nodeCount + "");
		for (int i = 0; i < nodeCount; i++)
			for (int j = i + 1; j < nodeCount; j++) {
				String edgeId = i + "_" + j;
				addEdge(edgeId, i + "", j + "");
				if (allowRemove)
					edgeIds.add(edgeId);
			}
	}

	/**
	 * If edge removing is allowed, removes a small fraction of the existing
	 * edges. Then adds a new node and connects it randomly with some of the
	 * existing nodes.
	 * @return <code>true</code>
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 * @complexity Each call of this method takes on average O(k) steps, where k
	 *             is the average degree. Thus generating a graph with n nodes
	 *             will take O(nk) time. The space complexity is O(nk) if edge
	 *             removing is allowed and O(k) otherwise.
	 */
	public boolean nextEvents() {
		double addProbability = averageDegree / nodeCount;
		if (allowRemove)
			removeExistingEdges(1.0 / nodeCount);
		else
			addProbability /= 2;
		addNode(nodeCount + "");
		addNewEdges(addProbability);
		nodeCount++;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.BaseGenerator#end()
	 */
	@Override
	public void end() {
		super.end();
		if (allowRemove) {
			edgeIds.clear();
			edgeIds = null;
		}
		subset.clear();
		subset = null;
	}

	/**
	 * Constructor helper
	 * 
	 * @param averageDegree
	 *            The average degree of the resulting graph.
	 * @param allowRemove
	 *            If true, some existing edges are removed at each step.
	 */
	protected void init(double averageDegree, boolean allowRemove) {
		if (averageDegree < 0)
			throw new IllegalArgumentException(
					"The average degree must be non negative");
		this.averageDegree = averageDegree;
		this.allowRemove = allowRemove;
	}

	/**
	 * nextEvents helper. Adds edges between the freshly created node and the
	 * old nodes.
	 * 
	 * @param p
	 *            probability to add each possible edge
	 */
	protected void addNewEdges(double p) {
		RandomTools.randomPsubset(nodeCount, p, subset, random);
		String nodeId = nodeCount + "";
		subset.forEach(i -> {
			String edgeId = i + "_" + nodeId;
			addEdge(edgeId, i + "", nodeId);
			if (allowRemove)
				edgeIds.add(edgeId);
		});
	}

	/**
	 * nextEvents helper. Removes existing edges.
	 * 
	 * @param p
	 *            probability to remove each existing edge.
	 */
	protected void removeExistingEdges(double p) {
		RandomTools.randomPsubset(edgeIds.size(), p, subset, random);
		// Now we can't just remove all the edges with indices in the subset
		// because
		// (1) indices change after each removal and (2) shifting will increase
		// the complexity up to n^2
		// Instead we are filling the gaps with the elements in the end of the
		// list
		int last = edgeIds.size() - 1;
		while (!subset.isEmpty()) {
			if (subset.contains(last)) {
				subset.remove(last);
				delEdge(edgeIds.get(last));
			} else {
				Iterator<Integer> it = subset.iterator();
				int i = it.next();
				it.remove();
				delEdge(edgeIds.get(i));
				edgeIds.set(i, edgeIds.get(last));
			}
			edgeIds.remove(last);
			last--;
		}
	}
}
