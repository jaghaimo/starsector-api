package com.fs.starfarer.api.impl.campaign.econ.impl;

import java.util.ArrayList;
import java.util.List;


public class ConstructionQueue {

	public static class ConstructionQueueItem {
		public String id;
		public int cost;
		public ConstructionQueueItem(String id, int cost) {
			this.id = id;
			this.cost = cost;
		}
	}
	
	protected List<ConstructionQueueItem> items = new ArrayList<ConstructionQueueItem>();
	
	protected Object readResolve() {
		if (items == null) {
			items = new ArrayList<ConstructionQueueItem>();
		}
		return this;
	}

	public List<ConstructionQueueItem> getItems() {
		return items;
	}
	public void setItems(List<ConstructionQueueItem> items) {
		this.items = items;
	}
	
	public void addToEnd(String id, int cost) {
		ConstructionQueueItem item = new ConstructionQueueItem(id, cost);
		items.add(item);
	}
	
	public void moveUp(String id) {
		ConstructionQueueItem item = getItem(id);
		if (item == null) return;
		int index = items.indexOf(item);
		index--;
		if (index < 0) index = 0;
		items.remove(item);
		items.add(index, item);
	}
	
	public void moveDown(String id) {
		ConstructionQueueItem item = getItem(id);
		if (item == null) return;
		int index = items.indexOf(item);
		index++;
		items.remove(item);
		if (index > items.size()) index = items.size();
		items.add(index, item);
	}
	
	public void moveToFront(String id) {
		ConstructionQueueItem item = getItem(id);
		if (item == null) return;
		items.remove(item);
		items.add(0, item);
	}
	
	public void moveToBack(String id) {
		ConstructionQueueItem item = getItem(id);
		if (item == null) return;
		items.remove(item);
		items.add(item);
	}
	
	public void removeItem(String id) {
		items.remove(getItem(id));
	}
	
	public ConstructionQueueItem getItem(String id) {
		for (ConstructionQueueItem item : items) {
			if (item.id.equals(id)) return item;
		}
		return null;
	}
	
	public boolean hasItem(String id) {
		return getItem(id) != null;
	}
}





