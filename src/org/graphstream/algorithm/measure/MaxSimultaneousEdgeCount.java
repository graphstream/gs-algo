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
 * @since 2010-10-01
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.measure;

import org.graphstream.stream.SinkAdapter;

/**
 * A measure to get the maximum count of edges appearing simultaneously in the
 * dynamic graph. It works as a sink, so it just needs to be added to the source
 * providing graph informations.
 * 
 * For example, in the graph :
 * 
 * <pre>
 * ae AB A B
 * ae BC B C
 * ae CD C D
 * de BC
 * ae DE D E
 * de CD
 * ae EF E F
 * ae FG F G
 * de EF
 * </pre>
 * 
 * the maximal count of edges appearing simultaneously in the graph is 4.
 */
public class MaxSimultaneousEdgeCount extends SinkAdapter {
	/**
	 * Current count of edges in the graph.
	 */
	protected int count;

	/**
	 * Max count of edges.
	 */
	protected int max;

	public MaxSimultaneousEdgeCount() {
		count = 0;
		max = 0;
	}

	/**
	 * Reset the max value.
	 */
	public void reset() {
		max = 0;
	}

	/**
	 * Get the max value.
	 * 
	 * @return the max count of edges appearing simultaneously in the graph
	 */
	public int getMaxSimultaneousEdgeCount() {
		return max;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#edgeAdded(java.lang.String, long,
	 * java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public void edgeAdded(String sourceId, long timeId, String edgeId,
			String node1, String node2, boolean directed) {
		count++;
		
		if (count > max)
			max = count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#edgeRemoved(java.lang.String,
	 * long, java.lang.String)
	 */
	public void edgeRemoved(String sourceId, long timeId, String edgeId) {
		count--;
	}

	/*
	 * (non-Javadoc)
	 * @see org.graphstream.stream.SinkAdapter#graphCleared(java.lang.String, long)
	 */
	public void graphCleared(String sourceId, long timeId) {
		count = 0;
	}
}
