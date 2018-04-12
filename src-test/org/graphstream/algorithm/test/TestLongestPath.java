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
 * @since 2017-11-30
 * 
 * @author jordilaforge <8899ph@web.de>
 */
package org.graphstream.algorithm.test;

import org.graphstream.algorithm.LongestPath;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.Path;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class TestLongestPath {

    private Graph getTestSmallDigraph() {
        Graph graph = new SingleGraph("Graph");
        graph.addNode("S");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addEdge("S-A", "S", "A", true);
        graph.addEdge("S-C", "S", "C", true);
        graph.addEdge("A-B", "A", "B", true);
        graph.addEdge("B-D", "B", "D", true);
        graph.addEdge("B-E", "B", "E", true);
        graph.addEdge("C-A", "C", "A", true);
        graph.addEdge("C-D", "C", "D", true);
        graph.addEdge("D-E", "D", "E", true);
        return graph;
    }

    private Graph getTestSmallDigraphWeighted(String weightAttribute) {
        Graph graph = new SingleGraph("Graph");
        graph.addNode("S");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        Edge sa = graph.addEdge("S-A", "S", "A", true);
        sa.setAttribute(weightAttribute, 1.0);
        Edge sc = graph.addEdge("S-C", "S", "C", true);
        sc.setAttribute(weightAttribute, 2.0);
        Edge ab = graph.addEdge("A-B", "A", "B", true);
        ab.setAttribute(weightAttribute, 6.0);
        Edge bd = graph.addEdge("B-D", "B", "D", true);
        bd.setAttribute(weightAttribute, 1.0);
        Edge be = graph.addEdge("B-E", "B", "E", true);
        be.setAttribute(weightAttribute, 2.0);
        Edge ca = graph.addEdge("C-A", "C", "A", true);
        ca.setAttribute(weightAttribute, 4.0);
        Edge cd = graph.addEdge("C-D", "C", "D", true);
        cd.setAttribute(weightAttribute, 3.0);
        Edge de = graph.addEdge("D-E", "D", "E", true);
        de.setAttribute(weightAttribute, 1.0);
        return graph;
    }

    private Graph getTestBiggerDigraph() {
        Graph graph = new SingleGraph("Graph");
        graph.addNode("A");
        graph.addNode("B");
        graph.addNode("C");
        graph.addNode("D");
        graph.addNode("E");
        graph.addNode("F");
        graph.addNode("G");
        graph.addNode("H");
        graph.addNode("I");
        graph.addEdge("A-B", "A", "B", true);
        graph.addEdge("B-C", "B", "C", true);
        graph.addEdge("B-D", "B", "D", true);
        graph.addEdge("C-H", "C", "H", true);
        graph.addEdge("C-G", "C", "G", true);
        graph.addEdge("D-E", "D", "E", true);
        graph.addEdge("D-F", "D", "F", true);
        graph.addEdge("E-G", "E", "G", true);
        graph.addEdge("E-H", "E", "H", true);
        graph.addEdge("F-I", "F", "I", true);
        graph.addEdge("G-H", "G", "H", true);
        graph.addEdge("H-I", "H", "I", true);
        return graph;
    }

    @Test
    public void testLongestPathSmallGraph() {
        Graph graph = getTestSmallDigraph();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        List<Node> longestPath = path.getLongestPathList();
        Assert.assertEquals("[S, C, A, B, D, E]", longestPath.toString());
    }

    @Test
    public void testLongestPathBiggerGraph() {
        Graph graph = getTestBiggerDigraph();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        List<Node> longestPath = path.getLongestPathList();
        Assert.assertEquals("[A, B, D, E, G, H, I]", longestPath.toString());
    }

    @Test(expected = IllegalStateException.class)
    public void testCycledGraph() {
        Graph graph = getTestBiggerDigraph();
        graph.addEdge("H-G", "H", "G", true);
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
    }

    @Test
    public void testLongestPathHops() {
        Graph graph = getTestBiggerDigraph();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        Assert.assertEquals(Double.valueOf(6.0), path.getLongestPathValue());
    }

    @Test
    public void testLongestPathSmallGraphWeighted() {
        Graph graph = getTestSmallDigraphWeighted(LongestPath.DEFAULT_WEIGHT_ATTRIBUTE);
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        List<Node> longestPath = path.getLongestPathList();
        Assert.assertEquals("[S, C, A, B, D, E]", longestPath.toString());
    }

    @Test
    public void testLongestPathWeighted() {
        Graph graph = getTestSmallDigraphWeighted(LongestPath.DEFAULT_WEIGHT_ATTRIBUTE);
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        Assert.assertEquals(Double.valueOf(14.0), path.getLongestPathValue());
    }

    @Test
    public void testLongestPathSmallGraphType() {
        Graph graph = getTestSmallDigraph();
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        Path longestPath = path.getLongestPath();
        Assert.assertEquals("[S, C, A, B, D, E]", longestPath.toString());
    }

    @Test
    public void testSetWeightAttribute() {
        String weigthAttribute = "test";
        Graph graph = getTestSmallDigraphWeighted(weigthAttribute);
        LongestPath path = new LongestPath();
        path.setWeightAttribute(weigthAttribute);
        path.init(graph);
        path.compute();
        Path longestPath = path.getLongestPath();
        for (Edge edge : longestPath.getEdgeSet()) {
            Assert.assertTrue(edge.hasAttribute(weigthAttribute));
        }
        Assert.assertEquals(weigthAttribute,path.getWeightAttribute());
    }

    @Test
    public void testDefaultWeightAttribute() {
        Graph graph = getTestSmallDigraphWeighted(LongestPath.DEFAULT_WEIGHT_ATTRIBUTE);
        LongestPath path = new LongestPath();
        path.init(graph);
        path.compute();
        Path longestPath = path.getLongestPath();
        for (Edge edge : longestPath.getEdgeSet()) {
            Assert.assertTrue(edge.hasAttribute(LongestPath.DEFAULT_WEIGHT_ATTRIBUTE));
        }
    }

}