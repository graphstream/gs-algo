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
