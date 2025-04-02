package com.fs.starfarer.api.impl.campaign.intel.group;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;



public class SindrianDiktatPunitiveExpedition extends GenericRaidFGI  {

	public static final String SDPE_FLEET = "$SDPE_fleet";
	
	public static String KEY = "$SDPE_ref";
	public static SindrianDiktatPunitiveExpedition get() {
		return (SindrianDiktatPunitiveExpedition) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	
	public SindrianDiktatPunitiveExpedition(GenericRaidParams params) {
		super(params);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
	}
	
	@Override
	protected String getFleetCreationFactionOverride(int size) {
		return Factions.LIONS_GUARD;
	}

	@Override
	protected void notifyEnding() {
		super.notifyEnding();
		
		Global.getSector().getMemoryWithoutUpdate().unset(KEY);
	}
	
	@Override
	protected void notifyEnded() {
		super.notifyEnded();
	}
	

	@Override
	public String getNoun() {
		return super.getNoun();
		//return "punitive expedition";
	}

	@Override
	public String getForcesNoun() {
		return super.getForcesNoun();
	}


	@Override
	public String getBaseName() {
		return super.getBaseName();
		//return Misc.ucFirst(getFaction().getPersonNamePrefix()) + " " + "Punitive Expedition";
	}


	@Override
	protected void preConfigureFleet(int size, FleetCreatorMission m) {
		m.setFleetTypeMedium(FleetTypes.TASK_FORCE); // default would be "Patrol", don't want that
	}
	
	@Override
	protected void configureFleet(int size, FleetCreatorMission m) {
		m.triggerSetFleetFlag(SDPE_FLEET);
		if (size >= 8) {
			m.triggerSetFleetDoctrineOther(5, 0); // more capitals in large fleets
		}
	}

	
	@Override
	public void abort() {
		if (!isAborted()) {
			for (CampaignFleetAPI curr : getFleets()) {
				curr.getMemoryWithoutUpdate().unset(SDPE_FLEET);
			}
		}
		super.abort();
	}
	
	

	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		float days = Misc.getDays(amount);
		interval.advance(days);
		
		if (interval.intervalElapsed()) {
			if (isCurrent(PAYLOAD_ACTION)) {
				String reason = "SDPunEx";
				for (CampaignFleetAPI curr : getFleets()) {
					Misc.setFlagWithReason(curr.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE,
											reason, true, 1f);
				}
				
			}
		}
	}


	
}




