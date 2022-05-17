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
 * @author Yoann Pigné <yoann.pigne@graphstream-project.org>
 * @author Antoine Dutot <antoine.dutot@graphstream-project.org>
 * @author Guillaume-Jean Herbiet <guillaume-jean@herbiet.net>
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm;

import org.graphstream.algorithm.util.Result;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Structure;

import java.util.*;
import java.util.stream.Stream;

/**
 * Compute the number of biconnected components of a graph
 * according to the algorithm by Hopcroft and Tarjan (See https://doi.org/10.1145%2F362248.362272)
 * using a depth-first approach
 *
 * <h2>An overview can be found on <a href=
 * "https://en.wikipedia.org/wiki/Biconnected_component"
 * >Wikipedia</a></h2>
 *
 * The algorithm first calculates via a depth first search approach so called articulation points or cut vertices that, when removed, split the graph into separate pieces.
 * Once the articulation points are known, from each articulation points' neighbor that has a higher lowpoint than the articulation points depth, a biconnected is formed from the articulation point, the neighbor and the subtree from that neighbor.
 * For the root node this computation is also done, but if it is not an articulation point a biconnected component simply is the tree from the root node until other biconnected components are reached.
 *
 * <p>
 * This algorithm computes the biconnected components for a given graph. Biconnected
 * components are the set of its maximal biconnected subgraphs,
 * for which every one contained node can be removed
 * without splitting the subgraph further. When two nodes belong to the
 * same biconnected component there exist at least two paths (without considering the
 * direction of the edges) between them. The algorithm does not
 * consider the direction of the edges.
 * </p>
 * <p>
 *
 * <h2>Usage</h2>
 *
 * <p>
 * To start using the algorithm, you first need an instance of
 * {@link Graph}, then you only have to instantiate the
 * algorithm class. You can also specify a starting {@link Node} for the algorithm, otherwise the first node will be chosen. You can specify a reference to the graph in the
 * constructor or you set it with the {@link #init(Graph)} method.
 * </p>
 *
 * <p>
 * The computation of the algorithm starts only when the graph is specified with
 * the {@link #init(Graph)} method or with the appropriated constructor. In case
 * of a static graph, you may call the {@link #compute()} method. In case of a
 * dynamic graph, the algorithm will compute itself automatically when an event
 * (node or edge added or removed) occurs.
 * </p>
 *
 * <p>
 * You may ask the algorithm for the number of biconnected components at
 * any moment with a call to the {@link #getBiconnectedComponentsCount()} method.
 * </p>
 *
 *
 * <h2>Additional features</h2>
 *
 *
 * <h3>Giant component</h3>
 * <p>
 * The {@link #getGiantComponent()} method gives you a list of nodes belonging
 * to the biggest biconnected component of the graph.
 * </p>
 *
 * <p>
 * Note that setting the cut attribute will trigger a new computation of the
 * algorithm.
 * </p>
 *
 * @author Max Kißgen
 * @complexity For the articulation points, let n be the number of nodes, then
 * the time complexity is 0(n). For the re-optimization steps, let k be
 * the number of nodes concerned by the changes (k <= n), the
 * complexity is O(k).
 * @since May 05 2022
 */
public class HopcroftTarjanBiconnectedComponents implements Algorithm {
    protected HashSet<BiconnectedComponent> components;
    protected HashMap<Node, ArrayList<BiconnectedComponent>> componentsMap;
    protected Graph graph;

    /**
     * Optional attribute to set on each node of a given component. This
     * attribute will have for value an index different for each component.
     */
    protected String countAttribute;

    /**
     * Flag used to tell if the {@link #compute()} method has already been
     * called.
     */
    protected boolean started;

    /**
     * Used to get components index.
     */
    protected int currentComponentId;


    /**
     * Map of node depths from Node index to Node depth
     */
    protected HashMap<Integer, Integer> nodeDepths;

    /**
     * Map of node lowpoints from Node index to Node lowpoints
     */
    protected HashMap<Integer, Integer> nodeLowpoints;

    /**
     * Map of node depths from Node index to Node parent index
     */
    protected HashMap<Integer, Integer> nodeParents;

    /**
     * Map of node depths from Node index to Boolean
     */
    protected HashMap<Integer, Boolean> nodeArticulationPoints;

    /**
     * Node to start computation from
     */
    protected Node root;

    /**
     * Build a new biconnected component algorithm
     */
    public HopcroftTarjanBiconnectedComponents() {
        nodeDepths = new HashMap<Integer, Integer>();
        nodeLowpoints = new HashMap<Integer, Integer>();
        nodeParents = new HashMap<Integer, Integer>();
        nodeArticulationPoints = new HashMap<Integer, Boolean>();
        components = new HashSet<BiconnectedComponent>();
        componentsMap = new HashMap<Node, ArrayList<BiconnectedComponent>>();
        this.started = false;
    }

    /**
     * Build a new biconnected component algorithm
     *
     * @param graph the graph to perform computation on
     */
    public HopcroftTarjanBiconnectedComponents(Graph graph) {
        nodeDepths = new HashMap<Integer, Integer>();
        nodeLowpoints = new HashMap<Integer, Integer>();
        nodeParents = new HashMap<Integer, Integer>();
        nodeArticulationPoints = new HashMap<Integer, Boolean>();
        components = new HashSet<BiconnectedComponent>();
        componentsMap = new HashMap<Node, ArrayList<BiconnectedComponent>>();
        this.started = false;

        init(graph);
    }

    public HopcroftTarjanBiconnectedComponents(Graph graph, Node node) {
        root = node;
        nodeDepths = new HashMap<Integer, Integer>();
        nodeLowpoints = new HashMap<Integer, Integer>();
        nodeParents = new HashMap<Integer, Integer>();
        nodeArticulationPoints = new HashMap<Integer, Boolean>();
        components = new HashSet<BiconnectedComponent>();
        componentsMap = new HashMap<Node, ArrayList<BiconnectedComponent>>();
        this.started = false;

        init(graph);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.graphstream.algorithm.Algorithm#init(org.graphstream.graph.Graph)
     */
    @Override
    public void init(Graph graph) {
        this.graph = graph;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.graphstream.algorithm.Algorithm#compute()
     */
    @Override
    public void compute() {
        started = true;

        if (graph.getNodeCount() != 0) {


            if (root == null) {
                root = graph.getNode(0);
            }
            calculateArticulationPoints(root, 0);
            calculateBiconnectedComponents(root);
        }
    }

    /**
     * Recursively compute the articulation points starting at `from`.
     *
     * @param from  The node we start from
     * @param depth the nodes' depth in the DFS tree
     */
    protected void calculateArticulationPoints(Node from, Integer depth) {

        nodeDepths.put(from.getIndex(), depth);
        nodeLowpoints.put(from.getIndex(), depth);
        boolean isArticulationPoint = false;
        int childCount = 0;

        Iterator<Node> neighborIt = from.neighborNodes().iterator();
        while (neighborIt.hasNext()) {
            Node neighbor = neighborIt.next();
            if (nodeDepths.get(neighbor.getIndex()) == null) {
                nodeParents.put(neighbor.getIndex(), from.getIndex());
                calculateArticulationPoints(neighbor, depth + 1);
                childCount++;

                if (nodeLowpoints.get(neighbor.getIndex()) >= nodeDepths.get(from.getIndex())) {
                    isArticulationPoint = true;
                }
                nodeLowpoints.put(from.getIndex(), Math.min(nodeLowpoints.get(from.getIndex()), nodeLowpoints.get(neighbor.getIndex())));
            } else if (nodeParents.get(from.getIndex()) == null || nodeParents.get(from.getIndex()) != neighbor.getIndex()) {
                nodeLowpoints.put(from.getIndex(), Math.min(nodeLowpoints.get(from.getIndex()), nodeDepths.get(neighbor.getIndex())));
            }
        }

        if ((nodeParents.get(from.getIndex()) == null && childCount > 1) || (nodeParents.get(from.getIndex()) != null && isArticulationPoint)) {
            nodeArticulationPoints.put(from.getIndex(), true);
        }
    }

    /**
     * Using the articulation points, compute the biconnected components iteratively
     *
     * <p>
     * We use here the {@link BiconnectedComponent#registerNode(Node)} method
     * which will update the {@link #componentsMap} and the size of the
     * biconnected component
     * @param root The root node of the DFS that computed the articulation points
     */
    protected void calculateBiconnectedComponents(Node root) {
        HashMap<Integer, Boolean> visitedNormalNodes = new HashMap<Integer, Boolean>(); // All special children are nodes adjacent to an articulation point that have a smaller lowpoint than the articulation points depth

        for (Integer currentNodeIndex : nodeArticulationPoints.keySet()) {
            Node currentNode = graph.getNode(currentNodeIndex);
            HashMap<Integer, Boolean> visitedArticulationPoints = new HashMap<Integer, Boolean>(); // Articulation points can (and have to) be in multiple biconnected components, therefore only allow one shared component for every pair of them

            Iterator<Node> lowpointNeighbors = currentNode.neighborNodes()
                    .filter(n -> (visitedNormalNodes.get(n.getIndex()) == null) && nodeLowpoints.get(n.getIndex()) >= nodeDepths.get(currentNode.getIndex()))
                    .iterator(); // get all neighbors with larger lowpoints than current articulation points' depth
            while (lowpointNeighbors.hasNext()) {
                Node neighbor = lowpointNeighbors.next();

                BiconnectedComponent bcc = createBiconnectedComponent(visitedNormalNodes, visitedArticulationPoints, currentNode, neighbor);

                if (bcc.size != 0) { // Only add component if component didn't contain only previously visited articulation points
                    components.add(bcc);
                }
            }
        }

        //Do last iteration for root node
        if (nodeArticulationPoints.get(root.getIndex()) == null) {
            HashMap<Integer, Boolean> visitedArticulationPoints = new HashMap<Integer, Boolean>();

            BiconnectedComponent bcc = createBiconnectedComponent(visitedNormalNodes, visitedArticulationPoints, root, root);

            components.add(bcc);
        }
    }

    /**
     * Creates a biconnected component by first adding one initial node and from a node starting point adding all other nodes in the tree, each branch ending when either articulation points or already visited nodes are hit.
     * @param visitedNormalNodes A Hashmap of non-articulation point nodes Indexes (Integer) and Booleans whether they are visited or not (To save memory non visited wont even have an entry)
     * @param visitedArticulationPoints A Hashmap of articulation point nodes Indexes (Integer) and Booleans whether they are visited or not (To save memory non visited wont even have an entry)
     * @param first The first node of the biconnected component
     * @param startingSearchFrom The node to start the visiting other nodes from. Can be the same as first
     * @return the completed biconnected Component.
     */
    protected BiconnectedComponent createBiconnectedComponent(HashMap<Integer, Boolean> visitedNormalNodes, HashMap<Integer, Boolean> visitedArticulationPoints, Node first, Node startingSearchFrom) {
        BiconnectedComponent bcc = new BiconnectedComponent();
        bcc.registerNode(first);

        LinkedList<Node> open = new LinkedList<Node>();
        open.add(startingSearchFrom);
        while (!open.isEmpty()) {
            Node n = open.poll();
            if (nodeArticulationPoints.get(n.getIndex()) == null) { // If we dont have an articulation point here we can add further neighbors
                visitedNormalNodes.put(n.getIndex(), true);
                open.addAll(Arrays.asList(n.neighborNodes().filter(child -> (child != first
                        && visitedNormalNodes.get(child.getIndex()) == null
                        && visitedArticulationPoints.get(child.getIndex()) == null)).toArray(Node[]::new))
                ); // Add all still unvisited neighbors and articulation points that are unvisited for the "first" node
            }
            if (visitedArticulationPoints.get(n.getIndex()) == null) {
                bcc.registerNode(n);
                if (nodeArticulationPoints.get(n.getIndex()) != null) {
                    visitedArticulationPoints.put(n.getIndex(), true);
                }
            }
        }

        return bcc;
    }

    /**
     * Get the biconnected component that contains the biggest number of nodes.
     *
     * @return the biggest BCC.
     */
    public BiconnectedComponent getGiantComponent() {
        checkStarted();

        BiconnectedComponent maxBCC = null;

        maxBCC = components.stream()
                .max((bcc1, bcc2) -> Integer.compare(bcc1.size, bcc2.size))
                .get();

        return maxBCC;
    }

    /**
     * Ask the algorithm for the number of biconnected components.
     *
     * @return the number of biconnected components in this graph.
     */
    public int getBiconnectedComponentsCount() {
        checkStarted();

        return components.size();
    }

    @Result
    public String defaultResult() {
        return getBiconnectedComponentsCount() + " biconnected component(s) in this graph";
    }

    /**
     * Ask the algorithm for the number of biconnected components whose size is
     * equal to or greater than the specified threshold.
     *
     * @param sizeThreshold Minimum size for the biconnected component to be considered
     * @return the number of biconnected components, bigger than the given size
     * threshold, in this graph.
     */
    public int getBiconnectedComponentsCount(int sizeThreshold) {
        return getBiconnectedComponentsCount(sizeThreshold, 0);
    }

    /**
     * Ask the algorithm for the number of biconnected components whose size is
     * equal to or greater than the specified threshold and lesser than the
     * specified ceiling.
     *
     * @param sizeThreshold Minimum size for the biconnected component to be considered
     * @param sizeCeiling   Maximum size for the biconnected component to be considered (use
     *                      0 or lower values to ignore the ceiling)
     * @return the number of biconnected components, bigger than the given size
     * threshold, and smaller than the given size ceiling, in this
     * graph.
     */
    public int getBiconnectedComponentsCount(int sizeThreshold, int sizeCeiling) {
        checkStarted();

        //
        // Simplest case : threshold is lesser than or equal to 1 and
        // no ceiling is specified, we return all the counted components
        //
        if (sizeThreshold <= 1 && sizeCeiling <= 0) {
            return components.size();
        } else {
            int count = 0;

            count = (int) components.stream()
                    .filter(bcc -> (bcc.size >= sizeThreshold && (sizeCeiling <= 0 || bcc.size < sizeCeiling)))
                    .count();

            return count;
        }
    }

    /**
     * Return the biconnected component where a node belonged. The validity of the
     * result ends if any new computation is done. So you will have to call this
     * method again to be sure you are manipulating the good component.
     *
     * @param n a node
     * @return the biconnected component containing `n`
     */
    public ArrayList<BiconnectedComponent> getBiconnectedComponentsOf(Node n) {
        return n == null ? null : componentsMap.get(n);
    }

    /**
     * Same as {@link #getBiconnectedComponentsOf(Node)} but using the node id.
     *
     * @param nodeId a node id
     * @return the biconnected component containing the node `nodeId`
     */
    public ArrayList<BiconnectedComponent> getBiconnectedComponentsOf(String nodeId) {
        return getBiconnectedComponentsOf(graph.getNode(nodeId));
    }

    /**
     * Same as {@link #getBiconnectedComponentsOf(Node)} but using the node index.
     *
     * @param nodeIndex a node index
     * @return the biconnected component containing the node `nodeIndex`
     */
    public ArrayList<BiconnectedComponent> getBiconnectedComponentOf(int nodeIndex) {
        return getBiconnectedComponentsOf(graph.getNode(nodeIndex));
    }


    protected void checkStarted() {
        if (!started && graph != null) {
            compute();
        }
    }

    /**
     * A representation of a biconnected component. These objects are used to
     * store informations about components and to allow to iterate over all
     * nodes of a same component.
     * <p>
     * You can retrieve these objects using the
     * {@link HopcroftTarjanBiconnectedComponents#getBiconnectedComponentsOf(Node)} methods of the
     * algorithm.
     */
    public class BiconnectedComponent implements Structure {
        /**
         * The unique id of this component.
         * <p>
         * The uniqueness of the id is local to an instance of the
         * {@link HopcroftTarjanBiconnectedComponents} algorithm.
         */
        public final int id = currentComponentId++;

        int size;

        BiconnectedComponent() {
            this.size = 0;

        }

        void registerNode(Node n) {
            componentsMap.computeIfAbsent(n, k -> new ArrayList<BiconnectedComponent>());
            componentsMap.get(n).add(this);

            if (countAttribute != null) {
                n.setAttribute(countAttribute, id);
            }

            size++;
        }

        void unregisterNode(Node n) {
            size--;

            if (size == 0) {
                components.remove(this);
            }
        }

        /**
         * Return an stream over the nodes of this component.
         *
         * @return an stream over the nodes of this component
         */

        public Stream<Node> nodes() {
            return graph.nodes().filter(n -> componentsMap.get(n).contains(BiconnectedComponent.this));
        }


        /**
         * Get a set containing all the nodes of this component.
         * <p>
         * A new set is built for each call to this method, so handle with care.
         *
         * @return a new set of nodes belonging to this component
         */
        public Set<Node> getNodeSet() {
            HashSet<Node> nodes = new HashSet<Node>();

            nodes().forEach(n -> nodes.add(n));

            return nodes;
        }

        /**
         * Return an stream over the edge of this component.
         * <p>
         * An edge is in the component if the two ends of this edges are in the
         * component and the edge does not have the cut attribute. Note that,
         * using cut attribute, some edges can be in none of the components.
         *
         * @return an stream over the edges of this component
         */
        public Stream<Edge> edges() {
            return graph.edges().filter(e -> {
                return (componentsMap.get(e.getNode0()).contains(BiconnectedComponent.this))
                        && (componentsMap.get(e.getNode1()).contains(BiconnectedComponent.this));
            });
        }

        /**
         * Test if this component contains a given node.
         *
         * @param n a node
         * @return true if the node is in this component
         */
        public boolean contains(Node n) {
            return componentsMap.get(n).contains(this);
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("BiconnectedComponent#%d", id);
        }

        @Override
        public int getNodeCount() {
            return (int) nodes().count();
        }

        @Override
        public int getEdgeCount() {
            return (int) edges().count();
        }
    }
}
