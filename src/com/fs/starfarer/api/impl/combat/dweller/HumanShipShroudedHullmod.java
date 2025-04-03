package com.fs.starfarer.api.impl.combat.dweller;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;

public class HumanShipShroudedHullmod extends DwellerHullmod {
	
	public static float CREW_CASUALTIES = 50f;
	public static DwellerShipCreator CREATOR = new HumanShipShroudCreator();

	public static boolean ALLOW_ON_PHASE_SHIPS = false;

	
	public boolean isApplicableToShip(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains(HullMods.FRAGMENT_SWARM)) {
			return false;
		}
		if (!ALLOW_ON_PHASE_SHIPS) {
			if (ship != null && ship.getHullSpec().isPhase()) {
				return false;
			}
		}
		return true;
	}
	
	public String getUnapplicableReason(ShipAPI ship) {
		if (ship.getVariant().getHullMods().contains(HullMods.FRAGMENT_SWARM)) {
			return "Incompatible with Fragment Swarm";
		}
		return "Can not be installed on a phase ship";
	}
	
	protected boolean increasesCrewCasualties() {
		return true;
	}
	
	@Override
	protected boolean addStrategyAI() {
		return false;
	}
	
	@Override
	protected DwellerShipCreator getShipCreator(String hullId) {
		return CREATOR;
	}	
	
	@Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		super.applyEffectsBeforeShipCreation(hullSize, stats, id);
		if (increasesCrewCasualties()) {
			stats.getCrewLossMult().modifyPercent(id, CREW_CASUALTIES);
		}
		stats.getDynamic().getStat(AssayingRiftEffect.HUNGERING_RIFT_HEAL_MULT_STAT).modifyMult(id, 0f);
	}
	
	protected boolean skipFluxUseWhenOverloadedOrVenting() {
		return true;
	}
	protected boolean deductFlux(ShipAPI ship, float fluxCost) {
		if (skipFluxUseWhenOverloadedOrVenting() && ship.getFluxTracker().isOverloadedOrVenting()) {
			return true;
		}
		if (!ship.getFluxTracker().increaseFlux(fluxCost, false)) {
			return false;
		}
		return true;
	}

	
	public void addCrewCasualties(TooltipMakerAPI tooltip, float opad) {
		tooltip.addPara("Crew casualties in combat are increased by %s.", opad, Misc.getHighlightColor(),
						"" + (int) CREW_CASUALTIES + "%");
	}	

}














