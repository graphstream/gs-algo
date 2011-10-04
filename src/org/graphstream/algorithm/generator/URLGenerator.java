/*
 * Copyright 2006 - 2011 
 *     Julien Baudry	<julien.baudry@graphstream-project.org>
 *     Antoine Dutot	<antoine.dutot@graphstream-project.org>
 *     Yoann Pigné		<yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin	<guilhelm.savin@graphstream-project.org>
 * 
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
 */
package org.graphstream.algorithm.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;

public class URLGenerator extends BaseGenerator {

	public static enum Mode {
		HOST, PATH, FULL
	}

	private static String REGEX = "href=\"([^\"]*)\"";

	HashSet<String> urls;
	LinkedList<String> stepUrls;
	Pattern hrefPattern;
	Mode mode;
	int threads = 4;

	public URLGenerator(String... startFrom) {
		urls = new HashSet<String>();
		stepUrls = new LinkedList<String>();
		hrefPattern = Pattern.compile(REGEX);
		mode = Mode.HOST;

		setUseInternalGraph(true);

		if (startFrom != null) {
			for (int i = 0; i < startFrom.length; i++) {
				urls.add(startFrom[i]);
				stepUrls.add(startFrom[i]);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#begin()
	 */
	public void begin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.algorithm.generator.Generator#nextEvents()
	 */
	public boolean nextEvents() {
		HashSet<String> newUrls;

		if (threads > 1)
			newUrls = nextEventsThreaded();
		else {
			newUrls = new HashSet<String>();

			for (String url : stepUrls) {
				try {
					parseUrl(url, newUrls);
				} catch (IOException e) {
					System.err.printf("Failed to parse \"%s\" : %s\n", url, e
							.getMessage());
				}
			}
		}

		stepUrls.clear();
		stepUrls.addAll(newUrls);

		return newUrls.size() > 0;
	}

	protected HashSet<String> nextEventsThreaded() {
		int t = Math.min(threads, stepUrls.size());
		int byThreads = stepUrls.size() / t;

		LinkedList<Worker> workers = new LinkedList<Worker>();
		LinkedList<Thread> workersThreads = new LinkedList<Thread>();

		HashSet<String> newUrls = new HashSet<String>();

		for (int i = 0; i < t; i++) {
			int start = i * byThreads;
			int stop = (i + 1) * byThreads;

			if (i == t - 1)
				stop += stepUrls.size() % t;

			Worker w = new Worker(start, stop, stepUrls);
			Thread u = new Thread(w);

			u.start();

			workers.add(w);
			workersThreads.add(u);

		}

		for (int i = 0; i < t; i++) {
			try {
				workersThreads.get(i).join();
				newUrls.addAll(workers.get(i).newUrls);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		return newUrls;
	}

	protected boolean isValid(String url) {
		if (url.matches("^(javascript:|mailto:|#).*"))
			return false;

		if (url.matches(".*[.](avi|tar|gz|zip|mp3|mpg|jpg|jpeg|png)$"))
			return false;

		return true;
	}

	protected void parseUrl(String url, HashSet<String> newUrls)
			throws IOException {
		URI uri;
		InputStream stream;
		BufferedReader reader;

		if (!isValid(url))
			return;

		try {
			uri = new URI(url);
		} catch (URISyntaxException e1) {
			throw new IOException(e1);
		}

		if (uri.getHost() == null) {
			System.err.printf("skip invalid uri : '%s'\n", url);
			return;
		}

		if (!uri.isAbsolute()) {
			System.err.printf("skip non-absolute uri : '%s'\n", url);
			return;
		}

		stream = uri.toURL().openStream();
		reader = new BufferedReader(new InputStreamReader(stream));

		while (reader.ready()) {
			String line = reader.readLine();
			Matcher m = hrefPattern.matcher(line);

			while (m.find()) {
				String href = m.group(1);

				if (href == null || href.length() == 0)
					continue;

				href = href.trim();

				if (href.charAt(0) == '/')
					href = String.format("%s://%s%s", uri.getScheme(), uri
							.getHost(), href);
				
				if (href.charAt(0) == '.')
					href = String.format("%s%s", url, href);

				//if (!newUrls.contains(href) && !urls.contains(href)) {
					try {
						synchronizedOperation(href, null);
						synchronizedOperation(url, href);
					} catch (URISyntaxException e) {
						throw new IOException(e);
					}

					newUrls.add(href);
				//}
			}
		}
	}

	protected String getNodeId(String url) throws URISyntaxException {
		String nodeId = url;
		URI uri = new URI(url);

		switch (mode) {
		case HOST:
			nodeId = String.format("%s://%s", uri.getScheme(), uri.getHost());
			break;
		case PATH:
			nodeId = String.format("%s://%s%s", uri.getScheme(), uri.getHost(),
					uri.getPath());
			break;
		case FULL:
			nodeId = String.format("%s://%s%s%s", uri.getScheme(), uri
					.getHost(), uri.getPath(), uri.getQuery());
			break;
		}

		return nodeId;
	}

	protected String getEdgeId(String nodeId1, String nodeId2) {
		if (nodeId1.compareTo(nodeId2) < 0)
			return String.format("%s > %s", nodeId1, nodeId2);

		return String.format("%s > %s", nodeId2, nodeId1);
	}

	protected synchronized void synchronizedOperation(String url1, String url2)
			throws URISyntaxException {
		if (url2 == null)
			addNodeURL(url1);
		else
			connect(url1, url2);
	}

	protected void addNodeURL(String url)
			throws URISyntaxException {
		String nodeId = getNodeId(url);

		urls.add(url);

		if (internalGraph.getNode(nodeId) == null) {
			addNode(nodeId);
			sendNodeAttributeAdded(sourceId, nodeId, "label", nodeId);
			// System.out.printf("> new url '%s'\n", nodeId);
		}

		Node n = internalGraph.getNode(nodeId);
		double w;

		if (n.hasNumber("weight"))
			w = n.getNumber("weight");
		else
			w = 0;

		n.setAttribute("weight", w + 1);
	}

	protected void connect(String url1, String url2) throws URISyntaxException {
		String src, trg, eid;

		src = getNodeId(url1);
		trg = getNodeId(url2);

		eid = getEdgeId(src, trg);

		if (internalGraph.getEdge(eid) == null)
			addEdge(eid, src, trg);
	}

	private class Worker implements Runnable {
		int start, stop;
		LinkedList<String> urls;
		HashSet<String> newUrls;

		public Worker(int start, int stop, LinkedList<String> urls) {
			this.start = start;
			this.stop = stop;
			this.urls = urls;
			this.newUrls = new HashSet<String>();
		}

		public void run() {
			for (int i = start; i < stop; i++)
				try {
					parseUrl(urls.get(i), newUrls);
				} catch (IOException e) {
					System.err.printf("Failed to parse \"%s\" : %s\n", urls
							.get(i), e.getMessage());
				}
		}
	}

	public static void main(String... args) {
		URLGenerator gen = new URLGenerator("http://graphstream-project.org");
		DefaultGraph g = new DefaultGraph("g");
		
		gen.addSink(g);
		g.display(true);
		
		gen.begin();
		while (gen.nextEvents())
			;
		gen.end();
	}
}
