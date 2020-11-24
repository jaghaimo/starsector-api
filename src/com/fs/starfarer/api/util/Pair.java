package com.fs.starfarer.api.util;


/**
 * Simple container class for a pair of items.  Has valid hashCode() and equals() methods.
 * 
 * A pair is equal to another pair if each of the item .equals() their counterpart.
 * 
 * @author Alexander Mosolov
 *
 */
public class Pair<A, B> {
	public A one;
	public B two;
	
	public Pair() {}
	
	public Pair(A one, B two) {
		super();
		this.one = one;
		this.two = two;
	}
	
	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((one == null) ? 0 : one.hashCode());
		result = PRIME * result + ((two == null) ? 0 : two.hashCode());
		return result;
	}
	@SuppressWarnings("unchecked")
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Pair other = (Pair) obj;
		if (one == null) {
			if (other.one != null)
				return false;
		} else if (!one.equals(other.one))
			return false;
		if (two == null) {
			if (other.two != null)
				return false;
		} else if (!two.equals(other.two))
			return false;
		return true;
	}
}
