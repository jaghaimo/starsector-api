package com.fs.starfarer.api.impl.combat.threat;

import java.util.List;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.input.InputEventAPI;


public class ThreatCombatStrategyForBothSidesPlugin extends BaseEveryFrameCombatPlugin {

	protected ThreatCombatStrategyAI playerSide;
	protected ThreatCombatStrategyAI enemySide;

	public ThreatCombatStrategyForBothSidesPlugin() {
		playerSide = new ThreatCombatStrategyAI(0);
		enemySide = new ThreatCombatStrategyAI(1);
	}
	
	@Override
	public void advance(float amount, List<InputEventAPI> events) {
		if (Global.getCurrentState() != GameState.COMBAT) return;
		
		playerSide.advance(amount);
		enemySide.advance(amount);
	}

}













