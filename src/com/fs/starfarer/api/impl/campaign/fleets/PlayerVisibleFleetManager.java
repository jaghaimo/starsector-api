package com.fs.starfarer.api.impl.campaign.fleets;

import java.util.Iterator;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;


public abstract class PlayerVisibleFleetManager extends BaseLimitedFleetManager {

	protected IntervalUtil despawnTracker = new IntervalUtil(0.75f, 1.25f);
	
	protected Object readResolve() {
		super.readResolve();
		if (despawnTracker == null) {
			despawnTracker = new IntervalUtil(0.75f, 1.25f);
		}
		return this;
	}
	
	@Override
	public void advance(float amount) {
		super.advance(amount);
		
		boolean reset = false;
		//reset = true;
		
		if (reset) {
			if (this instanceof DisposableFleetManager) {
				DisposableFleetManager dfm = (DisposableFleetManager) this;
				dfm.recentSpawns.clear();
			}
		}
		
		float days = Global.getSector().getClock().convertToDays(amount);
		despawnTracker.advance(days);
		if (despawnTracker.intervalElapsed()) {
			Iterator<ManagedFleetData> iter = active.iterator();
			while (iter.hasNext()) {
				ManagedFleetData curr = iter.next();
				if (reset ||
						(!isVisibleToPlayer(curr.fleet) && isOkToDespawnAssumingNotPlayerVisible(curr.fleet))) {
					if (curr.fleet.getBattle() == null) {
						curr.fleet.despawn(FleetDespawnReason.PLAYER_FAR_AWAY, null);
						iter.remove();
					}
					// can't just directly despawn as it might be involved in a battle or something else
//					if (curr.fleet.getAI() != null) {
//						curr.fleet.getAI().clearAssignments();
//						curr.fleet.getAI().addAssignmentAtStart(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, curr.fleet, 100f, null);
//					}
				}
			}
		}
	}
	
	protected abstract boolean isOkToDespawnAssumingNotPlayerVisible(CampaignFleetAPI fleet);
	
	protected boolean isVisibleToPlayer(CampaignFleetAPI fleet) {
		CampaignFleetAPI player = Global.getSector().getPlayerFleet();
		if (player == null) return false;
		
		if (player.getContainingLocation() != fleet.getContainingLocation()) {
			float dist = Misc.getDistance(player.getLocationInHyperspace(), fleet.getLocationInHyperspace());
			return dist < getHyperspaceCullRange();
		}
		
		float cullRange = player.getMaxSensorRangeToDetect(fleet) + getInSystemCullRange();
		float dist = Misc.getDistance(player.getLocation(), fleet.getLocation());
		return dist < cullRange;
	}
	
	protected float getHyperspaceCullRange() {
		return 1500;
	}
	
	protected float getInSystemCullRange() {
		return 500;
	}
}


















