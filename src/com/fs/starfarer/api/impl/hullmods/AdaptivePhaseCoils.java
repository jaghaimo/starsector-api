package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.PhaseCloakStats;

public class AdaptivePhaseCoils extends BaseHullMod {

	public static float FLUX_THRESHOLD_INCREASE_PERCENT = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(
				Stats.PHASE_CLOAK_FLUX_LEVEL_FOR_MIN_SPEED_MOD).modifyPercent(id, FLUX_THRESHOLD_INCREASE_PERCENT);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int) Math.round(FLUX_THRESHOLD_INCREASE_PERCENT) + "%";
		if (index == 1) return "" + (int) Math.round(PhaseCloakStats.BASE_FLUX_LEVEL_FOR_MIN_SPEED * 100f) + "%";
		if (index == 2) return "" + (int)Math.round(
				PhaseCloakStats.BASE_FLUX_LEVEL_FOR_MIN_SPEED * 100f * 
				(1f + FLUX_THRESHOLD_INCREASE_PERCENT/100f)) + "%";
		return null;
	}
	
	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(HullMods.PHASE_ANCHOR)) return false;
		return ship.getHullSpec().isPhase();
	}

	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().hasHullMod(HullMods.PHASE_ANCHOR)) {
			return "Incompatible with Phase Anchor";
		}
		if (!ship.getHullSpec().isPhase()) {
			return "Can only be installed on phase ships";
		}
		return super.getUnapplicableReason(ship);
	}
	
}

