package com.fs.starfarer.api.util;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

public abstract class DelayedActionScript implements EveryFrameScript {

	protected float daysLeft;
	protected Boolean done = null;
	public DelayedActionScript(float daysLeft) {
		this.daysLeft = daysLeft;
	}

	public void advance(float amount) {
		float days = Global.getSector().getClock().convertToDays(amount);
		daysLeft -= days;
		if (daysLeft <= 0) {
			doAction();
			done = true;
		}
	}
	
	public abstract void doAction();

	public boolean isDone() {
		return done != null && done == true;
	}

	public boolean runWhilePaused() {
		return false;
	}

}
