package com.fs.starfarer.api.combat;

import com.fs.starfarer.api.campaign.CampaignUIAPI.CoreUITradeMode;
import com.fs.starfarer.api.campaign.econ.Industry;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Industries;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

public class BaseHullMod implements HullModEffect {

	protected HullModSpecAPI spec;

	public void init(HullModSpecAPI spec) {
		this.spec = spec;
		
	}
	
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize,
											   MutableShipStatsAPI stats, String id) {
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}

	public boolean isApplicableToShip(ShipAPI ship) {
		return true;
	}

	
	public void advanceInCampaign(FleetMemberAPI member, float amount) {
		
	}
	
	public void advanceInCombat(ShipAPI ship, float amount) {
		
	}

	public String getUnapplicableReason(ShipAPI ship) {
		return null;
	}

	public boolean affectsOPCosts() {
		return false;
	}

	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
		return getDescriptionParam(index, hullSize);
	}

	public boolean canBeAddedOrRemovedNow(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (spec == null) return true;
		
		boolean reqSpaceport = spec.hasTag(HullMods.TAG_REQ_SPACEPORT);
		if (!reqSpaceport) return true;
		
		if (marketOrNull == null) return false;
		if (mode == CoreUITradeMode.NONE || mode == null) return false;
		
		for (Industry ind : marketOrNull.getIndustries()) {
			if (ind.getSpec().hasTag(Industries.TAG_STATION)) return true;
			if (ind.getSpec().hasTag(Industries.TAG_SPACEPORT)) return true;
		}
		
		return false;
	}

	public String getCanNotBeInstalledNowReason(ShipAPI ship, MarketAPI marketOrNull, CoreUITradeMode mode) {
		if (spec == null) return null;
		
		boolean reqSpaceport = spec.hasTag(HullMods.TAG_REQ_SPACEPORT);
		if (!reqSpaceport) return null;
		
		boolean has = ship.getVariant().hasHullMod(spec.getId());
		
		String verb = "installed";
		if (has) verb = "removed";
		
		return "Can only be " + verb + " at a colony with a spaceport or an orbital station";
	}
	

	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
		return true;
	}
	
	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
		
	}

	public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
		
	}

}




