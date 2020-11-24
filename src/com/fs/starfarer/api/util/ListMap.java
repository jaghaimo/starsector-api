package com.fs.starfarer.api.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ListMap<V> extends LinkedHashMap<String, List<V>> {
	private static final long serialVersionUID = 1L;

	public void add(String key, V value) {
		List<V> list = getList(key);
		list.add(value);
	}
	public void remove(String key, V value) {
		List<V> list = getList(key);
		list.remove(value);
	}
	
	@Override
	public List<V> get(Object key) {
		return getList((String) key);
	}
	public List<V> getList(String key) {
		List<V> list = super.get(key);
		if (list == null) {
			list = new ArrayList<V>();
			put(key, list);
		}
		return list;
	}

}
