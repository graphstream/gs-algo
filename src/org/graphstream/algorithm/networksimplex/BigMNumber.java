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
 * @since 2011-12-04
 * 
 * @author Stefan Balev <stefan.balev@graphstream-project.org>
 * @author Guilhelm Savin <guilhelm.savin@graphstream-project.org>
 */
package org.graphstream.algorithm.networksimplex;

/**
 * This class is used to present numbers of the form a + bM, where M is
 * "big enough". A helper class for the network simplex method
 * 
 * @author Stefan Balev
 * 
 */
class BigMNumber {
	protected long small;
	protected long big;

	public BigMNumber() {
		set(0, 0);
	}
	
	public BigMNumber(long small) {
		set(small, 0);
	}

	public void set(long small, long big) {
		this.small = small;
		this.big = big;
	}

	public void set(BigMNumber b) {
		set(b.small, b.big);
	}
	
	public void set(long small) {
		set(small, 0);
	}

	public void plus(BigMNumber b) {
		small += b.small;
		big += b.big;
	}

	public void minus(BigMNumber b) {
		small -= b.small;
		big -= b.big;
	}

	public void minus() {
		small = -small;
		big = -big;
	}
	
	public void plusTimes(int multiplier, BigMNumber b) {
		small += multiplier * b.small;
		big += multiplier * b.big;
	}
	
	public boolean isNegative() {
		return big < 0 || (big == 0 && small < 0);
	}
	
	public boolean isInfinite() {
		return big != 0;
	}
	
	public int compareTo(BigMNumber b) {
		if (big < b.big)
			return -1;
		if (big > b.big)
			return 1;
		if (small < b.small)
			return -1;
		if (small > b.small)
			return 1;
		return 0;
	}
	
	public long getSmall() {
		return small;
	}
	
	@Override
	public String toString() {
		if (big == 0)
			return small + "";
		String r = "";
		if (small != 0) {
			r += small;
			if (big > 0)
				r += "+";
		}
		if (big == -1)
			r += "-";
		else if (big != 1)
			r+= big;
		r += "M";
		return r;
	}

}
