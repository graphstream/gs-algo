package org.graphstream.algorithm.test;

import org.graphstream.algorithm.PageRank;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class DemoPageRank {
	public static void main(String[] args) throws InterruptedException {
		Graph graph = new SingleGraph("test");
		graph.addAttribute("ui.antialias", true);
		graph.addAttribute("ui.stylesheet",
				"node {fill-color: red; size-mode: dyn-size;} edge {fill-color:grey;}");
		graph.display();

		DorogovtsevMendesGenerator generator = new DorogovtsevMendesGenerator();
		generator.setDirectedEdges(true, true);
		generator.addSink(graph);

		PageRank pageRank = new PageRank();
		pageRank.setVerbose(true);
		pageRank.init(graph);

		generator.begin();
		while (graph.getNodeCount() < 100) {
			generator.nextEvents();
			for (Node node : graph) {
				double rank = pageRank.getRank(node);
				node.addAttribute("ui.size",
						5 + Math.sqrt(graph.getNodeCount() * rank * 20));
				node.addAttribute("ui.label",
						String.format("%.2f%%", rank * 100));
			}
			Thread.sleep(1000);
		}
	}

}
