package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;

public class BDeck extends BaseHullMod {

	public static float REPLACEMENT_RATE_THRESHOLD = 0.4f;
	public static float REPLACEMENT_RATE_RESET = 1f;
	public static float CR_COST_MULT = 0f;
	
	
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
	}

	@Override
	public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
		ship.addListener(new BDeckListener(ship));
	}
	
	public static Object STATUS_KEY = new Object();
	
	public static class BDeckListener implements AdvanceableListener {
		protected ShipAPI ship;
		protected boolean fired = false;
		public BDeckListener(ShipAPI ship) {
			this.ship = ship;
		}

		public void advance(float amount) {
			float cr = ship.getCurrentCR();
			float crCost = ship.getDeployCost() * CR_COST_MULT;
			
			if (!fired && cr >= crCost) {
				if (ship.getSharedFighterReplacementRate() <= REPLACEMENT_RATE_THRESHOLD) {
					fired = true;
					
					for (FighterLaunchBayAPI bay : ship.getLaunchBaysCopy()) {
						if (bay.getWing() == null) continue;
						
						float rate = REPLACEMENT_RATE_RESET;
						bay.setCurrRate(rate);
						
						bay.makeCurrentIntervalFast();
						FighterWingSpecAPI spec = bay.getWing().getSpec();
						
						int maxTotal = spec.getNumFighters();
						int actualAdd = maxTotal - bay.getWing().getWingMembers().size();
						if (actualAdd > 0) {
							bay.setFastReplacements(bay.getFastReplacements() + actualAdd);
//							bay.setExtraDeployments(actualAdd);
//							bay.setExtraDeploymentLimit(maxTotal);
//							bay.setExtraDuration(EXTRA_FIGHTER_DURATION);
						}
						
						if (crCost > 0) {
							ship.setCurrentCR(ship.getCurrentCR() - crCost);
						}
					}
				}
			}
			
			if (Global.getCurrentState() == GameState.COMBAT &&
					Global.getCombatEngine() != null && Global.getCombatEngine().getPlayerShip() == ship) {

				String status = "ON STANDBY";
				boolean penalty = false;
				if (fired) status = "OPERATIONAL";
				if (!fired && cr < crCost) {
					status = "NOT READY";
					penalty = true;
				}
				Global.getCombatEngine().maintainStatusForPlayerShip(STATUS_KEY,
						Global.getSettings().getSpriteName("ui", "icon_tactical_bdeck"),
						"B-DECK", status, penalty);
			}
		}
		
	}
	
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) {
			return (int) Math.round(REPLACEMENT_RATE_THRESHOLD * 100f) + "%";
		}
		if (index == 1) {
			return (int) Math.round(REPLACEMENT_RATE_RESET * 100f) + "%";
		}
		return null;
	}
//	
//	@Override
//	public boolean shouldAddDescriptionToTooltip(HullSize hullSize, ShipAPI ship, boolean isForModSpec) {
//		return false;
//	}
//
//	@Override
//	public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
//		float pad = 3f;
//		float opad = 10f;
//		Color h = Misc.getHighlightColor();
//		Color bad = Misc.getNegativeHighlightColor();
//		
//		
//		if (!Misc.isAutomated(ship)) {
//			tooltip.addPara("Originally designed by the Tri-Tachyon Corporation for use on its combat droneships, "
//					+ "the coherence field strengh has to be dialed down to allow operation on crewed vessels.", opad);
//			tooltip.addPara("Increases the base range of all non-beam Energy and Hybrid weapons by %s.", opad, h,
//					"" + (int)CREWED_RANGE_BONUS);
//			tooltip.addPara("The coherence field is unstable under combat conditions, with stresses on the hull "
//					+ "resulting in spot failures that release bursts of lethal radiation. "
//					+ "Crew casualties in combat are increased by %s.", opad, h,
//					"" + (int) CREW_CASUALTIES + "%");
//		} else {
//			tooltip.addPara("Increases the base range of all non-beam Energy and Hybrid weapons by %s.", opad, h,
//				"" + (int)RANGE_BONUS);
//		}
//		
//		tooltip.addSectionHeading("Interactions with other modifiers", Alignment.MID, opad);
//		tooltip.addPara("Since the base range is increased, this range modifier"
//				+ " - unlike most other flat modifiers in the game - "
//				+ "is increased by percentage modifiers from other hullmods and skills.", opad);
//	}
//	
////	@Override
////	public boolean isApplicableToShip(ShipAPI ship) {
////		return getUnapplicableReason(ship) == null;
////	}
////	
////	public String getUnapplicableReason(ShipAPI ship) {
////		if (ship != null && 
////				ship.getHullSize() != HullSize.CAPITAL_SHIP && 
////				ship.getHullSize() != HullSize.DESTROYER && 
////				ship.getHullSize() != HullSize.CRUISER) {
////			return "Can only be installed on destroyer-class hulls and larger";
////		}
////		return null;
////	}
	
}









