package com.fs.starfarer.api.util;

public class MutatingValueUtil {

	private float value;
	
	private float min;
	private float max;
	
	private float rate;
	private float rateSign;
	private float rateMult = 1f;
	private float valueMult = 1f;
	
	private float sign = 0;

	public MutatingValueUtil() {
		
	}
	
	public MutatingValueUtil(float min, float max, float rate) {
		this.min = min;
		this.max = max;
		this.rate = Math.abs(rate);
		
		value = min + (float) Math.random() * (max - min);
		rateSign = Math.signum(rate);
	}
	
	public void advance(float amount) {
		if (rateSign != 0) {
			value += amount * rate * rateSign * rateMult;
		} else {
			value += amount * rate * rateMult;
		}
		if (value > max) {
			rateSign = -1f;
		} else if (value < min) {
			rateSign = 1f;
		}
	}
	
	public float getRateMult() {
		return rateMult;
	}

	public void setRateMult(float rateMult) {
		this.rateMult = rateMult;
	}

	public float getValue() {
		if (sign != 0) return value * sign * valueMult;
		return value * valueMult;
	}
	

	public float getValueMult() {
		return valueMult;
	}

	public void setValueMult(float valueMult) {
		this.valueMult = valueMult;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public float getMin() {
		return min;
	}

	public void setMin(float min) {
		this.min = min;
	}

	public float getMax() {
		return max;
	}

	public void setMax(float max) {
		this.max = max;
	}

	public float getRate() {
		return rate;
	}

	public void setRate(float rate) {
		//System.out.println("RATE: " + rate);
		this.rate = Math.abs(rate);
		//rateSign = Math.signum(rate);
	}

	public float getSign() {
		return sign;
	}

	public void setSign(float sign) {
		this.sign = Math.signum(sign);
	}
	
	public void setRandomSign() {
		sign = (float) Math.signum(Math.random() - 0.5f);
		if (sign == 0) sign = 1;
	}
	public void setRandomRateSign() {
		rateSign = (float) Math.signum(Math.random() - 0.5f);
		if (rateSign == 0) rateSign = 1;
	}


	public float getRateSign() {
		return rateSign;
	}


	public void setRateSign(float rateSign) {
		this.rateSign = rateSign;
	}
	
	
	
}
