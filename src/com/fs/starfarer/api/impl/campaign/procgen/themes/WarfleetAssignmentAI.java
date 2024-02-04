package com.fs.starfarer.api.impl.campaign.procgen.themes;

import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.util.Misc;

/**
 * Can capture objectives and raid colonies; does not do anything else.
 * @author Alex
 *
 * Copyright 2023 Fractal Softworks, LLC
 */
public class WarfleetAssignmentAI extends BaseAssignmentAI {

	protected boolean doCapture = true;
	protected boolean doRaid = true;
	
	public WarfleetAssignmentAI(CampaignFleetAPI fleet, boolean doCapture, boolean doRaid) {
		super(fleet);
		this.doCapture = doCapture;
		this.doRaid = doRaid;
	}


	@Override
	public void advance(float amount) {
		if (Misc.isFleetReturningToDespawn(fleet)) return;
		
		super.advance(amount);
		
		if (doCapture) {
			checkCapture(amount);
		}
		//checkBuild(amount);
		if (doRaid) {
			checkRaid(amount);
		}
	}
	
	
	@Override
	protected void giveInitialAssignments() {
	}

	@Override
	protected void pickNext() {
	}

	public boolean isDoRaid() {
		return doRaid;
	}
	
	
}










