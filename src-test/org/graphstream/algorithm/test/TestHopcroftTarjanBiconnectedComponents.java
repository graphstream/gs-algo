package org.graphstream.algorithm.test;

import org.graphstream.algorithm.HopcroftTarjanBiconnectedComponents;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;

public class TestHopcroftTarjanBiconnectedComponents {

    @Test
    public void testBasic() throws Exception {
        Graph g = new DefaultGraph("g");
        load(g, "data/bcc-basic.dgs", true);

        HopcroftTarjanBiconnectedComponents bcc = new HopcroftTarjanBiconnectedComponents(g, g.getNode("C"));

        bcc.compute();

        check(bcc, createBCC("A", "B", "C"), createBCC("D", "E", "F"), createBCC("C", "D"));
    }

    @Test
    public void testSingleDFSStrand() {
        Graph g = new DefaultGraph("g");
        load(g, "data/bcc-basic.dgs", true);

        HopcroftTarjanBiconnectedComponents bcc = new HopcroftTarjanBiconnectedComponents(g);

        bcc.compute();

        check(bcc, createBCC("A", "B", "C"), createBCC("D", "E", "F"), createBCC("C", "D"));
    }

    @Test
    public void testSingleNode() {
        Graph g = new DefaultGraph("g");

        g.addNode("A");

        HopcroftTarjanBiconnectedComponents bcc = new HopcroftTarjanBiconnectedComponents(g);

        bcc.compute();

        check(bcc, createBCC("A"));
    }

    @Test
    public void testNodePair() {
        Graph g = new DefaultGraph("g");

        g.addNode("A");
        g.addNode("B");
        g.addEdge("AB","A","B");

        HopcroftTarjanBiconnectedComponents bcc = new HopcroftTarjanBiconnectedComponents(g);

        bcc.compute();

        check(bcc, createBCC("A","B"));
    }

    @Test
    public void testNormal() {
        Graph g = new DefaultGraph("g");
        load(g, "data/bcc-normal.dgs", true);

        HopcroftTarjanBiconnectedComponents bcc = new HopcroftTarjanBiconnectedComponents(g, g.getNode("H"));

        bcc.compute();

        check(bcc, createBCC("A", "B", "C", "D"), createBCC("D", "E"), createBCC("E", "F"), createBCC("F", "G"), createBCC("G", "H", "I", "J", "K", "L"), createBCC("G", "M"), createBCC("L", "N"));
    }

    @Test
    public void testNormalSubtreeWithoutCutVertex() {
        Graph g = new DefaultGraph("g");
        load(g, "data/bcc-normal-subtree-wo-cut-vertex.dgs", true);

        HopcroftTarjanBiconnectedComponents bcc = new HopcroftTarjanBiconnectedComponents(g, g.getNode("K"));

        bcc.compute();

        check(bcc, createBCC("A", "B", "C", "F"), createBCC("F", "G"), createBCC("G", "H", "I", "J", "K", "L"), createBCC("G", "M"), createBCC("L", "N"));
    }

    static FileSourceDGS load(Graph g, String dgsPath, boolean all) {
        FileSourceDGS dgs = new FileSourceDGS();
        InputStream in = TestHopcroftTarjanBiconnectedComponents.class.getResourceAsStream(dgsPath);

        dgs.addSink(g);

        if (all) {
            try {
                dgs.readAll(in);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        } else {
            try {
                dgs.begin(in);
            } catch (IOException e) {
                Assert.fail(e.getMessage());
            }
        }

        return dgs;
    }

    static String[] createBCC(String... nodes) {
        return nodes;
    }

    static void check(HopcroftTarjanBiconnectedComponents algo, String[]... bccs) {
        Assert.assertEquals(bccs.length, algo.getBiconnectedComponentsCount());

        for (int i = 0; i < bccs.length; i++) {
            for (int j = 1; j < bccs[i].length; j++) {
                ArrayList<HopcroftTarjanBiconnectedComponents.BiconnectedComponent> cc1 = algo.getBiconnectedComponentsOf(bccs[i][0]);
                ArrayList<HopcroftTarjanBiconnectedComponents.BiconnectedComponent> cc2 = algo.getBiconnectedComponentsOf(bccs[i][j]);

                Assert.assertFalse(Collections.disjoint(cc1, cc2));
            }

            for (int j = i + 1; j < bccs.length; j++) {
                ArrayList<HopcroftTarjanBiconnectedComponents.BiconnectedComponent> cc1 = algo.getBiconnectedComponentsOf(bccs[i][0]);
                ArrayList<HopcroftTarjanBiconnectedComponents.BiconnectedComponent> cc2 = algo.getBiconnectedComponentsOf(bccs[j][0]);

                Assert.assertTrue(Collections.disjoint(cc1, cc2) || (cc1.size() > 1 || cc2.size() > 1));
            }
        }
    }
}
