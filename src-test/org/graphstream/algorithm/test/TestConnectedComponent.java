package org.graphstream.algorithm.test;

import java.io.IOException;
import java.io.InputStream;

import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.algorithm.ConnectedComponents.ConnectedComponent;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.stream.file.FileSourceDGS;
import org.junit.Assert;
import org.junit.Test;

public class TestConnectedComponent {

	@Test
	public void testBasic() {
		Graph g = new DefaultGraph("g");
		load(g, "data/cc-basic.dgs", true);

		ConnectedComponents cc = new ConnectedComponents();

		cc.init(g);
		cc.compute();

		check(cc, createCC("A", "B", "C"), createCC("D", "E", "F"));
	}

	@Test
	public void testEdgeDynamics() {
		Graph g = new DefaultGraph("g");

		load(g, "data/cc-basic.dgs", true);
		FileSourceDGS dgs = load(g, "data/cc-edge-dynamics.dgs", false);

		ConnectedComponents cc = new ConnectedComponents();

		cc.init(g);
		cc.compute();

		try {
			//
			// The two connected components wil be joined by an edge in this
			// next step.
			//
			dgs.nextStep();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		check(cc, createCC("A", "B", "C", "D", "E", "F"));

		try {
			//
			// Now we delete AB, AC, BC, DE, DF and we add CD.
			//
			dgs.nextStep();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		check(cc, createCC("A", "D", "C"), createCC("E", "F"), createCC("B"));
	}

	@Test
	public void testNodeDynamics() {
		Graph g = new DefaultGraph("g");

		load(g, "data/cc-basic.dgs", true);

		ConnectedComponents cc = new ConnectedComponents();

		cc.init(g);
		cc.compute();

		FileSourceDGS dgs = load(g, "data/cc-node-dynamics.dgs", false);

		try {
			//
			// We add a new node, linked to no one else.
			//
			dgs.nextStep();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		check(cc, createCC("A", "B", "C"), createCC("D", "E", "F"), createCC("G"));

		try {
			//
			// We link G to A.
			//
			dgs.nextStep();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		check(cc, createCC("A", "B", "C", "G"), createCC("D", "E", "F"));

		try {
			//
			// We remove A.
			//
			dgs.nextStep();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		check(cc, createCC("B", "C"), createCC("D", "E", "F"), createCC("G"));
	}

	@Test
	public void testEdgeCut() {
		Graph g = new DefaultGraph("g");

		load(g, "data/cc-edge-cut.dgs", true);

		ConnectedComponents cc = new ConnectedComponents();

		cc.init(g);
		cc.setCutAttribute("cut");
		cc.compute();

		check(cc, createCC("A", "B", "C"), createCC("D", "E", "F"));
	}
	
	@Test
	public void testEdgeCutDynamics() {
		Graph g = new DefaultGraph("g");

		load(g, "data/cc-edge-cut.dgs", true);

		ConnectedComponents cc = new ConnectedComponents();

		cc.init(g);
		cc.setCutAttribute("cut");
		cc.compute();

		FileSourceDGS dgs = load(g, "data/cc-edge-cut-dynamics.dgs", false);

		try {
			//
			// AD will not be a "but edge" anymore.
			//
			dgs.nextStep();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		check(cc, createCC("A", "B", "C", "D", "E", "F"));

		try {
			//
			// AD will be a cut edge.
			//
			dgs.nextStep();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}

		check(cc, createCC("A", "B", "C"), createCC("D", "E", "F"));
	}

	static FileSourceDGS load(Graph g, String dgsPath, boolean all) {
		FileSourceDGS dgs = new FileSourceDGS();
		InputStream in = TestConnectedComponent.class.getResourceAsStream(dgsPath);

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

	static String[] createCC(String... nodes) {
		return nodes;
	}

	static void check(ConnectedComponents algo, String[]... ccs) {
		Assert.assertEquals(ccs.length, algo.getConnectedComponentsCount());

		for (int i = 0; i < ccs.length; i++) {
			for (int j = 1; j < ccs[i].length; j++) {
				ConnectedComponent cc1 = algo.getConnectedComponentOf(ccs[i][0]);
				ConnectedComponent cc2 = algo.getConnectedComponentOf(ccs[i][j]);

				Assert.assertEquals(cc1, cc2);
			}

			for (int j = i + 1; j < ccs.length; j++) {
				ConnectedComponent cc1 = algo.getConnectedComponentOf(ccs[i][0]);
				ConnectedComponent cc2 = algo.getConnectedComponentOf(ccs[j][0]);

				Assert.assertNotEquals(cc1, cc2);
			}
		}
	}
}
