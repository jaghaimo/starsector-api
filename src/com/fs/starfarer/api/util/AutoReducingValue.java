package com.fs.starfarer.api.util;


public class AutoReducingValue {

	public static interface ValueChangedListener {
		void valueChanged(int prev, int val, AutoReducingValue arv);
	}
	
	private float elapsed = 0f;
	private int value = 0;
	private float elapsedPerPoint;
	private final ValueChangedListener listener;
	

	public AutoReducingValue(int value, float elapsedPerPoint, ValueChangedListener listener) {
		this.value = value;
		this.elapsedPerPoint = elapsedPerPoint;
		this.listener = listener;
	}

	public void advance(float amount) {
		elapsed += amount;
		
		if (elapsed >= elapsedPerPoint) {
			elapsed -= elapsedPerPoint;
			
			int prev = value;
			value--;
			if (value < 0) value = 0;
			if (prev != value && listener != null) {
				listener.valueChanged(prev, value, this);
			}
		}
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		int prev = value;
		this.value = value;
		if (prev != value && listener != null) {
			listener.valueChanged(prev, value, this);
		}
	}
	
	public void increaseValue(int amt) {
		int prev = value;
		this.value += amt;
		if (value < 0) {
			value = 0;
		}
		if (prev != value && listener != null) {
			listener.valueChanged(prev, value, this);
		}
	}
	
	public void reduceValue(int amt) {
		int prev = value;
		this.value -= amt;
		if (value < 0) {
			value = 0;
		}
		if (prev != value && listener != null) {
			listener.valueChanged(prev, value, this);
		}
	}
	
}
