package com.fs.starfarer.api.impl.combat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class ReserveWingStats extends BaseShipSystemScript {
	
	public static String RD_NO_EXTRA_CRAFT = "rd_no_extra_craft";
	public static String RD_FORCE_EXTRA_CRAFT = "rd_force_extra_craft";
	
//	public static float EXTRA_FIGHTER_DURATION = 15;
//	public static float RATE_COST = 0.25f;
//	public static float RATE_COST_1_BAY = 0.15f;
	public static float EXTRA_FIGHTER_DURATION = 30;
	public static float RATE_COST = 0f;
	public static float RATE_COST_1_BAY = 0f;
	
	public static float getRateCost(int bays) {
		if (bays <= 1) return RATE_COST_1_BAY;
		return RATE_COST;
	}
	
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
		} else {
			return;
		}
		
		if (effectLevel == 1) {
			
			//need to make this more balanced
			//possibly don't count the "added" fighters to helping restore the replacement rate?
			//also: need to adjust the AI to be more conservative using this

			
			float minRate = Global.getSettings().getFloat("minFighterReplacementRate");
			
			int bays = ship.getLaunchBaysCopy().size();
			float cost = getRateCost(bays);
			for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
				if (bay.getWing() == null) continue;
				
				float rate = Math.max(minRate, bay.getCurrRate() - cost);
				bay.setCurrRate(rate);
				
				bay.makeCurrentIntervalFast();
				FighterWingSpecAPI spec = bay.getWing().getSpec();
				
				int addForWing = getAdditionalFor(spec, bays);
				int maxTotal = spec.getNumFighters() + addForWing;
				int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
				//int actualAdd = addForWing;
				//actualAdd = Math.min(spec.getNumFighters(), actualAdd);
				if (actualAdd > 0) {
					bay.setFastReplacements(bay.getFastReplacements() + addForWing);
					bay.setExtraDeployments(actualAdd);
					bay.setExtraDeploymentLimit(maxTotal);
					bay.setExtraDuration(EXTRA_FIGHTER_DURATION);
					//bay.setExtraDuration(99999999999f);
				}
			}
		}
	}
	
	public static int getAdditionalFor(FighterWingSpecAPI spec, int bays) {
		//if (spec.isBomber() && !spec.hasTag(RD_FORCE_EXTRA_CRAFT)) return 0;
		if (spec.hasTag(RD_NO_EXTRA_CRAFT)) return 0;
		
		int size = spec.getNumFighters();
		if (true) return size;
		
		if (bays == 1) {
			return Math.max(size, 2);
		}
		
//		if (size <= 3) return 1;
//		return 2;
		if (size <= 3) return 1;
		if (size <= 5) return 2;
		return 3;
	}
	
	
	public void unapply(MutableShipStatsAPI stats, String id) {
	}
	

	
	public StatusData getStatusData(int index, State state, float effectLevel) {
//		if (index == 0) {
//			return new StatusData("deploying additional fighters", false);
//		}
		return null;
	}


	@Override
	public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
		return true;
	}
	

	
}








