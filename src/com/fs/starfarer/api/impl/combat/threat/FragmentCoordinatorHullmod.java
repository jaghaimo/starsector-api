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

public class FragmentCoordinatorHullmod extends BaseHullMod {
	
	public static float SIZE_INCREASE = 60f;
	public static float SMOD_SIZE_INCREASE = 40f;
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.FRAGMENT_SWARM_SIZE_MOD).modifyPercent(id, SIZE_INCREASE);
		
		boolean sMod = isSMod(stats);
		if (sMod) {
			stats.getDynamic().getMod(Stats.FRAGMENT_SWARM_SIZE_MOD).modifyPercent(id, SMOD_SIZE_INCREASE);
		}
	}

	@Override
	public CargoStackAPI getRequiredItem() {
		//return Global.getSettings().createCargoStack(CargoItemType.RESOURCES, Commodities.ALPHA_CORE, null);
		return Global.getSettings().createCargoStack(CargoItemType.SPECIAL, 
								new SpecialItemData(Items.THREAT_PROCESSING_UNIT, null), null);
	}
	

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + (int)SIZE_INCREASE + "%";
		return null;
	}
	
	@Override
	public String getSModDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		if (index == 0) return "" + (int)SMOD_SIZE_INCREASE + "%";
		return null;
	}
	
	public boolean isApplicableToShip(ShipAPI ship) {
		return ship.getVariant().hasHullMod(HullMods.FRAGMENT_SWARM);
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		return "Requires Fragment Swarm hullmod";
	}
}











