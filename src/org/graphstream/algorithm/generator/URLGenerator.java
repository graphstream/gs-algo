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
 * @since 2011-10-04
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 * @author Hicham Brahimi <hicham.brahimi@graphstream-project.org>
 */
package org.graphstream.algorithm.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Node;

/**
 * Generate a graph using the web. Some urls are given to start and the
 * generator will extract links on these pages. Each url is a node and there is
 * an edge between two urls when one has a link to the other. Links are
 * extracted using the "href" attribute of html elements.
 * 
 */
public class URLGenerator extends BaseGenerator {

	public static enum Mode {
		HOST, PATH, FULL
	}

	private static String REGEX = "href=\"([^\"]*)\"";

	protected HashSet<String> urls;
	protected LinkedList<String> stepUrls;
	protected HashSet<String> newUrls;
	protected Pattern hrefPattern;
	protected Mode mode;
	protected int threads = 2;
	protected String nodeWeight = "weight";
	protected String edgeWeight = "weight";
	protected LinkedList<URLFilter> filters;
	protected double step;
	protected boolean printProgress;
	protected int depthLimit;
	protected final ReentrantLock lock;

	public URLGenerator(String... startFrom) {
		urls = new HashSet<String>();
		stepUrls = new LinkedList<String>();
		newUrls = new HashSet<String>();
		hrefPattern = Pattern.compile(REGEX);
		mode = Mode.HOST;
		filters = new LinkedList<URLFilter>();
		directed = false;
		step = 0;
		printProgress = false;
		depthLimit = 0;
		lock = new ReentrantLock();

		declineMatchingURL("^(javascript:|mailto:|#).*");
		declineMatchingURL(".*[.](avi|tar|gz|zip|mp3|mpg|jpg|jpeg|png|ogg|flv|ico|svg)$");

		setUseInternalGraph(true);

		if (startFrom != null) {
			for (int i = 0; i < startFrom.length; i++) {
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
		sendStepBegins(sourceId, step);
		sendGraphAttributeChanged(sourceId, "urls.parsed", null, urls.size());
		sendGraphAttributeChanged(sourceId, "urls.remaining", null,
				stepUrls.size());

		if (printProgress)
			progress();
		
		stepUrls.forEach(url -> {
			try {
				addNodeURL(url);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		});
		
		urls.addAll(stepUrls);
		newUrls.clear();

		if (threads > 1)
			nextEventsThreaded();
		else {
			stepUrls.forEach(url -> {
				try {
					parseUrl(url);
				} catch (IOException e) {
					System.err.printf("Failed to parse \"%s\" : %s\n", url,
							e.getMessage());
				}
			});
		}

		stepUrls.clear();
		stepUrls.addAll(newUrls);
		step++;
		
		return newUrls.size() > 0;
	}

	/**
	 * Add an url to process.
	 * 
	 * @param url
	 *            a new url
	 */
	public void addURL(String url) {
		stepUrls.add(url);
	}

	/**
	 * Create directed edges.
	 * 
	 * @param on
	 *            true to create directed edges
	 */
	public void setDirected(boolean on) {
		setDirectedEdges(on, false);
	}

	/**
	 * Set the attribute key used to store weight of nodes. Whenever a node is
	 * reached, its weight is increased by one.
	 * 
	 * @param attribute
	 *            attribute key of the weight of nodes
	 */
	public void setNodeWeightAttribute(String attribute) {
		this.nodeWeight = attribute;
	}

	/**
	 * Set the attribute key used to store weight of edges. Whenever an edge is
	 * reached, its weight is increased by one.
	 * 
	 * @param attribute
	 *            attribute key of the weight of edges
	 */
	public void setEdgeWeightAttribute(String attribute) {
		this.edgeWeight = attribute;
	}

	/**
	 * Set the way that url are converted to node id. When mode is Mode.FULL,
	 * then the id is the raw url. With Mode.PATH, the query of the url is
	 * truncated so the url http://host/path?what=xxx will be converted as
	 * http://host/path. With Mode.HOST, the url is converted to the host name
	 * so the url http://host/path will be converted as http://host.
	 * 
	 * @param mode
	 *            mode specifying how to convert url to have node id
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * Set the amount of threads used to parse urls. Threads are created in the
	 * {@link #nextEvents()} step. At the end of this method, all working thread
	 * have stop.
	 * 
	 * @param count
	 *            amount of threads
	 */
	public void setThreadCount(int count) {
		this.threads = count;
	}

	/**
	 * Set the maximum steps before stop. If 0 or less, limit is disabled.
	 * 
	 * @param depthLimit maximum steps before stop
	 */
	public void setDepthLimit(int depthLimit) {
		this.depthLimit = depthLimit;
	}

	public void enableProgression(boolean on) {
		printProgress = on;
	}

	/**
	 * Can be used to filter url. Url not matching this regex will be discarded.
	 * 
	 * @param regex regex used to filter url
	 */
	public void acceptOnlyMatchingURL(final String regex) {
		URLFilter f = new URLFilter() {
			public boolean accept(String url) {
				if (url.matches(regex))
					return true;

				return false;
			}
		};

		filters.add(f);
	}

	/**
	 * Can be used to filter url. Url matching this regex will be discarded.
	 * 
	 * @param regex regex used to filter url
	 */
	public void declineMatchingURL(final String regex) {
		URLFilter f = new URLFilter() {
			public boolean accept(String url) {
				if (!url.matches(regex))
					return true;

				return false;
			}
		};

		filters.add(f);
	}

	/**
	 * Can be used to filter url according to the host. Note that several calls
	 * to this method may lead to discard all url. All hosts should be gived in
	 * a single call.
	 * 
	 * @param hosts
	 *            list of accepted hosts
	 */
	public void addHostFilter(String... hosts) {
		if (hosts != null) {
			StringBuilder b = new StringBuilder(
					"^(\\w+:)?(//)?([\\w-\\d]+[.])?(");
			b.append(hosts[0]);

			for (int i = 1; i < hosts.length; i++)
				b.append("|").append(hosts[i]);

			b.append(").*");

			acceptOnlyMatchingURL(b.toString());
		}
	}

	protected void nextEventsThreaded() {
		int t = Math.min(threads, stepUrls.size());
		int byThreads = stepUrls.size() / t;

		LinkedList<Worker> workers = new LinkedList<Worker>();
		LinkedList<Thread> workersThreads = new LinkedList<Thread>();

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
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	protected boolean isValid(String url) {
		for (int i = 0; i < filters.size(); i++) {
			if (!filters.get(i).accept(url))
				return false;
		}

		return true;
	}

	/**
	 * Parse an url and add all extracted links in a specified set.
	 * 
	 * @param url
	 *            the url to parse
	 * @throws IOException
	 * 			  exception if url is wrong
	 */
	protected void parseUrl(String url) throws IOException {
		URI uri;
		URLConnection conn;
		InputStream stream;
		BufferedReader reader;
		HashSet<String> localUrls = new HashSet<String>();

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

		conn = uri.toURL().openConnection();
		conn.setConnectTimeout(1000);
		conn.setReadTimeout(1000);
		conn.connect();

		if (conn.getContentType() == null
				|| !conn.getContentType().startsWith("text/html"))
			return;

		stream = conn.getInputStream();
		reader = new BufferedReader(new InputStreamReader(stream));

		while (reader.ready()) {
			String line = reader.readLine();

			if (line == null)
				continue;

			Matcher m = hrefPattern.matcher(line);

			while (m.find()) {
				String href = m.group(1);

				if (href == null || href.length() == 0)
					continue;

				href = href.trim();

				if (href.charAt(0) == '/')
					href = String.format("%s://%s%s", uri.getScheme(),
							uri.getHost(), href);

				if (href.charAt(0) == '.')
					href = String.format("%s%s", url, href);

				if (!isValid(href))
					continue;

				try {
					if (depthLimit == 0 || step < depthLimit) {
						synchronizedOperation(href, null);
						synchronizedOperation(url, href);
					} else {
						if (urls.contains(href))
							synchronizedOperation(url, href);
					}
				} catch (URISyntaxException e) {
					throw new IOException(e);
				}

				if (!urls.contains(href)
						&& (depthLimit == 0 || step < depthLimit))
					localUrls.add(href);
			}
		}

		lock.lock();

		try {
			newUrls.addAll(localUrls);
		} finally {
			lock.unlock();
		}

		localUrls.clear();
		localUrls = null;

		try {
			if (conn.getDoOutput())
				conn.getOutputStream().close();
			reader.close();
			stream.close();
		} catch (IOException e) {
			// Do not throw this exception
		}

		if (conn instanceof HttpURLConnection)
			((HttpURLConnection) conn).disconnect();
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
					.getHost(), uri.getPath(), uri.getQuery() == null ? ""
					: uri.getQuery());
			break;
		}

		return nodeId;
	}

	protected String getNodeLabel(String url) throws URISyntaxException {
		return url;
	}

	protected String getEdgeId(String nodeId1, String nodeId2) {
		if (directed || nodeId1.compareTo(nodeId2) < 0)
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

	protected void addNodeURL(String url) throws URISyntaxException {
		String nodeId = getNodeId(url);

		// urls.add(url);

		if (internalGraph.getNode(nodeId) == null) {
			addNode(nodeId);
			sendNodeAttributeAdded(sourceId, nodeId, "label", getNodeLabel(url));
			// System.out.printf("> new url '%s' --> '%s'\n", url, nodeId);
		}

		Node n = internalGraph.getNode(nodeId);
		double w;

		if (n.hasNumber(nodeWeight))
			w = n.getNumber(nodeWeight);
		else
			w = 0;

		n.setAttribute(nodeWeight, w + 1);
		sendNodeAttributeChanged(sourceId, nodeId, nodeWeight, null, w + 1);
	}

	protected void connect(String url1, String url2) throws URISyntaxException {
		String src, trg, eid;

		src = getNodeId(url1);
		trg = getNodeId(url2);

		if (internalGraph.getNode(src) == null)
			addNode(src);

		if (internalGraph.getNode(trg) == null)
			addNode(trg);

		if (!src.equals(trg)) {
			eid = getEdgeId(src, trg);

			if (internalGraph.getEdge(eid) == null)
				addEdge(eid, src, trg);

			Edge e = internalGraph.getEdge(eid);
			double w;

			if (e.hasNumber(edgeWeight))
				w = e.getNumber(edgeWeight);
			else
				w = 0;

			e.setAttribute(edgeWeight, w + 1);
			sendEdgeAttributeChanged(sourceId, eid, edgeWeight, null, w + 1);
		}
	}

	protected void progress() {
		System.out.printf("\033[s\033[K%d urls parsed, %d remaining\033[u",
				urls.size(), stepUrls.size());
	}

	/*
	 * Private class used to distribute url parsing.
	 */
	private class Worker implements Runnable {
		int start, stop;
		LinkedList<String> urls;

		public Worker(int start, int stop, LinkedList<String> urls) {
			this.start = start;
			this.stop = stop;
			this.urls = urls;
		}

		public void run() {
			for (int i = start; i < stop; i++) {
				try {
					parseUrl(urls.get(i));
				} catch (IOException e) {
					System.err.printf("Failed to parse \"%s\" : %s\n",
							urls.get(i), e.getMessage());
				}
			}
		}
	}

	/**
	 * Defines url filter.
	 */
	public static interface URLFilter {
		/**
		 * Called by the generator to know if the specified url can be accepted
		 * by this filter. If a filter return false, then the url is discarded.
		 * 
		 * @param url
		 *            the url to check if it can be accepted
		 * @return true if the url is accepted
		 */
		boolean accept(String url);
	}
}
