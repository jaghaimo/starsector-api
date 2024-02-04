package com.fs.starfarer.api.util;

import java.util.LinkedHashMap;

public class CountingMap<K> extends LinkedHashMap<K, Integer> {
	private static final long serialVersionUID = 1L;

	public void add(K key, int quantity) {
		Integer val = super.get(key);
		if (val == null) val = 0;
		val+=quantity;
		put(key, val);
	}
	
	public void add(K key) {
		add(key, 1);
	}
	
	public void sub(K key) {
		sub(key, 1);
	}
	public void sub(K key, int quantity) {
		Integer val = super.get(key);
		if (val == null) val = 0;
		val-=quantity;
		if (val <= 0) {
			remove(key);
			return;
		}
		put(key, val);
	}
	
	public int getCount(K key) {
		Integer val = super.get(key);
		if (val == null) val = 0;
		return val;
	}

	@Override
	public Integer get(Object key) {
		return getCount((K) key);
	}
	
	public int getTotal() {
		int total = 0;
		for (K key : keySet()) {
			total += get(key);
		}
		return total;
	}

	public K getLargest() {
		int max = 0;
		K maxKey = null;
		for (K key : keySet()) {
			int c = getCount(key);
			if (c > max) {
				max = c;
				maxKey = key;
			}
		}
		return maxKey;
	}
	
}
