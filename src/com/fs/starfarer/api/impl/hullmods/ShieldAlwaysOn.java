package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class ShieldAlwaysOn extends BaseHullMod {

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
		
	public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
	
	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);

		if (!ship.isAlive()) return;
		
		//CombatEngineAPI engine = Global.getCombatEngine();
		
		// so the ship AI can't control it
		ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
		ship.blockCommandForOneFrame(ShipCommand.VENT_FLUX);
		
		String id = "shield_always_on";
		ship.getMutableStats().getOverloadTimeMod().modifyMult(id, 2f); // frigate hull size; makes it 8 sec base
		
		if (ship.getFluxTracker().isOverloadedOrVenting()) {
			ship.getMutableStats().getFluxDissipation().modifyMult(id, 10f);
		} else {
			ship.getMutableStats().getFluxDissipation().modifyMult(id, 1f);
			if (!ship.getShield().isOn()) {
				ship.getShield().toggleOn();
			}
			if (ship.getFluxLevel() > 0.99f) {
				ship.getFluxTracker().beginOverloadWithTotalBaseDuration(5f);
			}
		}
		
	}
}











