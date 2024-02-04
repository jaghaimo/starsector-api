package com.fs.starfarer.api;


public interface SoundAPI {
	void stop();
	void setVolume(float newValue);
	void setPitch(float pitch);
	void setLocation(float x, float y);
	boolean isPlaying();
}
