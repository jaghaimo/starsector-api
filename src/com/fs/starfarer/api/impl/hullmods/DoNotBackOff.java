package com.fs.starfarer.api.impl.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;

public class DoNotBackOff extends BaseHullMod {

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		CombatEngineAPI engine = Global.getCombatEngine();
		if (ship.isHulk() || !engine.isEntityInPlay(ship)) return;
		
		ShipwideAIFlags flags = ship.getAIFlags();
		if (flags == null) return;
		flags.setFlag(AIFlags.DO_NOT_BACK_OFF, 0.5f);
	}
	
}









