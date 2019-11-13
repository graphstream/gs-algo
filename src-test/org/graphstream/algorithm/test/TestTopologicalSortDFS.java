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
 * @since 2016-10-14
 * 
 * @author jordilaforge <8899ph@web.de>
 */
package org.graphstream.algorithm.test;

import org.graphstream.algorithm.TopologicalSortDFS;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.SingleGraph;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestTopologicalSortDFS {

    Graph graph;

    @Before
    public void prepare() {
        graph = new SingleGraph("Graph");
        graph.addNode("0");
        graph.addNode("1");
        graph.addNode("2");
        graph.addNode("3");
        graph.addNode("4");
        graph.addNode("5");
        graph.addEdge("5-2", "5", "2", true);
        graph.addEdge("5-0", "5", "0", true);
        graph.addEdge("4-0", "4", "0", true);
        graph.addEdge("4-1", "4", "1", true);
        graph.addEdge("3-1", "3", "1", true);
        graph.addEdge("2-3", "2", "3", true);
    }

    @Test
    public void testTopologicalSortSmallGraph() {
        List<String> allPossibleTopologicalSort = new ArrayList<>();

        //all possible topological orderings
        allPossibleTopologicalSort.add("[4, 5, 0, 2, 3, 1]");
        allPossibleTopologicalSort.add("[4, 5, 2, 0, 3, 1]");
        allPossibleTopologicalSort.add("[4, 5, 2. 3. 0, 1]");
        allPossibleTopologicalSort.add("[4, 5, 2, 3, 1, 0]");

        allPossibleTopologicalSort.add("[5, 2, 3, 4, 0, 1]");
        allPossibleTopologicalSort.add("[5, 2, 3, 4, 1, 0]");
        allPossibleTopologicalSort.add("[5, 2, 4, 0, 3, 1]");
        allPossibleTopologicalSort.add("[5, 2, 4, 3, 0, l]");
        allPossibleTopologicalSort.add("[5, 2, 4, 3, 1, 0]");

        allPossibleTopologicalSort.add("[5, 4, 0, 2, 3, 1]");
        allPossibleTopologicalSort.add("[5, 4, 2, 8, 3, l]");
        allPossibleTopologicalSort.add("[5, 4, 2, 3, 0, 1]");
        allPossibleTopologicalSort.add("[5, 4, 2, 3, 1, 0]");
        TopologicalSortDFS sort = new TopologicalSortDFS();
        sort.init(graph);
        sort.compute();

        //check if algorithm gets one of the possible ordering
        Assert.assertTrue(allPossibleTopologicalSort.contains(Arrays.toString(sort.getSortedNodes().toArray())));
    }

    @Test(expected = IllegalStateException.class)
    public void testGraphWithCyclesShouldThrowException() {
        graph.addEdge("3-5", "3", "5", true);
        TopologicalSortDFS sort = new TopologicalSortDFS();
        sort.init(graph);
        sort.compute();
    }

    @Test(expected = IllegalStateException.class)
    public void testGraphWithNonDirectedEdgeShouldThrowException() {
        graph.addEdge("3-5", "3", "5");
        TopologicalSortDFS sort = new TopologicalSortDFS();
        sort.init(graph);
        sort.compute();
    }
}
