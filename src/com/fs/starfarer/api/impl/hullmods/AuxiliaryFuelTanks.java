package com.fs.starfarer.api.impl.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;

public class AuxiliaryFuelTanks extends BaseLogisticsHullMod {

	public static final float MIN_FRACTION = 0.3f;
	
	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 30f);
		mag.put(HullSize.DESTROYER, 60f);
		mag.put(HullSize.CRUISER, 100f);
		mag.put(HullSize.CAPITAL_SHIP, 200f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		
		float mod = (Float) mag.get(hullSize);
		if (stats.getVariant() != null) {
			mod = Math.max(stats.getVariant().getHullSpec().getFuel() * MIN_FRACTION, mod);
		}
		stats.getFuelMod().modifyFlat(id, mod);
		
		if (stats.getVariant() != null && stats.getVariant().hasHullMod(HullMods.CIVGRADE) && !stats.getVariant().hasHullMod(HullMods.MILITARIZED_SUBSYSTEMS)) {
			stats.getSuppliesPerMonth().modifyPercent(id, AdditionalBerthing.MAINTENANCE_PERCENT);
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue();
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue();
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue();
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue();
		if (index == 4) return "" + (int) Math.round(MIN_FRACTION * 100f) + "%";
		if (index == 5) return "" + (int)Math.round(AdditionalBerthing.MAINTENANCE_PERCENT) + "%";
		return null;
	}

}




