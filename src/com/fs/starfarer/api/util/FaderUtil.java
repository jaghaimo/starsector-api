package com.fs.starfarer.api.util;


public class FaderUtil  {

	public static enum State {IN, OUT, IDLE};
	
	private float currBrightness;
	private float durationIn, durationOut;
	private State state;
	
	private boolean bounceDown = false;
	private boolean bounceUp = false;
	
	public FaderUtil(float currBrightness, float duration) {
		this.currBrightness = currBrightness;
		this.durationIn = duration;
		this.durationOut = duration;
		state = State.IDLE;
	}
	
	public FaderUtil(float currBrightness, float durationIn, float durationOut) {
		this.currBrightness = currBrightness;
		this.durationIn = durationIn;
		this.durationOut = durationOut;
		state = State.IDLE;
	}
	
	public FaderUtil(float currBrightness, float durationIn, float durationOut, boolean bounceUp, boolean bounceDown) {
		this(currBrightness, durationIn, durationOut);
		setBounce(bounceUp, bounceDown);
	}
	
	public void setBounceDown(boolean bounceDown) {
		this.bounceDown = bounceDown;
	}

	public void setBounceUp(boolean bounceUp) {
		this.bounceUp = bounceUp;
	}
	
	public void setBounce(boolean up, boolean down) {
		this.bounceUp = up;
		this.bounceDown = down;
	}

	public void forceIn() {
		currBrightness = 1;
		if (bounceDown) state = State.OUT;
		else state = State.IDLE;
	}
	
	public void forceOut() {
		currBrightness = 0;
		if (bounceUp) state = State.IN;
		else state = State.IDLE;
	}
	
	public void fadeIn() {
		if (durationIn <= 0) {
			forceIn();
		} else {
			state = State.IN;
		}
	}
	
	public void fadeOut() {
		if (durationOut <= 0) {
			forceOut();
		} else {
			state = State.OUT;
		}
	}
	
	public boolean isFadedOut() {
		return getBrightness() == 0 && (isIdle() || isFadingOut());
	}
	
	public boolean isFadedIn() {
		return getBrightness() == 1 && isIdle();
	}
	
	public boolean isFadingOut() {
		return state == State.OUT;
	}
	
	public boolean isFadingIn() {
		return state == State.IN;
	}
	
	public boolean isIdle() {
		return state == State.IDLE;
	}
	
	public void advance(float amount) {
		if (state == State.IDLE) return;
		
		if (state == State.IN) {
			if (currBrightness == 1) { 
				if (bounceDown) state = State.OUT;
				else state = State.IDLE;
				return;
			}
			float delta = amount / durationIn;
			currBrightness += delta;
			if (currBrightness > 1) { 
				currBrightness = 1;
			}
		} else if (state == State.OUT) {
			if (currBrightness == 0) {
				if (bounceUp) state = State.IN;
				else state = State.IDLE;
				return;
			}
			float delta = amount / durationOut;
			currBrightness -= delta;
			if (currBrightness < 0) {
				currBrightness = 0;
			}
		}
	}
	
	public void setDurationIn(float durationIn) {
		this.durationIn = durationIn;
	}

	public void setDurationOut(float durationOut) {
		this.durationOut = durationOut;
	}
	
	public FaderUtil setDuration(float in, float out) {
		this.durationIn = in;
		this.durationOut = out;
		return this;
	}

	public float getBrightness() {
		return currBrightness;
	}

	public float getDurationIn() {
		return durationIn;
	}

	public float getDurationOut() {
		return durationOut;
	}

	public void setBrightness(float brightness) {
		this.currBrightness = brightness;
	}

	@Override
	public String toString() {
		return String.format("%s, curr: %f, in: %f, out: %f, up: %b, down: %b", state.name(), 
							 currBrightness, durationIn, durationOut,
							 bounceUp, bounceDown);
	}

	public boolean isBounceDown() {
		return bounceDown;
	}

	public boolean isBounceUp() {
		return bounceUp;
	}

	
	
}
