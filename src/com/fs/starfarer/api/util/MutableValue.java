package com.fs.starfarer.api.util;


public class MutableValue {

	private float value;

	public MutableValue(float value) {
		this.value = value;
	}
	
	public MutableValue() {
		super();
	}

	public float get() {
		return value;
	}

	public void set(float value) {
		this.value = value;
	}
	
	public void add(float amt) {
		value += amt;
	}
	
	public void subtract(float amt) {
		value -= amt;
	}
}
