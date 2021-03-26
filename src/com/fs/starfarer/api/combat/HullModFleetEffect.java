package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;

/**
 * Implement this in addition to HullModEffect, not by itself.
 * 
 * Note: the effect class is instantiated once per application session.
 * Storing campaign data in members of an implementing class is a bad idea,
 * use SectorAPI.getPersistentData() instead.
 * 
 * @author Alex Mosolov
 *
 * Copyright 2013 Fractal Softworks, LLC
 */
public interface HullModFleetEffect {

	/**
	 * Called for *every* fleet, even fleets that don't have a ship with the specific hullmod.
	 * 
	 * Shouldn't try to do a lot here; could have a lot of performance repercussions.
	 * 
	 * @param fleet
	 */
	void advanceInCampaign(CampaignFleetAPI fleet);
	
	
	/**
	 * Whether the advanceInCampaign() method should be called for this hullmod.
	 * @return
	 */
	boolean withAdvanceInCampaign();
	
	/**
	 * Whether the withOnFleetSync() method should be called for this hullmod.
	 * @return
	 */
	boolean withOnFleetSync();
	
	
	/**
	 * Called when anything about the fleet composition changes, including hullmod changes.
	 * Also called for all fleets, including fleets without ships with this hullmod.
	 * @param fleet
	 */
	void onFleetSync(CampaignFleetAPI fleet);
}
