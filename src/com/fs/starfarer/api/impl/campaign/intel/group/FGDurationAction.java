package com.fs.starfarer.api.impl.campaign.intel.group;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.fleets.RouteManager.RouteSegment;

public class FGDurationAction extends BaseFGAction {

	protected float durDays;
	protected float origDurDays;
	
	public FGDurationAction(float durDays) {
		this.durDays = durDays;
		origDurDays = durDays;
	}

	@Override
	public void notifyFleetsSpawnedMidSegment(RouteSegment segment) {
		super.notifyFleetsSpawnedMidSegment(segment);
		durDays *= (1f - segment.getProgress());
	}
	
	@Override
	public void directFleets(float amount) {
		super.directFleets(amount);
		
		float days = Global.getSector().getClock().convertToDays(amount);
		durDays -= days;
		
		if (durDays <= 0) {
			setActionFinished(true);
			return;
		}
	}

	public float getEstimatedDaysToComplete() {
		if (intel.isSpawnedFleets()) {
			return durDays;
		} else {
			RouteSegment segment = intel.getSegmentForAction(this);
			if (segment == null) return 0f;
			return Math.max(0f, segment.daysMax - segment.elapsed);
		}
	}

	public float getDurDays() {
		return durDays;
	}

	public void setDurDays(float waitDays) {
		this.durDays = waitDays;
	}
	
	public float getOrigDurDays() {
		return origDurDays;
	}

	public void setOrigDurDays(float origDurDays) {
		this.origDurDays = origDurDays;
	}

}




