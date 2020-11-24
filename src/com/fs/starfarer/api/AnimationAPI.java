package com.fs.starfarer.api;

public interface AnimationAPI {
	/**
	 * @return Index of current animation frame, 0-based.
	 */
	int getFrame();
	
	
	int getNumFrames();
	void setFrame(int frame);
	
	void play();
	void pause();
	
	/**
	 * Reset the pause/play state and let the animation behave normally.
	 */
	void reset();
	
	float getAlphaMult();
	void setAlphaMult(float alphaMult);


	float getFrameRate();
	void setFrameRate(float frameRate);
}
