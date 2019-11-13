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
 * @since 2014-05-18
 * 
 * @author beltex <cse93251@cse.yorku.ca>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

/**
 * Generator for a chain graph of any size.
 *
 * <p>
 * This generator creates a chain graph (much like a linked-list). By default,
 * the graph is directed and doubly linked (much like a doubly linked-list).
 * That is, each node has an edge to the next node in the chain, and one going
 * back to the previous (except for the first and last nodes). A loop back edge
 * can be added such that the last node in the chain has an edge directly back
 * to the starting node.
 * </p>
 *
 * <h2>Usage</h2>
 *
 * <p>
 * Calling {@link #begin()} will add an initial node with no edges. Each call
 * to {@link #nextEvents()} will add a new node, connected to the previous node,
 * in a chain. If the graph is directed and doubly linked, each call will result
 * in the addition of two new edges, otherwise just a single edge is added. Thus,
 * if the graph is undirected, then the doubly linked parameter is ignored, and
 * only single edges are created between nodes.
 * </p>
 *
 * <p>
 * If you are displaying the graph, directed and doubly-linked chain graphs are
 * best viewed with the {@code cubic-curve} edge shape CSS property, though it
 * will require the use of the <b>gs-ui</b> renderer to work.
 * </p>
 *
 * @since 2014
 */
public class ChainGenerator extends BaseGenerator {


    /**
     * Used to generate node names
     */
    protected int nodeNames = 0;


    /**
     * Create doubly linked nodes. That is, each node will have an extra edge
     * directed towards the previous node (except for the first and last node).
     * This does not apply if the graph is undirected.
     */
    protected boolean doublyLinked = true;


    /**
     * Create a loop back edge. That is, the last node has an edge looping back
     * to the starting node.
     */
    private boolean loopBack = false;


    /**
     * New chain generator. By default edges are directed, nodes doubly
     * linked, and there is no loop back edge.
     */
    public ChainGenerator() {
        this(true, true, false);
    }


    /**
     * New chain generator.
     *
     * @param directed Should the edges be directed?
     * @param doublyLinked Should the nodes be doubly linked?
     * @param loopBack Should the graph have a loop back edge?
     */
    public ChainGenerator(boolean directed, boolean doublyLinked, boolean loopBack) {
        super(directed, false);

        this.doublyLinked = doublyLinked;
        this.loopBack = loopBack;
    }


    /**
     * Add an initial node.
     *
     * @see org.graphstream.algorithm.generator.Generator#begin()
     */
    public void begin() {
        addNode(Integer.toString(nodeNames));
    }


    /**
     * Add a new node (link) in the chain.
     *
     * @see org.graphstream.algorithm.generator.Generator#nextEvents()
     */
    public boolean nextEvents() {
        String id_previous = Integer.toString(nodeNames);
        String id_next = Integer.toString(++nodeNames);

        addNode(id_next);
        addEdge(null, id_previous, id_next);

        // No point in adding double edge if undirected
        if (doublyLinked && directed) {
            addEdge(null, id_next, id_previous);
        }

        return true;
    }


    /**
     * If the graph is set to have a loop back edge, this method will add it.
     *
     * @see org.graphstream.algorithm.generator.Generator#end()
     */
    @Override
    public void end() {
        if (loopBack) {
            addEdge(null, Integer.toString(nodeNames), "0");
        }

        super.end();
    }
}
