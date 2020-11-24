package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class MilitarizedSubsystems extends BaseLogisticsHullMod {

	private static final int BURN_LEVEL_BONUS = 1;
	private static final float MAINTENANCE_PERCENT = 100;
	private static final float DEPLOY_COST_MULT = 0.7f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getSensorStrength().unmodify(HullMods.CIVGRADE);
		stats.getSensorProfile().unmodify(HullMods.CIVGRADE);
		
		stats.getMaxBurnLevel().modifyFlat(id, BURN_LEVEL_BONUS);
		

		stats.getCRPerDeploymentPercent().modifyMult(id, DEPLOY_COST_MULT);
		stats.getSuppliesToRecover().modifyMult(id, DEPLOY_COST_MULT);
		
		//stats.getSuppliesPerMonth().modifyPercent(id, MAINTENANCE_PERCENT);
		stats.getMinCrewMod().modifyPercent(id, MAINTENANCE_PERCENT);
		
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + BURN_LEVEL_BONUS;
		if (index == 1) return "" + (int)Math.round((1f - DEPLOY_COST_MULT) * 100f) + "%";
		if (index == 2) return "" + (int)Math.round(MAINTENANCE_PERCENT) + "%";
		return null;
	}
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().hasHullMod(HullMods.CIVGRADE) && super.isApplicableToShip(ship);
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (!ship.getVariant().hasHullMod(HullMods.CIVGRADE)) {
			return "Can only be installed on civilian-grade hulls";
		}
		return super.getUnapplicableReason(ship);
	}
	
	
	
	
}

