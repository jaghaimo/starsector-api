package com.fs.starfarer.api.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SaveableIterator<T> implements Iterator<T>{

	private List<T> list;
	private int index;
	public SaveableIterator(List<T> list) {
		this.list = new ArrayList<T>(list);
		index = -1;
	}

	public boolean hasNext() {
		return index + 1 < list.size();
	}

	public T next() {
		index++;
		return list.get(index);
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
