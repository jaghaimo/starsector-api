package com.fs.starfarer.api.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TimeoutTracker<T> {

	public static class ItemData<T> {
		public T item;
		public float remaining;
	}
	
	private List<ItemData<T>> items = new ArrayList<ItemData<T>>();
	transient private Set<T> set = new HashSet<T>();
	
	Object readResolve() {
		set = new HashSet<T>();
		for (ItemData<T> item : items) {
			set.add(item.item);
		}
		return this;
	}
	
	Object writeReplace() {
		return this;
	}
	
	public void add(T item, float time) {
		getData(item).remaining += time;
	}
	
	public void add(T item, float time, float limit) {
		ItemData<T> d = getData(item);
		if (time > 0 && d.remaining + time > limit) {
			time = Math.max(0, limit - d.remaining);
		}
		d.remaining += time;
		if (d.remaining < 0) {
			d.remaining = 0;
		}
	}
	
	public void set(T item, float time) {
		getData(item).remaining = time;
	}
	
	public float getRemaining(T item) {
		return getData(item).remaining;
	}
	
	public void remove(T item) {
		for (ItemData<T> d : items) {
			//if (d.item == item) {
			if (d.item.equals(item)) {
				items.remove(d);
				set.remove(item);
				return;
			}
		}
	}
	
	public List<T> getItems() {
		List<T> list = new ArrayList<T>();
		for (ItemData<T> d : items) {
			list.add(d.item);
		}
		return list;
	}
	
	public void clear() {
		items.clear();
		set.clear();
	}
	
	
	private ItemData<T> getData(T item) {
		for (ItemData<T> d : items) {
			//if (d.item == item) {
			if (d.item.equals(item)) {
				return d;
			}
		}
		ItemData<T> d = new ItemData<T>();
		d.item = item;
		d.remaining = 0;
		items.add(d);
		set.add(item);
		return d;
	}
	
	public void advance(float amount) {
		List<ItemData<T>> remove = new ArrayList<ItemData<T>>();
		for (ItemData<T> d : items) {
//			if (d.item instanceof CampaignEventListener) {
//				System.out.println("Remaining: " + d.remaining);
//			}
			d.remaining -= amount;
			if (d.remaining <= 0) {
				remove.add(d);
				set.remove(d.item);
			}
		}
		items.removeAll(remove);
	}

	public boolean contains(T item) {
		return set.contains(item);
//		for (ItemData d : items) {
//			if (d.item == item) {
//				return true;
//			}
//		}
//		return false;
	}
}







