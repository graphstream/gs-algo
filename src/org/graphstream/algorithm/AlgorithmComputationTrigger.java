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
 * @since 2012-02-10
 * 
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm;

import org.graphstream.stream.SinkAdapter;

/**
 * Provides a way to trigger the computation of an algorithm according to a type
 * of events.
 */
public class AlgorithmComputationTrigger extends SinkAdapter {
	/**
	 * Defines when the computation is triggered.
	 */
	public static enum Mode {
		BY_STEP
	}

	/**
	 * Mode of this trigger.
	 */
	protected Mode mode;

	/**
	 * Algorithm computed.
	 */
	protected Algorithm algo;

	public AlgorithmComputationTrigger(Mode mode, Algorithm algo) {
		this.mode = mode;
		this.algo = algo;
	}

	/**
	 * Set the trigger mode.
	 * 
	 * @param mode the trigger mode
	 */
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	/**
	 * Get the trigger mode.
	 * 
	 * @return the trigger mode
	 */
	public Mode getMode() {
		return mode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.graphstream.stream.SinkAdapter#stepBegins(java.lang.String,
	 * long, double)
	 */
	public void stepBegins(String sourceId, long timeId, double step) {
		switch (mode) {
		case BY_STEP:
			algo.compute();
			break;
		}
	}
}
