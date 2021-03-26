package com.fs.starfarer.api.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;

import com.fs.starfarer.api.campaign.econ.MarketAPI;

public class WeightedRandomPicker<T> implements Cloneable {

	@Override
	public WeightedRandomPicker<T> clone() {
		try {
			WeightedRandomPicker<T> copy = (WeightedRandomPicker<T>) super.clone();
			copy.items = new ArrayList<T>(items);
			copy.weights = new ArrayList<Float>(weights);
			return copy;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

	private List<T> items = new ArrayList<T>();
	transient private List<Float> weights = new ArrayList<Float>();
	private String w;
	
	private float total = 0f;
	private final boolean ignoreWeights;
	
	private Random random = null;
	
	public WeightedRandomPicker() {
		this(false);
	}
	
	public WeightedRandomPicker(boolean ignoreWeights) {
		this.ignoreWeights = ignoreWeights;
	}

	public WeightedRandomPicker(Random random) {
		this(false);
		this.random = random;
	}
	
	Object readResolve() {
		try {
			weights = new ArrayList<Float>();
			if (w != null) {
				JSONArray arr = new JSONArray(w);
				for (int i = 0; i < arr.length(); i++) {
					weights.add((float)arr.getDouble(i));
				}
			}
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
		return this;
	}
	
	Object writeReplace() {
		JSONArray arr = new JSONArray();
		for (Float f : weights) {
			arr.put(f);
		}
		w = arr.toString();
		
		return this;
	}


	public void clear() {
		items.clear();
		weights.clear();
		total = 0;
	}

//	public void addAll(List<T> items) {
//		for (T item : items) {
//			add(item);
//		}
//	}
	
	public void addAll(Collection<T> items) {
		for (T item : items) {
			add(item);
		}
	}
	
	public void addAll(WeightedRandomPicker<T> other) {
		for (int i = 0; i < other.items.size(); i++) {
			add(other.items.get(i), other.weights.get(i));
		}
	}
	
	public void add(T item) {
		add(item, 1f);
	}
	public void add(T item, float weight) {
		//if (weight < 0) weight = 0;
		if (weight <= 0) return;
		items.add(item);
		weights.add(weight); // + (weights.isEmpty() ? 0 : weights.get(weights.size() - 1)));
		total += weight;
	}
	
	public void remove(T item) {
		int index = items.indexOf(item);
		if (index != -1) {
			items.remove(index);
			float weight = weights.remove(index);
			total -= weight;
		}
	}
	
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	public List<T> getItems() {
		return items;
	}
	
	public float getWeight(T item) {
		int index = items.indexOf(item);
		if (index < 0) return 0;
		return getWeight(index);
	}
	
	public float getWeight(int index) {
		return weights.get(index);
	}
	public void setWeight(int index, float weight) {
		float w = getWeight(index);
		weights.set(index, weight);
		total += weight - w;
	}

	public T pickAndRemove() {
		T pick = pick();
		remove(pick);
		return pick;
	}
	
	public T pick(Random random) {
		Random orig = this.random;
		this.random = random;
		T pick = pick();
		this.random = orig;
		return pick;
	}
	
	public T pick() {
		if (items.isEmpty()) return null;
		
		if (ignoreWeights) {
			int index;
			if (random != null) {
				index = (int) (random.nextDouble() * items.size());
			} else {
				index = (int) (Math.random() * items.size());
			}
			return items.get(index);
		}
		
		float random;
		if (this.random != null) {
			random = this.random.nextFloat() * total;
		} else {
			random = (float) (Math.random() * total);
		}
		if (random > total) random = total;
		
		float weightSoFar = 0f;
		int index = 0;
		for (Float weight : weights) {
			weightSoFar += weight;
			if (random <= weightSoFar) break;
			index++;
		}
		return items.get(Math.min(index, items.size() - 1));
	}

	public Random getRandom() {
		return random;
	}

	public void setRandom(Random random) {
		this.random = random;
	}

	
	
	public void print(String title) {
		System.out.println(title);
		
		Map<T, Integer> indices = new HashMap<T, Integer>();
		for (int i = 0; i < items.size(); i++) {
			T item = items.get(i);
			indices.put(item, i);
		}
		
		List<T> sorted = new ArrayList<T>(items);
		Collections.sort(sorted, new Comparator<T>() {
			public int compare(T o1, T o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		for (T item : sorted) {
			int index = indices.get(item);
			float weight = weights.get(index);
			//String percent = Misc.getRoundedValueMaxOneAfterDecimal((weight / total) * 100f) + "%";
			String percent = "" + (int)((weight / total) * 100f) + "%";
			
			//System.out.println("    " + item.toString() + ": " + percent + " (" + Misc.getRoundedValue(weight) + ")");
			String itemStr = "";
			if (item instanceof MarketAPI) {
				itemStr = ((MarketAPI)item).getName();
			} else {
				itemStr = item.toString();
			}
			System.out.println(String.format("    %-30s%10s%10s", itemStr, percent, Misc.getRoundedValue(weight)));
		}
		//System.out.println("  Total: " + (int) total);
		//System.out.println();
	}

	public float getTotal() {
		return total;
	}
	
}






