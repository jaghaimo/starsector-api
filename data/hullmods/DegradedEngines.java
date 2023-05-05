package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.hullmods.CompromisedStructure;

public class DegradedEngines extends BaseHullMod {
	//public static final float PROFILE_PERCENT = 50f;
	
	public static final float MANEUVER_PENALTY_MULT = 0.85f;
	
//	private static Map mag = new HashMap();
//	static {
//		mag.put(HullSize.FRIGATE, -1f);
//		mag.put(HullSize.DESTROYER, -1f);
//		mag.put(HullSize.CRUISER, -1f);
//		mag.put(HullSize.CAPITAL_SHIP, -1f);
//	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		float effect = stats.getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		float mult = MANEUVER_PENALTY_MULT + (1f - MANEUVER_PENALTY_MULT) * (1f - effect);
		
		stats.getMaxSpeed().modifyMult(id, mult);
		
		stats.getAcceleration().modifyMult(id, mult);
		stats.getDeceleration().modifyMult(id, mult);
		stats.getTurnAcceleration().modifyMult(id, mult);
		stats.getMaxTurnRate().modifyMult(id, mult);
		
		CompromisedStructure.modifyCost(hullSize, stats, id);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		float effect = 1f;
		if (ship != null) effect = ship.getMutableStats().getDynamic().getValue(Stats.DMOD_EFFECT_MULT);
		float mult = MANEUVER_PENALTY_MULT + (1f - MANEUVER_PENALTY_MULT) * (1f - effect);
		
		if (index == 0) return "" + (int) Math.round((1f - mult) * 100f) + "%";
		if (index >= 1) return CompromisedStructure.getCostDescParam(index, 1);
		return null;
	}

	
}







