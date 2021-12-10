package com.fs.starfarer.api.impl.campaign.ghosts;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;

public class BaseGhostBehavior implements GhostBehavior {
	protected float duration;
	protected List<GhostBehaviorInterrupt> interrupts = new ArrayList<GhostBehaviorInterrupt>();
	
	public BaseGhostBehavior(float duration) {
		this.duration = duration;
	}

	public boolean isDone() {
		return duration <= 0f;
	}

	public void advance(float amount, SensorGhost ghost) {
		float days = Global.getSector().getClock().convertToDays(amount);
		duration -= days;
		
		for (GhostBehaviorInterrupt curr : interrupts) {
			curr.advance(amount, ghost, this);
			if (curr.shouldInterruptBehavior(ghost, this)) {
				end();
				break;
			}
		}
	}
	
	public void end() {
		duration = 0f;
	}
	
	public void addInterrupt(GhostBehaviorInterrupt interrupt) {
		interrupts.add(interrupt);
	}
}
