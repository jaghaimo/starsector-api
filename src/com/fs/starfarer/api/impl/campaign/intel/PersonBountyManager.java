package com.fs.starfarer.api.impl.campaign.intel;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;

public class PersonBountyManager extends BaseEventManager {

	public static final String KEY = "$core_personBountyManager";
	
	public static PersonBountyManager getInstance() {
		Object test = Global.getSector().getMemoryWithoutUpdate().get(KEY);
		return (PersonBountyManager) test; 
	}
	
	public PersonBountyManager() {
		super();
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	@Override
	protected int getMinConcurrent() {
		return Global.getSettings().getInt("minPersonBounties");
	}
	@Override
	protected int getMaxConcurrent() {
		return Global.getSettings().getInt("maxPersonBounties");
	}
	
	@Override
	protected float getIntervalRateMult() {
//		if (true) {
//			currMax = 200;
//			return 1000f;
//		}
		return super.getIntervalRateMult();
	}

	@Override
	protected EveryFrameScript createEvent() {
		if ((float) Math.random() < 0.75f) return null;
		
		PersonBountyIntel intel = new PersonBountyIntel();
		if (intel.isDone()) intel = null;

		return intel;
	}
	
}
