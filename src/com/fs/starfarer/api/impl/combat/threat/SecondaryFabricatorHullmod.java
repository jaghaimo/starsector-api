package com.fs.starfarer.api.impl.combat.threat;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoAPI.CargoItemType;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.SpecialItemData;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Items;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class SecondaryFabricatorHullmod extends BaseHullMod {
	
	public static float RATE_INCREASE = 30f;
	public static float SMOD_RATE_INCREASE = 20f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getStat(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT).modifyPercent(id, RATE_INCREASE);
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getDynamic().getStat(Stats.FRAGMENT_SWARM_RESPAWN_RATE_MULT).modifyPercent(id, SMOD_RATE_INCREASE);
		}
	}

	@Override
	public CargoStackAPI getRequiredItem() {
		return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, 
								new SpecialItemData(Items.FRAGMENT_FABRICATOR, null), null);
	}
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)RATE_INCREASE + "%";
		return null;
	}
	
	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int)SMOD_RATE_INCREASE + "%";
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().hasHullMod(HullMods.FRAGMENT_SWARM);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Requires Fragment Swarm hullmod";
	}
}











