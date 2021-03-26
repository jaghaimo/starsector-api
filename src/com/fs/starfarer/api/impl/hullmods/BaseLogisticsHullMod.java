package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.HullModSpecAPI;

public class BaseLogisticsHullMod extends BaseHullMod {

	public static int MAX_MODS = Global.getSettings().getInt("maxLogisticsHullmods");
	
	public int getMax(ShipAPI ship) {
		return (int) Math.round(ship.getMutableStats().getDynamic().getMod(Stats.MAX_LOGISTICS_HULLMODS_MOD).computeEffective(MAX_MODS));
	}
	
	@Override
	public String getUnapplicableReason(ShipAPI ship) {
		boolean has = spec != null && ship.getVariant().hasHullMod(spec.getId());
		int num = getNumLogisticsMods(ship);
		if (has) num--;
		int max = getMax(ship);
		if (num >= max) {
			String text = "many";
			if (max == 1) text = "one";
			else if (max == 2) text = "two";
			else if (max == 3) text = "three";
			else if (max == 4) text = "four";
			
//			text = "" + MAX_MODS;
			text = "" + max;
			if (max == MAX_MODS) {
				return "Maximum of " + text + " non-built-in \"Logistics\" hullmods per hull";
			} else {
				return "Maximum of " + text + " non-built-in \"Logistics\" hullmods for this hull";
			}
		}
		return super.getUnapplicableReason(ship);
	}

	@Override
	public boolean isApplicableToShip(ShipAPI ship) {
		boolean has = spec != null && ship.getVariant().hasHullMod(spec.getId());
		int num = getNumLogisticsMods(ship);
		if (has) num--;
		int max = getMax(ship);
		if (num >= max) {
			return false;
		}
		return super.isApplicableToShip(ship);
	}
	
	protected int getNumLogisticsMods(ShipAPI ship) {
		//int num = (int) Math.round(ship.getMutableStats().getDynamic().getMod(NUM_LOGISTICS_MODS).computeEffective(0));
		int num = 0;
		for (String id : ship.getVariant().getHullMods()) {
			if (ship.getHullSpec().isBuiltInMod(id)) continue;
			if (ship.getVariant().getPermaMods().contains(id)) continue;
			
			HullModSpecAPI mod = Global.getSettings().getHullModSpec(id);
			if (mod.hasUITag(HullMods.TAG_UI_LOGISTICS)) {
			//if (mod.hasUITag(HullMods.TAG_REQ_SPACEPORT)) {
				num++;
			}
		}
		return num;
	}

	
}


