package com.fs.starfarer.api.impl.campaign.intel.group;

import java.util.Random;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes;
import com.fs.starfarer.api.impl.campaign.ids.MemFlags;
import com.fs.starfarer.api.impl.campaign.intel.PerseanLeagueMembership;
import com.fs.starfarer.api.impl.campaign.intel.events.EstablishedPolityScript;
import com.fs.starfarer.api.impl.campaign.missions.FleetCreatorMission;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;



public class PerseanLeaguePunitiveExpedition extends GenericRaidFGI  {

	public static final String PLPE_FLEET = "$PLPE_fleet";
	
	public static String KEY = "$PLPE_ref";
	public static PerseanLeaguePunitiveExpedition get() {
		return (PerseanLeaguePunitiveExpedition) Global.getSector().getMemoryWithoutUpdate().get(KEY);
	}
	
	
	protected IntervalUtil interval = new IntervalUtil(0.1f, 0.3f);
	
	
	public PerseanLeaguePunitiveExpedition(GenericRaidParams params) {
		super(params);
		
		Global.getSector().getMemoryWithoutUpdate().set(KEY, this);
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
		return "punitive expedition";
	}

	@Override
	public String getForcesNoun() {
		return super.getForcesNoun();
	}


	@Override
	public String getBaseName() {
		return Misc.ucFirst(getFaction().getPersonNamePrefix()) + " " + "Punitive Expedition";
	}


	@Override
	protected CampaignFleetAPI createFleet(int size, float damage) {
		
		Random r = getRandom();
		
		Vector2f loc = origin.getLocationInHyperspace();
		
		FleetCreatorMission m = new FleetCreatorMission(r);
		m.setFleetTypeMedium(FleetTypes.TASK_FORCE); // default would be "Patrol", don't want that
		m.beginFleet();
		
		m.createFleet(params.style, size, params.factionId, loc);
		if (size >= 8) {
			m.triggerSetFleetDoctrineOther(5, 0); // more capitals in large fleets
		}
		m.triggerSetFleetFlag(PLPE_FLEET);
		
		m.setFleetSource(params.source);
		setFleetCreatorQualityFromRoute(m);
		m.setFleetDamageTaken(damage);
	
		m.triggerSetWarFleet();
		m.triggerMakeLowRepImpact();
		//m.triggerMakeHostile();
		m.triggerMakeAlwaysSpreadTOffHostility();
	
		CampaignFleetAPI fleet = m.createFleet();
		
		return fleet;
	}

	@Override
	public void abort() {
		if (!isAborted()) {
			PerseanLeagueMembership.setDefeatedPunEx(true);
			new EstablishedPolityScript();
			
			for (CampaignFleetAPI curr : getFleets()) {
				curr.getMemoryWithoutUpdate().unset(PLPE_FLEET);
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
				String reason = "PLPunEx";
				for (CampaignFleetAPI curr : getFleets()) {
					Misc.setFlagWithReason(curr.getMemoryWithoutUpdate(), MemFlags.MEMORY_KEY_MAKE_HOSTILE,
											reason, true, 1f);
				}
				
			}
		}
	}


	
}




