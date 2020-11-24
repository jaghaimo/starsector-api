package com.fs.starfarer.api.impl.hullmods;

import java.util.HashMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class ECMPackage extends BaseHullMod {

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 2f);
		mag.put(HullSize.DESTROYER, 3f);
		mag.put(HullSize.CRUISER, 4f);
		mag.put(HullSize.CAPITAL_SHIP, 5f);
	}
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(id, (Float) mag.get(hullSize));
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		return null;
	}
}




