package com.fs.starfarer.api.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class TimeoutTrackerMap<K, V> {

	public static class ItemData<K, V> {
		public K key;
		public V item;
		public float remaining;
	}
	
	private Map<K, ItemData<K, V>> items = new LinkedHashMap<K, ItemData<K, V>>();
	public void add(K key, V item, float time) {
		getData(key, item).remaining += time;
	}
	
	public void add(K key, V item, float time, float limit) {
		ItemData<K, V> d = getData(key, item);
		if (time > 0 && d.remaining + time > limit) {
			time = Math.max(0, limit - d.remaining);
		}
		d.remaining += time;
		if (d.remaining < 0) {
			d.remaining = 0;
		}
	}
	
	public void set(K key, V item, float time) {
		getData(key, item).remaining = time;
	}
	
	public float getRemaining(K key) {
		return getData(key, null).remaining;
	}
	public V getItem(K key) {
		return getData(key, null).item;
	}
	
	public void remove(K key) {
		items.remove(key);
	}
	
	public void clear() {
		items.clear();
	}
	
	
	private ItemData<K, V> getData(K key, V item) {
		ItemData<K, V> data = items.get(key);
		if (data == null) {
			data = new ItemData<K, V>();
			data.key = key;
			data.item = item;
			data.remaining = 0;
			items.put(key, data);
			
		}
		return data;
	}
	
	public void advance(float amount) {
		List<K> remove = new ArrayList<K>();
		Set<Entry<K, ItemData<K, V>>> entrySet = items.entrySet();
		for (Entry<K, ItemData<K, V>> entry : entrySet) {
			entry.getValue().remaining -= amount;
			if (entry.getValue().remaining <= 0) {
				remove.add(entry.getKey());
			}
		}
		for (K key : remove) {
			items.remove(key);
		}
	}

	public boolean contains(K key) {
		return items.containsKey(key);
	}
}







